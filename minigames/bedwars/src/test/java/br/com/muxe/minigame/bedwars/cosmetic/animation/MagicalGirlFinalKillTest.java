package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class MagicalGirlFinalKillTest {

    private final Vec victim = new Vec(10.5, 27, -3.5);
    private final Scene facingSouth = Scene.at(victim, new Vec(0, 0, 1));

    @Test
    void eachRainbowBandIsAQuarterArcFromTheVictimToTheKillersLeft() {
        Rainbow rainbow = MagicalGirlFinalKill.rainbow(facingSouth);
        List<Vec> red = rainbow.band(Rainbow.BANDS.getFirst(), 1);
        List<Vec> violet = rainbow.band(Rainbow.BANDS.getLast(), 1);

        assertThat(red).hasSize(37);
        assertThat(violet).hasSize(27);
        assertThat(red.getFirst().x()).isCloseTo(victim.x() - 0.6, offset(1e-9));
        assertThat(red.getFirst().y()).isCloseTo(victim.y() + 1, offset(1e-9));
        assertThat(violet.getFirst().x()).isCloseTo(victim.x() + 0.6, offset(1e-9));
        assertThat(red.getLast().y()).isCloseTo(victim.y() + 5.6, offset(0.01));
        assertThat(red.getLast().x()).isCloseTo(victim.x() + 3.97, offset(0.05));
        assertThat(red).allSatisfy(point -> assertThat(point.z()).isEqualTo(victim.z()));
    }

    @Test
    void theRainbowGrowsUpwardsOverTwelveTicks() {
        Rainbow rainbow = MagicalGirlFinalKill.rainbow(facingSouth);

        assertThat(rainbow.band(Rainbow.BANDS.getFirst(), MagicalGirlFinalKill.growth(0))).hasSize(6);
        assertThat(MagicalGirlFinalKill.growth(12)).isEqualTo(1);
    }

    @Test
    void theRibbonSpreadsAcrossTheFlightPath() {
        Vec heading = new Vec(1, 0, 0);

        assertThat(MagicalGirlFinalKill.ribbonSide(new Vec(0, 1, 0), heading).distance(new Vec(-1, 0, 0))).isCloseTo(0, offset(1e-9));
        assertThat(MagicalGirlFinalKill.ribbonSide(new Vec(1, 0, 0), heading).distance(new Vec(0, 1, 0))).isCloseTo(0, offset(1e-9));
    }

    @Test
    void aStarFallingFasterThanABlockPerTickStillRestsOnTopOfTheFloor() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);

        Vec rest = MagicalGirlFinalKill.landing(stage, new Vec(3.2, 27.3, 1.5), new Vec(3.3, 26, 1.5));

        assertThat(rest).isEqualTo(new Vec(3.3, 27, 1.5));
    }

    @Test
    void tenStarsShootOutLeavingGoldenTrailsAndLandAroundTheVictim() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);
        Timeline timeline = new Timeline();

        new MagicalGirlFinalKill().script(facingSouth, stage, timeline, new Random(5));
        for (int tick = 0; tick < timeline.length(); tick++) {
            timeline.run(tick);
        }

        assertThat(stage.actors).hasSize(MagicalGirlFinalKill.STARS)
            .allSatisfy(star -> assertThat(star.skulls).containsEntry(Slot.HELMET, MagicalGirlFinalKill.STAR))
            .allSatisfy(star -> assertThat(star.position.y()).isCloseTo(27 - MagicalGirlFinalKill.HEAD_ABOVE_BASE, offset(1e-9)))
            .allSatisfy(star -> assertThat(Math.hypot(star.position.x() - victim.x(), star.position.z() - victim.z())).isBetween(2.0, 3.0));
        assertThat(stage.particles).containsKeys("REDSTONE", "FIREWORKS_SPARK");
        assertThat(stage.sounds).filteredOn("random.orb"::equals).hasSize(MagicalGirlFinalKill.STARS);
        timeline.run(timeline.length());
        assertThat(stage.actors).allSatisfy(actor -> assertThat(actor.removed()).isTrue());
    }
}
