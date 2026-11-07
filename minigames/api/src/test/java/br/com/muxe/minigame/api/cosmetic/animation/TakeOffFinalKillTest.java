package br.com.muxe.minigame.api.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SignedSkin;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.core.bukkit.item.SkullTexture;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.api.Test;

class TakeOffFinalKillTest {

    private final Vec victim = new Vec(18.5, 15, 8.5);

    @Test
    void theBodyShootsSixBlocksAwayFromTheKillerAndTwoAndAHalfUpEveryTick() {
        Vec away = new Vec(-1, 0, 0);

        Vec step = TakeOffFinalKill.flight(away, 1);
        Vec last = TakeOffFinalKill.flight(away, TakeOffFinalKill.FLIGHT + 1);

        assertThat(step.x()).isCloseTo(-6, offset(1e-9));
        assertThat(step.y()).isCloseTo(2.5, offset(1e-9));
        assertThat(last.x()).isCloseTo(-120, offset(1e-9));
        assertThat(last.y()).isCloseTo(50, offset(1e-9));
    }

    @Test
    void theVictimsBodyWearsTheirSkinLeavesSparksAndIsGoneAfterTheBlast() {
        RecordingStage stage = new RecordingStage(block -> false);
        Timeline timeline = new Timeline();

        new TakeOffFinalKill().script(new Scene(victim, new Vec(-1, 0, 0), Optional.of("victim-skin")), stage, timeline, new Random(1));
        RecordingStage.FakeActor body = stage.actors.getFirst();
        for (int tick = 0; tick <= timeline.length(); tick++) {
            timeline.run(tick);
        }

        assertThat(stage.skins).containsExactly(new SignedSkin(SkullTexture.value("victim-skin"), null));
        assertThat(body.type).isEqualTo("Player");
        assertThat(body.yaw).isCloseTo(-90, offset(1e-3f));
        assertThat(body.position.y()).isGreaterThan(victim.y() + 40);
        assertThat(stage.particles).containsEntry("FIREWORKS_SPARK", TakeOffFinalKill.FLIGHT + 1);
        assertThat(stage.sounds).containsExactly("fireworks.launch", "fireworks.blast");
        assertThat(stage.actors).allSatisfy(actor -> assertThat(actor.removed()).isTrue());
    }

    @Test
    void withoutTheVictimsSkinAnotherBodyTakesOff() {
        RecordingStage stage = new RecordingStage(block -> false);
        Timeline timeline = new Timeline();

        new TakeOffFinalKill().script(Scene.at(victim, new Vec(0, 0, 1)), stage, timeline, new Random(2));

        assertThat(stage.skins).isEmpty();
        assertThat(stage.actors).extracting(actor -> actor.type).containsExactly("Player");
    }
}
