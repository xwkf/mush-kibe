package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SignedSkin;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.bedwars.cosmetic.animation.RecordingStage.FakeActor;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.api.Test;

class WrappedFinalKillTest {

    private static final Vec VICTIM = new Vec(8.5, 33, -5.5);

    @Test
    void theBodySinksTwoBlocksIntoTheGiftInTenTicks() {
        assertThat(WrappedFinalKill.sunk(0)).isZero();
        assertThat(WrappedFinalKill.sunk(WrappedFinalKill.SINK.length)).isCloseTo(2, offset(1e-9));
        assertThat(WrappedFinalKill.sunk(100)).isCloseTo(2, offset(1e-9));
    }

    @Test
    void theGiftClosesOnceTheVictimIsInsideAndPopsAtTheEnd() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 33);
        Timeline timeline = new Timeline();
        new WrappedFinalKill().script(Scene.at(VICTIM, new Vec(-1, 0, 1)).withBody(Optional.of(new SignedSkin("v", "s"))), stage, timeline, new Random(2));
        FakeActor body = stage.actors.stream().filter(actor -> actor.type.equals("Player")).findFirst().orElseThrow();
        FakeActor gift = stage.actors.stream().filter(actor -> actor.type.equals("ArmorStand")).findFirst().orElseThrow();

        for (int tick = 0; tick < WrappedFinalKill.CLOSE; tick++) {
            timeline.run(tick);
        }
        assertThat(body.position.y()).isCloseTo(VICTIM.y() - 2, offset(1e-9));
        assertThat(gift.skulls).containsEntry(Slot.HELMET, WrappedFinalKill.OPEN_GIFT);
        timeline.run(WrappedFinalKill.CLOSE);
        assertThat(gift.skulls).containsEntry(Slot.HELMET, WrappedFinalKill.CLOSED_GIFT);
        assertThat(body.removed()).isTrue();
        for (int tick = WrappedFinalKill.CLOSE + 1; tick <= timeline.length(); tick++) {
            timeline.run(tick);
        }

        assertThat(stage.skins).hasSize(1);
        assertThat(stage.sounds).contains("fireworks.launch", "mob.horse.armor", "random.pop");
        assertThat(stage.actors).extracting(actor -> actor.type).contains("Firework");
        assertThat(stage.actors).allSatisfy(actor -> assertThat(actor.removed()).isTrue());
    }
}
