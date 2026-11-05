package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.Random;
import org.junit.jupiter.api.Test;

class CookiesAndMilkFinalKillTest {

    private final Vec victim = new Vec(6.5, 11, 17.5);

    @Test
    void milkAndCookiesAlternateWithARisingNote() {
        assertThat(CookiesAndMilkFinalKill.milk(0)).isTrue();
        assertThat(CookiesAndMilkFinalKill.milk(1)).isFalse();
        assertThat(CookiesAndMilkFinalKill.pitch(0)).isCloseTo(0.55f, offset(0.001f));
        assertThat(CookiesAndMilkFinalKill.pitch(CookiesAndMilkFinalKill.TREATS - 1)).isCloseTo(1.25f, offset(0.001f));
    }

    @Test
    void eachTreatFliesOutLowAndCrumblesTwoBlocksAway() {
        Vec velocity = CookiesAndMilkFinalKill.velocity(90);
        Vec crumbs = CookiesAndMilkFinalKill.crumbs(victim, 0);

        assertThat(velocity.z()).isCloseTo(0.2, offset(1e-9));
        assertThat(velocity.y()).isCloseTo(0.1, offset(1e-9));
        assertThat(crumbs.distance(victim)).isCloseTo(2.15, offset(1e-9));
        assertThat(crumbs.y()).isEqualTo(victim.y());
    }

    @Test
    void fifteenTreatsAreThrownOneATickAndEachLastsTenTicks() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 11);
        Timeline timeline = new Timeline();

        new CookiesAndMilkFinalKill().script(Scene.at(victim, new Vec(0, 0, -1)), stage, timeline, new Random(6));
        for (int tick = 0; tick <= CookiesAndMilkFinalKill.LIFE; tick++) {
            timeline.run(tick);
        }
        assertThat(stage.actors.stream().filter(actor -> actor.type.startsWith("Item:"))).hasSize(11);
        assertThat(stage.actors.stream().filter(actor -> actor.type.startsWith("Item:") && actor.removed())).hasSize(1);
        for (int tick = CookiesAndMilkFinalKill.LIFE + 1; tick <= timeline.length(); tick++) {
            timeline.run(tick);
        }

        assertThat(stage.actors.stream().filter(actor -> actor.type.equals("Item:COOKIE"))).hasSize(7);
        assertThat(stage.actors.stream().filter(actor -> actor.type.equals("Item:MILK_BUCKET"))).hasSize(8);
        assertThat(stage.actors).allSatisfy(actor -> assertThat(actor.removed()).isTrue());
        assertThat(stage.sounds.stream().filter("note.pling"::equals)).hasSize(15);
        assertThat(stage.sounds).contains("fireworks.launch", "mob.zombie.woodbreak");
        assertThat(stage.particles).containsKeys("REDSTONE", "EXPLOSION_HUGE");
    }
}
