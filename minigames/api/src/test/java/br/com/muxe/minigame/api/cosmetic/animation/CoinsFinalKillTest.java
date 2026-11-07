package br.com.muxe.minigame.api.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.Random;
import org.junit.jupiter.api.Test;

class CoinsFinalKillTest {

    private final Vec victim = new Vec(10.5, 27, -3.5);

    @Test
    void coinsSpiralUpThreeBlocksAtHalfABlockFromTheVictim() {
        Vec first = CoinsFinalKill.spiral(victim, 1);
        Vec last = CoinsFinalKill.spiral(victim, CoinsFinalKill.COINS_PER_TICK * CoinsFinalKill.TICKS);

        assertThat(Math.hypot(first.x() - victim.x(), first.z() - victim.z())).isCloseTo(0.5, offset(1e-9));
        assertThat(first.y() - victim.y()).isCloseTo(0.05, offset(1e-9));
        assertThat(last.y() - victim.y()).isCloseTo(3, offset(1e-9));
        assertThat(Math.toDegrees(Math.atan2(first.z() - victim.z(), first.x() - victim.x()))).isCloseTo(17.19, offset(0.01));
    }

    @Test
    void eachCoinIsThrownOutwardsFromWhereItAppears() {
        Vec out = CoinsFinalKill.outward(5);
        Vec at = CoinsFinalKill.spiral(Vec.ZERO, 5);

        assertThat(out.length()).isCloseTo(1, offset(1e-9));
        assertThat(out.x() * at.x() + out.z() * at.z()).isCloseTo(0.5, offset(1e-9));
    }

    @Test
    void sixtyCoinsSpillWithAChimeEachTickAndVanishAfterFiveSeconds() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);
        Timeline timeline = new Timeline();
        new CoinsFinalKill().script(Scene.at(victim, new Vec(0, 0, 1)), stage, timeline, new Random(5));

        for (int tick = 0; tick < CoinsFinalKill.END; tick++) {
            timeline.run(tick);
        }

        assertThat(stage.actors).hasSize(60).allSatisfy(actor -> assertThat(actor.type).isEqualTo("Item:DOUBLE_PLANT"));
        assertThat(stage.alive()).isEqualTo(60);
        assertThat(stage.sounds).hasSize(30).containsOnly("random.orb");
        assertThat(stage.particles).containsEntry("REDSTONE", 120);

        timeline.run(CoinsFinalKill.END);

        assertThat(stage.alive()).isZero();
        assertThat(timeline.length()).isEqualTo(CoinsFinalKill.END);
    }
}
