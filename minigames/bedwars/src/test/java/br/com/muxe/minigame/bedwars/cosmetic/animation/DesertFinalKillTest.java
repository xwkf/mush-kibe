package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockChange;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.bedwars.cosmetic.animation.DesertSun.Dot;
import br.com.muxe.minigame.bedwars.cosmetic.animation.RecordingStage.FakeActor;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class DesertFinalKillTest {

    private static final Vec VICTIM = new Vec(18.5, 15, 8.5);

    @Test
    void theQuicksandCoversTheGroundUnderTheVictimAndAFewPatchesAround() {
        List<BlockChange> plan = Quicksand.plan(VICTIM, new Random(2));

        assertThat(plan).hasSize(9 + Quicksand.EDGE_PATCHES).allSatisfy(change -> {
            assertThat(change.y()).isEqualTo(14);
            assertThat(change.material()).isIn(Material.SAND, Material.SANDSTONE);
        });
        assertThat(plan.stream().filter(change -> change.x() == 18 && change.z() == 8)).hasSize(1);
        assertThat(plan.stream().map(change -> change.x() + "," + change.z()).distinct()).hasSize(plan.size());
    }

    @Test
    void theQuicksandNeverFillsHoles() {
        RecordingStage stage = new RecordingStage(block -> false);

        Quicksand.spread(stage, VICTIM, new Random(2));

        assertThat(stage.blocks).isEmpty();
    }

    @Test
    void theSunWearsDarkGlassesAndSmilesWhileItsRaysTurn() {
        assertThat(DesertSun.FACE).anyMatch(dot -> dot.tint().equals(DesertSun.GLASSES) && dot.v() > 0)
            .anyMatch(dot -> dot.tint().equals(DesertSun.SMILE) && dot.v() < 0)
            .anyMatch(dot -> dot.tint().equals(DesertSun.CHEEK))
            .allSatisfy(dot -> assertThat(Math.hypot(dot.u(), dot.v())).isLessThanOrEqualTo(DesertSun.RADIUS + 1e-9));
        Dot first = DesertSun.rays(0).getFirst();
        Dot turned = DesertSun.rays(DesertFinalKill.turn(10)).getFirst();
        assertThat(first.v()).isCloseTo(0, offset(1e-9));
        assertThat(turned.v()).isNegative();
        assertThat(DesertSun.rays(0)).allSatisfy(dot -> assertThat(Math.hypot(dot.u(), dot.v())).isBetween(DesertSun.RAY_FROM - 1e-9, DesertSun.LONG_RAY + 1e-9));
    }

    @Test
    void theVictimSinksIntoTheSandBeforeBeingSwallowed() {
        assertThat(DesertFinalKill.sunk(DesertFinalKill.SINK_FROM)).isZero();
        assertThat(DesertFinalKill.sunk(DesertFinalKill.SINK_UNTIL)).isCloseTo(DesertFinalKill.SINK_DEPTH, offset(1e-9));
        assertThat(DesertFinalKill.wobble(-80, 3)).isCloseTo(-87f, offset(1e-3f));
    }

    @Test
    void sunDropsFallToTheGroundAndStopOnceTheSunIsGone() {
        RecordingStage stage = new RecordingStage(block -> false);
        SunDrops drops = new SunDrops(VICTIM.add(0, 3.6, 0), new Vec(1, 0, 0), VICTIM.y());
        Random random = new Random(1);

        drops.tick(stage, random, true);
        assertThat(drops.falling()).isEqualTo(SunDrops.PER_TICK);
        for (int tick = 0; tick < SunDrops.fallTicks(3.6); tick++) {
            drops.tick(stage, random, false);
        }

        assertThat(drops.falling()).isZero();
    }

    @Test
    void theWholeScenePlaysAndCleansUp() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 15);
        Timeline timeline = new Timeline();
        new DesertFinalKill().script(new Scene(VICTIM, new Vec(-1, 0, 0), Optional.of("victim")), stage, timeline, new Random(6));
        FakeActor[] victim = new FakeActor[1];

        for (int tick = 0; tick <= timeline.length(); tick++) {
            timeline.run(tick);
            if (tick == DesertFinalKill.SWALLOW - 1) {
                victim[0] = stage.actors.getFirst();
                assertThat(victim[0].position.y()).isCloseTo(VICTIM.y() - DesertFinalKill.SINK_DEPTH, offset(1e-9));
                assertThat(victim[0].removed()).isFalse();
            }
        }

        assertThat(victim[0].style).isEqualTo(DesertFinalKill.VICTIM);
        assertThat(victim[0].pose).isEqualTo(DesertFinalKill.REACHING_UP);
        assertThat(stage.blocks).hasSize(9 + Quicksand.EDGE_PATCHES);
        assertThat(stage.actors.stream().filter(actor -> SandCactus.HEAD.equals(actor.skulls.get(Slot.HELMET))))
            .hasSize(DesertFinalKill.CACTI.size());
        assertThat(stage.particles).containsKeys("REDSTONE", "FIREWORKS_SPARK", "BLOCK_DUST", "EXPLOSION_NORMAL");
        assertThat(stage.sounds).contains("random.levelup", "random.burp", "mob.chicken.plop");
        assertThat(stage.actors).allSatisfy(actor -> assertThat(actor.removed()).isTrue());
    }
}
