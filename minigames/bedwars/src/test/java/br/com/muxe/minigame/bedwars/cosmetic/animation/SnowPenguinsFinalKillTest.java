package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.Random;
import org.junit.jupiter.api.Test;

class SnowPenguinsFinalKillTest {

    private final Vec victim = new Vec(10.5, 27, -3.5);

    @Test
    void snowballsLeaveAWideningSpiralUpToSevenBlocks() {
        Vec first = SnowPenguinsFinalKill.spiral(victim, 0, 0);
        Vec last = SnowPenguinsFinalKill.spiral(victim, 0, 59);

        assertThat(first.y() - victim.y()).isCloseTo(1.1, offset(1e-9));
        assertThat(Math.hypot(first.x() - victim.x(), first.z() - victim.z())).isCloseTo(0, offset(1e-9));
        assertThat(last.y() - victim.y()).isCloseTo(7, offset(1e-9));
        assertThat(Math.hypot(last.x() - victim.x(), last.z() - victim.z())).isCloseTo(0.885, offset(1e-9));
        assertThat(SnowPenguinsFinalKill.angle(10, 2) - SnowPenguinsFinalKill.angle(10, 1)).isCloseTo(Math.toDegrees(0.5), offset(1e-9));
    }

    @Test
    void penguinsAndFishPopOutWhileSnowballsShootUpThenAllClearAway() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);
        Timeline timeline = new Timeline();
        new SnowPenguinsFinalKill().script(Scene.at(victim, new Vec(0, 0, 1)), stage, timeline, new Random(8));

        for (int tick = 0; tick < SnowPenguinsFinalKill.SNOWBALLS_GONE; tick++) {
            timeline.run(tick);
        }

        assertThat(stage.actors.stream().filter(actor -> actor.type.equals("Item:RAW_FISH"))).hasSize(11);
        assertThat(stage.actors.stream().filter(actor -> actor.type.startsWith("Skull:")))
            .hasSize(15)
            .allSatisfy(actor -> assertThat(SnowPenguinsFinalKill.PENGUINS).contains(actor.type.substring("Skull:".length())));
        assertThat(stage.actors.stream().filter(actor -> actor.type.equals("Snowball"))).hasSize(60);
        assertThat(stage.sounds.stream().filter(sound -> sound.equals("mob.chicken.plop"))).hasSize(60);
        assertThat(stage.sounds).contains("step.snow", "mob.chicken.say");
        assertThat(stage.alive()).isEqualTo(86);

        timeline.run(SnowPenguinsFinalKill.SNOWBALLS_GONE);
        assertThat(stage.alive()).isEqualTo(26);
        timeline.run(SnowPenguinsFinalKill.TOSSED_GONE);

        assertThat(stage.alive()).isZero();
        assertThat(timeline.length()).isEqualTo(SnowPenguinsFinalKill.TOSSED_GONE);
    }
}
