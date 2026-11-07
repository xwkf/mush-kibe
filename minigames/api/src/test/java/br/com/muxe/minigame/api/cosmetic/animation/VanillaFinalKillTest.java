package br.com.muxe.minigame.api.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SignedSkin;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.api.Test;

class VanillaFinalKillTest {

    private final Vec victim = new Vec(6.5, 11, 17.5);
    private final Vec away = new Vec(0, 0, -1);

    @Test
    void theBodyIsKnockedAboutTwoBlocksAwayFromTheKillerAndLandsAfterSixTicks() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 11);

        var slide = VanillaFinalKill.slide(stage, victim, away);

        assertThat(slide).hasSize(VanillaFinalKill.SLIDE_TICKS);
        assertThat(slide.getFirst().z() - victim.z()).isCloseTo(-0.41, offset(1e-9));
        assertThat(slide.getFirst().y() - victim.y()).isCloseTo(0.2, offset(1e-9));
        assertThat(slide.getLast().y()).isCloseTo(victim.y(), offset(1e-9));
        assertThat(slide.getLast().z() - victim.z()).isBetween(-2.1, -1.8);
        assertThat(slide).allSatisfy(point -> assertThat(point.x()).isCloseTo(victim.x(), offset(1e-9)));
    }

    @Test
    void theVictimsCopyFacesTheKillerDiesAndIsGoneAtTheRecordedTick() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 11);
        Timeline timeline = new Timeline();
        SignedSkin skin = new SignedSkin("value", "signature");
        Scene scene = new Scene(victim, away, Optional.of("value")).withBody(Optional.of(skin));

        new VanillaFinalKill().script(scene, stage, timeline, new Random(1));
        RecordingStage.FakeActor body = stage.actors.getFirst();

        assertThat(stage.actors).hasSize(1);
        assertThat(body.type).isEqualTo("Player");
        assertThat(stage.skins).containsExactly(skin);
        assertThat(body.watched).containsEntry(VanillaFinalKill.HEALTH, 0f);

        for (int tick = 0; tick < VanillaFinalKill.END; tick++) {
            timeline.run(tick);
        }

        assertThat(body.removed()).isFalse();
        assertThat(body.position.z()).isLessThan(victim.z() - 1.8);
        assertThat(body.yaw).isCloseTo(0, offset(1e-3f));
        assertThat(stage.sounds).isEmpty();
        assertThat(stage.particles).isEmpty();

        timeline.run(VanillaFinalKill.END);

        assertThat(body.removed()).isTrue();
        assertThat(timeline.length()).isEqualTo(VanillaFinalKill.END);
    }

    @Test
    void overTheVoidTheBodyKeepsFallingUntilItIsRemoved() {
        RecordingStage stage = new RecordingStage(block -> false);
        Timeline timeline = new Timeline();

        new VanillaFinalKill().script(Scene.at(victim, away), stage, timeline, new Random(1));
        for (int tick = 0; tick <= timeline.length(); tick++) {
            timeline.run(tick);
        }

        assertThat(VanillaFinalKill.slide(stage, victim, away)).hasSize(VanillaFinalKill.END);
        assertThat(stage.actors).allSatisfy(actor -> assertThat(actor.removed()).isTrue());
    }
}
