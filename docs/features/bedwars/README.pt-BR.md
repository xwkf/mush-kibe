[English](README.md) | **Português**

# BedWars

BedWars para a rede Muxe. Fica no `minigames:bedwars` (`Muxe-BedWars`) e roda nos servidores do grupo `bedwars`.

## Histórias de usuário

- Como jogador, quero proteger a cama do meu time e destruir a dos outros, para que o meu time seja o último de pé.

## Como funciona

O plugin carrega os nove modos, dá à staff o [setup de arenas](../../architecture/game-engine/README.pt-BR.md#setup-de-arenas) e roda as partidas do servidor, várias ao mesmo tempo, cada uma na própria [célula](../../decisions/0009-own-map-format-and-match-cells.pt-BR.md) do mundo das partidas.

### Modos

| Modo | Mínimo | Máximo | Por time | Times |
|---|---:|---:|---:|---|
| Solo | 6 | 8 | 1 | 8 das 9 cores, escolhidas no setup |
| Duplas | 8 | 16 | 2 | 8 das 9 cores, escolhidas no setup |
| Trios | 6 | 12 | 3 | Azul, Vermelho, Verde, Amarelo |
| Quartetos | 8 | 16 | 4 | Azul, Vermelho, Verde, Amarelo |
| 1v1 | 2 | 2 | 1 | Azul, Vermelho |
| 2v2 | 4 | 4 | 2 | Azul, Vermelho |
| 3v3 | 6 | 6 | 3 | Azul, Vermelho |
| 4v4 | 8 | 8 | 4 | Azul, Vermelho |
| 4s | 8 | 8 | 4 | Azul, Vermelho, num mapa com mais duas ilhas de recursos e NPCs |

### Setup de arenas

As arenas são configuradas no lobby do BedWars, não nos servidores de partida, e salvas no PostgreSQL: todo servidor de BedWars as carrega ao ligar e recarrega uma quando o lobby a salva (`minigame.arena_saved`). Para adicionar um mapa, envie a pasta do mundo para a raiz de um servidor de lobby do BedWars e rode `/arena criar <modo>_<mapa>` lá. Editar coloca o membro da staff no modo construção (para o lobby deixar voar no criativo e quebrar e colocar blocos), e `/arena sair` tira.

Uma arena se chama `<modo>_<mapa>`, como `1v1_slip` ou `quartetos_air_nation`. `/arena criar 1v1_slip` converte uma pasta de mundo chamada `1v1_slip` ou abre um mundo vazio. O nome da pasta precisa ser exatamente o nome da arena.

Marque primeiro o `spawn` de cada time: ele também marca a cama mais perto, a até 20 blocos, que nenhum outro time tem. Depois disso, os outros passos de time (cama, loja, melhorias, ferro, ouro) são uma linha cada na checklist e vão para o time com o spawn mais perto.

| Passo | Por time | Tipo | Obrigatório |
|---|---|---|---|
| `espera`: spawn da espera | não | onde o jogador está | sim |
| `area-espera`: área da espera, removida quando a partida começa | não | Pos 1 e Pos 2 | sim |
| `espectador`: spawn dos espectadores | não | onde o jogador está | sim |
| `void`: altura do void | não | o Y do jogador | sim |
| `limite`: bloco mais alto que os jogadores podem colocar | não | o Y do jogador | não |
| `diamante`, `esmeralda`: geradores | não | o bloco sob o jogador | não, até 16 cada |
| `spawn` | sim | onde o jogador está | sim |
| `cama` | sim | o bloco para onde o jogador olha | sim |
| `loja`, `melhorias`: NPCs da loja e das melhorias | sim | onde o jogador está | sim |
| `ferro`, `ouro`: geradores da ilha | sim | o bloco sob o jogador | pelo menos 1, até 4 |
| `loja-extra`, `melhorias-extra`, `ferro-extra`, `ouro-extra` | não | como acima | só no 4s, opcionais |

### Partidas

Quem chega num servidor de BedWars volta para a partida que deixou, se ainda puder, e senão vai para uma sala do `default-mode` (ou do modo escolhido com `/bedwars entrar <modo>`): a sala mais cheia esperando jogadores, ou uma sala nova numa arena pronta aleatória desse modo.

| Fase | O que acontece |
|---|---|
| Espera | Os jogadores esperam na área de espera acima do mapa, no modo sobrevivência como na partida, sem dano nem fome e sem quebrar ou colocar blocos. A hotbar tem a esmeralda `&aMenu do Bed Wars` (slot 5, abre o menu) e `&aVoltar ao Lobby` (slot 9). O placar segue o anexo 2, e o anexo 3 quando a contagem roda. Uma sala sem ninguém dentro e sem ninguém a caminho (nenhuma reserva pendente) fecha depois de `timings.empty-room-seconds` (60) e libera a célula. |
| Contagem | A [contagem para iniciar](../../architecture/game-engine/README.pt-BR.md#contagem-para-iniciar) da engine, com os títulos `&a30 segundos`, `&610 segundos` e `&c5` a `&c1`. Cada título toca um clique de botão (`sounds.countdown`). |
| Jogando | Os times são sorteados, os jogadores nascem na ilha com couro completo na cor do time e uma espada de madeira. Os NPCs da loja e das melhorias aparecem com os hologramas, e os geradores começam. Um time que ficou sem jogadores não tem ilha: a cama dele é tirada (procurada a até 2 blocos do ponto marcado) e os geradores e NPCs dele não são criados. Logo depois, a área de espera entre a Pos 1 e a Pos 2 some de uma vez, chunk por chunk (`RegionEraser`). |
| Fim | Quando sobra um time, todos recebem o resultado (anexo 11), centralizado: vencedor, tempo da partida, top kills finais e top camas quebradas, com os nomes na cor da tag. Depois de 30 segundos todos vão para o `lobby-group` e a sala fecha. |

**Durante a partida:**

- **Times:** o placar segue o anexo 5 (2 ou 4 times) ou o anexo 12 (8 times). Um time mostra `&a✔` enquanto tem cama e alguém conectado, `&e✔` quando todos saíram mas ainda podem voltar, o número de jogadores vivos depois que a cama cai, e `&c✘` quando é eliminado. Times sem ninguém já começam fora.
- **Name tags e tab:** a letra e a cor do time (`A` em azul), pelo `MuxeCore.nameTags()`. O tab mostra a vida dos jogadores vivos como um número amarelo à direita do nome.
- **Vida abaixo do nome:** os jogadores vivos mostram a vida embaixo do nome (`20 ❤`, com o coração de `messages.health-below-name`), pela [vida abaixo do nome](../../architecture/game-engine/README.pt-BR.md#vida-abaixo-do-nome) da engine. Espectadores e quem espera renascer não mostram nada.
- **Chat:** só a sala vê, com o time antes do formato de sempre: `[A] medalha tag nome`. Nos modos solo todos da sala leem. Nos modos com time, durante a partida, o chat chega só ao time do jogador; `/g <mensagem>` fala com a sala toda, com `&6[G]` antes (`messages.global-prefix`). O chat roda fora da thread principal, então lê um retrato de cada sala (quem ouve quem e o prefixo do time) refeito na thread principal a cada segundo e sempre que alguém entra ou sai.
- **Camas:** quebrar a cama de um inimigo manda `CAMA DESTRUÍDA` para todos e `+100 XP` para quem quebrou. Um integrante do time que estava fora é eliminado, e a kill final vai para quem quebrou a cama. Ninguém quebra a própria cama.
- **Mortes:**
  - A kill vai para o último inimigo que bateu nos últimos 15 segundos. Uma morte final sem golpe vai para quem quebrou a cama.
  - Quem mata recebe o ferro, ouro, diamantes e esmeraldas da vítima, com linhas como `+28 Ferros` e `+12 XP` (kill) ou `+60 XP` (kill final).
  - Cair abaixo da altura do void da arena mata na hora (`morreu no void`).
  - Com cama, o jogador assiste como espectador e renasce depois de 5 segundos, protegido por 4 segundos, com a armadura comprada e a picareta e o machado um nível abaixo. Sem cama, o jogador sai (`KILL FINAL!`), e o último jogador do time a sair elimina o time (`TIME ELIMINADO`).
- **Blocos:** só blocos colocados durante a partida podem ser quebrados. Nada pode ser colocado perto dos spawns (5 blocos), dos NPCs (1) e dos geradores (1), nem acima do limite de construção da arena (`Você não pode colocar blocos aqui.`). Explosões só quebram blocos colocados, nunca vidro. A TNT acende sozinha e a bola de fogo é lançada com o botão direito.
- **Geradores:** o ferro da ilha cai a cada 0,9 s, tantos lingotes quanto jogadores por time (até 4), o ouro a cada 7 s, cada um com um limite de itens no chão num raio de 3 blocos. Diamante e esmeralda mostram nível e contagem num holograma e aceleram aos 6, 12, 18 e 24 minutos. Ferro e ouro coletados são copiados para os companheiros a até 2 blocos, que ouvem o som de coleta.
- **AFK:** quem não se mexe, não clica e não fala por 45 segundos está AFK: não pega itens dos geradores e não recebe nada da coleta de um companheiro, até agir de novo.
- **Chão do ferro:** quando a partida começa, cada gerador de ferro da ilha procura um chão de lajes embaixo dele (de um bloco acima a dois abaixo, sem nada sólido em cima). Um chão 3x3 solta o ferro em cruz (frente, lados, centro, trás); um chão 2x3, cujo ponto do gerador é o centro da frente, passa pela fileira da frente e pelo meio, e os cantos de trás ficam vazios. A frente é o lado virado para o spawn do time; um chão 2x3 virado para outro lado também vale. Cada lingote cai na próxima célula, então 4 lingotes de uma vez caem em 4 células. Sem um chão 2x3 ou 3x3 o ferro cai no ponto e um aviso vai para o log. O ouro sempre cai no ponto.
- **Itens:** caem 1,3 bloco acima do ponto, sem velocidade e sem espera para pegar. Com `forges.stack-items: false` os itens de cada célula são marcados para não se juntarem com os de outra. Pegar um recolhe de uma vez todos os itens do mesmo tipo a até meio bloco, sem a marca.
- **Diamante e esmeralda:** um bloco de diamante ou de esmeralda gira acima de cada gerador (6° por tick, uma volta a cada 3 s, subindo e descendo 0,08 bloco), com as três linhas do holograma acima dele, a 0,3 de distância.
- **Eventos:** depois da Esmeralda III, todas as camas são destruídas (30 min), a morte súbita é anunciada (40 min; os dragões ainda não entraram) e a partida termina (42 min), empatada se sobrar mais de um time.
- **Baús:** um baú dentro do raio da ilha (17 blocos) do spawn de um time é do time mais próximo. Só esse time abre enquanto está na partida; depois que o time é eliminado, qualquer um abre (`Este baú é do Time ...`). Baús longe das ilhas são de todos. Espectadores não abrem baú.
- **Baú do fim:** todo baú do fim abre o `Baú do Fim` do próprio jogador, o mesmo em qualquer baú do fim do mapa, mantido entre as mortes. Quando o jogador é eliminado, os itens dele caem no primeiro gerador de ferro da ilha do time (ou no spawn, se não houver).
- **Desconexão:** quem sai com a cama de pé pode voltar em até 3 minutos e renasce 5 segundos depois (`reconectou`). O `/l` avisa antes `Você desconectou de uma partida...`. Sair sem cama elimina o jogador, dando a kill ao último golpe ou a quem quebrou a cama.

### Loja

O NPC da loja abre os menus dos exports: Compra Rápida (o layout do export) e as sete categorias, com a aba da categoria aberta marcada por um vidro verde.

- Um item custa ferro, ouro ou esmeraldas do inventário. O nome fica vermelho quando não dá para comprar, com `Você não tem <recurso> o suficiente.`
- Cada compra diz `Você comprou <item>.` e toca um clique de botão (`sounds.bought`); comprar uma melhoria toca `sounds.upgrade`; uma compra sem recursos suficientes toca `VILLAGER_NO` (`sounds.cannot-afford`).
- A lã vem na cor do time. Uma espada substitui a de madeira. A armadura dá bota e calça e não pode ser comprada abaixo da que o jogador tem. A picareta e o machado sobem um nível por compra. A tesoura é permanente.
- Ainda não funcionam: o shift-click (editar a compra rápida), o editor de hotbar, e os itens especiais Golem de Ferro, Leite Mágico, Torre Compacta, Ovo das Pontes, Rastreador e Estilingue, que avisam que chegam em breve e não cobram nada.

### Melhorias do time

O NPC das melhorias abre `Melhorias do Time` com as sete melhorias dos exports, pagas com diamantes:

| Melhoria | Efeito |
|---|---|
| Afiação nas Espadas I–II | Afiação em todas as espadas e machados do time. |
| Reforço na Armadura I–IV | Proteção em todas as peças de armadura. |
| Sem tempo Irmão I–II | Pressa permanente. |
| Forja I–IV | Ferro e ouro da ilha 1,5×, 2×, um gerador de esmeralda na ilha, depois 3× e esmeraldas em dobro. |
| Regeneração na Ilha | Regeneração para o time a até 17 blocos do spawn. |
| É uma Cilada! | O próximo inimigo a até 17 blocos de um time com cama recebe Cegueira e Lentidão por 8 s. Usada uma vez. |
| Fadiga I–II | O próximo inimigo recebe Fadiga por 7 ou 10 s. Usada uma vez. |

O time inteiro vê `<jogador> adquiriu a melhoria <melhoria>.`

### Cosméticos de destruição de cama

O jogador escolhe uma animação que toca quando ele quebra uma cama inimiga. Os menus seguem os exports gravados:

- **Menu do Bed Wars** (esmeralda): na sala de espera dos servidores de BedWars e no slot 3 da hotbar do lobby de BedWars. Cinco linhas, como no export de referência: `Missões` (slot 11), `Cosméticos` (baiacu, slot 13), `Lista de Níveis` (slot 15, com as insígnias de nível na descrição), `Compra rápida` (29), `Editor de hotbar` (31) e `Preferências` (33). Por enquanto só **Cosméticos** funciona; Missões, Compra rápida, Editor de hotbar e Preferências respondem `coming-soon`.
- **Cosméticos do Bed Wars**: a categoria **Destruição de Cama** mostra `possuídos/total (porcentagem%)` e o cosmético selecionado. As outras categorias, **Cor preferida** e o botão **Loja** respondem "em breve".
- **Destruição de Cama (página/páginas)**: 28 itens por página. A página 1 começa com **Nenhuma** e **Aleatório** (o padrão). Primeiro vêm os cosméticos possuídos em ordem alfabética, depois todos os outros em ordem alfabética (`CosmeticOrder`, a regra de todo menu de cosméticos da rede).
  - Possuído: botão esquerdo seleciona, botão direito visualiza. O selecionado brilha e mostra `Já selecionado.`
  - À venda: botão esquerdo compra com créditos (preço por estrelas: 750, 900, 1.300, 1.750, 2.000), botão direito visualiza. Quando faltam créditos, a lore mostra `Faltam <n> créditos!`.
  - Indisponível (cosméticos de evento que o jogador não tem, ou cosméticos ainda sem animação): botão direito visualiza quando existe animação.
  - Os cosméticos `Incluso no ULTRA` são de todos que têm `muxe.cosmetic.ultra`.
  - Quem tem `muxe.cosmetic.all` (ops, por padrão) possui todos os cosméticos. Os que não têm animação continuam `Indisponível`.
  - Na prévia, a cama some para quem está vendo assim que é atingida e continua escondida até a volta: ela fica mascarada com um `BlockMask`, então a cama real que o servidor reenvia depois do golpe nunca chega a quem está vendo.
- São 30 cosméticos; 13 têm animação (Ajudante de Papai Noel, Avalanche, Coelhos, Passarinhos, Friaca, Fogos de Artifício, Colheita de Abóboras, Apavorante, Deserto, Tinta, Bola de Canhão, Enderman, Sal na Ferida). Os outros 17 mostram `Indisponível.` até a animação deles existir.
- As animações são feitas só com pacotes (armor stands, falling blocks, itens, fogos, partículas, sons e blocos falsos), então o mundo real nunca muda. São reproduções nossas do comportamento gravado, com sprites originais e cabeças da comunidade.

**Na partida** a seleção de quem quebrou toca na cama quebrada para todos da sala. `Aleatório` sorteia uma animação que o jogador possui; `Nenhuma` não toca nada.

**Prévia** (só no lobby de BedWars; em outro lugar o jogador recebe `Você só pode visualizar cosméticos no lobby.`):

1. O jogador é salvo (lugar, inventário, armadura, modo de jogo, scoreboard) e teleportado para o mundo da prévia, com o inventário vazio.
2. Quem está em prévia não vê os outros em prévia. A scoreboard mostra `Visualizando:` e o nome do cosmético, e a action bar repete `&eQuebre a cama!`.
3. Ao bater na cama, ela some só para esse jogador e a animação toca só para ele.
4. `preview.return-delay` depois do fim da animação (2 s), ou depois de `preview.timeout` sem quebrar a cama (2 min), o jogador volta para onde estava, com os itens do lobby.

### Lobby do BedWars

O lobby do BedWars (grupo `bedwars-lobby`) roda o Muxe-Lobby e este plugin no modo lobby. O seletor de jogo principal, os NPCs de jogo e o `/jogar bedwars` levam para lá.

- **NPCs de modo**: SOLO, DUPLAS, TRIOS e QUARTETOS abrem o menu do modo; 1V1/2V2/3V3/4V4/4S abre o menu de duelos; MEGA e PILARES ainda não abrem nada. `{players} jogando` conta os jogadores nas salas dos seus modos em todos os servidores de BedWars. As duas linhas de cima do PILARES trocam entre branco e rosa a cada 0,5 s.
- **Menu do modo** (`Bed Wars Solo`...): `Bed Wars (Solo)` mostra os jogadores do modo e o top 3 do winstreak diário, e manda o jogador para uma partida; `Selecionar mapa` abre o menu de mapas para quem tem `muxe.bedwars.map-select` (VIPs) e responde `map-vip-only` para os outros; `Treino: Ponte` responde `coming-soon`. Os modos de duelo também têm `Voltar`, para o menu de duelos.
- **Menu de mapas** (`Bed Wars 1v1 (1/1)`): os mapas do modo encontrados nos servidores de BedWars, favoritos primeiro, cada grupo em ordem alfabética. Botão esquerdo pede uma sala naquele mapa; botão direito adiciona ou remove o favorito. `Mapa favorito aleatório` entra num favorito aleatório, `Pesquisar` filtra pelo nome digitado no chat (sem diferenciar acentos) e as setas trocam de página.
- **Matchmaking**: cada servidor de BedWars publica as suas salas e arenas prontas a cada 2 s (`bedwars.rooms`). Entrar escolhe a sala mais cheia do modo (e do mapa) que ainda tem vaga em qualquer servidor; se não houver, pede ao servidor mais vazio que tenha uma arena desse modo (e mapa) para criar uma. O lobby manda `bedwars.join_request` para esse servidor, que reserva uma vaga na sala por 30 s e responde com o código. As reservas pendentes contam como vagas ocupadas, tanto na escolha da sala quanto no que o servidor publica, então uma vaga nunca é prometida duas vezes; as vencidas são descartadas a cada segundo e se criou a sala; o jogador vê `&aEnviando para <código>...` e é movido para lá. Quando o lobby pede uma sala nova, o jogador vê antes `&aSolicitando nova sala...`, e `&aSala criada!` depois da linha da sala quando o servidor de fato a criou. Ao chegar, ele entra na sala reservada, ou em outra sala do modo se ela tiver enchido nesse meio tempo.
- **Tops**: quatro hologramas (kills finais, vitórias, winstreak atual e nível) com o top 100 em 10 páginas. Cada jogador vê a sua própria página: um clique (esquerdo ou direito) na parte de cima (nomes para cima) troca a página, e na parte de baixo (`Clique para trocar!` para baixo) troca o período (Semanal, Mensal, Total; Total primeiro no top de nível). No Total o top de nível mostra a insígnia do nível; no Mensal e no Semanal ele ordena e mostra o XP ganho naquele período (`120.806 XP`, `lobby.tops.xp-value`). Todo clique toca `CLICK` (`lobby.tops.click-sound`). Posições vazias mostram `Ninguém` com `0` (`0 XP` no Mensal e no Semanal do top de nível). Os nomes têm a cor da maior tag que o jogador tem. Os tops recarregam a cada 60 s, e uma falha ao carregar vira um aviso no log.
- **Seletor de modo**: o holograma `SELECIONAR MODO` do export lista cinco modos (Geral, Solo, Duplas, Trios, Quartetos), o escolhido em verde; um clique abre `Selecionar modo`, e a escolha vale para todos os tops com modos (o top de nível não tem). Quando o modo escolhido fica mais abaixo no menu, a lista anda para ele continuar visível.
- **Scoreboard** (anexo 1): `&b&lBED WARS` com o nível, a barra de XP, as vitórias e a winstreak atual dos dois modos que o jogador mais jogou (1v1 e Solo enquanto ele não tem partidas), `Jogadores` (jogadores nos servidores e lobbies de BedWars) e o site. Ela substitui a scoreboard do lobby pelo `core.sidebars()`.
- **NPC de estatísticas**: mostra cada jogador como ele mesmo (com a skin dele), com nível, barra de XP, vitórias, kills, kills finais, camas e winstreak acima. Clicar abre `Suas estatísticas (Bed Wars)`, com Geral (incluindo nível e XP) e cada modo.
- **NPC de missões**: `MISSÕES` / `Clique para ver!` em 24.5 81 8.5 abre `Missões do Bed Wars` sem `Voltar`; o botão `Missões` do `Menu do Bed Wars` abre com `Voltar`. Veja [Missões](#missões).
- **Espaçamento dos hologramas**: as linhas dos NPCs seguem as alturas dos exports. Os NPCs de modo e de missões usam `lobby.npc-labels` (última linha 2.25625 acima dos pés, depois 0.28125, 0.46875 e 0.28125); o NPC de estatísticas usa `lobby.stats-npc.labels`, com os espaços maiores da referência antes de `Vitórias`, `Winstreak` e `SUAS ESTATÍSTICAS`. Os tops usam `lobby.tops.spacing` (0.28125 abaixo do cabeçalho, 0.375 antes do primeiro nome e antes do `Clique para trocar!`, 0.28125 entre os nomes, 0.34375 antes dos períodos) e a lista do seletor `lobby.tops.selector.list-spacing` (0.28125).
- **Regra dos menus**: um menu aberto por outro menu tem `Voltar`; aberto direto (NPC, item, comando) não tem. `Cosméticos do Bed Wars` só mostra `Voltar` quando aberto pelo `Menu do Bed Wars`, não pelo `/cosmeticos`.

### Missões

O BedWars é o primeiro jogo com [missões](../missions/README.pt-BR.md). Ele registra o seu catálogo, os seus objetivos e a sua recompensa (o XP da missão vai para as estatísticas de BedWars do jogador, então conta no nível e nos tops); o Muxe-Core faz o resto.

| Objetivo | Conta | Missões |
|---|---|---|
| `KILL_STREAK` | melhor | Kills seguidas numa partida, voltando a 0 ao morrer. Ninguém me para! (2). |
| `INVISIBLE_BED` | soma | Camas quebradas estando invisível. Quem foi? (1). |
| `END_STONE_DEFENSE` | soma | 1 quando o jogador coloca `end-stone-blocks` (10) Pedras do Fim a até `end-stone-radius` (3) blocos da própria cama numa partida. Proteção do Fim (1). |
| `CLEAN_WIN` | soma | Vitórias sem colocar TNT nem lançar Bola de Fogo. Rei das pontes (1). |
| `BEDBUG_KILLS` | soma | Kills de um inimigo que um Percevejo do time de quem matou acertou nos últimos `bedbug-assist-seconds` (10). Um aliado inesperado (3). |
| `EMERALDS_IN_A_LIFE` | melhor | Esmeraldas pegas dos geradores sem morrer. Colecionador de Esmeraldas (8). |
| `FIREBALL_DAMAGE` | soma | Dano causado em outros jogadores com Bolas de Fogo. Sou eu bola de fogo (1.000). |
| `FINAL_KILLS` | soma | Kills finais. Fim da linha (35). |
| `EXPLOSIVE_BLOCKS` | soma | Blocos colocados destruídos pela TNT ou Bola de Fogo do jogador. Olha a explosão (1.000). |
| `FAST_WIN` | soma | Vitórias em até `fast-win-seconds` (210). Ansiedade mais de 8000 (1). |
| `CLAY_BOUGHT` | soma | Blocos de Argila comprados. Artesão (50). |

As diárias dão 625 XP; as semanais 2.500 XP e 15 créditos; o Complecionista (completar todas as semanais) dá 15 créditos. Cada jogador recebe 3 das 6 diárias e as 5 semanais da semana. As missões de Halloween da referência não entraram.

O Percevejo agora pode ser comprado: arremessada, a bola de neve faz surgir uma traça do time de quem arremessou onde cair. Por 15 segundos ela persegue o inimigo mais próximo a até 16 blocos, nunca quebra blocos, não dropa nada, e as kills dela contam para quem arremessou.

### Estatísticas

- Todo fim de partida salva, para cada jogador que jogou, partidas, camas quebradas e perdidas, kills, mortes, assistências, kills finais, mortes finais, assistências finais, vitórias, derrotas, winstreak e XP, por modo e em `geral`, no dia, na semana (segunda a domingo), no mês e no total, no fuso de São Paulo.
- Uma assistência vai para cada outro inimigo que bateu na vítima nos últimos 15 segundos. O XP de kill, kill final e cama vem de `xp-rewards`; no fim cada jogador também ganha o XP por minuto, por companheiro e por vitória.
- O winstreak cresce a cada vitória e volta a 0 numa derrota; um empate o mantém. O maior winstreak nunca diminui.
- O nível vem do XP total: 5000 por nível até o 9, 10000 do 10 ao 19 e mais 5000 a cada 10 níveis. A insígnia segue a lista de níveis (`&7[0✫]`, `&a[10✶]`... `&4[100❤]`... arco-íris a partir do 1000).

## Comandos

| Comando | Permissão | Descrição |
|---|---|---|
| `/arena ...` | `muxe.minigame.setup` | Cria e configura arenas, só no lobby do BedWars. Veja o [setup de arenas](../../architecture/game-engine/README.pt-BR.md#setup-de-arenas). |
| `/bedwars entrar <modo>` (`/bw`) | nenhuma | Leva o jogador para uma sala desse modo, enquanto espera. |
| `/g <mensagem>` | nenhuma | Fala com todos da sala, com `&6[G]` antes da mensagem. Nas partidas com time o chat normal chega só ao time. |
| `/bedwars iniciar` | `muxe.bedwars.admin` | Começa a contagem em 10 segundos sem o mínimo de jogadores, para testes. |
| `/cosmeticos` (`/cosmeticosbw`) | nenhuma | Abre Cosméticos do Bed Wars. |
| `/cosmeticos dar <jogador> <cosmetico>` | `muxe.cosmetics.admin` | Dá um cosmético de destruição de cama, para cosméticos de evento e premiações. |

## Permissões

| Permissão | Padrão | Descrição |
|---|---|---|
| `muxe.minigame.setup` | op | Permite o `/arena`. |
| `muxe.bedwars.admin` | op | Permite o `/bedwars iniciar`. |
| `muxe.cosmetics.admin` | op | Permite o `/cosmeticos dar`. |
| `muxe.bedwars.map-select` | op | Escolher o mapa nos menus dos modos (VIPs). Dê aos grupos VIP. |
| `muxe.cosmetic.all` | op | Possui todos os cosméticos do BedWars. |
| `muxe.cosmetic.ultra` | false | Possui os cosméticos `Incluso no ULTRA`. O grupo `ultra` tem essa permissão (migração V14). |

## Configuração

`plugins/Muxe-BedWars/config.yml`:

| Chave | Padrão | Descrição |
|---|---|---|
| `modes.<id>.display-name` | `Solo`, `Duplas`, `1v1`... | Nome mostrado aos jogadores. |
| `modes.<id>.min-players` | veja a tabela acima | Jogadores necessários para iniciar a contagem. |
| `modes.<id>.max-players` | veja a tabela acima | Jogadores por partida. |
| `modes.<id>.team-size` | veja a tabela acima | Jogadores por time. |
| `modes.<id>.fixed-teams` | nenhum (Solo, Duplas) | Cores dos times nos modos em que eles são fixos. Sem ela, o setup escolhe `max-players / team-size` cores. |
| `modes.<id>.extra-islands` | `true` no 4s | Adiciona ao setup os passos de loja, melhorias e geradores extras. |
| `lobby-group` | `bedwars-lobby` | Para onde os jogadores vão quando a partida acaba ou quando saem da espera, e o grupo que recebe as listas de salas. |
| `default-mode` | `solo` | O modo da sala em que o jogador entra ao chegar. |
| `timings.*` | 5 s renascer, 180 s voltar, 30 s reiniciar, 4 s proteção, raio da ilha 17, raios de proteção 5/1/1, 60 s sala vazia | Tempos da partida e áreas protegidas; `empty-room-seconds` fecha as salas que ficaram vazias. |
| `countdown.titles` | `30`, `10`, `5`...`1` | Títulos da contagem para iniciar. |
| `sounds.*` | `CLICK`, `VILLAGER_NO`, `CHEST_OPEN` | `countdown`, `bought`, `upgrade`, `cannot-afford` e `ender-chest`, cada um com `sound`, `volume` e `pitch`. Um som vazio não toca nada. |
| `forges.*` | os valores da especificação | Ferro, ouro e esmeralda da ilha, e os três níveis de diamante e esmeralda com o segundo de início. `stack-items: false` mantém separados os itens de células diferentes; `split-radius` (2) é a distância em que os companheiros dividem ferro e ouro. |
| `events.*` | 1800, 2400, 2520 | Segundos dos eventos de camas destruídas, morte súbita e fim de jogo, e os nomes deles no placar. |
| `xp-rewards.*` | 10, 5, 250, 100, 12, 60 | XP por minuto, por companheiro, vitória, cama, kill e kill final. |
| `hotbar.waiting.*` | esmeralda e cama | Itens da sala de espera. |
| `scoreboard.*` | anexos 2, 3, 5 e 12 | Linhas do placar; `{teams}` e `{event}` viram as linhas dos times e o próximo evento. |
| `holograms.*` | anexos 7 e 8 | Hologramas dos NPCs e dos geradores. |
| `shop.*`, `upgrades.*` | os exports | Categorias, itens, preços e textos da loja; melhorias, níveis e preços. |
| `messages.*` | anexos 4, 6, 9, 10, 11, 13, 14, 15 | Todas as mensagens da partida. `setup-*` sobrescreve as mensagens de setup da engine. `cosmetic-*` e `preview-*` são as mensagens dos cosméticos. |
| `cosmetics.category` | `bedwars-cama` | Categoria em que a posse e a seleção são salvas. |
| `cosmetics.prices.<estrelas>` | 750, 900, 1300, 1750, 2000 | Preço em créditos por estrelas. |
| `cosmetics.bed-destruction.<id>` | os 30 cosméticos | `name`, `icon` (item), `stars`, `tag`, `availability` (`SALE`, `ULTRA`, `EVENT`) e `description`. O id precisa ser o de uma animação para ter animação. |
| `cosmetics.menus.*` | os exports | Títulos, slots, itens e linhas de lore dos três menus. |
| `preview.world` | `previa_quebra_de_cama` | Pasta do mundo da prévia na raiz do servidor. Sem ela a prévia fica desligada. |
| `preview.spawn`, `preview.bed` | 6.5 11 21.5; pé 6 11 17, cabeceira 6 16 | Onde o jogador fica e a cama que ele quebra. |
| `preview.timeout`, `preview.return-delay` | `120s`, `2s` | Tempo para quebrar a cama e espera depois da animação. |
| `preview.action-bar`, `preview.scoreboard` | `&eQuebre a cama!`, MUXE / Visualizando | Textos da prévia; `{name}` é o cosmético. |
| `lobby.menu-item` | slot 3, esmeralda | Item Menu do Bed Wars no lobby de BedWars. |
| `levels.badges`, `levels.rainbow` | a lista de níveis | Insígnia de cada faixa de nível; a partir de `rainbow.from` (1000) os dígitos ganham as cores do arco-íris. |
| `lobby.npcs.<id>` | os exports | `action` (um id de modo, `duelos` ou `nenhuma`), `location`, `skin` assinada, `players` (modos contados em `{players}`) e `lines`; `{blink}` pega a próxima cor de `lobby.blink-colors` a cada 0,5 s. |
| `lobby.stats-npc` | 22.5 81 -1.5 | Posição, linhas, tamanho da barra de XP e `labels` (alturas das linhas) do NPC de estatísticas. |
| `lobby.npc-labels` | os exports | `bottom`, `gaps` e `spacing` das linhas dos NPCs de modo e de missões. |
| `lobby.tops.spacing.*` | os exports | Espaços `title`, `list`, `entries`, `click` e `periods` dos tops. |
| `missions.*` | os exports | O catálogo e os textos das [missões](../missions/README.pt-BR.md#configuração), mais `fast-win-seconds` (210), `end-stone-radius` (3), `end-stone-blocks` (10) e `bedbug-assist-seconds` (10). |
| `lobby.tops.boards.<id>` | os quatro tops | `category` (`FINAL_KILLS`, `WINS`, `WINSTREAK`, `LEVEL`), `title`, `location` do suporte do topo, `periods` na ordem de exibição e `modes`. |
| `lobby.tops.*` | os exports | Textos de cabeçalho, posição, vazio (`Ninguém` - `0`), clique e períodos, `label-above-stand` (2.38125, onde o cliente 1.8 desenha o nome de um suporte em tamanho cheio), `refresh-seconds` (60), `click-sound` (`CLICK`, vazio para nenhum) e `xp-value` (`{xp} XP`, o top de nível fora do Total). |
| `lobby.tops.selector.*` | o export | `location` do suporte do `SELECIONAR MODO`, `list-offset` e `footer-offset` (0.375 e 1.90625 abaixo dele), `list-spacing` (0.28125), `visible-modes` (5) e os textos de cabeçalho, rodapé, selecionado, outros e `general-name`. |
| `lobby.scoreboard.*` | anexo 1 | `title`, `lines` (`{level}`, `{bar}`, `{players}`; `{modes}` vira as linhas dos modos), `mode-lines` (`{mode}` e os contadores), `default-modes` (1v1, solo), `shown-modes` (2) e `bar-segments` (10). |
| `lobby.menus.*` | os exports | Menus de modo, duelos, mapas, modo do top e estatísticas. |

## Pacotes

| Tipo | De → para | Entrega | Para quê |
|---|---|---|---|
| `bedwars.rooms` | servidores de BedWars → `lobby-group` | volátil, 10 s | Salas (código, modo, arena, aberta, jogadores, máximo) e arenas prontas do servidor, a cada 2 s. |
| `bedwars.join_request` | lobby do BedWars → um servidor de BedWars | pedido, 5 s | Reserva uma sala de um modo, opcionalmente de uma arena ou uma sala específica. |
| `bedwars.join_reply` | servidor de BedWars → lobby | resposta | Se uma sala foi reservada, o código dela, e se ela foi criada para este pedido. |
| `minigame.arena_saved` | lobby do BedWars → grupo `bedwars` | padrão | Uma arena foi salva; os servidores de BedWars a recarregam do PostgreSQL. |

## Dados

| Arquivo | O que guarda |
|---|---|
| `core.arenas` (jogo `bedwars`) | Uma linha por arena: modo, `ready`, a definição (nome de exibição, times, pontos e alturas, em YAML) e o mapa no [formato de mapa da Muxe](../../decisions/0009-own-map-format-and-match-cells.pt-BR.md). Migração `V16__create_arenas.sql`. Compartilhada por todos os servidores de BedWars. |
| `core.owned_cosmetics`, `core.selected_cosmetics` | Cosméticos possuídos e selecionados, categoria `bedwars-cama`. Veja [Créditos e cosméticos](../credits/README.pt-BR.md). |
| `bedwars.player_stats` | Uma linha por jogador, modo (e `geral`), período (`diario`, `semanal`, `mensal`, `total`) e início do período, com todos os contadores e o XP. Migração `V1__create_player_stats_and_favourite_maps.sql`. |
| `bedwars.recorded_matches` | O id de cada partida já salva, reivindicado na mesma transação das estatísticas, então uma partida salva duas vezes conta uma vez só. Migração `V2__create_recorded_matches.sql`. |
| `bedwars.favourite_maps` | Arenas favoritas de cada jogador. |
| `<server>/previa_quebra_de_cama/` | O mundo da prévia. Vai só para o lobby de BedWars, nunca para o repositório. |

Comandos e consultas em `br.com.muxe.minigame.bedwars.stats`: `RecordMatchCommand`, `ToggleFavouriteMapCommand`, `FindPlayerStatsQuery`, `FindTopQuery`, `FindFavouriteMapsQuery`.

## Métricas

`muxe_matches_active{game="bedwars"}`, `muxe_arenas_available{game="bedwars"}`, `muxe_arenas_disabled{game="bedwars"}`.

## Integrações

- [Lobby](../lobby/README.pt-BR.md): `/jogar bedwars`.
- [Créditos e cosméticos](../credits/README.pt-BR.md): preços, compras e posse.
- [Motor de jogo](../../architecture/game-engine/README.pt-BR.md).

## Critérios de aceitação

- [x] Os modos são carregados da configuração (testes do motor).
- [x] Os nove modos têm os limites e os times da especificação (`BedWarsSchemaTest`).
- [x] Uma arena pode ser configurada no jogo, com uma checklist do que falta (testes do motor).
- [x] Placares, indicadores dos times e as regras de forja, linha do tempo, kills, níveis, XP e ranking seguem a especificação (`RoomTest`, `GameRulesTest`, `GeneratorTimerTest`).
- [x] Nomes e lores da loja e das melhorias seguem os exports (`ShopCatalogTest`, `UpgradeCatalogTest`).
- [x] As 13 animações terminam sem deixar nada e seguem os tempos gravados (`BedAnimationsTest`).
- [x] Estados, preços, ordem e lore dos cosméticos seguem os exports (`CosmeticCatalogTest`).
- [x] Mortes, assistências, camas perdidas, XP e resultados das partidas são contados (`RoomStatsTest`); períodos, contadores, tops e favoritos são salvos (`StatsHandlersTest`, `StatCountersTest`, `StatsPeriodTest`, `PostgresBedWarsStatsRepositoryIntegrationTest`).
- [x] O matchmaking escolhe a sala aberta mais cheia ou pede uma ao servidor mais vazio (`MatchmakerTest`).
- [x] NPCs, menus, tops, o seletor de modo, insígnias de nível, o NPC de estatísticas e a scoreboard do lobby seguem os exports e o anexo 1 (`LobbyViewTest`, `LobbyFormatTest`).
- [ ] Uma partida completa conferida no jogo.
- [ ] NPCs, menus, matchmaking, escolha de mapa e tops do lobby conferidos no jogo.
- [ ] Todas as animações e a prévia conferidas no jogo.

## Falhas e recuperação

Se o Muxe-Core não estiver habilitado, o plugin se desabilita.

## Limitações conhecidas

- MEGA e PILARES ainda não são modos, então os NPCs deles não abrem nada. O `/retornar` e o `/l` ainda não entraram.
- Quem chega a um servidor de BedWars sem reserva ainda entra numa sala do `default-mode`.
- O lobby só conhece as salas dos servidores que publicaram nos últimos 10 segundos; a lista de um servidor que para é esquecida depois disso.
- Os outros itens especiais da loja, o editor da compra rápida, os dragões e os títulos de vitória e derrota ainda não entraram.
- Um reinício ou crash encerra todas as partidas do servidor sem compensação.
- 17 cosméticos de destruição de cama ainda não têm animação. As outras categorias de cosméticos, Cor preferida e a loja de cosméticos ainda não existem.
- Uma compra atualiza o menu na hora, mas outro servidor só vê quando o jogador entra nele de novo.
