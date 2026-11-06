package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.cosmetic.animation.Area;
import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockChange;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockType;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.bedwars.cosmetic.animation.RecordingStage.FakeActor;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Random;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class FrozenVictoryTest {

    private static final int FLOOR = 27;
    private static final Area MAP = new Area(-60, -60, 59, 59);

    private final UUID winner = UUID.randomUUID();

    private static RecordingStage island() {
        RecordingStage stage = new RecordingStage(block -> block[1] < FLOOR && block[1] >= FLOOR - 3);
        stage.types = block -> {
            if (block[1] == FLOOR && block[0] == 5 && block[2] == 5) {
                return new BlockType(Material.RED_ROSE, 0);
            }
            if (block[1] == FLOOR - 1 && block[0] == -5 && block[2] == -5) {
                return new BlockType(Material.GLASS, 0);
            }
            return new BlockType(block[1] < FLOOR && block[1] >= FLOOR - 3 ? Material.WOOL : Material.AIR, 0);
        };
        return stage;
    }

    private Timeline script(RecordingStage stage, Area area, long seed) {
        Timeline timeline = new Timeline();
        new FrozenVictory().script(new Scene(new Vec(0.5, FLOOR, 0.5), new Vec(0, 0, 1), Optional.empty(), List.of(winner), area), stage, timeline, new Random(seed));
        return timeline;
    }

    private static void run(Timeline timeline, int from, int until) {
        for (int tick = from; tick <= until; tick++) {
            timeline.run(tick);
        }
    }

    private static List<FakeActor> snowmen(RecordingStage stage) {
        return stage.actors.stream().filter(actor -> actor.type.equals("SnowMan")).toList();
    }

    @Test
    void itIsTheCongeladoEffectAndLastsTwoHundredTicks() {
        Timeline timeline = script(island(), MAP, 1);

        assertThat(new FrozenVictory().id()).isEqualTo("congelado");
        assertThat(timeline.length()).isEqualTo(200);
    }

    @Test
    void theWholeMapFreezesFromTheWinnerOutwards() {
        RecordingStage stage = island();
        Timeline timeline = script(stage, MAP, 1);

        timeline.run(0);
        assertThat(stage.bulk).hasSize(FrostRules.MIN_COLUMNS_PER_TICK * 3 + 1);
        assertThat(stage.bulk).allSatisfy(change -> assertThat(Math.abs(change.x()) + Math.abs(change.z())).isLessThanOrEqualTo(12));

        run(timeline, 1, 199);

        Set<String> frozen = new HashSet<>();
        for (BlockChange change : stage.bulk) {
            frozen.add(change.x() + "," + change.y() + "," + change.z());
        }
        assertThat(frozen).hasSize(120 * 120 * 3 + 1);
        assertThat(stage.bulk).extracting(BlockChange::material).containsOnly(Material.SNOW_BLOCK, Material.PACKED_ICE, Material.ICE, Material.AIR);
        assertThat(stage.bulk).filteredOn(change -> change.x() == 5 && change.y() == FLOOR && change.z() == 5).extracting(BlockChange::material).containsExactly(Material.AIR);
        assertThat(stage.bulk).filteredOn(change -> change.x() == -5 && change.y() == FLOOR - 1 && change.z() == -5).extracting(BlockChange::material).containsExactly(Material.ICE);
        long packed = stage.bulk.stream().filter(change -> change.material() == Material.PACKED_ICE).count();
        assertThat((double) packed / stage.bulk.size()).isBetween(0.05, 0.3);
    }

    @Test
    void onTheRecordedMapTheRingsReachThirtySixBlocksAfterTenTicksAndTheCornersAfterNinetyEight() {
        RecordingStage stage = island();
        Timeline timeline = script(stage, new Area(-78, -78, 78, 78), 4);

        run(timeline, 0, 9);
        assertThat(stage.bulk.stream().mapToInt(change -> Math.abs(change.x()) + Math.abs(change.z())).max().orElseThrow()).isBetween(34, 37);

        run(timeline, 10, 96);
        assertThat(stage.bulk.stream().mapToInt(change -> Math.abs(change.x()) + Math.abs(change.z())).max().orElseThrow()).isLessThan(156);
        timeline.run(97);
        assertThat(stage.bulk).hasSize(157 * 157 * 3 + 1);
    }

    @Test
    void snowmenWakeUpOnTheFrozenGroundWanderSlowlyAndLeaveAtTheEnd() {
        RecordingStage stage = island();
        Timeline timeline = script(stage, MAP, 9);

        run(timeline, 0, 199);

        List<FakeActor> snowmen = snowmen(stage);
        assertThat(snowmen).hasSize(FrozenVictory.MAX_SNOWMEN);
        assertThat(snowmen).allSatisfy(snowman -> {
            assertThat(snowman.position.y()).isEqualTo(FLOOR);
            assertThat(Math.abs(snowman.position.x()) + Math.abs(snowman.position.z())).isLessThan(30);
            assertThat(snowman.removed).isFalse();
        });
        assertThat(stage.sounds).anyMatch(sound -> sound.equals("step.snow") || sound.equals("step.stone"));

        timeline.run(200);
        assertThat(stage.alive()).isZero();
    }

    @Test
    void aSnowmanStrollsAtAFewCentimetresATickAndRestsBetweenWalks() {
        RecordingStage stage = island();
        FakeActor body = (FakeActor) stage.mob("SnowMan", new Vec(0.5, FLOOR, 0.5), 0);
        SnowmanStroll stroll = new SnowmanStroll(body, new Vec(0.5, FLOOR, 0.5), 0, (x, y, z) -> FrostRules.SNOW);
        Random random = new Random(5);
        Vec before = stroll.position();

        stroll.step(stage, random);

        assertThat(stroll.position().distance(before)).isLessThanOrEqualTo(SnowmanStroll.SPEED + 1e-9);
        for (int tick = 0; tick < 400; tick++) {
            stroll.step(stage, random);
            assertThat(stroll.position().y()).isEqualTo(FLOOR);
        }
        assertThat(stroll.position().distance(before)).isGreaterThan(0);
    }

    @Test
    void snowmenCrunchOnSnowAndClickOnIce() {
        assertThat(SnowmanStroll.step(FrostRules.SNOW)).isEqualTo("step.snow");
        assertThat(SnowmanStroll.step(FrostRules.PACKED_ICE)).isEqualTo("step.stone");
        assertThat(SnowmanStroll.step(FrostRules.ICE)).isEqualTo("step.stone");
    }

    @Test
    void itRoarsOnceAndLaunchesFireworkSoundsEveryTenTicks() {
        RecordingStage stage = island();
        Timeline timeline = script(stage, MAP, 1);

        run(timeline, 0, 200);

        assertThat(stage.sounds.stream().filter("mob.enderdragon.growl"::equals)).hasSize(1);
        assertThat(stage.sounds.stream().filter("fireworks.launch"::equals)).hasSize(20);
    }

    @Test
    void thePreviewFreezesItsSmallAreaInFiveTicks() {
        RecordingStage stage = island();
        Timeline timeline = script(stage, Area.around(new Vec(0.5, FLOOR, 0.5), Scene.REACH), 2);

        run(timeline, 0, 2);

        assertThat(stage.bulk).hasSize(3 * FrostRules.MIN_COLUMNS_PER_TICK * 3 + 1);
        run(timeline, 3, 200);
        assertThat(stage.bulk).hasSize(33 * 33 * 3 + 1);
    }
}
