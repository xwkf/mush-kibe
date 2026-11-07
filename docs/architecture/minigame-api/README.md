**English** | [Português](README.pt-BR.md)

# Minigame API

The layer every game is built on. The [game engine](../game-engine/README.md) gives the low-level pieces (worlds, maps, cells, arena setup, modes, the countdown). The minigame API runs the matches with them: the lifecycle, joining and leaving, teams, deaths, respawns, spectators, the scoreboard, titles, chat, matchmaking, the server wiring and the [cosmetics](#cosmetics) shared by every game. A game declares its arena, its rules and the hooks for what only it does.

Code: `minigames/api/src/main/java/br/com/muxe/minigame/api`. Module `minigames:api`, bundled inside each game's jar together with the engine.

## Building a game

A game is made of these pieces:

| Piece | What the game writes |
|---|---|
| Arena schema | The steps of `/arena`, with `ArenaSchema.builder(modes)`. |
| Arena | Its typed arena (a `MatchArena`) read from the stored draft with `ArenaView`, or `StandardArena` when the standard steps are enough. |
| `Minigame<A, S>` | The id, the `MatchRules`, how to read an arena and how to open the state of a new match. |
| `MatchState` | What happens in one match that only this game does, through hooks. Every hook has a default. |
| `GameHandlers<S>` | Optional. The events of a match being played: blocks, interactions, explosions, damage, pickups. |

```java
public final class SkyWarsGame implements Minigame<StandardArena, SkyWarsMatch> {

    public String id() {
        return "skywars";
    }

    public MatchRules rules() {
        return MatchRules.defaults("mxsw").breakingAnything();
    }

    public StandardArena arena(LoadedArena loaded) {
        return StandardArena.from(loaded);
    }

    public SkyWarsMatch open(MatchRuntime<SkyWarsMatch> runtime, StandardArena arena) {
        return new SkyWarsMatch(runtime, arena);
    }
}
```

```java
ArenaSchema schema = ArenaSchema.builder(modes)
    .waiting()
    .spectator()
    .voidHeight()
    .teamSpawn()
    .step(CHESTS)
    .fixedTeams(modesSection)
    .build();
```

The bootstrap wires the rest:

```java
MinigameBootstrap minigame = new MinigameBootstrap(plugin, core, "skywars", modes, messages, commands, config);
if (minigame.isLobby()) {
    minigame.setup(schema);
    return;
}
MatchServer<StandardArena, SkyWarsMatch> server = minigame.matches(new SkyWarsGame(), MatchSettings.from(config), MatchEvents.NONE);
server.listen(new SkyWarsHandlers());
```

## Arena schema

`ArenaSchema` is the engine's `SetupSchema`, built declaratively:

| Builder | Adds |
|---|---|
| `waiting()` | `espera` (spawn of the waiting area) and `area-espera` (the region removed when the match starts). |
| `spectator()` | `espectador`, where the dead and spectators appear. |
| `voidHeight()` | `void`, the height below which a player dies. |
| `teamSpawn()` | `spawn` for each team, also the anchor that places the other team steps. |
| `step(...)` | The game's own steps, in the order of the checklist. |
| `stepsFor(modes, ...)` | Steps only some modes have. |
| `linked(...)` | Blocks found on their own around the anchor, such as a bed. |
| `fixedTeams(...)` | The fixed teams of a mode, by hand or from `modes.<id>.fixed-teams`. Their count must match the mode. |

`ArenaView` reads the stored draft with types: `point(step)`, `point(step, team)` (failing when missing), `points(...)`, `height(step)`, `optionalHeight(step)` and `perTeam(reader)`. `StandardArena` reads the standard steps for a game that needs nothing else.

## Match lifecycle

`WAITING → STARTING → PLAYING → ENDING`, then the match closes and frees its cell. The API runs every step; the game only fills the hooks.

| When | What the API does | Hooks |
|---|---|---|
| A player joins | Resets them to survival, says `room-joined` to the room, teleports them to the waiting spawn, gives the waiting hotbar and the scoreboard. | `arrived` |
| The minimum is reached | Runs the [start countdown](../game-engine/README.md#start-countdown), with the titles of `countdown.titles` and the `countdown` sound. | |
| The countdown ends | Draws the teams (favourite colours from `Minigame.teamPreferences`), protects everyone, spawns each player on their team spawn, removes the waiting area, says `match-started`. Teams nobody joined stop respawning and are out. | `starting`, `spawned`, `started` |
| Every second | Counts the time, respawns players whose time is up, eliminates players who did not return in time, refreshes the scoreboard. | `everySecond`, `playing` |
| Every 5 ticks | Shows the health below the name. | `fastTick` |
| A player dies | Finds the killer (last enemy hit within 15 seconds; for a final death, whoever took the team's respawn away), credits kills and assists, says the death message, turns the player into a spectator and either schedules the respawn or eliminates them. | `died`, `deathMessage` |
| A team is out | Says `team-eliminated`. When one team is left (or none, in a match forced with one team) the match ends. | `standingsChanged` |
| The match ends | Plays the end sounds, gives the winners and spectators their hotbar, shows the victory or defeat title, draws a celebrant for the victory cosmetic, sends the centred result with the rankings. | `ending`, `ended`, `score`, `area` |
| `timings.restart-seconds` later | Sends everyone to `lobby-group`, and closes the match 3 seconds after. | `closed` |

### Respawn and final kills

A team either respawns or not (`MatchTeam.respawns`). `MatchRules.respawning()` makes teams start respawning; a game turns it off for a team with `stopRespawning(takenBy)`, and whoever took it away gets the final kills nobody else earned. A death while the team respawns is a death; otherwise it is a final death and eliminates the player. In BedWars, breaking the bed stops the team respawning.

### Leaving and returning

- Leaving before the start leaves the room (`room-left`) and may cancel the countdown.
- Leaving while the team respawns keeps the seat for `timings.rejoin-seconds` and stores `match:return:<player>` so `/retornar` works. Arriving again puts the player back as a spectator who respawns after `timings.respawn-seconds` (`reconnected`).
- Leaving while the team does not respawn is a final death credited to the last hitter.
- Not returning in time eliminates the player.

### Rules

`MatchRules` holds what changes from game to game, with defaults:

| Rule | Default | Meaning |
|---|---|---|
| `respawn` | off | Teams start respawning. |
| `breakOnlyPlaced` | on | Only blocks placed during the match can be broken or exploded. |
| `hunger` | off | Hunger changes. |
| `crafting` | off | Crafting is allowed. |
| `enterBeds` | off | Beds can be used in the match world. |
| `moveArmor` | off | Armour slots can be clicked. |
| `undroppable` | nothing | Items that cannot be dropped. |
| `nameTagPrefix` | | Prefix of the scoreboard teams of the name tags (BedWars: `mxbw`). |

Always on: friendly fire is off, players not alive cannot hit, the void kills, spawn protection ends when the player hits someone, spectators cannot touch their inventory.

## Pieces

| Package | Classes | Server? |
|---|---|---|
| `match` | `Match`, `Participant`, `MatchTeam`, `MatchPhase`, `TeamStatus`, `ChatView`, `Ranking`, `MatchArena` | No |
| `combat` | `KillCredit` | No |
| `timeline` | `Timeline<E>`: events at fixed seconds and the next one for the scoreboard | No |
| `arena` | `ArenaSchema`, `ArenaView`, `StandardArena` | No |
| `runtime` | `Minigame`, `MatchState`, `MatchRules`, `MatchRuntime`, `MatchService`, `Matches`, `Stage`, `Cells`, `Death`, `MatchEvents`, `TeamPreferences` | No static server calls |
| `settings` | `MatchSettings` and its parts, read from the game's `config.yml` | Configuration only |
| `view` | `MatchBoard` (scoreboard), `NameTagStyle`, `EndTitles`, `TitleSweep` | |
| `spectator` | `Ghosts`, `Spectating`, `TeleporterMenu`, `SpectatorTargets` | |
| `listener` | `MatchListener`, `GameHandlers` | Yes |
| `network`, `matchmaking` | Room packets, `RoomPublisher`, `MatchConnector`, `Matchmaker`, `RoomChoice`, `RoomDirectory`, `Reservations` | Yes |
| `bukkit` | `BukkitStage`, `MatchWorldCells`, `MatchTicker` | Yes |
| `bootstrap` | `MinigameBootstrap`, `MatchServer` | Yes |
| `command` | `GlobalChatCommand` (`/g`) | Yes |
| `cosmetic` | `GameCosmetics`, `CosmeticKind`, `CosmeticDefaults`, `CosmeticCatalog`, profiles, menus, previews, auras, trails, kill messages and the scene animations | Yes |

- **`MatchRuntime`** runs one match. It never calls the server directly: everything it does to players goes through the `Stage` port (`BukkitStage` on a server, a fake in the tests), so the whole lifecycle is tested without a server.
- **`MatchService`** keeps the matches of the server: creates them on a random ready arena, picks the fullest open room, holds reservations for 30 seconds, sends arriving players back to the match they can return to, keeps the chat views and the name tags, closes idle matches after `timings.empty-room-seconds` and closes a match whose logic throws. Games read it through `Matches<S>` (`of(player)`, `at(location)`, `all()`, `playing(player)`).
- **`MatchListener`** handles the events every game shares and calls `GameHandlers` only for a player alive in a match being played, so a game never repeats the phase and alive checks. It also handles the waiting hotbar (`menu`, `leave`), the spectator hotbar (teleporter, random player, player in combat, play again, leave), the lobby commands warning (`left-match`), the void, hunger, drops, crafting, beds, armour slots and the match chat with `/g`.
- **`MatchEvents`** tells cosmetics about victories and final kills.

## Cosmetics

Victory effects, final kill effects, auras, projectile trails and kill messages are shared by every game (`CosmeticKind`), together with the catalog, the ownership profiles, the cosmetic menus and the lobby previews. A game adds its own on top (BedWars: bed destruction and wood types).

| Kind | Key | Ownership category | Lobby preview section |
|---|---|---|---|
| `VICTORY_EFFECT` | `victory-effect` | `<game>-vitoria` | `victory-preview` |
| `FINAL_KILL` | `final-kill` | `<game>-kill-final` | `final-kill-preview` |
| `AURA` | `aura` | `<game>-aura` | `aura-preview` |
| `PROJECTILE_TRAIL` | `projectile-trail` | `<game>-rastro` | `projectile-trail-preview` |
| `KILL_MESSAGE` | `kill-message` | `<game>-mensagem-kill` | none |

- **One catalog.** `cosmetics.yml`, inside the api jar, declares every shared cosmetic once: name, icon, stars, tag, availability, description, the prices by stars and the kill message sets (`kill-messages`).
- **Ownership by game.** A cosmetic bought in one game is not owned in another: each game keeps its cosmetics in `<game>-<kind>`, generated from the game id. BedWars keeps `bedwars-vitoria` and the others it had; a new game gets its own categories without any setup.
- **Overrides.** A game may override the catalog in its own `config.yml`, under `cosmetics.<key>`: `prices`, any field of a cosmetic, `hidden: true` to leave one out, or a cosmetic only that game has. `kill-messages` overrides the message sets. `CosmeticDefaults` merges both.
- **One call.** `GameCosmetics.start(setup)` takes the game id and the kinds it uses, and builds the catalogs, the profiles, the effects played in the match, the auras, the trails, the kill messages and, on the game's lobby, the previews (read from the preview sections above). It gives the game the menu category of each kind (`category(kind, menus)`) and is the game's `MatchEvents`.
- **Menus.** `CosmeticMenuSettings.from(menus, baseList, keys)` reads the categories menu of the game, and `CosmeticMenus` shows it and the lists: buying, selecting, previews and a way back to the game's own menu.
- **Messages.** `cosmetic-selected`, `cosmetic-bought`, `cosmetic-not-enough`, `cosmetic-already-owned`, `cosmetic-unavailable`, `cosmetic-no-preview`, `cosmetic-failed`, `preview-only-lobby`, `preview-busy`.
- **A game's own cosmetics.** Their animations can use the api helpers (`Kit`, `Body`, `Flight`, `HeadLantern`...). The stage test helpers (`RecordingStage`, `TracedStage`, `FakeTrail`) are test fixtures of the api: `testImplementation(testFixtures(projects.minigames.api))`.

## Configuration

Read by `MatchSettings.from(config)` from the game's `config.yml`:

| Key | Default | Description |
|---|---|---|
| `lobby-group` | `lobby` | Where players go after the match and with the leave items. |
| `default-mode` | `solo` | Mode of a player who arrives without a reservation. |
| `timings.respawn-seconds` | `5` | Time to respawn. |
| `timings.rejoin-seconds` | `180` | How long a player who left can return. |
| `timings.restart-seconds` | `30` | Seconds between the end and sending everyone to the lobby. |
| `timings.respawn-protection-seconds` | `3` | Spawn protection at the start and after a respawn. |
| `timings.empty-room-seconds` | `60` | An empty waiting match closes after this. |
| `countdown.titles` | | Title of each second of the countdown. |
| `scoreboard.*` | | Title and lines of each phase (`waiting`, `starting`, `playing`, `playing-eight-teams`) and the team line. Placeholders: `{code}`, `{map}`, `{players}`, `{max}`, `{seconds}`, `{mode}`, `{winstreak}`, `{kills}`, `{final-kills}`, `{teams}`, `{event}` and the game's own (`MatchState.placeholders`). |
| `hotbar.waiting.menu`, `hotbar.waiting.leave` | | Waiting hotbar items. |
| `hotbar.spectator.*` | | Spectator items: `teleporter`, `random-player`, `player-in-combat`, `play-again`, `leave`. |
| `hotbar.teleporter.title`, `hotbar.teleporter.entry-lore` | | Teleporter menu. |
| `sounds.countdown`, `sounds.eliminated`, `sounds.team-eliminated`, `sounds.match-end`, `sounds.match-end-firework` | | Match sounds. |
| `end-titles.victory`, `end-titles.defeat` | `VITÓRIA`, `DERROTA` | Word, colours and sound of the end titles. |

Messages, under `messages`: `room-joined`, `room-left`, `died-title`, `respawn-subtitle`, `spectator-subtitle`, `reconnected`, `team-eliminated`, `match-started`, `result` (with `{top-<ranking>}` lines), `result-draw`, `result-nobody`, `result-rank-1` to `result-rank-3`, `kill` and `death` (when the game has no death messages of its own), `no-arena`, `left-match`, `spectator-no-target`, `spectator-no-combat`, `global-prefix`, `health-below-name`, `room-requesting`, `room-sending`, `room-created`, `room-unavailable`.

## Packets

Each game has its own three packets, named after the game id (`MatchPackets`):

| Packet | Delivery | What it carries |
|---|---|---|
| `<game>.rooms` | volatile, 10 s | Every 2 s, from each game server to `lobby-group` and the game group: the rooms (code, mode, arena, open, players, max) and the ready arenas. |
| `<game>.join_request` | 5 s | From a lobby to a game server: reserve a seat in a mode, arena or room. |
| `<game>.join_reply` | 5 s | Whether the seat was reserved, the room code and whether the room was created. |

BedWars keeps `bedwars.rooms`, `bedwars.join_request` and `bedwars.join_reply`.

## How BedWars uses it

| BedWars class | On the API |
|---|---|
| `BedWarsGame` | The `Minigame`: respawning rules, the tools that cannot be dropped, `mxbw` name tags, favourite colours. |
| `BedWarsSchema` | The `ArenaSchema` with the generators, beds, shops, upgrades and the extra islands of `4s`. |
| `BedWarsArena` | The `MatchArena`, read with `ArenaView`. |
| `Room` | Wraps the `Match` with the beds (a broken bed stops the team respawning), team upgrades, tool tiers and mission counters, outcomes and the map area. |
| `BedWarsMatch` | The `MatchState`: forges, NPCs, island chests, ender chests, traps and heal pool, the timeline events, missions, levels, stats and the kill messages. |
| `BedWarsHandlers` | The `GameHandlers`: beds, foliage, TNT, fireballs, the slingshot, bedbugs, chest deposits and forge pickups. |
| `CosmeticFeatures` | `GameCosmetics` with the five shared kinds, plus the bed destruction (`BedBreakCosmetics`, its animations and preview), the wood types and the BedWars menu (`BedWarsMenu`: missions, quick buy, hotbar, preferences, cosmetics). |

## Tests

- `MatchTest`, `ChatViewTest`, `TeamStatusTest`, `KillCreditTest`, `TimelineTest`, `RankingTest`: the match rules.
- `MatchRuntimeTest`: the whole lifecycle with a fake `Stage` (joining, countdown, start, deaths, respawns, final kills, leaving, returning, timeouts, the end and closing).
- `MatchServiceTest`: arriving, reservations, returning, chat, staff commands, idle matches and switching modes.
- `CosmeticDefaultsTest`, `CosmeticMenuSettingsTest`, `LifetimeTallyTest` and the tests of every scene animation, aura and trail: the cosmetics.
- `ArenaSchemaTest`, `MatchmakerTest`, `ReservationsTest`, `JoinNoticesTest`, `TitleSweepTest`, `TitleRunsTest`, `SpectatorTargetsTest`, `BukkitStageTest`.
