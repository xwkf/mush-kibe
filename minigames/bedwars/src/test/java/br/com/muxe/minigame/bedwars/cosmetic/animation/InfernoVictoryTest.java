package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.bedwars.cosmetic.animation.RecordingStage.FakeActor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class InfernoVictoryTest {

    private static final Vec ORIGIN = new Vec(0.5, 27, 0.5);
    private static final Vec FACING = new Vec(0, 0, 1);
    private static final String DEBRIS = "FallingBlock:" + Material.NETHERRACK + ":0";

    private final UUID winner = UUID.randomUUID();

    private static RecordingStage stage() {
        return new RecordingStage(block -> block[1] < 27);
    }

    private static Timeline script(Stage stage, List<UUID> riders) {
        Timeline timeline = new Timeline();
        new InfernoVictory().script(new Scene(ORIGIN, FACING, Optional.empty(), riders), stage, timeline, new Random(7));
        return timeline;
    }

    private static void run(Timeline timeline, int from, int to) {
        for (int tick = from; tick <= to; tick++) {
            timeline.run(tick);
        }
    }

    private static List<FakeActor> actors(RecordingStage stage, String type) {
        return stage.actors.stream().filter(actor -> actor.type.equals(type)).toList();
    }

    private RecordingStage standing() {
        RecordingStage stage = stage();
        stage.eyesOf.put(winner, ORIGIN.add(0, Riders.EYE_HEIGHT, 0));
        return stage;
    }

    @Test
    void itIsTheInfernoEffectAndLastsTwoHundredTicks() {
        RecordingStage stage = standing();
        Timeline timeline = script(stage, List.of(winner));

        run(timeline, 0, timeline.length());

        assertThat(new InfernoVictory().id()).isEqualTo("inferno");
        assertThat(timeline.length()).isEqualTo(InfernoVictory.LENGTH);
        assertThat(stage.alive()).isZero();
    }

    @Test
    void theGroundUnderTheWinnerTurnsIntoNetherrackOnTheFirstTick() {
        RecordingStage stage = standing();
        Timeline timeline = script(stage, List.of(winner));

        run(timeline, 0, 0);
        assertThat(stage.bulk).isEmpty();
        run(timeline, 1, 1);

        assertThat(stage.bulk).anyMatch(change -> change.x() == 0 && change.y() == 26 && change.z() == 0
            && (change.material() == Material.NETHERRACK || change.material() == Material.STATIONARY_LAVA));
    }

    @Test
    void nightFallsOverTheFirstSecondEasingOutFromNoonToMidnight() {
        RecordingStage stage = standing();
        Timeline timeline = script(stage, List.of(winner));

        run(timeline, 0, timeline.length());

        assertThat(stage.times).hasSize(InfernoVictory.DUSK_TICKS);
        assertThat(stage.times.subList(0, 3)).containsExactly(7170L, 8280L, 9330L);
        assertThat(stage.times.get(9)).isEqualTo(15000L);
        assertThat(stage.times.getLast()).isEqualTo(18000L);
        assertThat(InfernoVictory.dayTime(40)).isEqualTo(18000L);
    }

    @Test
    void fifteenBlazesAppearOnARingAroundTheWinnerAndSinkToTheGround() {
        RecordingStage stage = standing();
        Timeline timeline = script(stage, List.of(winner));

        run(timeline, 0, 0);
        List<FakeActor> blazes = actors(stage, "Blaze");
        assertThat(blazes).hasSize(InfernoVictory.BLAZES);
        for (FakeActor blaze : blazes) {
            assertThat(Math.hypot(blaze.position.x() - ORIGIN.x(), blaze.position.z() - ORIGIN.z())).isBetween(9.2, 10.8);
            assertThat(blaze.position.y()).isBetween(27.0, 27.0 + InfernoVictory.BLAZE_MAX_HEIGHT);
        }
        run(timeline, 1, InfernoVictory.LENGTH - 1);

        assertThat(blazes).allSatisfy(blaze -> {
            assertThat(blaze.removed()).isFalse();
            assertThat(blaze.position.y()).isEqualTo(27.0);
        });
    }

    @Test
    void aBlazeShootsAFireballEveryFiveTicksFromTickFive() {
        RecordingStage stage = standing();
        Timeline timeline = script(stage, List.of(winner));

        run(timeline, 0, 4);
        assertThat(actors(stage, "Fireball")).isEmpty();
        run(timeline, 5, 5);
        assertThat(actors(stage, "Fireball")).hasSize(1);
        run(timeline, 6, 99);

        assertThat(actors(stage, "Fireball")).hasSize(19);
        assertThat(stage.headings).allMatch(FACING::equals);
    }

    @Test
    void aFireballThatHitsAPlayerLaunchesThemWithABigExplosion() {
        RecordingStage stage = standing();
        Timeline timeline = script(stage, List.of(winner));

        run(timeline, 0, 60);

        assertThat(stage.pushes).isNotEmpty();
        for (Vec push : stage.pushes) {
            assertThat(push.y()).isEqualTo(InfernoVictory.KNOCK_UP);
            assertThat(Math.hypot(push.x(), push.z())).isCloseTo(InfernoVictory.KNOCK_SIDEWAYS, within(1e-9));
        }
        assertThat(stage.particles.get("EXPLOSION_HUGE")).isEqualTo(stage.pushes.size());
        assertThat(stage.particles.get("EXPLOSION_LARGE")).isGreaterThanOrEqualTo(stage.pushes.size());
    }

    @Test
    void aFireballThatHitsTheGroundBlowsACraterAndThrowsNetherrackThatVanishesLater() {
        RecordingStage stage = stage();
        Timeline timeline = script(stage, List.of());
        Map<FakeActor, Integer> born = new HashMap<>();

        for (int tick = 0; tick < InfernoVictory.LENGTH; tick++) {
            timeline.run(tick);
            for (FakeActor piece : actors(stage, DEBRIS)) {
                born.putIfAbsent(piece, tick);
            }
            for (Map.Entry<FakeActor, Integer> piece : born.entrySet()) {
                int age = tick - piece.getValue();
                assertThat(piece.getKey().removed()).isEqualTo(age >= InfernoVictory.DEBRIS_LIFE);
            }
        }

        assertThat(stage.bulk).anyMatch(change -> change.material() == Material.AIR);
        assertThat(born).isNotEmpty();
        assertThat(stage.pushes).isEmpty();
    }

    @Test
    void flamesBurstAroundTheWinnerEveryTickAndTheFireworksLaunchEveryTenTicks() {
        RecordingStage stage = standing();
        Timeline timeline = script(stage, List.of(winner));

        run(timeline, 0, InfernoVictory.LENGTH);

        assertThat(stage.particles.get("FLAME")).isEqualTo(InfernoVictory.LENGTH - 1);
        assertThat(stage.sounds.stream().filter("fireworks.launch"::equals)).hasSize(20);
    }

    @Test
    void theBlazesAimAtEveryLivingPlayer() {
        UUID other = UUID.randomUUID();
        RecordingStage recording = standing();
        recording.eyesOf.put(other, new Vec(12.5, 27 + Riders.EYE_HEIGHT, 0.5));
        Set<UUID> launched = new HashSet<>();
        Stage stage = (Stage) Proxy.newProxyInstance(Stage.class.getClassLoader(), new Class<?>[] {Stage.class}, (proxy, method, args) -> {
            if (method.getName().equals("push")) {
                launched.add((UUID) args[0]);
            }
            try {
                return method.invoke(recording, args);
            } catch (InvocationTargetException exception) {
                throw exception.getCause();
            }
        });
        Timeline timeline = script(stage, List.of(winner, other));

        run(timeline, 0, InfernoVictory.LENGTH);

        assertThat(launched).containsExactlyInAnyOrder(winner, other);
    }
}
