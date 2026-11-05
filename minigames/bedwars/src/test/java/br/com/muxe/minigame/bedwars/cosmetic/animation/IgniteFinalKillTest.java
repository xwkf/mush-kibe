package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.Random;
import org.junit.jupiter.api.Test;

class IgniteFinalKillTest {

    private final Vec victim = new Vec(10.5, 27, -3.5);

    @Test
    void theFlameIsDrawnFromTheBottomRowUp() {
        Vec left = new Vec(1, 0, 0);

        assertThat(IgniteFinalKill.pixels(victim, left, 0)).hasSize(3)
            .allSatisfy(pixel -> assertThat(pixel.position().y()).isCloseTo(victim.y() + IgniteFinalKill.PIXEL, offset(1e-9)));
        assertThat(IgniteFinalKill.pixels(victim, left, 11)).hasSize(53);
        assertThat(IgniteFinalKill.pixels(victim, left, 30)).hasSize(53);
    }

    @Test
    void theFlameStandsCentredOnTheVictimAcrossTheKillersView() {
        Vec left = new Vec(1, 0, 0);

        assertThat(IgniteFinalKill.pixels(victim, left, 0)).extracting(pixel -> pixel.position().x())
            .containsExactly(victim.x() - IgniteFinalKill.PIXEL, victim.x(), victim.x() + IgniteFinalKill.PIXEL);
        assertThat(IgniteFinalKill.pixels(victim, left, 20)).allSatisfy(pixel -> assertThat(pixel.position().z()).isEqualTo(victim.z()));
    }

    @Test
    void aSkeletonSinksIntoTheGroundWhileItRattlesLowerAndLower() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);
        Timeline timeline = new Timeline();

        new IgniteFinalKill().script(Scene.at(victim, new Vec(0, 0, 1)), stage, timeline, new Random(6));
        for (int tick = 0; tick < 10; tick++) {
            timeline.run(tick);
        }

        assertThat(stage.actors).singleElement().satisfies(skeleton -> {
            assertThat(skeleton.type).isEqualTo("Skeleton");
            assertThat(skeleton.position.y()).isCloseTo(24.5, offset(1e-9));
        });
        for (int tick = 10; tick <= timeline.length(); tick++) {
            timeline.run(tick);
        }
        assertThat(stage.sounds).filteredOn("mob.skeleton.death"::equals).hasSize(IgniteFinalKill.RATTLES);
        assertThat(stage.pitches.get(stage.sounds.lastIndexOf("mob.skeleton.death"))).isCloseTo(0.67f, offset(0.01f));
        assertThat(stage.sounds).startsWith("ambient.weather.thunder", "random.explode").endsWith("mob.ghast.fireball");
        assertThat(stage.particles).containsKeys("FLAME", "SMOKE_NORMAL", "REDSTONE");
        assertThat(stage.actors).allSatisfy(actor -> assertThat(actor.removed()).isTrue());
    }
}
