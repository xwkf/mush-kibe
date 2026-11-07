package br.com.muxe.minigame.api.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.Random;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class MagicTrickFinalKillTest {

    private final Vec victim = new Vec(10.5, 27, -3.5);

    @Test
    void aWhiteRabbitComesOutOfATopHatAndHopsAwayFromTheKiller() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);
        Timeline timeline = new Timeline();
        new MagicTrickFinalKill().script(Scene.at(victim, new Vec(1, 0, 0)), stage, timeline, new Random(4));

        for (int tick = 0; tick < MagicTrickFinalKill.END; tick++) {
            timeline.run(tick);
        }

        RecordingStage.FakeActor hat = stage.actors.get(0);
        RecordingStage.FakeActor brim = stage.actors.get(1);
        RecordingStage.FakeActor rabbit = stage.actors.get(2);
        assertThat(hat.skulls).containsEntry(Slot.HELMET, MagicTrickFinalKill.TOP_HAT);
        assertThat(hat.style.small()).isTrue();
        assertThat(brim.equipment).containsEntry(Slot.HELMET, Material.CARPET);
        assertThat(rabbit.type).isEqualTo("Rabbit");
        assertThat(rabbit.watched).containsEntry(MagicTrickFinalKill.RABBIT_TYPE, MagicTrickFinalKill.WHITE);
        assertThat(rabbit.position.x() - victim.x()).isBetween(19.0, 24.0);
        assertThat(rabbit.position.z()).isCloseTo(victim.z(), offset(1e-9));
        assertThat(stage.sounds).containsExactly("dig.cloth", "mob.rabbit.hurt", "mob.rabbit.hurt", "mob.rabbit.hurt");
        assertThat(stage.particles).containsKey("CLOUD");

        timeline.run(MagicTrickFinalKill.END);

        assertThat(stage.alive()).isZero();
    }

    @Test
    void eachHopLandsAboutFiveBlocksAheadAfterThirteenTicks() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);
        Body hop = Body.living(victim, MagicTrickFinalKill.launch(new Vec(0, 0, 1)));
        int ticks = 0;
        double highest = 0;
        while (!hop.landed() && ticks < 40) {
            hop.step(stage);
            highest = Math.max(highest, hop.position().y() - victim.y());
            ticks++;
        }

        assertThat(ticks).isLessThanOrEqualTo(MagicTrickFinalKill.HOP_EVERY);
        assertThat(hop.position().z() - victim.z()).isBetween(4.5, 5.5);
        assertThat(highest).isBetween(1.5, 1.7);
    }
}
