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

class BountyHunterFinalKillTest {

    private final Vec victim = new Vec(8.5, 33, -5.5);

    @Test
    void aRewardHopsAboutTwoAndAHalfBlocksHighAndLandsAboutOneBlockAway() {
        double highest = 0;
        for (int tick = 0; tick <= BountyHunterFinalKill.LANDING; tick++) {
            highest = Math.max(highest, BountyHunterFinalKill.position(0, tick).y());
        }
        Vec landed = BountyHunterFinalKill.position(0, BountyHunterFinalKill.LANDING);

        assertThat(highest).isCloseTo(2.375, offset(1e-9));
        assertThat(BountyHunterFinalKill.position(0, BountyHunterFinalKill.LANDING - 1).y()).isPositive();
        assertThat(landed.y()).isZero();
        assertThat(landed.x()).isBetween(1.1, 1.3);
        assertThat(BountyHunterFinalKill.position(0, BountyHunterFinalKill.END)).isEqualTo(landed);
    }

    @Test
    void sixRewardsAlternateBetweenTwoHeadsAndSpreadEvenlyAroundTheVictim() {
        RecordingStage stage = new RecordingStage(block -> false);
        Timeline timeline = new Timeline();

        new BountyHunterFinalKill().script(Scene.at(victim, new Vec(0, 0, 1)), stage, timeline, new Random(4));
        for (int tick = 0; tick <= timeline.length(); tick++) {
            timeline.run(tick);
        }

        List<RecordingStage.FakeActor> heads = stage.actors.stream().filter(actor -> actor.type.equals("ArmorStand")).toList();
        assertThat(heads).hasSize(BountyHunterFinalKill.HEADS);
        assertThat(heads).extracting(head -> head.skulls.get(Slot.HELMET)).containsExactly(
            BountyHunterFinalKill.REWARDS.get(0), BountyHunterFinalKill.REWARDS.get(1),
            BountyHunterFinalKill.REWARDS.get(0), BountyHunterFinalKill.REWARDS.get(1),
            BountyHunterFinalKill.REWARDS.get(0), BountyHunterFinalKill.REWARDS.get(1));
        Vec sum = heads.stream().map(head -> head.position.subtract(victim)).reduce(Vec.ZERO, Vec::add);
        assertThat(sum.x()).isCloseTo(0, offset(1e-6));
        assertThat(sum.z()).isCloseTo(0, offset(1e-6));
        assertThat(heads).allSatisfy(head -> assertThat(head.position.y()).isCloseTo(victim.y() - BountyHunterFinalKill.BASE_BELOW, offset(1e-9)));
        assertThat(stage.sounds).containsExactlyInAnyOrder("fireworks.launch", "random.orb", "random.orb");
        assertThat(stage.actors).allSatisfy(actor -> assertThat(actor.removed()).isTrue());
    }
}
