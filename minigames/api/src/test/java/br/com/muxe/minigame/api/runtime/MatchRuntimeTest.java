package br.com.muxe.minigame.api.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.common.testing.MutableClock;
import br.com.muxe.minigame.api.match.Match;
import br.com.muxe.minigame.api.match.MatchPhase;
import br.com.muxe.minigame.api.match.Participant.State;
import br.com.muxe.minigame.api.match.Participant;
import br.com.muxe.minigame.api.match.TestArena;
import br.com.muxe.minigame.api.runtime.Death.Cause;
import br.com.muxe.minigame.api.runtime.Stage.Look;
import br.com.muxe.minigame.engine.mode.GameMode;
import br.com.muxe.minigame.engine.team.TeamColor;
import br.com.muxe.minigame.engine.world.cell.Cell;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MatchRuntimeTest {

    private static final GameMode ONE_V_ONE = new GameMode("1v1", "1v1", 2, 2, 1);
    private static final MatchRules RESPAWNING = MatchRules.defaults("mxt").respawning();

    private final MutableClock clock = MutableClock.startingAt("2026-11-07T20:00:00Z");
    private final FakeStage stage = new FakeStage();
    private final List<MatchRuntime<RecordingState>> closed = new ArrayList<>();
    private final UUID red = UUID.nameUUIDFromBytes("red".getBytes());
    private final UUID blue = UUID.nameUUIDFromBytes("blue".getBytes());

    @Test
    void joiningPutsThePlayerInTheWaitingRoomWithTheHotbarAndTheBoard() {
        MatchRuntime<RecordingState> runtime = runtime(RESPAWNING);

        join(runtime, red, "red");

        assertThat(stage.looks).containsEntry(red, Look.PLAYER);
        assertThat(stage.positions.get(red)).isEqualTo(runtime.match().arena().waitingSpawn());
        assertThat(stage.items.get(red)).extracting("material").containsExactly("CHEST", "BED");
        assertThat(stage.messagesOf(red)).containsExactly("red entrou (1/2)");
        assertThat(stage.boards.get(red)).containsExactly("abc123", "Jogadores: 1/2");
        assertThat(runtime.state().calls).containsExactly("arrived");
    }

    @Test
    void aFullMatchCountsDownTenSecondsAndStartsWithEveryoneOnTheirSpawn() {
        MatchRuntime<RecordingState> runtime = runtime(RESPAWNING);
        join(runtime, red, "red");
        join(runtime, blue, "blue");

        assertThat(runtime.match().phase()).isEqualTo(MatchPhase.STARTING);
        assertThat(stage.lastTitle(red).orElseThrow().title()).isEqualTo("§610 segundos");

        seconds(runtime, 10);

        Match match = runtime.match();
        assertThat(match.phase()).isEqualTo(MatchPhase.PLAYING);
        assertThat(stage.positions.get(red)).isEqualTo(match.arena().spawn(match.player(red).orElseThrow().team()));
        assertThat(stage.erased).containsExactlyElementsOf(match.arena().waitingArea());
        assertThat(stage.messagesOf(blue)).contains("Começou!");
        assertThat(runtime.state().calls).containsSubsequence("starting", "spawned red", "spawned blue", "started");
        assertThat(stage.boards.get(red)).contains("Pontos: 7", "§cV §fVermelho: §a✔ §7VOCÊ");
    }

    @Test
    void dyingWhileTheTeamRespawnsMakesAGhostThatComesBackAfterTheRespawnTime() {
        MatchRuntime<RecordingState> runtime = started(RESPAWNING);

        runtime.die(red, false);

        Participant player = runtime.match().player(red).orElseThrow();
        assertThat(player.state()).isEqualTo(State.RESPAWNING);
        assertThat(stage.looks).containsEntry(red, Look.SPECTATOR);
        assertThat(stage.positions.get(red)).isEqualTo(runtime.match().arena().spectator());
        assertThat(stage.lastTitle(red).orElseThrow().subtitle()).isEqualTo("renasce em 5");
        assertThat(stage.messagesOf(blue)).contains("§cred morreu");

        seconds(runtime, 4);
        assertThat(player.state()).isEqualTo(State.RESPAWNING);
        seconds(runtime, 1);

        assertThat(player.state()).isEqualTo(State.ALIVE);
        assertThat(stage.looks).containsEntry(red, Look.PLAYER);
        assertThat(player.isProtected(clock.instant())).isTrue();
    }

    @Test
    void theFinalKillEndsTheMatchCreditsTheKillerAndShowsTheResult() {
        MatchRuntime<RecordingState> runtime = started(RESPAWNING);
        Match match = runtime.match();
        TeamColor redTeam = match.player(red).orElseThrow().team();
        match.credit().hit(red, blue, clock.instant());
        match.team(redTeam).stopRespawning(blue);

        runtime.die(red, false);

        Death death = runtime.state().deaths.getFirst();
        assertThat(death.finalKill()).isTrue();
        assertThat(death.killer().map(Participant::id)).contains(blue);
        assertThat(death.cause()).isEqualTo(Cause.DAMAGE);
        assertThat(match.player(blue).orElseThrow().finalKills()).isEqualTo(1);
        assertThat(match.phase()).isEqualTo(MatchPhase.ENDING);
        assertThat(stage.messagesOf(blue)).contains("§cred morreu para §9blue", "Vermelho eliminado");
        assertThat(stage.endTitles).containsEntry(blue, true).containsEntry(red, false);
        assertThat(stage.centered.get(blue)).containsExactly("Vencedor: Azul", "1. blue 1");
        assertThat(stage.items.get(blue)).extracting("material").containsExactly("PAPER", "BED");
        assertThat(runtime.state().winner).contains(match.player(blue).orElseThrow().team());
    }

    @Test
    void inAGameWithoutRespawnTheFirstDeathEndsADuel() {
        MatchRuntime<RecordingState> runtime = started(MatchRules.defaults("mxt"));

        runtime.fellIntoVoid(red);

        assertThat(runtime.match().player(red).orElseThrow().state()).isEqualTo(State.ELIMINATED);
        assertThat(runtime.state().deaths.getFirst().cause()).isEqualTo(Cause.VOID);
        assertThat(runtime.match().phase()).isEqualTo(MatchPhase.ENDING);
    }

    @Test
    void leavingWhileTheTeamRespawnsKeepsTheSeatUntilThePlayerReturns() {
        MatchRuntime<RecordingState> runtime = started(RESPAWNING);

        stage.present.remove(red);
        runtime.quit(red);

        Participant player = runtime.match().player(red).orElseThrow();
        assertThat(player.state()).isEqualTo(State.DISCONNECTED);
        assertThat(stage.returns).containsExactly(red);
        assertThat(runtime.canReturn(red, clock.instant())).isTrue();

        stage.present.add(red);
        runtime.rejoin(red);

        assertThat(player.state()).isEqualTo(State.RESPAWNING);
        assertThat(stage.returns).isEmpty();
        assertThat(stage.messagesOf(blue)).contains("§cred voltou");
    }

    @Test
    void aPlayerWhoDoesNotReturnInTimeIsEliminated() {
        MatchRuntime<RecordingState> runtime = started(RESPAWNING);
        stage.present.remove(red);
        runtime.quit(red);

        clock.advance(Duration.ofSeconds(181));
        seconds(runtime, 1);

        assertThat(runtime.match().player(red).orElseThrow().state()).isEqualTo(State.ELIMINATED);
        assertThat(runtime.state().deaths.getFirst().cause()).isEqualTo(Cause.TIMEOUT);
        assertThat(runtime.match().phase()).isEqualTo(MatchPhase.ENDING);
        assertThat(stage.returns).isEmpty();
    }

    @Test
    void leavingWithoutRespawnIsAFinalDeathCreditedToTheLastHitter() {
        MatchRuntime<RecordingState> runtime = started(MatchRules.defaults("mxt"));
        runtime.match().credit().hit(red, blue, clock.instant());

        stage.present.remove(red);
        runtime.quit(red);

        Death death = runtime.state().deaths.getFirst();
        assertThat(death.cause()).isEqualTo(Cause.QUIT);
        assertThat(death.killer().map(Participant::id)).contains(blue);
        assertThat(runtime.match().player(blue).orElseThrow().finalKills()).isEqualTo(1);
    }

    @Test
    void leavingTheWaitingRoomTellsTheOthersAndCancelsTheCountdown() {
        MatchRuntime<RecordingState> runtime = runtime(RESPAWNING);
        join(runtime, red, "red");
        join(runtime, blue, "blue");

        stage.present.remove(red);
        runtime.quit(red);

        assertThat(runtime.match().players()).extracting(Participant::id).containsExactly(blue);
        assertThat(runtime.match().phase()).isEqualTo(MatchPhase.WAITING);
        assertThat(stage.messagesOf(blue)).contains("red saiu");
        assertThat(stage.boards).doesNotContainKey(red);
    }

    @Test
    void afterTheEndPlayersGoToTheLobbyAndTheMatchCloses() {
        MatchRuntime<RecordingState> runtime = started(RESPAWNING);
        runtime.forceEnd();

        seconds(runtime, 10);
        assertThat(stage.sentTo).containsEntry(red, "game-lobby").containsEntry(blue, "game-lobby");
        assertThat(closed).isEmpty();

        seconds(runtime, 3);

        assertThat(closed).containsExactly(runtime);
        assertThat(runtime.state().calls).endsWith("closed");
        assertThat(stage.teleportedOut).containsExactlyInAnyOrder(red, blue);
    }

    @Test
    void fallingIntoTheVoidBeforeTheStartGoesBackToTheWaitingSpawn() {
        MatchRuntime<RecordingState> runtime = runtime(RESPAWNING);
        join(runtime, red, "red");
        stage.positions.remove(red);

        runtime.fellIntoVoid(red);

        assertThat(stage.positions.get(red)).isEqualTo(runtime.match().arena().waitingSpawn());
        assertThat(runtime.belowVoid(40)).isTrue();
        assertThat(runtime.belowVoid(41)).isFalse();
    }

    @Test
    void spectatorsAreTheDeadAndTheEliminatedOnceTheMatchIsOn() {
        MatchRuntime<RecordingState> runtime = started(RESPAWNING);

        runtime.die(red, false);

        assertThat(runtime.isSpectator(red)).isTrue();
        assertThat(runtime.isSpectator(blue)).isFalse();
        assertThat(stage.spectators).containsExactly(red);
    }

    private MatchRuntime<RecordingState> runtime(MatchRules rules) {
        Match match = new Match("abc123", ONE_V_ONE, TestArena.of("1v1", TestArena.RED_BLUE), new Cell(0, 0, 0, 64), rules.respawn());
        return new MatchRuntime<>(match, stage, RuntimeFixtures.environment(clock, rules), closed::add, opened -> new RecordingState());
    }

    private MatchRuntime<RecordingState> started(MatchRules rules) {
        MatchRuntime<RecordingState> runtime = runtime(rules);
        join(runtime, red, "red");
        join(runtime, blue, "blue");
        seconds(runtime, 10);
        return runtime;
    }

    private void join(MatchRuntime<RecordingState> runtime, UUID id, String name) {
        stage.present.add(id);
        runtime.join(id, name);
    }

    private void seconds(MatchRuntime<RecordingState> runtime, int count) {
        for (int second = 0; second < count; second++) {
            runtime.second();
        }
    }
}
