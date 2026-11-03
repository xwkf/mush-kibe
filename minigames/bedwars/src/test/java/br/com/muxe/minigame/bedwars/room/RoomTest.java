package br.com.muxe.minigame.bedwars.room;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.cosmetic.animation.Area;
import br.com.muxe.minigame.bedwars.game.TeamStatus.Kind;
import br.com.muxe.minigame.bedwars.room.RoomPlayer.State;
import br.com.muxe.minigame.engine.match.StartCountdown.Change;
import br.com.muxe.minigame.engine.team.TeamColor;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RoomTest {

    private static final Instant NOW = Instant.parse("2026-10-30T20:00:00Z");
    private static final List<TeamColor> RED_BLUE = List.of(TeamColor.VERMELHO, TeamColor.AZUL);
    private static final List<TeamColor> EIGHT = List.of(TeamColor.VERMELHO, TeamColor.AZUL, TeamColor.VERDE, TeamColor.AMARELO,
        TeamColor.CIANO, TeamColor.LARANJA, TeamColor.ROSA, TeamColor.CINZA);

    private final RoomView view = new RoomView(RoomFixtures.SETTINGS.scoreboard(), RoomFixtures.SETTINGS.timeline());

    @Test
    void theMapAreaCoversEveryIslandWithAMarginButNotTheWaitingLobby() {
        Room room = RoomFixtures.room("2v7are", RoomFixtures.ONE_V_ONE, RoomFixtures.arena("1v1", "Slip", RED_BLUE));
        double offset = room.cell().offsetBlockX();

        assertThat(room.area()).isEqualTo(new Area(
            offset - 2.5 - Room.MAP_MARGIN, offset + 0.5 - Room.MAP_MARGIN, offset + 43.5 + Room.MAP_MARGIN, offset + 64.5 + Room.MAP_MARGIN));
    }

    @Test
    void theWaitingScoreboardMatchesTheReference() {
        Room room = RoomFixtures.room("2v79c1", RoomFixtures.SOLO, RoomFixtures.arena("solo", "Athens", EIGHT));
        UUID first = join(room, "tadeu");
        join(room, "x572");

        assertThat(view.title()).isEqualTo("&b&lBED WARS");
        assertThat(view.lines(room, first, 0)).containsExactly(
            "&82v79c1", "", "&fMapa: &aAthens", "&fJogadores: &a2/8", "", "&fAguardando...", "", "&fModo: &aSolo", "&fWinstreak: &a0", "", "&amuxe.com.br");
    }

    @Test
    void reachingTheMinimumShowsTheCountdownAndLosingAPlayerGoesBackToWaiting() {
        Room room = RoomFixtures.room("2v79c1", RoomFixtures.SOLO, RoomFixtures.arena("solo", "Athens", EIGHT));
        List<UUID> players = new ArrayList<>();
        for (int index = 0; index < 5; index++) {
            players.add(join(room, "p" + index));
        }

        assertThat(room.join(UUID.randomUUID(), "p5")).isEqualTo(Change.STARTED);
        assertThat(room.phase()).isEqualTo(Room.Phase.STARTING);
        assertThat(view.lines(room, players.getFirst(), 0)).contains("&fInicia em: &a30s");
        assertThat(room.leaveBeforeStart(players.getFirst())).isEqualTo(Change.CANCELLED);
        assertThat(room.phase()).isEqualTo(Room.Phase.WAITING);
    }

    @Test
    void theTwoTeamScoreboardShowsTheNextEventTheTeamsAndTheStats() {
        Room room = RoomFixtures.room("2v7bss", RoomFixtures.ONE_V_ONE, RoomFixtures.arena("1v1", "Slip", RED_BLUE));
        UUID tadeu = join(room, "tadeu");
        join(room, "x572");
        room.start(List.of(), NOW);
        for (int second = 0; second < 109; second++) {
            room.tick();
        }

        assertThat(view.lines(room, tadeu, 0)).containsExactly(
            "&82v7bss", "", "&fDiamante II em &a4:11", "",
            "&cV &fVermelho: &a✔ &7VOCÊ", "&9A &fAzul: &a✔", "",
            "&fKills: &a0", "&fKills Finais: &a0", "&fCamas Quebradas: &a0", "", "&amuxe.com.br");
    }

    @Test
    void theTeamLetterIsBoldInTheNameTagAndTabList() {
        assertThat(TeamColor.VERMELHO.boldLetter()).isEqualTo("&lV&c");
        assertThat(RoomService.tag(TeamColor.VERMELHO).prefix()).isEqualTo("§c§lV§c ");
    }

    @Test
    void eightTeamsHideTheStatsAndEmptyTeamsAreOut() {
        Room room = RoomFixtures.room("2v9cid", RoomFixtures.SOLO, RoomFixtures.arena("solo", "Athens", EIGHT));
        UUID first = null;
        for (int index = 0; index < 6; index++) {
            UUID id = join(room, "p" + index);
            first = first == null ? id : first;
        }
        room.start(List.of(), NOW);

        List<String> lines = view.lines(room, first, 0);

        assertThat(lines).hasSize(14).doesNotContain("&fKills: &a0");
        assertThat(lines).contains("&cV &fVermelho: &a✔ &7VOCÊ", "&8C &fCinza: &c✘", "&dR &fRosa: &c✘");
    }

    @Test
    void theIndicatorFollowsBedDisconnectionAndElimination() {
        Room room = RoomFixtures.room("abc123", RoomFixtures.DOUBLES, RoomFixtures.arena("duplas", "Slip", RED_BLUE));
        List<UUID> ids = List.of(join(room, "a"), join(room, "b"), join(room, "c"), join(room, "d"));
        room.start(List.of(), NOW);
        RoomPlayer red1 = room.player(ids.get(0)).orElseThrow();
        RoomPlayer red2 = room.player(ids.get(2)).orElseThrow();
        RoomPlayer blue = room.player(ids.get(1)).orElseThrow();

        room.disconnected(red1, NOW.plusSeconds(180));
        assertThat(room.status(TeamColor.VERMELHO).kind()).isEqualTo(Kind.BED_ONLINE);
        room.disconnected(red2, NOW.plusSeconds(180));
        assertThat(room.status(TeamColor.VERMELHO).kind()).isEqualTo(Kind.BED_OFFLINE);

        List<RoomPlayer> lost = room.breakBed(TeamColor.VERMELHO, blue);

        assertThat(lost).containsExactly(red1, red2);
        assertThat(room.status(TeamColor.VERMELHO).kind()).isEqualTo(Kind.ELIMINATED);
        assertThat(blue.beds()).isEqualTo(1);
        assertThat(room.winner()).contains(TeamColor.AZUL);
    }

    @Test
    void dyingWithABedRespawnsAndWithoutOneEliminates() {
        Room room = RoomFixtures.room("abc123", RoomFixtures.ONE_V_ONE, RoomFixtures.arena("1v1", "Slip", RED_BLUE));
        RoomPlayer red = room.player(join(room, "red")).orElseThrow();
        RoomPlayer blue = room.player(join(room, "blue")).orElseThrow();
        room.start(List.of(), NOW);
        red.pickaxeTier(3);

        room.die(red, 5);

        assertThat(red.state()).isEqualTo(State.RESPAWNING);
        assertThat(red.respawnAt()).isEqualTo(5);
        assertThat(red.pickaxeTier()).isEqualTo(2);
        assertThat(room.status(TeamColor.VERMELHO).kind()).isEqualTo(Kind.BED_ONLINE);

        room.breakBed(TeamColor.VERMELHO, blue);
        room.respawned(red, NOW);
        room.die(red, 5);

        assertThat(red.state()).isEqualTo(State.ELIMINATED);
        assertThat(room.winner()).contains(TeamColor.AZUL);
    }

    @Test
    void aMatchForcedWithASinglePlayerKeepsGoingUntilThatPlayerIsEliminated() {
        Room room = RoomFixtures.room("abc123", RoomFixtures.SOLO, RoomFixtures.arena("solo", "Athens", EIGHT));
        UUID tadeu = join(room, "tadeu");
        room.forceStart(10);
        room.start(List.of(), NOW);
        RoomPlayer player = room.player(tadeu).orElseThrow();

        assertThat(room.isOver()).isFalse();
        room.die(player, 5);
        assertThat(room.isOver()).isFalse();

        room.team(player.team()).breakBed(null);
        room.respawned(player, NOW);
        room.die(player, 5);
        assertThat(room.isOver()).isTrue();
        assertThat(room.winner()).isEmpty();
    }

    @Test
    void aForcedMatchWithTwoPlayersIsWonByEliminatingTheOther() {
        Room room = RoomFixtures.room("abc123", RoomFixtures.SOLO, RoomFixtures.arena("solo", "Athens", EIGHT));
        UUID tadeu = join(room, "tadeu");
        UUID other = join(room, "x572");
        room.forceStart(10);
        room.start(List.of(), NOW);
        RoomPlayer loser = room.player(other).orElseThrow();

        assertThat(room.isOver()).isFalse();
        room.team(loser.team()).breakBed(null);
        room.die(loser, 5);

        assertThat(room.isOver()).isTrue();
        assertThat(room.winner()).contains(room.player(tadeu).orElseThrow().team());
    }

    @Test
    void emptyTeamsLoseTheirBedButKeepTheirIslandWithForgesAndNpcs() {
        Room room = RoomFixtures.room("abc123", RoomFixtures.SOLO, RoomFixtures.arena("solo", "Athens", EIGHT));
        UUID tadeu = join(room, "tadeu");
        room.forceStart(10);
        room.start(List.of(), NOW);
        TeamColor own = room.player(tadeu).orElseThrow().team();

        assertThat(room.islands()).containsExactlyInAnyOrderElementsOf(EIGHT);
        assertThat(EIGHT).filteredOn(color -> color != own).allSatisfy(color -> assertThat(room.team(color).bedAlive()).isFalse());
        assertThat(room.team(own).bedAlive()).isTrue();
    }

    @Test
    void endingAForcedMatchWithASinglePlayerIsThatPlayersVictory() {
        Room room = RoomFixtures.room("abc123", RoomFixtures.SOLO, RoomFixtures.arena("solo", "Athens", EIGHT));
        UUID tadeu = join(room, "tadeu");
        room.forceStart(10);
        room.start(List.of(), NOW);

        assertThat(room.winner()).contains(room.player(tadeu).orElseThrow().team());
    }

    @Test
    void endingAMatchWithSeveralTeamsStandingIsADraw() {
        Room room = RoomFixtures.room("abc123", RoomFixtures.SOLO, RoomFixtures.arena("solo", "Athens", EIGHT));
        join(room, "tadeu");
        join(room, "x572");
        room.forceStart(10);
        room.start(List.of(), NOW);

        assertThat(room.winner()).isEmpty();
    }

    @Test
    void onlyOneWinnerOfTheTeamIsDrawnToPlayTheVictoryEffect() {
        Room room = RoomFixtures.room("abc123", RoomFixtures.ONE_V_ONE, RoomFixtures.arena("1v1", "Slip", RED_BLUE));
        UUID tadeu = join(room, "tadeu");
        UUID other = join(room, "x572");
        room.start(List.of(), NOW);
        TeamColor team = room.player(tadeu).orElseThrow().team();

        assertThat(room.celebrant(team, Set.of(tadeu, other), new Random(1))).contains(tadeu);
        assertThat(room.celebrant(team, Set.of(other), new Random(1))).isEmpty();
    }

    @Test
    void theCelebrantIsDrawnAmongTheOnlineWinners() {
        Room room = RoomFixtures.room("abc123", RoomFixtures.DOUBLES, RoomFixtures.arena("duplas", "Slip", RED_BLUE));
        UUID first = join(room, "tadeu");
        UUID second = join(room, "x572");
        room.forceStart(10);
        room.start(List.of(List.of(first, second)), NOW);
        TeamColor team = room.player(first).orElseThrow().team();
        Set<UUID> drawn = new HashSet<>();
        Random random = new Random(7);
        for (int draw = 0; draw < 40; draw++) {
            room.celebrant(team, Set.of(first, second), random).ifPresent(drawn::add);
        }

        assertThat(room.player(second).orElseThrow().team()).isEqualTo(team);
        assertThat(drawn).containsExactlyInAnyOrder(first, second);
    }

    @Test
    void staffCanOnlyEndAMatchThatIsBeingPlayed() {
        Room room = RoomFixtures.room("abc123", RoomFixtures.SOLO, RoomFixtures.arena("solo", "Athens", EIGHT));
        join(room, "tadeu");
        assertThat(room.canForceEnd()).isFalse();

        room.forceStart(10);
        room.start(List.of(), NOW);
        assertThat(room.canForceEnd()).isTrue();

        room.end();
        assertThat(room.canForceEnd()).isFalse();
    }

    @Test
    void aForcedStartReportsTheCountdownSoTheTenSecondsAreShownRightAway() {
        Room room = RoomFixtures.room("abc123", RoomFixtures.SOLO, RoomFixtures.arena("solo", "Athens", EIGHT));
        join(room, "tadeu");

        assertThat(room.forceStart(10)).contains(Change.STARTED);
        assertThat(room.countdown().remaining()).hasValue(10);
        assertThat(RoomFixtures.SETTINGS.countdownTitles().at(10)).contains("&610 segundos");
        assertThat(room.forceStart(10)).contains(Change.NONE);
    }

    @Test
    void forcingARunningCountdownShortensItToTenSeconds() {
        Room room = RoomFixtures.room("abc123", RoomFixtures.SOLO, RoomFixtures.arena("solo", "Athens", EIGHT));
        for (int index = 0; index < RoomFixtures.SOLO.minPlayers(); index++) {
            join(room, "player" + index);
        }
        assertThat(room.countdown().remaining()).hasValue(30);

        assertThat(room.forceStart(10)).contains(Change.SHORTENED);
        assertThat(room.countdown().remaining()).hasValue(10);
    }

    @Test
    void aMatchThatWasNotForcedEndsWhenOneTeamIsLeft() {
        Room room = RoomFixtures.room("abc123", RoomFixtures.ONE_V_ONE, RoomFixtures.arena("1v1", "Slip", RED_BLUE));
        join(room, "tadeu");
        UUID other = join(room, "x572");
        room.start(List.of(), NOW);
        RoomPlayer loser = room.player(other).orElseThrow();
        room.team(loser.team()).breakBed(null);
        room.die(loser, 5);

        assertThat(room.isOver()).isTrue();
    }

    @Test
    void staffCanForceAStartThatPlayersJoiningOrLeavingDoNotCancel() {
        Room room = RoomFixtures.room("abc123", RoomFixtures.SOLO, RoomFixtures.arena("solo", "Athens", EIGHT));
        assertThat(room.forceStart(10)).isEmpty();
        UUID first = join(room, "tadeu");

        assertThat(room.forceStart(10)).contains(Change.STARTED);
        UUID second = join(room, "x572");
        room.leaveBeforeStart(second);

        assertThat(room.phase()).isEqualTo(Room.Phase.STARTING);
        assertThat(room.countdown().remaining()).hasValue(10);
        assertThat(room.player(first)).isPresent();
    }

    @Test
    void onlyBlocksPlacedByPlayersAreRemembered() {
        Room room = RoomFixtures.room("abc123", RoomFixtures.ONE_V_ONE, RoomFixtures.arena("1v1", "Slip", RED_BLUE));

        room.placed(-1030, 64, 2049);

        assertThat(room.wasPlaced(-1030, 64, 2049)).isTrue();
        assertThat(room.wasPlaced(-1030, 65, 2049)).isFalse();
        room.unplaced(-1030, 64, 2049);
        assertThat(room.wasPlaced(-1030, 64, 2049)).isFalse();
    }

    @Test
    void playersStartOnTheirFavouriteColourWhenTheArenaHasIt() {
        Room room = RoomFixtures.room("abc124", RoomFixtures.SOLO, RoomFixtures.arena("solo", "Athens", EIGHT));
        UUID first = join(room, "tadeu");
        UUID second = join(room, "x572");
        UUID third = join(room, "lia");

        room.start(List.of(), Map.of(first, TeamColor.CINZA, third, TeamColor.ROSA), new Random(1), NOW);

        assertThat(room.player(first).orElseThrow().team()).isEqualTo(TeamColor.CINZA);
        assertThat(room.player(third).orElseThrow().team()).isEqualTo(TeamColor.ROSA);
        assertThat(room.player(second).orElseThrow().team()).isEqualTo(TeamColor.VERMELHO);
        assertThat(room.team(TeamColor.CINZA).members()).containsExactly(first);
        assertThat(room.team(TeamColor.ROSA).members()).containsExactly(third);
    }

    @Test
    void aColourTheArenaDoesNotHaveFallsBackToTheEmptiestTeam() {
        Room room = RoomFixtures.room("abc125", RoomFixtures.ONE_V_ONE, RoomFixtures.arena("1v1", "Slip", RED_BLUE));
        UUID first = join(room, "tadeu");
        UUID second = join(room, "x572");

        room.start(List.of(), Map.of(first, TeamColor.ROSA, second, TeamColor.AZUL), new Random(1), NOW);

        assertThat(room.player(second).orElseThrow().team()).isEqualTo(TeamColor.AZUL);
        assertThat(room.player(first).orElseThrow().team()).isEqualTo(TeamColor.VERMELHO);
    }

    @Test
    void inDoublesPlayersWhoShareAFavouriteColourPlayTogether() {
        Room room = RoomFixtures.room("abc126", RoomFixtures.DOUBLES, RoomFixtures.arena("duplas", "Slip", EIGHT.subList(0, 4)));
        UUID first = join(room, "tadeu");
        UUID second = join(room, "x572");
        UUID third = join(room, "lia");
        UUID fourth = join(room, "rui");

        room.start(List.of(), Map.of(second, TeamColor.VERDE, fourth, TeamColor.VERDE), new Random(1), NOW);

        assertThat(room.team(TeamColor.VERDE).members()).containsExactlyInAnyOrder(second, fourth);
        assertThat(room.player(first).orElseThrow().team()).isNotEqualTo(TeamColor.VERDE);
        assertThat(room.player(third).orElseThrow().team()).isNotEqualTo(TeamColor.VERDE);
    }

    @Test
    void startingWithoutPreferencesKeepsTheJoinOrderTeams() {
        Room room = RoomFixtures.room("abc127", RoomFixtures.SOLO, RoomFixtures.arena("solo", "Athens", EIGHT));
        UUID first = join(room, "tadeu");
        UUID second = join(room, "x572");

        room.start(List.of(), NOW);

        assertThat(room.player(first).orElseThrow().team()).isEqualTo(TeamColor.VERMELHO);
        assertThat(room.player(second).orElseThrow().team()).isEqualTo(TeamColor.AZUL);
    }

    private static UUID join(Room room, String name) {
        UUID id = UUID.nameUUIDFromBytes(name.getBytes());
        room.join(id, name);
        return id;
    }
}
