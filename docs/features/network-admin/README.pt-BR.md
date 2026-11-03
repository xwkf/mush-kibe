[English](README.md) | **Português**

# Administração da rede

Comandos da equipe para ver a rede de dentro do jogo. Fica no `core:bukkit` (`Muxe-Core`) e está disponível em todos os servidores Spigot.

## Histórias de usuário

- Como membro da equipe, quero listar todos os servidores vivos com seus jogadores, para saber o estado da rede.
- Como membro da equipe, quero descobrir em qual servidor um jogador está.
- Como operador, quero uma visão rápida da saúde dos pacotes sem abrir o sistema de monitoramento.

## Como funciona

- `/muxe servers` lista os servidores do diretório de servidores local: id, grupo, tipo e jogadores.
- `/muxe find <player>` envia um `player.locate_request` para o proxy e mostra a resposta. Se o proxy não responder dentro de `messaging.request-timeout`, o motivo é mostrado.
- `/muxe metrics` mostra os totais de pacotes publicados, recebidos, duplicados, rejeitados, as falhas de handler e o TPS atual.
- `/muxe latencia` mostra as conexões do banco em uso e esperando, os handlers de comandos e consultas rodando e na fila, e os 12 timers com a maior média desde que o servidor ligou, com contagem, média e máximo em milissegundos. Serve para achar o que deixa o servidor lento para responder quando o TPS está normal.

## Comandos

| Comando | Descrição | Permissão |
|---|---|---|
| `/muxe servers` | Lista os servidores vivos. | `muxe.admin` |
| `/muxe find <player>` | Mostra em qual servidor um jogador está. | `muxe.admin` |
| `/muxe metrics` | Mostra um resumo das métricas de pacotes e o TPS. | `muxe.admin` |
| `/muxe latencia` | Mostra os pools de conexão, as filas e os timers mais lentos. | `muxe.admin` |

O `/muxe find` completa os nomes dos jogadores online com tab.

## Permissões

| Permissão | Libera | Padrão |
|---|---|---|
| `muxe.admin` | Todos os comandos `/muxe`. | op |

## Configuração

Mensagens em `messages.admin.*` no `config.yml` do Muxe-Core, além de `messages.no-permission` e das [mensagens de comandos](../../development/commands/README.pt-BR.md#mensagens).

## Pacotes

| Id | Destino | Política | Propósito |
|---|---|---|---|
| `player.locate_request` | proxy | standard, idade máxima de 5 s | Pergunta onde um jogador está. |
| `player.locate_response` | quem pediu | standard, idade máxima de 5 s | A resposta. |

## Dados

Não se aplica.

## Métricas

`muxe_requests_total{type="player.locate_request"}` e `muxe_request_seconds{type="player.locate_request"}`.

## Integrações

O proxy responde ao `player.locate_request`.

## Critérios de aceitação

- [ ] `/muxe servers` lista todos os servidores que enviaram um heartbeat nos últimos 10 segundos (conferido no jogo).
- [ ] `/muxe find` mostra o servidor de um jogador online e avisa quando o jogador está offline (conferido no jogo).
- [x] Requisição e resposta trafegam pelo Redis e expiram quando ninguém responde (testes de integração e unitários).

## Falhas e recuperação

- Proxy inacessível: `/muxe find` mostra o motivo do timeout; nada mais é afetado.

## Limitações conhecidas

- A lista de servidores vem do diretório local, então um servidor que acabou de iniciar pode levar até 2 segundos para aparecer.
