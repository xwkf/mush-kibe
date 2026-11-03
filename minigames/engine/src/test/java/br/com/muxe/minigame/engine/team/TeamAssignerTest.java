package br.com.muxe.minigame.engine.team;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TeamAssignerTest {

    private static final List<TeamColor> FOUR = List.of(TeamColor.AZUL, TeamColor.VERMELHO, TeamColor.VERDE, TeamColor.AMARELO);
    private static final long SEED_STEP = 0x9E3779B97F4A7C15L;

    @Test
    void soloPlayersEachGetTheirOwnTeamInJoinOrder() {
        List<UUID> players = players(3);

        Map<TeamColor, List<UUID>> teams = TeamAssigner.assign(FOUR, 1, players, List.of());

        assertThat(teams.get(TeamColor.AZUL)).containsExactly(players.get(0));
        assertThat(teams.get(TeamColor.VERMELHO)).containsExactly(players.get(1));
        assertThat(teams.get(TeamColor.VERDE)).containsExactly(players.get(2));
        assertThat(teams.get(TeamColor.AMARELO)).isEmpty();
    }

    @Test
    void anIncompleteRoomIsSpreadEvenlyAcrossTheTeams() {
        Map<TeamColor, List<UUID>> teams = TeamAssigner.assign(FOUR, 4, players(6), List.of());

        assertThat(teams.values()).extracting(List::size).containsExactly(2, 2, 1, 1);
    }

    @Test
    void aPartyStaysTogetherAndBigPartiesArePlacedFirst() {
        List<UUID> players = players(8);
        List<UUID> pair = List.of(players.get(6), players.get(7));
        List<UUID> trio = List.of(players.get(1), players.get(3), players.get(5));

        Map<TeamColor, List<UUID>> teams = TeamAssigner.assign(FOUR, 4, players, List.of(pair, trio));

        assertThat(teams.get(TeamColor.AZUL)).startsWith(trio.toArray(UUID[]::new));
        assertThat(teams.get(TeamColor.VERMELHO)).startsWith(pair.toArray(UUID[]::new));
        assertThat(teams.values().stream().mapToInt(List::size).sum()).isEqualTo(8);
    }

    @Test
    void aPartyLargerThanATeamSpillsIntoTheNextAndAbsentMembersAreIgnored() {
        List<UUID> players = players(3);
        List<UUID> party = new ArrayList<>(players);
        party.add(UUID.randomUUID());

        Map<TeamColor, List<UUID>> teams = TeamAssigner.assign(List.of(TeamColor.AZUL, TeamColor.VERMELHO), 2, players, List.of(party));

        assertThat(teams.get(TeamColor.AZUL)).containsExactly(players.get(0), players.get(1));
        assertThat(teams.get(TeamColor.VERMELHO)).containsExactly(players.get(2));
    }

    @Test
    void playersBeyondTheRoomSizeAreLeftOut() {
        Map<TeamColor, List<UUID>> teams = TeamAssigner.assign(List.of(TeamColor.AZUL, TeamColor.VERMELHO), 1, players(3), List.of());

        assertThat(teams.values().stream().mapToInt(List::size).sum()).isEqualTo(2);
    }

    @Test
    void anEmptyPreferenceMapKeepsTheOldBehaviour() {
        List<UUID> players = players(7);
        List<List<UUID>> parties = List.of(List.of(players.get(1), players.get(4)));

        Map<TeamColor, List<UUID>> old = TeamAssigner.assign(FOUR, 2, players, parties);
        Map<TeamColor, List<UUID>> same = TeamAssigner.assign(FOUR, 2, players, parties, Map.of(), new Random(1));

        assertThat(same).isEqualTo(old);
    }

    @Test
    void inSoloEveryonePreferringADifferentColourGetsIt() {
        List<UUID> players = players(3);
        Map<UUID, TeamColor> preferences = Map.of(players.get(0), TeamColor.AMARELO, players.get(1), TeamColor.VERDE, players.get(2), TeamColor.AZUL);

        Map<TeamColor, List<UUID>> teams = TeamAssigner.assign(FOUR, 1, players, List.of(), preferences, new Random(1));

        assertThat(teams.get(TeamColor.AMARELO)).containsExactly(players.get(0));
        assertThat(teams.get(TeamColor.VERDE)).containsExactly(players.get(1));
        assertThat(teams.get(TeamColor.AZUL)).containsExactly(players.get(2));
        assertThat(teams.get(TeamColor.VERMELHO)).isEmpty();
    }

    @Test
    void inSoloPlayersWithoutPreferenceTakeTheTeamsLeftOver() {
        List<UUID> players = players(3);
        Map<UUID, TeamColor> preferences = Map.of(players.get(2), TeamColor.AZUL);

        Map<TeamColor, List<UUID>> teams = TeamAssigner.assign(FOUR, 1, players, List.of(), preferences, new Random(1));

        assertThat(teams.get(TeamColor.AZUL)).containsExactly(players.get(2));
        assertThat(teams.get(TeamColor.VERMELHO)).containsExactly(players.get(0));
        assertThat(teams.get(TeamColor.VERDE)).containsExactly(players.get(1));
        assertThat(teams.get(TeamColor.AMARELO)).isEmpty();
    }

    @Test
    void inSoloTwoPlayersWantingTheSameColourDrawForIt() {
        List<UUID> players = players(2);
        Map<UUID, TeamColor> preferences = Map.of(players.get(0), TeamColor.VERDE, players.get(1), TeamColor.VERDE);
        Set<UUID> winners = new HashSet<>();

        for (long seed = 0; seed < 40; seed++) {
            Map<TeamColor, List<UUID>> teams = TeamAssigner.assign(FOUR, 1, players, List.of(), preferences, new Random(seed * SEED_STEP));

            assertThat(teams.get(TeamColor.VERDE)).hasSize(1);
            UUID winner = teams.get(TeamColor.VERDE).getFirst();
            UUID loser = players.get(0).equals(winner) ? players.get(1) : players.get(0);
            assertThat(teams.get(TeamColor.AZUL)).containsExactly(loser);
            winners.add(winner);
        }

        assertThat(winners).containsExactlyInAnyOrderElementsOf(players);
    }

    @Test
    void theDrawIsRepeatableWithTheSameRandom() {
        List<UUID> players = players(6);
        Map<UUID, TeamColor> preferences = new HashMap<>();
        players.forEach(player -> preferences.put(player, TeamColor.AZUL));

        Map<TeamColor, List<UUID>> first = TeamAssigner.assign(FOUR, 2, players, List.of(), preferences, new Random(99));
        Map<TeamColor, List<UUID>> second = TeamAssigner.assign(FOUR, 2, players, List.of(), preferences, new Random(99));

        assertThat(second).isEqualTo(first);
    }

    @Test
    void inDoublesPlayersWhoPreferTheSameColourEndUpTogether() {
        List<UUID> players = players(4);
        Map<UUID, TeamColor> preferences = Map.of(players.get(1), TeamColor.ROSA, players.get(3), TeamColor.ROSA);
        List<TeamColor> teams = List.of(TeamColor.AZUL, TeamColor.VERMELHO, TeamColor.ROSA, TeamColor.VERDE);

        Map<TeamColor, List<UUID>> assigned = TeamAssigner.assign(teams, 2, players, List.of(), preferences, new Random(1));

        assertThat(assigned.get(TeamColor.ROSA)).containsExactly(players.get(1), players.get(3));
        assertThat(assigned.get(TeamColor.AZUL)).containsExactly(players.get(0));
        assertThat(assigned.get(TeamColor.VERMELHO)).containsExactly(players.get(2));
    }

    @Test
    void inDoublesTheThirdPlayerWantingAFullColourFallsBack() {
        List<UUID> players = players(3);
        Map<UUID, TeamColor> preferences = Map.of(players.get(0), TeamColor.AZUL, players.get(1), TeamColor.AZUL, players.get(2), TeamColor.AZUL);
        Set<UUID> outsiders = new HashSet<>();

        for (long seed = 0; seed < 40; seed++) {
            Map<TeamColor, List<UUID>> teams = TeamAssigner.assign(FOUR, 2, players, List.of(), preferences, new Random(seed * SEED_STEP));

            assertThat(teams.get(TeamColor.AZUL)).hasSize(2);
            assertThat(teams.get(TeamColor.VERMELHO)).hasSize(1);
            assertThat(teams.get(TeamColor.VERDE)).isEmpty();
            outsiders.add(teams.get(TeamColor.VERMELHO).getFirst());
        }

        assertThat(outsiders).containsExactlyInAnyOrderElementsOf(players);
    }

    @Test
    void inQuadsPlayersWantingTheSameColourFillItAndTheOthersSpread() {
        List<UUID> players = players(8);
        Map<UUID, TeamColor> preferences = new HashMap<>();
        for (int index = 0; index < 5; index++) {
            preferences.put(players.get(index), TeamColor.VERDE);
        }
        preferences.put(players.get(5), TeamColor.AMARELO);

        Map<TeamColor, List<UUID>> teams = TeamAssigner.assign(FOUR, 4, players, List.of(), preferences, new Random(3));

        assertThat(teams.get(TeamColor.VERDE)).hasSize(4).allMatch(player -> players.indexOf(player) < 5);
        assertThat(teams.get(TeamColor.AMARELO)).contains(players.get(5));
        assertThat(teams.values().stream().mapToInt(List::size).sum()).isEqualTo(8);
        assertThat(teams.values()).allMatch(members -> members.size() <= 4);
    }

    @Test
    void aColourTheArenaDoesNotHaveIsIgnored() {
        List<UUID> players = players(2);
        Map<UUID, TeamColor> preferences = Map.of(players.get(0), TeamColor.LARANJA, players.get(1), TeamColor.ROSA);

        Map<TeamColor, List<UUID>> teams = TeamAssigner.assign(FOUR, 1, players, List.of(), preferences, new Random(1));

        assertThat(teams.keySet()).doesNotContain(TeamColor.LARANJA, TeamColor.ROSA);
        assertThat(teams.get(TeamColor.AZUL)).containsExactly(players.get(0));
        assertThat(teams.get(TeamColor.VERMELHO)).containsExactly(players.get(1));
    }

    @Test
    void aPartyTakesItsMostCommonPreferenceWhenThatTeamIsFree() {
        List<UUID> players = players(4);
        List<UUID> party = List.of(players.get(1), players.get(2));
        Map<UUID, TeamColor> preferences = Map.of(players.get(1), TeamColor.AMARELO, players.get(2), TeamColor.AMARELO, players.get(3), TeamColor.AZUL);

        Map<TeamColor, List<UUID>> teams = TeamAssigner.assign(FOUR, 2, players, List.of(party), preferences, new Random(1));

        assertThat(teams.get(TeamColor.AMARELO)).containsExactly(players.get(1), players.get(2));
        assertThat(teams.get(TeamColor.AZUL)).containsExactly(players.get(3));
        assertThat(teams.get(TeamColor.VERMELHO)).containsExactly(players.get(0));
    }

    @Test
    void aPartyWithDifferentPreferencesFollowsTheMostCommonAndTheFirstOnATie() {
        List<UUID> players = players(7);
        List<UUID> trio = List.of(players.get(0), players.get(1), players.get(2));
        List<UUID> pair = List.of(players.get(3), players.get(4));
        Map<UUID, TeamColor> preferences = Map.of(
            players.get(0), TeamColor.AZUL, players.get(1), TeamColor.VERDE, players.get(2), TeamColor.VERDE,
            players.get(3), TeamColor.AMARELO, players.get(4), TeamColor.VERMELHO);

        Map<TeamColor, List<UUID>> teams = TeamAssigner.assign(FOUR, 4, players, List.of(trio, pair), preferences, new Random(1));

        assertThat(teams.get(TeamColor.VERDE)).startsWith(trio.toArray(UUID[]::new));
        assertThat(teams.get(TeamColor.AMARELO)).startsWith(pair.toArray(UUID[]::new));
    }

    @Test
    void aPartyIsNeverSplitByPreferences() {
        List<UUID> players = players(4);
        List<UUID> party = List.of(players.get(0), players.get(1));
        Map<UUID, TeamColor> preferences = Map.of(players.get(0), TeamColor.AZUL, players.get(1), TeamColor.VERMELHO,
            players.get(2), TeamColor.AZUL, players.get(3), TeamColor.VERMELHO);

        Map<TeamColor, List<UUID>> teams = TeamAssigner.assign(FOUR, 2, players, List.of(party), preferences, new Random(1));

        assertThat(teams.get(TeamColor.AZUL)).containsExactly(players.get(0), players.get(1));
        assertThat(teams.get(TeamColor.VERMELHO)).containsExactly(players.get(3));
        assertThat(teams.get(TeamColor.VERDE)).containsExactly(players.get(2));
    }

    @Test
    void aPartyWhosePreferredTeamIsTakenOrMissingGoesToTheEmptiestTeam() {
        List<UUID> players = players(4);
        List<UUID> firstParty = List.of(players.get(0), players.get(1));
        List<UUID> secondParty = List.of(players.get(2), players.get(3));
        Map<UUID, TeamColor> preferences = Map.of(players.get(0), TeamColor.VERDE, players.get(1), TeamColor.VERDE,
            players.get(2), TeamColor.VERDE, players.get(3), TeamColor.VERDE);

        Map<TeamColor, List<UUID>> teams = TeamAssigner.assign(FOUR, 2, players, List.of(firstParty, secondParty), preferences, new Random(1));

        assertThat(teams.get(TeamColor.VERDE)).containsExactly(players.get(0), players.get(1));
        assertThat(teams.get(TeamColor.AZUL)).containsExactly(players.get(2), players.get(3));

        Map<TeamColor, List<UUID>> missing = TeamAssigner.assign(FOUR, 2, players, List.of(firstParty), Map.of(players.get(0), TeamColor.ROSA), new Random(1));

        assertThat(missing.get(TeamColor.AZUL)).containsExactly(players.get(0), players.get(1));
    }

    @Test
    void aPartyBiggerThanATeamStartsOnItsPreferredTeamAndSpillsOver() {
        List<UUID> players = players(3);
        Map<UUID, TeamColor> preferences = Map.of(players.get(0), TeamColor.VERDE, players.get(1), TeamColor.VERDE, players.get(2), TeamColor.VERDE);

        Map<TeamColor, List<UUID>> teams = TeamAssigner.assign(FOUR, 2, players, List.of(players), preferences, new Random(1));

        assertThat(teams.get(TeamColor.VERDE)).containsExactly(players.get(0), players.get(1));
        assertThat(teams.get(TeamColor.AZUL)).containsExactly(players.get(2));
    }

    @Test
    void loosePlayersCannotTakeASeatInATeamThePartyFilled() {
        List<UUID> players = players(3);
        List<UUID> party = List.of(players.get(0), players.get(1));
        Map<UUID, TeamColor> preferences = Map.of(players.get(0), TeamColor.AZUL, players.get(1), TeamColor.AZUL, players.get(2), TeamColor.AZUL);

        Map<TeamColor, List<UUID>> teams = TeamAssigner.assign(FOUR, 2, players, List.of(party), preferences, new Random(1));

        assertThat(teams.get(TeamColor.AZUL)).containsExactly(players.get(0), players.get(1));
        assertThat(teams.get(TeamColor.VERMELHO)).containsExactly(players.get(2));
    }

    @Test
    void aOnePlayerPartyIsTreatedAsALoosePlayer() {
        List<UUID> players = players(2);
        List<List<UUID>> parties = List.of(List.of(players.get(0)));

        Map<TeamColor, List<UUID>> teams = TeamAssigner.assign(FOUR, 1, players, parties, Map.of(players.get(0), TeamColor.AMARELO), new Random(1));

        assertThat(teams.get(TeamColor.AMARELO)).containsExactly(players.get(0));
        assertThat(teams.get(TeamColor.AZUL)).containsExactly(players.get(1));
    }

    @Test
    void preferencesOfPlayersOutsideTheRoomAreIgnored() {
        List<UUID> players = players(2);
        Map<UUID, TeamColor> preferences = Map.of(UUID.randomUUID(), TeamColor.AZUL);

        Map<TeamColor, List<UUID>> teams = TeamAssigner.assign(FOUR, 1, players, List.of(), preferences, new Random(1));

        assertThat(teams.get(TeamColor.AZUL)).containsExactly(players.get(0));
        assertThat(teams.get(TeamColor.VERMELHO)).containsExactly(players.get(1));
    }

    @Test
    void playersBeyondTheRoomSizeAreStillLeftOutWithPreferences() {
        List<UUID> players = players(3);
        Map<UUID, TeamColor> preferences = Map.of(players.get(0), TeamColor.AZUL, players.get(1), TeamColor.AZUL, players.get(2), TeamColor.AZUL);

        Map<TeamColor, List<UUID>> teams = TeamAssigner.assign(List.of(TeamColor.AZUL, TeamColor.VERMELHO), 1, players, List.of(), preferences, new Random(1));

        assertThat(teams.values().stream().mapToInt(List::size).sum()).isEqualTo(2);
    }

    private static List<UUID> players(int count) {
        List<UUID> players = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            players.add(new UUID(0, index + 1));
        }
        return players;
    }
}
