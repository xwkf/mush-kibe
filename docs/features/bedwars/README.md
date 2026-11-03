**English** | [Português](README.pt-BR.md)

# BedWars

BedWars for the Muxe network. Lives in `minigames:bedwars` (`Muxe-BedWars`) and runs on servers of the `bedwars` group.

## User stories

- As a player, I want to protect my team's bed and destroy the others', so that my team is the last one standing.

## How it works

The plugin loads its nine modes, gives staff the [arena setup](../../architecture/game-engine/README.md#arena-setup) and runs the matches of the server, many at once, each in its own [cell](../../decisions/0009-own-map-format-and-match-cells.md) of the match world.

### Modes

| Mode | Minimum | Maximum | Per team | Teams |
|---|---:|---:|---:|---|
| Solo | 6 | 8 | 1 | 8 of the 9 colours, chosen in the setup |
| Duplas | 8 | 16 | 2 | 8 of the 9 colours, chosen in the setup |
| Trios | 6 | 12 | 3 | Azul, Vermelho, Verde, Amarelo |
| Quartetos | 8 | 16 | 4 | Azul, Vermelho, Verde, Amarelo |
| 1v1 | 2 | 2 | 1 | Azul, Vermelho |
| 2v2 | 4 | 4 | 2 | Azul, Vermelho |
| 3v3 | 6 | 6 | 3 | Azul, Vermelho |
| 4v4 | 8 | 8 | 4 | Azul, Vermelho |
| 4s | 8 | 8 | 4 | Azul, Vermelho, on a map with two more islands for resources and NPCs |

### Arena setup

Arenas are set up on the BedWars lobby, not on the match servers, and saved in PostgreSQL: every BedWars server loads them on start and reloads one when the lobby saves it (`minigame.arena_saved`). To add a map, upload its world folder to the root of a BedWars lobby server and run `/arena criar <mode>_<map>` there. Editing puts the staff member in build mode (so the lobby lets them fly in creative and break and place blocks) and `/arena sair` takes them out.

An arena is named `<mode>_<map>`, such as `1v1_slip` or `quartetos_air_nation`. `/arena criar 1v1_slip` converts a world folder named `1v1_slip` or opens an empty world. The folder name must match the arena name exactly.

Mark each team's `spawn` first: it also marks the nearest bed within 20 blocks that no other team has. After that, the other team steps (bed, shop, upgrades, iron, gold) are one line each in the checklist and go to the team whose spawn is nearest.

| Step | Per team | Kind | Required |
|---|---|---|---|
| `espera`: waiting spawn | no | where the player stands | yes |
| `area-espera`: waiting area, removed when the match starts | no | Pos 1 and Pos 2 | yes |
| `espectador`: spectator spawn | no | where the player stands | yes |
| `void`: void height | no | the player's Y | yes |
| `limite`: highest block players may place | no | the player's Y | no |
| `diamante`, `esmeralda`: generators | no | the block under the player | no, up to 16 each |
| `spawn` | yes | where the player stands | yes |
| `cama`: bed | yes | the block the player looks at | yes |
| `loja`, `melhorias`: shop and upgrades NPCs | yes | where the player stands | yes |
| `ferro`, `ouro`: island generators | yes | the block under the player | at least 1, up to 4 |
| `loja-extra`, `melhorias-extra`, `ferro-extra`, `ouro-extra` | no | as above | 4s only, optional |

### Matches

A player who reaches a BedWars server goes back to the match they left if they still can, and otherwise to a room of `default-mode` (or the one picked with `/bedwars entrar <modo>`): the fullest room waiting for players, or a new room on a random ready arena of that mode.

| Phase | What happens |
|---|---|
| Waiting | Players wait in the waiting area above the map, in survival like the match, without damage or hunger and without breaking or placing blocks. The hotbar has the emerald `&aMenu do Bed Wars` (slot 5, opens the menu) and `&aVoltar ao Lobby` (slot 9). The scoreboard follows annex 2, and annex 3 once the countdown runs. A room nobody is in and nobody is coming to (no pending reservation) closes after `timings.empty-room-seconds` (60) and frees its cell. |
| Countdown | The [start countdown](../../architecture/game-engine/README.md#start-countdown) of the engine, with the titles `&a30 segundos`, `&610 segundos` and `&c5` to `&c1`. Each title plays a button click (`sounds.countdown`). |
| Playing | Teams are drawn, players spawn at their island with full leather armour in the team colour and a wooden sword. The shop and upgrades NPCs appear with their holograms, and the generators start. A team that got no players loses its bed (removed when found within 2 blocks of the marked point), but its island keeps the generators and the shop and upgrades NPCs, which anyone can use. Right after, the waiting area between Pos 1 and Pos 2 disappears at once, chunk by chunk (`RegionEraser`). |
| Ending | When one team is left, every player gets the result (annex 11), centred: winner, match time, top final kills and top beds broken, with names in the colour of their tag. After 30 seconds everyone goes to `lobby-group` and the room closes. |

**During the match:**

- **Teams:** the scoreboard follows annex 5 (2 or 4 teams) or annex 12 (8 teams). A team shows `&a✔` while it has a bed and someone connected, `&e✔` when everyone left but can still return, the number of players alive once the bed is gone, and `&c✘` when eliminated. Teams nobody joined are out from the start.
- **Joining and leaving the room:** the server's own join and quit messages are hidden. Entering the waiting room tells the room `tadeu entrou na sala (3/8).` (`messages.room-joined`), and leaving it before the match starts sends `tadeu saiu da sala.` (`messages.room-left`), with the name in the colour of the player's tag.
- **Level:** from the waiting room on, the XP bar shows the BedWars level as the XP level and the progress to the next level as the bar, counting the XP earned in the match. It is refreshed every second, and the end-of-match XP is added when the match ends.
- **Level up:** reaching a new level, in the match or in the lobby, sends `&b&lPARABÉNS &7Você subiu para o nível [904⚝]&7!` with the badge of the new level (`messages.level-up`), through the engine's [level up](../../architecture/game-engine/README.md#level-up).
- **Name tags and tab list:** the team letter and colour (`A` in blue), through `MuxeCore.nameTags()`. The tab list shows the health of players alive as a yellow number to the right of the name.
- **Health below the name:** players alive show their health under the name (`20 ❤`, the heart from `messages.health-below-name`), through the engine's [health below the name](../../architecture/game-engine/README.md#health-below-the-name). Spectators and players waiting to respawn show nothing.
- **Chat:** only the room sees it, with the team before the usual format: `[A] medal tag name`. In solo modes everyone in the room reads it. In team modes, while the match is played, the chat reaches only the player's team; `/g <mensagem>` talks to the whole room with `&6[G]` before it (`messages.global-prefix`). The chat runs off the main thread, so it reads a snapshot of each room (who hears whom and the team prefix) rebuilt on the main thread every second and whenever someone joins or leaves.
- **Beds:** breaking an enemy bed sends `CAMA DESTRUÍDA` to everyone and `+100 XP` to the breaker. A team member who was away is eliminated, and the final kill goes to whoever broke the bed. Players cannot break their own bed.
- **Deaths:**
  - The kill goes to the last enemy hit within 15 seconds. A final death with no hit goes to whoever broke the bed.
  - The killer gets the victim's iron, gold, diamonds and emeralds, with `+28 Ferros`-style lines and `+12 XP` (kill) or `+60 XP` (final kill).
  - Falling below the arena's void height kills at once (`morreu no void`).
  - With a bed the player watches as a spectator and respawns after 5 seconds, protected for 4 seconds, with the armour bought and the pickaxe and axe one tier lower. Without a bed the player is out (`KILL FINAL!`), and the team's last player out eliminates the team (`TIME ELIMINADO`).
- **Blocks:** only blocks placed during the match can be broken. Nothing can be placed near spawns (5 blocks), NPCs (1) and generators (1), nor above the arena's build limit (`Você não pode colocar blocos aqui.`). Explosions only break placed blocks, never glass. TNT lights by itself and the fireball is thrown with a right click.
- **Generators:** island iron drops every 0.9 s, as many ingots as players per team (up to 4), gold every 7 s, each with a limit of items lying within 3 blocks. Diamond and emerald show their tier and countdown in a hologram and get faster at 6, 12, 18 and 24 minutes. Iron and gold picked up are copied to teammates within 2 blocks, who hear the pickup sound.
- **AFK:** a player who does not move, click or chat for 45 seconds is AFK: they cannot pick up generator items and get nothing from a teammate's pickup, until they act again.
- **Iron floor:** when the match starts, each island iron generator looks for a slab floor under it (from one block above to two below, with nothing solid on top). A 3x3 floor drops the iron in a plus (front, sides, centre, back); a 2x3 floor, whose generator point is the front centre, cycles through its front row and middle so the back corners stay empty. The front is the side that faces the team spawn; a 2x3 floor facing another way is also accepted. Each ingot lands on the next cell, so 4 ingots in one drop land on 4 cells. Without a 2x3 or 3x3 floor the iron drops on the point and a warning is logged. Gold always drops on the point.
- **Drops:** items fall 1.3 blocks above the point, without speed and with no pickup delay. With `forges.stack-items: false` each cell's items are marked so they do not merge with another cell's. Picking one up collects every item of the same kind within half a block at once, with the marker removed.
- **Diamond and emerald:** a diamond or emerald block spins above each generator (6° per tick, a full turn every 3 s, bobbing 0.08 block), with the three hologram lines above it, 0.3 apart.
- **Events:** after Emerald III, every bed is destroyed (30 min), the sudden death is announced (40 min; the dragons are not in yet) and the match ends (42 min), in a draw if more than one team is left.
- **Chests:** a chest within the island radius (17 blocks) of a team spawn belongs to the closest team. Only that team opens it while the team is in the match; once the team is eliminated anyone can (`Este baú é do Time ...`). Chests away from the islands are open to everyone. Spectators open no chest.
- **Ender chest:** every ender chest opens the player's own `Baú do Fim`, the same one from any ender chest of the map, kept between deaths. When the player is eliminated, its items drop on the team's first island iron generator (or the spawn when there is none).
- **Disconnecting:** a player who leaves with the bed alive can come back within 3 minutes and respawns 5 seconds later (`reconectou`). `/l` warns `Você desconectou de uma partida...` first. Leaving without a bed eliminates the player, crediting the last hit or the bed breaker.

### Shop

The shop NPC opens the menus of the exports: Compra Rápida (the exported layout) and the seven categories, with the tab of the open category marked by a green pane.

- An item costs iron, gold or emeralds from the inventory. Its name is red when unaffordable, with `Você não tem <recurso> o suficiente.`
- Each purchase says `Você comprou <item>.` and plays a button click (`sounds.bought`); buying an upgrade plays `sounds.upgrade`; a purchase without enough resources plays `VILLAGER_NO` (`sounds.cannot-afford`).
- Wool comes in the team colour. A sword replaces the wooden sword. Armour gives boots and leggings and cannot be bought below what the player owns. The pickaxe and axe go up one tier per purchase. Shears are permanent.
- Not working yet: shift-click (editing the quick buy), the hotbar editor, and the special items Golem de Ferro, Leite Mágico, Torre Compacta, Ovo das Pontes, Rastreador and Estilingue, which say they are coming soon and charge nothing.

### Team upgrades

The upgrades NPC opens `Melhorias do Time` with the seven exported upgrades, paid with diamonds:

| Upgrade | Effect |
|---|---|
| Afiação nas Espadas I–II | Sharpness on every sword and axe of the team. |
| Reforço na Armadura I–IV | Protection on every armour piece. |
| Sem tempo Irmão I–II | Permanent Haste. |
| Forja I–IV | Island iron and gold 1.5×, 2×, an island emerald generator, then 3× and double emeralds. |
| Regeneração na Ilha | Regeneration for the team within 17 blocks of its spawn. |
| É uma Cilada! | The next enemy within 17 blocks of a team with a bed gets Blindness and Slowness for 8 s. Used once. |
| Fadiga I–II | The next enemy gets Mining Fatigue for 7 or 10 s. Used once. |

The whole team sees `<player> adquiriu a melhoria <upgrade>.`

### Bed destruction cosmetics

Players pick an animation that plays when they break an enemy bed. The menus follow the recorded exports:

- **Menu do Bed Wars** (emerald): in the waiting room on BedWars servers, and in hotbar slot 3 on the BedWars lobby. Five rows, as in the reference export: `Missões` (slot 11), `Cosméticos` (pufferfish, slot 13), `Lista de Níveis` (slot 15, the level badges in its lore), `Compra rápida` (29), `Editor de hotbar` (31) and `Preferências` (33). Only **Cosméticos** works for now; Missões, Compra rápida, Editor de hotbar and Preferências answer `coming-soon`.
- **Cosméticos do Bed Wars**: the **Destruição de Cama** category shows `owned/total (percent%)` and the selected cosmetic. The other categories, **Cor preferida** and the **Loja** button answer "coming soon".
- **Destruição de Cama (page/pages)**: 28 entries per page. Page 1 starts with **Nenhuma** and **Aleatório** (the default). Owned cosmetics come first in alphabetical order, then all the others in alphabetical order (`CosmeticOrder`, the rule for every cosmetics menu of the network).
  - Owned: left click selects, right click previews. The selected one glows and shows `Já selecionado.`
  - For sale: left click buys with credits (price by stars: 750, 900, 1.300, 1.750, 2.000), right click previews. When the player lacks credits, the lore shows `Faltam <n> créditos!`.
  - Unavailable (event cosmetics the player does not own, or cosmetics without an animation yet): right click previews when there is an animation.
  - `Incluso no ULTRA` cosmetics are owned by everyone with `muxe.cosmetic.ultra`.
  - Players with `muxe.cosmetic.all` (ops by default) own every cosmetic. The ones without an animation still show `Indisponível`.
  - In the preview, the bed disappears for the viewer as soon as it is hit and stays hidden until they go back: it is masked with a `BlockMask`, so the real bed the server resends after the hit never reaches the viewer.
- 30 cosmetics are listed; 14 have an animation (Ajudante de Papai Noel, Avalanche, Coelhos, Passarinhos, Friaca, Fogos de Artifício, Colheita de Abóboras, Apavorante, Deserto, Tinta, Bola de Canhão, Enderman, Sal na Ferida, Espiral de Coelhos). In Espiral de Coelhos the bed and a jack o'lantern rise faster and faster while three rabbits circle them two blocks away, leaving clouds and dropping easter egg heads that fall to the ground. The other 16 show `Indisponível.` until their animation exists.
- Animations are drawn only with packets (armor stands, falling blocks, items, fireworks, particles, sounds and fake block changes), so the real world never changes. They are our own reproductions of the recorded behaviour, with original pixel sprites and community heads.

**In a match** the breaker's selection plays at the broken bed for everyone in the room. `Aleatório` picks one of the player's owned animations; `Nenhuma` plays nothing.

**Preview** (BedWars lobby only; anywhere else the player gets `Você só pode visualizar cosméticos no lobby.`):

1. The player is saved (place, inventory, armor, game mode, scoreboard) and teleported to the preview world, with an empty inventory except for the bed `&aVoltar ao Lobby` in slot 9 (`preview.back-item`). Right-clicking it ends the preview at once.
2. Other previewers are hidden both ways. The scoreboard shows `Visualizando:` and the cosmetic name, and the action bar repeats `&eQuebre a cama!`.
3. Left-clicking the bed hides it for that player only and plays the animation for that player only.
4. `preview.return-delay` after the animation ends (2 s), or after `preview.timeout` without breaking the bed (2 min), the player goes back to where they were, with their lobby items, and the cosmetics menu reopens on the page they were on. Leaving the server during the preview opens nothing.

### BedWars lobby

The BedWars lobby (`bedwars-lobby` group) runs Muxe-Lobby plus this plugin in lobby mode. The main game selector, game NPCs and `/jogar bedwars` lead there.

- **Mode NPCs**: SOLO, DUPLAS, TRIOS and QUARTETOS open their mode menu; 1V1/2V2/3V3/4V4/4S opens the duels menu; MEGA and PILARES open nothing yet. `{players} jogando` counts the players in the rooms of their modes on every BedWars server. The two top lines of PILARES change between white and pink every 0.5 s.
- **Mode menu** (`Bed Wars Solo`...): `Bed Wars (Solo)` shows the players of the mode and the top 3 of the daily winstreak, and sends the player to a match; `Selecionar mapa` opens the map menu for players with `muxe.bedwars.map-select` (VIPs) and answers `map-vip-only` to the others; `Treino: Ponte` answers `coming-soon`. The duel modes also have `Voltar`, back to the duels menu.
- **Level**: the XP bar shows the BedWars level as the XP level and the progress to the next level as the bar, loaded on join and refreshed every 30 seconds.
- **Map menu** (`Bed Wars 1v1 (1/1)`): the maps of the mode found on the BedWars servers, favourites first, each group in alphabetical order. Left click asks for a room on that map; right click adds or removes the favourite. `Mapa favorito aleatório` joins a random favourite, `Pesquisar` filters by name typed in the chat (accents ignored), and the arrows change page.
- **Matchmaking**: every BedWars server publishes its rooms and ready arenas every 2 s (`bedwars.rooms`). Joining picks the fullest room of the mode (and map) that still has space on any server; if there is none, it asks the emptiest server that has an arena of that mode (and map) to create one. The lobby sends `bedwars.join_request` to that server, which reserves a seat in the room for 30 s and replies with its code. Pending reservations count as taken seats, both when choosing a room and in what the server publishes, so a seat is never promised twice; expired ones are dropped every second and whether it created the room; the player sees `&aEnviando para <code>...` and is moved there. When the lobby asks for a new room, the player first sees `&aSolicitando nova sala...`, and `&aSala criada!` after the room line when the server did create it. When they arrive, they enter the reserved room, or another room of the mode if it filled up meanwhile.
- **Tops**: four holograms (final kills, wins, current winstreak and level) with the top 100 in 10 pages. Each player sees their own page: a click (left or right) on the upper part (names and above) turns the page, and on the lower part (`Clique para trocar!` and below) changes the period (Semanal, Mensal, Total; Total first on the level top). On Total the level top shows the level badge; on Mensal and Semanal it ranks and shows the XP earned in that period (`120.806 XP`, `lobby.tops.xp-value`). Every click plays `CLICK` (`lobby.tops.click-sound`). Empty positions show `Ninguém` with `0` (`0 XP` on the Mensal and Semanal level top). Names are coloured with the highest tag the player has. The tops reload every 60 s, and a failed load is logged as a warning.
- **Mode selector**: the `SELECIONAR MODO` hologram from the export lists five modes (Geral, Solo, Duplas, Trios, Quartetos), the chosen one in green; a click opens `Selecionar modo`, and the choice applies to every top with modes (the level top has none). When the chosen mode is further down the menu, the list moves so it stays visible.
- **Scoreboard** (annex 1): `&b&lBED WARS` with the level, the XP bar, the wins and current winstreak of the two modes the player played most (1v1 and Solo while they have no games), `Jogadores` (players on the BedWars servers and lobbies) and the site. It replaces the lobby scoreboard through `core.sidebars()`.
- **Statistics NPC**: shows each player as themselves (their own skin), with their level, XP bar, wins, kills, final kills, beds and winstreak above it. Clicking it opens `Suas estatísticas (Bed Wars)`, with Geral (level and XP included) and every mode.
- **Missions NPC**: `MISSÕES` / `Clique para ver!` at 24.5 81 8.5 opens `Missões do Bed Wars` without `Voltar`; the `Missões` button of `Menu do Bed Wars` opens it with `Voltar`. See [Missions](#missions).
- **Hologram spacing**: the NPC lines follow the exported heights. Mode and missions NPCs use `lobby.npc-labels` (bottom line 2.25625 above the feet, then 0.28125, 0.46875 and 0.28125); the statistics NPC uses `lobby.stats-npc.labels`, with the larger gaps of the reference before `Vitórias`, `Winstreak` and `SUAS ESTATÍSTICAS`. The tops use `lobby.tops.spacing` (0.28125 under the header, 0.375 before the first name and before `Clique para trocar!`, 0.28125 between names, 0.34375 before the periods) and the selector list `lobby.tops.selector.list-spacing` (0.28125).
- **Menu rule**: a menu opened from another menu has `Voltar`; opened directly (NPC, item, command) it has none. `Cosméticos do Bed Wars` shows `Voltar` only when opened from `Menu do Bed Wars`, not from `/cosmeticos`.

### Missions

BedWars is the first game with [missions](../missions/README.md). It registers its catalog, its goals and its reward (the mission XP goes to the player's BedWars statistics, so it counts in the level and the tops); Muxe-Core does the rest.

| Goal | Counts | Missions |
|---|---|---|
| `KILL_STREAK` | best | Kills in a row in a match, back to 0 on death. Ninguém me para! (2). |
| `INVISIBLE_BED` | sum | Beds broken while invisible. Quem foi? (1). |
| `END_STONE_DEFENSE` | sum | 1 when the player places `end-stone-blocks` (10) End Stone within `end-stone-radius` (3) blocks of their own bed in a match. Proteção do Fim (1). |
| `CLEAN_WIN` | sum | Wins without placing TNT or throwing a Fireball. Rei das pontes (1). |
| `BEDBUG_KILLS` | sum | Kills of an enemy a Percevejo of the killer's team hit in the last `bedbug-assist-seconds` (10). Um aliado inesperado (3). |
| `EMERALDS_IN_A_LIFE` | best | Emeralds picked up from generators without dying. Colecionador de Esmeraldas (8). |
| `FIREBALL_DAMAGE` | sum | Damage dealt to other players with Fireballs. Sou eu bola de fogo (1.000). |
| `FINAL_KILLS` | sum | Final kills. Fim da linha (35). |
| `EXPLOSIVE_BLOCKS` | sum | Placed blocks destroyed by the player's TNT or Fireball. Olha a explosão (1.000). |
| `FAST_WIN` | sum | Wins within `fast-win-seconds` (210). Ansiedade mais de 8000 (1). |
| `CLAY_BOUGHT` | sum | Clay blocks bought. Artesão (50). |

Daily missions give 625 XP; weekly ones 2.500 XP and 15 credits; Complecionista (complete every weekly mission) gives 15 credits. Each player gets 3 of the 6 daily missions and the week's 5 weekly missions. The Halloween missions of the reference are not included.

The Percevejo can be bought now: thrown, its snowball spawns a silverfish of the thrower's team where it lands. For 15 seconds it chases the closest enemy within 16 blocks, never breaks blocks, drops nothing, and its kills count for the thrower.

### Statistics

- Every match end saves, for each player who played, the games, beds broken and lost, kills, deaths, assists, final kills, final deaths, final assists, wins, losses, winstreak and XP, per mode and in `geral`, for the day, week (Monday to Sunday), month and total, in the São Paulo time zone.
- An assist goes to every other enemy who hit the victim in the last 15 seconds. Kill, final kill and bed XP come from `xp-rewards`; at the end each player also gets the per minute, per teammate and win XP.
- The winstreak grows with each win and goes back to 0 on a loss; a draw keeps it. The best winstreak never goes down.
- The level comes from the total XP: 5000 per level up to 9, 10000 from 10 to 19, and 5000 more every 10 levels. The badge follows the level list (`&7[0✫]`, `&a[10✶]`... `&4[100❤]`... rainbow from 1000).

## Commands

| Command | Permission | Description |
|---|---|---|
| `/arena ...` | `muxe.minigame.setup` | Creates and sets up arenas, on the BedWars lobby only. See [arena setup](../../architecture/game-engine/README.md#arena-setup). |
| `/bedwars entrar <modo>` (`/bw`) | none | Moves the player to a room of that mode, while waiting. |
| `/g <mensagem>` | none | Talks to everyone in the room, with `&6[G]` before the message. In team matches the plain chat reaches only the team. |
| `/bedwars iniciar` | `muxe.bedwars.admin` | Starts the countdown at 10 seconds without the minimum players, for tests. A match started this way with a single team keeps going until that team is eliminated; with two or more teams it ends as usual, when one team is left. |
| `/bedwars finalizar` | `muxe.bedwars.admin` | Ends the match being played right away, with no winner, for tests. |
| `/cosmeticos` (`/cosmeticosbw`) | none | Opens Cosméticos do Bed Wars. |
| `/cosmeticos dar <jogador> <cosmetico>` | `muxe.cosmetics.admin` | Gives a bed destruction cosmetic, for event and award cosmetics. |

## Permissions

| Permission | Default | Description |
|---|---|---|
| `muxe.minigame.setup` | op | Allows `/arena`. |
| `muxe.bedwars.admin` | op | Allows `/bedwars iniciar` and `/bedwars finalizar`. |
| `muxe.cosmetics.admin` | op | Allows `/cosmeticos dar`. |
| `muxe.bedwars.map-select` | op | Choosing the map in the mode menus (VIPs). Give it to the VIP groups. |
| `muxe.cosmetic.all` | op | Owns every BedWars cosmetic. |
| `muxe.cosmetic.ultra` | false | Owns the `Incluso no ULTRA` cosmetics. The `ultra` group has it (migration V14). |

## Configuration

`plugins/Muxe-BedWars/config.yml`:

| Key | Default | Description |
|---|---|---|
| `modes.<id>.display-name` | `Solo`, `Duplas`, `1v1`... | Name shown to players. |
| `modes.<id>.min-players` | see the table above | Players needed to start the countdown. |
| `modes.<id>.max-players` | see the table above | Players per match. |
| `modes.<id>.team-size` | see the table above | Players per team. |
| `modes.<id>.fixed-teams` | none (Solo, Duplas) | Team colours of modes whose teams are fixed. Without it, the setup chooses `max-players / team-size` colours. |
| `modes.<id>.extra-islands` | `true` on 4s | Adds the extra shop, upgrades and generator steps to the setup. |
| `lobby-group` | `bedwars-lobby` | Where players go when a match ends or they leave the waiting room, and the group that receives the room lists. |
| `default-mode` | `solo` | The mode of the room a player joins on arrival. |
| `timings.*` | 5 s respawn, 180 s rejoin, 30 s restart, 4 s protection, island radius 17, protection radii 5/1/1, 60 s empty room | Match timings and protected areas; `empty-room-seconds` closes rooms left empty. |
| `countdown.titles` | `30`, `10`, `5`...`1` | Titles of the start countdown. |
| `sounds.*` | `CLICK`, `VILLAGER_NO`, `CHEST_OPEN` | `countdown`, `bought`, `upgrade`, `cannot-afford` and `ender-chest`, each with `sound`, `volume` and `pitch`. A sound left empty plays nothing. |
| `forges.*` | the spec values | Island iron, gold and emerald, and the three diamond and emerald tiers with their start second. `stack-items: false` keeps items of different cells apart; `split-radius` (2) is how far teammates share iron and gold. |
| `events.*` | 1800, 2400, 2520 | Seconds of the beds destroyed, sudden death and game end events, and their names on the scoreboard. |
| `xp-rewards.*` | 10, 5, 250, 100, 12, 60 | XP per minute, per teammate, win, bed, kill and final kill. |
| `hotbar.waiting.*` | emerald and bed | Waiting room items. |
| `scoreboard.*` | annexes 2, 3, 5 and 12 | Scoreboard lines; `{teams}` and `{event}` become the team lines and the next event. |
| `holograms.*` | annexes 7 and 8 | NPC and generator holograms. |
| `shop.*`, `upgrades.*` | the exports | Shop categories, items, prices and texts; upgrades, tiers and prices. |
| `messages.*` | annexes 4, 6, 9, 10, 11, 13, 14, 15 | Every match message. `setup-*` overrides the engine's setup messages. `cosmetic-*` and `preview-*` are the cosmetic messages. |
| `cosmetics.category` | `bedwars-cama` | Category under which ownership and selection are saved. |
| `cosmetics.prices.<stars>` | 750, 900, 1300, 1750, 2000 | Price in credits by stars. |
| `cosmetics.bed-destruction.<id>` | the 30 cosmetics | `name`, `icon` (item), `stars`, `tag`, `availability` (`SALE`, `ULTRA`, `EVENT`) and `description`. The id must match an animation id to have an animation. |
| `cosmetics.menus.*` | the exports | Titles, slots, items and lore lines of the three menus. |
| `preview.world` | `previa_quebra_de_cama` | Folder of the preview world in the server root. Without it the preview is off. |
| `preview.spawn`, `preview.bed` | 6.5 11 21.5; foot 6 11 17, head 6 16 | Where the player stands and the bed they break. |
| `preview.timeout`, `preview.return-delay` | `120s`, `2s` | Time to break the bed and wait after the animation. |
| `preview.back-item` | slot 8 (the 9th), `BED`, `&aVoltar ao Lobby` | The item that ends the preview. The slot counts from 0. |
| `preview.action-bar`, `preview.scoreboard` | `&eQuebre a cama!`, MUXE / Visualizando | Preview texts; `{name}` is the cosmetic. |
| `lobby.menu-item` | slot 3, emerald | Menu do Bed Wars item on the BedWars lobby. |
| `levels.badges`, `levels.rainbow` | the level list | Badge of each level range; from `rainbow.from` (1000) the digits take the rainbow colours. |
| `lobby.npcs.<id>` | the exports | `action` (a mode id, `duelos` or `nenhuma`), `location`, signed `skin`, `players` (modes counted in `{players}`) and `lines`; `{blink}` takes the next colour of `lobby.blink-colors` every 0.5 s. |
| `lobby.stats-npc` | 22.5 81 -1.5 | Position, lines, XP bar size and `labels` (line heights) of the statistics NPC. |
| `lobby.npc-labels` | the exports | `bottom`, `gaps` and `spacing` of the mode and missions NPC lines. |
| `lobby.tops.spacing.*` | the exports | `title`, `list`, `entries`, `click` and `periods` gaps of the tops. |
| `missions.*` | the exports | The [missions](../missions/README.md#configuration) catalog and texts, plus `fast-win-seconds` (210), `end-stone-radius` (3), `end-stone-blocks` (10) and `bedbug-assist-seconds` (10). |
| `lobby.tops.boards.<id>` | the four tops | `category` (`FINAL_KILLS`, `WINS`, `WINSTREAK`, `LEVEL`), `title`, `location` of the top stand, `periods` in display order and `modes`. |
| `lobby.tops.*` | the exports | Header, entry, empty (`Ninguém` - `0`), click and period texts, `label-above-stand` (2.38125, where the 1.8 client draws the name of a full-size stand), `refresh-seconds` (60), `click-sound` (`CLICK`, empty for none) and `xp-value` (`{xp} XP`, the level top outside Total). |
| `lobby.tops.selector.*` | the export | `location` of the `SELECIONAR MODO` stand, `list-offset` and `footer-offset` (0.375 and 1.90625 below it), `list-spacing` (0.28125), `visible-modes` (5) and the header, footer, selected, other and `general-name` texts. |
| `lobby.scoreboard.*` | annex 1 | `title`, `lines` (`{level}`, `{bar}`, `{players}`; `{modes}` becomes the mode lines), `mode-lines` (`{mode}` and the counters), `default-modes` (1v1, solo), `shown-modes` (2) and `bar-segments` (10). |
| `lobby.menus.*` | the exports | Mode, duels, maps, top mode and statistics menus. |

## Packets

| Type | From → to | Delivery | Purpose |
|---|---|---|---|
| `bedwars.rooms` | BedWars servers → `lobby-group` | volatile, 10 s | Rooms (code, mode, arena, open, players, max) and ready arenas of the server, every 2 s. |
| `bedwars.join_request` | BedWars lobby → one BedWars server | request, 5 s | Reserves a room of a mode, optionally of an arena or a given room. |
| `bedwars.join_reply` | BedWars server → lobby | reply | Whether a room was reserved, its code, and whether it was created for this request. |
| `minigame.arena_saved` | BedWars lobby → `bedwars` group | standard | An arena was saved; the BedWars servers reload it from PostgreSQL. |

## Data

| File | What it holds |
|---|---|
| `core.arenas` (game `bedwars`) | One row per arena: mode, `ready`, the definition (display name, teams, points and heights, in YAML) and the map in the [Muxe map format](../../decisions/0009-own-map-format-and-match-cells.md). Migration `V16__create_arenas.sql`. Shared by every BedWars server. |
| `core.owned_cosmetics`, `core.selected_cosmetics` | Owned and selected cosmetics, category `bedwars-cama`. See [Credits and cosmetics](../credits/README.md). |
| `<server>/previa_quebra_de_cama/` | The preview world. Deployed to the BedWars lobby only, never committed. |
| `bedwars.player_stats` | One row per player, mode (and `geral`), period (`diario`, `semanal`, `mensal`, `total`) and period start, with every counter and the XP. Migration `V1__create_player_stats_and_favourite_maps.sql`. |
| `bedwars.recorded_matches` | The id of every match already saved, claimed in the same transaction as its stats, so a match saved twice counts once. Migration `V2__create_recorded_matches.sql`. |
| `bedwars.favourite_maps` | Favourite arenas of each player. |

Commands and queries in `br.com.muxe.minigame.bedwars.stats`: `RecordMatchCommand`, `ToggleFavouriteMapCommand`, `FindPlayerStatsQuery`, `FindTopQuery`, `FindFavouriteMapsQuery`.

## Metrics

`muxe_matches_active{game="bedwars"}`, `muxe_arenas_available{game="bedwars"}`, `muxe_arenas_disabled{game="bedwars"}`.

## Integrations

- [Lobby](../lobby/README.md): `/jogar bedwars`.
- [Credits and cosmetics](../credits/README.md): prices, purchases and ownership.
- [Game engine](../../architecture/game-engine/README.md).

## Acceptance criteria

- [x] Modes are loaded from configuration (engine tests).
- [x] The nine modes have the limits and teams of the spec (`BedWarsSchemaTest`).
- [x] An arena can be set up in game, with a checklist of what is missing (engine tests).
- [x] Scoreboards, team indicators and the forge, timeline, kill credit, level, XP and ranking rules follow the spec (`RoomTest`, `GameRulesTest`, `GeneratorTimerTest`).
- [x] Shop and upgrade names and lore follow the exports (`ShopCatalogTest`, `UpgradeCatalogTest`).
- [x] The 13 animations end without leftovers and follow the recorded timings (`BedAnimationsTest`).
- [x] Cosmetic states, prices, sorting and lore follow the exports (`CosmeticCatalogTest`).
- [x] Deaths, assists, beds lost, XP and match outcomes are counted (`RoomStatsTest`); periods, counters, tops and favourites are saved (`StatsHandlersTest`, `StatCountersTest`, `StatsPeriodTest`, `PostgresBedWarsStatsRepositoryIntegrationTest`).
- [x] Matchmaking picks the fullest open room or asks the emptiest server for one (`MatchmakerTest`).
- [x] Lobby NPCs, menus, tops, the mode selector, level badges, the statistics NPC and the lobby scoreboard follow the exports and annex 1 (`LobbyViewTest`, `LobbyFormatTest`).
- [ ] A full match checked in game.
- [ ] Lobby NPCs, menus, matchmaking, map choice and tops checked in game.
- [ ] Every animation and the preview checked in game.

## Failures and recovery

If Muxe-Core is not enabled, the plugin disables itself.

## Known limitations

- MEGA and PILARES are not modes yet, so their NPCs open nothing. `/retornar` and `/l` are not in yet.
- A player who reaches a BedWars server without a reservation still joins a room of `default-mode`.
- The lobby only knows the rooms of servers that published in the last 10 seconds; the room list of a server that stops is forgotten after that.
- The other special shop items, the quick buy editor, the dragons and the victory and defeat titles are not in yet.
- A restart or crash ends every match on the server without compensation.
- 17 bed destruction cosmetics have no animation yet. The other cosmetic categories, Cor preferida and the cosmetic shop are not in yet.
- A purchase updates the menu at once, but another server sees it only when the player joins it again.
