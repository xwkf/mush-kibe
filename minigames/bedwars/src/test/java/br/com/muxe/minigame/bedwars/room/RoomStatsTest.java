package br.com.muxe.minigame.bedwars.room;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.minigame.bedwars.game.KillCredit;
import br.com.muxe.minigame.bedwars.game.XpRewards;
import br.com.muxe.minigame.bedwars.stats.domain.MatchOutcome;
import br.com.muxe.minigame.bedwars.stats.domain.MatchResult;
import br.com.muxe.minigame.engine.team.TeamColor;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RoomStatsTest {

    private static final Instant NOW = Instant.parse("2026-11-02T20:00:00Z");
    private static final List<TeamColor> RED_BLUE = List.of(TeamColor.VERMELHO, TeamColor.AZUL);

    @Test
    void dyingWithABedIsADeathAndWithoutItAFinalDeath() {
        Room room = started();
        RoomPlayer red = player(room, TeamColor.VERMELHO);
        RoomPlayer blue = player(room, TeamColor.AZUL);

        room.die(red, 5);
        room.breakBed(TeamColor.VERMELHO, blue);
        room.respawned(red, NOW);
        room.die(red, 5);

        assertThat(red.deaths()).isEqualTo(1);
        assertThat(red.finalDeaths()).isEqualTo(1);
        assertThat(red.bedsLost()).isEqualTo(1);
        assertThat(blue.bedsLost()).isZero();
        assertThat(blue.beds()).isEqualTo(1);
    }

    @Test
    void anEliminationIsAFinalDeath() {
        Room room = started();
        RoomPlayer red = player(room, TeamColor.VERMELHO);

        room.eliminate(red);

        assertThat(red.finalDeaths()).isEqualTo(1);
        assertThat(red.deaths()).isZero();
    }

    @Test
    void everyOtherRecentAttackerOfTheVictimGetsAnAssist() {
        Room room = RoomFixtures.room("2v7bss", RoomFixtures.DOUBLES, RoomFixtures.arena("duplas", "Slip", RED_BLUE));
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID c = UUID.randomUUID();
        UUID d = UUID.randomUUID();
        room.join(a, "a");
        room.join(b, "b");
        room.join(c, "c");
        room.join(d, "d");
        room.start(List.of(List.of(a, b), List.of(c, d)), NOW);
        RoomPlayer victim = room.player(a).orElseThrow();
        RoomPlayer killer = room.player(c).orElseThrow();
        RoomPlayer helper = room.player(d).orElseThrow();
        RoomPlayer teammate = room.player(b).orElseThrow();
        room.credit().hit(a, d, NOW);
        room.credit().hit(a, b, NOW);
        room.credit().hit(a, c, NOW.plusSeconds(2));

        room.assists(victim, killer, false, NOW.plusSeconds(3));

        assertThat(helper.assists()).isEqualTo(1);
        assertThat(killer.assists()).isZero();
        assertThat(teammate.assists()).isZero();
    }

    @Test
    void assistsExpireWithTheKillWindow() {
        KillCredit credit = new KillCredit(Duration.ofSeconds(15));
        UUID victim = UUID.randomUUID();
        UUID old = UUID.randomUUID();
        UUID recent = UUID.randomUUID();
        UUID killer = UUID.randomUUID();
        credit.hit(victim, old, NOW);
        credit.hit(victim, recent, NOW.plusSeconds(10));
        credit.hit(victim, killer, NOW.plusSeconds(12));

        assertThat(credit.assisters(victim, killer, NOW.plusSeconds(20))).containsExactly(recent);

        credit.forget(victim);

        assertThat(credit.assisters(victim, killer, NOW.plusSeconds(20))).isEmpty();
    }

    @Test
    void killsAndBedsAddTheirExperience() {
        Room room = started();
        RoomPlayer blue = player(room, TeamColor.AZUL);

        blue.addXp(12);
        blue.addXp(100);

        assertThat(blue.xp()).isEqualTo(112);
    }

    @Test
    void theEndOfTheMatchTurnsEveryPlayerIntoAnOutcome() {
        Room room = started();
        RoomPlayer red = player(room, TeamColor.VERMELHO);
        RoomPlayer blue = player(room, TeamColor.AZUL);
        room.breakBed(TeamColor.VERMELHO, blue);
        blue.addXp(100);
        room.die(red, 5);
        room.credit(blue, true);
        blue.addXp(60);
        for (int second = 0; second < 125; second++) {
            room.tick();
        }

        List<MatchOutcome> outcomes = room.outcomes(Optional.of(TeamColor.AZUL), new XpRewards(10, 5, 250, 100, 12, 60));

        assertThat(outcomes).containsExactlyInAnyOrder(
            new MatchOutcome(blue.id(), "1v1", 1, 0, 0, 0, 0, 1, 0, 0, MatchResult.WIN, 160 + 20 + 250),
            new MatchOutcome(red.id(), "1v1", 0, 1, 0, 0, 0, 0, 1, 0, MatchResult.LOSS, 20));
        assertThat(room.outcomes(Optional.empty(), new XpRewards(10, 5, 250, 100, 12, 60))).extracting(MatchOutcome::result).containsOnly(MatchResult.NONE);
    }

    @Test
    void missionTotalsFollowTheMatch() {
        Room room = started();
        RoomPlayer red = player(room, TeamColor.VERMELHO);
        RoomPlayer blue = player(room, TeamColor.AZUL);
        room.credit(red, false);
        room.credit(red, false);
        room.die(red, 5);
        room.credit(red, true);
        red.addEmeralds(3);
        red.addFireballDamage(2.5);
        red.addClay(16);
        for (int second = 0; second < 200; second++) {
            room.tick();
        }

        Map<String, Long> winning = room.missionTotals(red, Optional.of(TeamColor.VERMELHO), 210, 10);
        blue.useExplosive();
        Map<String, Long> losing = room.missionTotals(blue, Optional.of(TeamColor.VERMELHO), 210, 10);

        assertThat(winning).containsEntry("KILL_STREAK", 2L).containsEntry("FINAL_KILLS", 1L).containsEntry("EMERALDS_IN_A_LIFE", 3L)
            .containsEntry("FIREBALL_DAMAGE", 2L).containsEntry("CLAY_BOUGHT", 16L).containsEntry("FAST_WIN", 1L).containsEntry("CLEAN_WIN", 1L);
        assertThat(losing).containsEntry("FAST_WIN", 0L).containsEntry("CLEAN_WIN", 0L);
        assertThat(room.missionTotals(red, Optional.empty(), 210, 10)).containsEntry("FAST_WIN", 0L);
    }

    private static Room started() {
        Room room = RoomFixtures.room("2v7bss", RoomFixtures.ONE_V_ONE, RoomFixtures.arena("1v1", "Slip", RED_BLUE));
        room.join(UUID.randomUUID(), "tadeu");
        room.join(UUID.randomUUID(), "x572");
        room.start(List.of(), NOW);
        return room;
    }

    private static RoomPlayer player(Room room, TeamColor team) {
        return room.players().stream().filter(player -> player.team() == team).findFirst().orElseThrow();
    }
}
