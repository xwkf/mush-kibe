package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.api.Test;

class SkeletalFinalKillTest {

    private static final Vec VICTIM = new Vec(8.5, 27, -1.5);

    @Test
    void theHeadSpinsOnceWhileSinkingFromHeadHeightToTheGround() {
        assertThat(SkeletalFinalKill.standAt(VICTIM, 0).y()).isCloseTo(VICTIM.y() + 1.0625, offset(1e-9));
        assertThat(SkeletalFinalKill.standAt(VICTIM, SkeletalFinalKill.SINKING).y()).isCloseTo(VICTIM.y() - 1.4375, offset(1e-9));
        assertThat(SkeletalFinalKill.spin(10, SkeletalFinalKill.SINKING)).isCloseTo(370f, offset(1e-3f));
    }

    @Test
    void bonesFallOffWithRisingCriesThenTheBodyBursts() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);
        Timeline timeline = new Timeline();
        new SkeletalFinalKill().script(new Scene(VICTIM, new Vec(1, 0, 0), Optional.of("victim")), stage, timeline, new Random(5));

        for (int tick = 0; tick <= timeline.length(); tick++) {
            timeline.run(tick);
        }

        int beats = SkeletalFinalKill.SINKING / SkeletalFinalKill.BEAT_EVERY + 1;
        assertThat(stage.sounds.stream().filter("mob.skeleton.hurt"::equals)).hasSize(beats);
        assertThat(stage.pitches.get(stage.sounds.lastIndexOf("mob.skeleton.hurt"))).isGreaterThan(stage.pitches.get(stage.sounds.indexOf("mob.skeleton.hurt")));
        assertThat(stage.actors.stream().filter(actor -> actor.type.startsWith("Item:"))).hasSize(beats + SkeletalFinalKill.BURST_PIECES);
        assertThat(stage.actors.getFirst().skulls).containsEntry(Slot.HELMET, "victim");
        assertThat(stage.particles).containsKeys("BLOCK_CRACK", "EXPLOSION_HUGE");
        assertThat(stage.actors).allSatisfy(actor -> assertThat(actor.removed()).isTrue());
    }

    @Test
    void theBurstThrowsBonesUpAndOutwards() {
        Random random = new Random(9);
        for (int piece = 0; piece < 20; piece++) {
            Vec push = SkeletalFinalKill.burst(piece * 18, random);
            assertThat(Math.hypot(push.x(), push.z())).isBetween(SkeletalFinalKill.MIN_PUSH, SkeletalFinalKill.MAX_PUSH);
            assertThat(push.y()).isBetween(SkeletalFinalKill.MIN_LIFT, SkeletalFinalKill.MAX_LIFT);
        }
    }
}
