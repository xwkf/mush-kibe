package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.api.Test;

class DuskFinalKillTest {

    private final Vec victim = new Vec(8.5, 27, -1.5);
    private final Vec right = new Vec(1, 0, 0);
    private final Vec ahead = new Vec(0, 0, 1);

    @Test
    void theOwlSwoopsInFromTheLeftAndSlowsDownOntoTheVictim() {
        Vec grip = victim.add(0, DuskFinalKill.GRIP, 0);

        Vec start = DuskFinalKill.position(grip, right, ahead, 0);
        Vec next = DuskFinalKill.position(grip, right, ahead, 1);
        Vec grab = DuskFinalKill.position(grip, right, ahead, DuskFinalKill.GRAB);

        assertThat(start.x() - grip.x()).isCloseTo(-9.934, offset(1e-9));
        assertThat(start.y() - grip.y()).isCloseTo(4.156, offset(1e-9));
        assertThat(next.x() - grip.x()).isCloseTo(-4.6, offset(0.2));
        assertThat(DuskFinalKill.glide(1)).isCloseTo(0.68, offset(0.01));
        assertThat(DuskFinalKill.glide(5)).isCloseTo(0.14, offset(0.02));
        assertThat(grab.distance(grip)).isCloseTo(0, offset(1e-9));
    }

    @Test
    void theOwlCarriesTheVictimAwayFasterAndFasterUpToTheRight() {
        double first = DuskFinalKill.flown(1);
        double second = DuskFinalKill.flown(2) - first;
        double last = DuskFinalKill.flown(DuskFinalKill.DEPARTURE) - DuskFinalKill.flown(DuskFinalKill.DEPARTURE - 1);
        Vec end = DuskFinalKill.position(victim, right, ahead, DuskFinalKill.END);

        assertThat(first).isCloseTo(0.6, offset(0.02));
        assertThat(second).isGreaterThan(first);
        assertThat(last).isCloseTo(6.7, offset(0.2));
        assertThat(end.x() - victim.x()).isCloseTo(29.9, offset(0.3));
        assertThat(end.y() - victim.y()).isCloseTo(29.9 * DuskFinalKill.CLIMB, offset(0.3));
    }

    @Test
    void theVictimHangsUnderTheOwlUntilBothVanish() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);
        Timeline timeline = new Timeline();

        new DuskFinalKill().script(new Scene(victim, ahead, Optional.of("skin")), stage, timeline, new Random(2));
        RecordingStage.FakeActor body = stage.actors.get(0);
        RecordingStage.FakeActor owl = stage.actors.get(1);
        for (int tick = 0; tick <= DuskFinalKill.GRAB; tick++) {
            timeline.run(tick);
        }
        assertThat(body.position).isEqualTo(victim);
        timeline.run(DuskFinalKill.GRAB + 1);
        assertThat(owl.position.y() - body.position.y()).isCloseTo(DuskFinalKill.GRIP, offset(1e-9));
        assertThat(body.position.y()).isGreaterThan(victim.y());
        for (int tick = DuskFinalKill.GRAB + 2; tick <= timeline.length(); tick++) {
            timeline.run(tick);
        }

        assertThat(body.type).isEqualTo("Player");
        assertThat(owl.skulls).containsValue(DuskFinalKill.OWL);
        assertThat(stage.actors).hasSize(2).allSatisfy(actor -> assertThat(actor.removed()).isTrue());
        assertThat(stage.sounds).contains("mob.enderdragon.wings", "mob.bat.takeoff", "random.fizz");
        assertThat(stage.particles).containsKeys("REDSTONE", "EXPLOSION_NORMAL", "CLOUD");
    }
}
