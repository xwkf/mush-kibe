[English](README.md) | **Português**

# Persistência

Como a Muxe guarda dados. O PostgreSQL é a fonte da verdade; o Redis é um transporte e cache que pode ser esvaziado a qualquer momento sem perder nada.

Código: `core/common/src/main/java/br/com/muxe/core/common/persistence`.

## Blocos de construção

| Classe | Responsabilidade |
|---|---|
| `DatabaseConfig` | URL, credenciais, tamanho do pool, timeout de conexão, se roda as migrações e o limite de comando lento. |
| `Database` | Um pool HikariCP sobre `PGSimpleDataSource`, com métricas do pool. Inserts em lote são reescritos pelo driver em inserts de várias linhas (`reWriteBatchedInserts`). |
| `Transactions` | A porta usada pelos handlers: `inTransaction(operation, work)` e `readOnly(operation, work)`. |
| `JdbcTransactions` | A implementação JDBC. |
| `TransactionContext` | Dá aos comandos SQL a conexão e o observador de comandos da transação atual. |
| `Table`, `Column`, `SqlType` | O schema tipado: uma tabela, suas colunas e como cada valor Java é escrito e lido. Pacote `persistence.sql`. |
| `Sql`, `Select`, `Write` | Comandos tipados montados uma vez a partir do schema: selects, inserts, upserts, updates, deletes e SQL escrito à mão. |
| `StatementMetrics` | Mede cada comando e avisa sobre os lentos. |
| `MigrationRunner` | Roda as migrações Flyway de um módulo no seu próprio schema. |
| `ExpiringStore` | Valores de curta duração no Redis (`RedisExpiringStore`), cada um com um tempo de vida. As chaves recebem o prefixo `muxe:`. |

## Transações

```java
return transactions.inTransaction("account.record_login", () -> {
    Optional<Account> holder = accounts.findByUsername(command.username());
    accounts.save(account);
    return Result.success(account);
});
```

- A conexão é vinculada com um **`ScopedValue`** do Java 25 durante o trabalho. Os repositórios chamam `TransactionContext.connection()`; chamar fora de uma transação falha com uma mensagem clara.
- Uma transação iniciada dentro de outra **se junta** a ela, então um handler pode compor chamadas de repositório livremente.
- O trabalho faz commit quando retorna e rollback quando lança exceção. Exceções checadas viram `PersistenceException`; exceções de runtime se propagam sem mudança.
- `readOnly` marca a conexão como somente leitura, para o PostgreSQL otimizar e para escritas acidentais falharem.
- **Nunca na thread principal.** No Spigot, `JdbcTransactions` usa um `MainThreadGuard` e lança `BlockingOnMainThreadException` se for chamado pela thread do servidor. Os handlers rodam nas virtual threads dos buses de CQRS.
- Toda transação é medida em `muxe_db_transaction_seconds{operation, outcome}`. O nome da operação é uma string estável e de baixa cardinalidade, como `auth.register`.

## Repositórios

- A interface fica no pacote `domain` da feature; a implementação em `infrastructure/postgres`.
- Os repositórios declaram suas tabelas e comandos com o schema tipado abaixo, como constantes `static final`. Sem ORM e sem reflexão: cada comando gera exatamente uma string SQL quando a classe carrega, sempre roda como `PreparedStatement`, e o custo de cada consulta continua visível (`statement.sql()`).
- As tabelas são sempre qualificadas pelo schema (`core.accounts`, `auth.credentials`).
- Testes unitários usam repositórios em memória com `DirectTransactions`; testes de integração usam o repositório real contra um container do PostgreSQL.

## Schema tipado e comandos

Código: `core/common/src/main/java/br/com/muxe/core/common/persistence/sql`.

```java
private static final Table ACCOUNTS = Table.of("core", "accounts");
private static final Column<UUID> ID = ACCOUNTS.column("id", SqlType.UUID);
private static final Column<String> USERNAME = ACCOUNTS.column("username", SqlType.TEXT);
private static final Column<Instant> LAST_SEEN_AT = ACCOUNTS.column("last_seen_at", SqlType.TIMESTAMPTZ);

private static final Select<Account> FIND_BY_IDS = Sql.select(ID, USERNAME, LAST_SEEN_AT).from(ACCOUNTS)
    .where(ID.in())
    .map(row -> new Account(row.get(ID), row.get(USERNAME), row.get(LAST_SEEN_AT)))
    .named("account.find_by_ids");

private static final Write<Account> SAVE = Sql.<Account>insertInto(ACCOUNTS)
    .value(ID, Account::id)
    .value(USERNAME, Account::username)
    .value(LAST_SEEN_AT, Account::lastSeenAt)
    .onConflict(ID).doUpdate(USERNAME, LAST_SEEN_AT)
    .named("account.save");

FIND_BY_IDS.list(ID.in(ids));   // SELECT id, username, last_seen_at FROM core.accounts WHERE id = ANY(?)
SAVE.execute(account);          // um upsert
SAVE.batch(accounts);           // uma ida ao banco para todas as contas
```

| Peça | O que faz |
|---|---|
| `Table.of(schema, name)` | Uma tabela. `table.column(name, type)` declara uma `Column<T>`. Os identificadores são validados (`[a-z_][a-z0-9_]*`), então nenhum SQL pode ser injetado por eles. |
| `SqlType<T>` | Como um valor Java é ligado e lido: `UUID`, `TEXT`, `BOOLEAN`, `SMALLINT`, `INTEGER`, `BIGINT`, `DOUBLE`, `REAL`, `TIMESTAMPTZ` (`Instant`), `DATE` (`LocalDate`), `TEXT_ARRAY` (`List<String>`), `BYTEA`. `type.map(read, write)` adapta um valor do domínio (`SqlType.TEXT.map(Visibility::parse, Visibility::name)`), `enumByName(type)` guarda um enum pelo nome, `optional()` transforma `NULL` do SQL em `Optional.empty()`. Um valor `null` é escrito como `NULL` do SQL do tipo certo. |
| Condições | `col.eq()`, `col.eqIgnoreCase()`, `col.in()` (`= ANY(?)`, um único parâmetro array para qualquer quantidade de valores), `col.isNull()`, `col.eq(outra)` para joins, `a.and(b)` e `Condition.sql("coins >= ?", COINS)` para o resto. |
| Argumentos | Os valores são ligados pela coluna, não pela posição: `select.first(GROUP.is(group), ID.is(id))`, e `col.in(valores)` para `ANY`. Um argumento faltando ou desconhecido falha com o nome do comando. |
| `Select<R>` | `first(...)` (a primeira linha), `list(...)`, `exists(...)`. As linhas são mapeadas com `row.get(coluna)`, lidas pelo índice. Um select com join qualifica todas as colunas. Também `orderBy(col.desc())`, `limit(Sql.parameter("limit", SqlType.INTEGER))` e `selectDistinct`. |
| `Write<T>` | Montado por `insertInto` (com `onConflict(...)` e depois `doNothing()`, `doUpdate(colunas)`, `doUpdate(coluna, "expressão")`, `where("condição")`, além de `as(alias)`), `update` (`set`, `where(coluna, getter)`) ou `deleteFrom` (a condição é obrigatória). Um valor vem da entidade (`value(col, Entidade::getter)`) ou de um argumento (`value(col)` e depois `execute(col.is(x))`). `execute(...)` devolve as linhas afetadas; `batch(valores)` manda todos os valores num único `executeBatch` e não faz nada com uma lista vazia. |
| `Sql.query(sql)`, `Sql.statement(sql)` | SQL escrito à mão para o que os builders não cobrem (rankings com joins, `RETURNING`, advisory locks), ainda com parâmetros tipados (`parameters(...)`, na ordem dos placeholders) e colunas de resultado tipadas (`columns(...)`). A quantidade de `?` é conferida quando o comando é declarado. |

Todo comando tem um nome estável e de baixa cardinalidade (`named("account.save")`), usado pelas métricas e pela `PersistenceException`. Os comandos rodam na transação atual (`TransactionContext`); fora de uma, falham como qualquer chamada de repositório.

### Lotes e buscas por vários ids

- Carregue o que uma tela ou um handler precisa em **uma** consulta: `col.in(valores)` em vez de um loop de chamadas `first`. `AccountRepository.findByIds`, `MissionRepository.find` e `PermissionRepository.saveUsers` funcionam assim.
- Grave muitas linhas com `batch(valores)`. O driver reescreve um insert em lote em inserts de várias linhas, então um lote de 1000 linhas é uma ida ao banco. Por isso, **um lote de upserts não pode ter a mesma chave duas vezes** (o PostgreSQL recusa atualizar a mesma linha duas vezes num comando): mantenha o último valor por chave antes de chamar `batch`.
- `ApplyPermissionChangesHandler` salva todos os usuários de um comando com `saveUsers` e grava o log de ações com `logAll`, então aplicar 20 mudanças de usuário leva 10 idas ao banco em vez de 105.

### Métricas de comandos

| Métrica | Tipo | O que mostra |
|---|---|---|
| `muxe_db_statement_seconds{statement,outcome}` | histogram | Todo comando, por nome e por `success` ou `error`. A contagem é o número de execuções. |
| `muxe_db_slow_statements_total{statement}` | counter | Comandos que levaram pelo menos `database.slow-statement-threshold` (padrão `250ms`). Cada um também vira um aviso no log com o nome e a duração. |

## Migrações

- Cada módulo é dono de um **schema** do PostgreSQL com o mesmo nome e das suas migrações em `src/main/resources/db/migration/<module>/`, nomeadas `V<n>__<description>.sql`.
- `MigrationRunner.migrate(module, classLoader)` cria o schema se necessário e mantém o histórico em `<module>.schema_history`.
- O core migra o schema `core` na inicialização quando `database.run-migrations` é true. Um plugin migra seu próprio schema ao habilitar: `core.migrations().migrate("auth", getClass().getClassLoader())`.
- O Flyway usa um lock do PostgreSQL, então vários servidores iniciando juntos nunca aplicam a mesma migração duas vezes.
- Uma migração já lançada nunca é editada. Mudanças são novas migrações.
- Em produção, as migrações podem ser aplicadas por um único servidor designado (ou por uma etapa de deploy) com um usuário privilegiado, e desabilitadas em todos os outros, como descrito em [Segurança](../security/README.pt-BR.md).

| Schema | Dono | Tabelas |
|---|---|---|
| `core` | `core:common` | `accounts` (V1), `accounts.premium` (V2), `accounts.username` anulável para nomes liberados (V3), `npc_placements`, `hologram_placements`, `register_placements` (colocados por grupo de servidores) e `shop_promotion` (uma linha para a rede) (V4), `player_visibility` (a escolha de visibilidade de cada jogador, copiada da antiga `lobby.player_settings`) (V5), `social_head_placements` (cabeças de redes sociais colocadas por grupo de servidores) (V6), `parkour_placements` (início, checkpoints e fim do parkour colocados por grupo de servidores) e `parkour_records` (o melhor tempo de parkour de cada jogador por grupo de servidores) (V7) |
| `auth` | `servers:auth` | `credentials` |
| `lobby` | `servers:lobby` | Nenhuma. A `player_settings` (V1) foi apagada pela V2 depois que as suas linhas foram para `core.player_visibility`. |

## Tipos de dados

| Tipo | Exemplos | Estratégia |
|---|---|---|
| Crítico | Credenciais, coins, compras, recompensas | Gravado em uma transação do PostgreSQL **antes** de o jogador ser informado de que aconteceu. |
| Não crítico | Preferências, opções | Aplicado em memória e gravado em segundo plano. Um crash pode perder os últimos segundos. |
| Estado de partida | Times, kills, timers | Só em memória, no servidor que roda a partida. |
| Estado efêmero da rede | Status de servidores, reservas de idempotência, sessões de login, consultas de premium | Redis ou pacotes, sempre com expiração. |

## Regras

- **Nunca bloqueie a thread principal.** Chamadas ao banco e ao Redis rodam fora dela; resultados que mexem no jogo são agendados de volta.
- **Dado crítico só é confirmado depois do commit.**
- **Saldos mudam atomicamente no SQL** (`UPDATE … SET coins = coins - 100 WHERE coins >= 100`), nunca escrevendo um valor lido antes.
- **A unicidade é garantida pelo banco**, não só pelo código. Exemplo: `accounts_username_lower_key` torna impossíveis duas contas cujos nomes diferem só em maiúsculas/minúsculas, mesmo em uma condição de corrida.
- **Dado durável nunca vive só no Redis.** Toda chave do Redis que não é cache tem expiração.
- **As chaves do Redis compartilham um prefixo:** `muxe:<area>:<id>`, como `muxe:idempotency:auth.authenticated:…`.
- **Nada de consultas dentro de laços.** Carregue o necessário em uma consulta (`col.in(valores)`) e grave muitas linhas com `batch`.
- **Toda consulta frequente tem um índice.**
- **Os pools ficam pequenos.** Cada conexão do PostgreSQL é um processo; 4 conexões por servidor é o padrão.

## Planejado: dono da sessão

Para permitir salvar dados de jogadores entre servidores com segurança, o plano é que os dados de cada jogador online tenham exatamente um servidor dono por vez, registrado no PostgreSQL com um número de geração que só aumenta, e que toda escrita carregue a geração do dono, para que um servidor travado ou desatualizado nunca sobrescreva dados mais novos. As transferências vão gravar e liberar a sessão antes de o destino assumi-la. Isso ainda não está implementado; será documentado aqui quando estiver.
