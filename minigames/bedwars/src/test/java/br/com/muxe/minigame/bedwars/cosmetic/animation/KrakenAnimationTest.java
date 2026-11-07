package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.BedFrame;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockChange;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.StandStyle;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.api.cosmetic.animation.Kit;
import br.com.muxe.minigame.api.cosmetic.animation.RecordingStage;
import br.com.muxe.minigame.api.cosmetic.animation.SoundPitch;
import br.com.muxe.minigame.api.cosmetic.animation.TracedStage;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class KrakenAnimationTest {

    private static final BedFrame BED = BedFrame.of(23, 60, 52, 23, 53);
    private static final BedFrame SIDEWAYS = BedFrame.of(0, 24, 2, 1, 2);
    private static final Vec BREAKER = new Vec(23.5, 60, 48.5);
    private static final double EPSILON = 1.0E-9;

    private static TracedStage play(BedFrame bed, int last) {
        int floor = (int) bed.foot().y() - 1;
        return new TracedStage(block -> block[1] <= floor).play(new KrakenAnimation(), bed, BREAKER, 3, last);
    }

    private static List<RecordingStage.FakeActor> stands(TracedStage stage) {
        return stage.recording.actors.stream().filter(actor -> actor.type.equals("ArmorStand")).toList();
    }

    private static int recorded(TracedStage.Call sound) {
        return (int) (sound.pitch() * SoundPitch.STEPS);
    }

    private static float volume(TracedStage.Call sound) {
        return (float) sound.args().get(2);
    }

    private static List<KrakenTentacle> tentacles(RecordingStage stage, BedFrame bed) {
        List<KrakenTentacle> tentacles = new ArrayList<>();
        for (KrakenAnimation.Hole hole : KrakenAnimation.HOLES) {
            Vec base = bed.middle().add(bed.forward().multiply(hole.ahead())).add(bed.right().multiply(hole.right()));
            tentacles.add(KrakenTentacle.spawn(stage, base, hole.head() ? bed.head() : bed.foot(), hole.rise(), hole.phase()));
        }
        return tentacles;
    }

    @Test
    void theKrakenIsRegisteredUnderItsCatalogId() {
        assertThat(new KrakenAnimation().id()).isEqualTo("kraken");
        assertThat(new BedAnimations().find("kraken")).containsInstanceOf(KrakenAnimation.class);
    }

    @Test
    void theBrokenBedFloatsOnTheSeaWithAWaterySplash() {
        TracedStage stage = play(BED, 0);

        assertThat(stage.recording.actors).filteredOn(actor -> actor.type.startsWith("FallingBlock:BED_BLOCK")).extracting(actor -> actor.type)
            .containsExactly("FallingBlock:BED_BLOCK:0", "FallingBlock:BED_BLOCK:8");
        assertThat(stage.recording.actors).filteredOn(actor -> actor.type.startsWith("FallingBlock")).allSatisfy(half -> {
            assertThat(half.vehicle).isInstanceOf(RecordingStage.FakeActor.class);
            assertThat(((RecordingStage.FakeActor) half.vehicle).style).isEqualTo(StandStyle.HIDDEN);
        });
        assertThat(stage.sounds("liquid.water")).singleElement().satisfies(water -> {
            assertThat(water.tick()).isZero();
            assertThat(recorded(water)).isEqualTo(37);
            assertThat(volume(water)).isEqualTo(1.5f);
            assertThat(water.at()).isEqualTo(BED.middle());
        });
        assertThat(stage.sounds("random.splash")).singleElement().satisfies(splash -> assertThat(recorded(splash)).isEqualTo(31));
    }

    @Test
    void theHalvesBobInOppositeDirectionsUntilTheTentaclesStrike() {
        for (int tick : new int[] {13, 22, 40}) {
            List<RecordingStage.FakeActor> halves = stands(play(BED, tick)).subList(0, 2);

            assertThat(halves.getFirst().position.y() - BED.foot().y()).isCloseTo(-KrakenBed.bob(tick), offset(EPSILON));
            assertThat(halves.getLast().position.y() - BED.head().y()).isCloseTo(KrakenBed.bob(tick), offset(EPSILON));
            assertThat(halves.getLast().position.x()).isEqualTo(BED.head().x());
        }
        assertThat(KrakenBed.bob(KrakenBed.BOB_LOW + 18)).isCloseTo(-KrakenBed.BOB, offset(EPSILON));
        assertThat(KrakenBed.bob(KrakenBed.BOB_LOW + 27)).isCloseTo(KrakenBed.BOB, offset(EPSILON));
        assertThat(KrakenBed.bob(KrakenAnimation.STRIKE)).isCloseTo(0, offset(EPSILON));
        assertThat(KrakenBed.bob(KrakenAnimation.STRIKE + 10)).isCloseTo(0, offset(EPSILON));
    }

    @Test
    void theSeaSwellsAfterTheBreakAndDrainsAtTheEnd() {
        assertThat(KrakenAnimation.level(0)).isCloseTo(0.05, offset(EPSILON));
        assertThat(KrakenAnimation.level(KrakenAnimation.FILL - 1)).isEqualTo(1);
        assertThat(KrakenAnimation.level(KrakenAnimation.END - KrakenAnimation.DRAIN)).isEqualTo(1);
        assertThat(KrakenAnimation.level(KrakenAnimation.END - 4)).isCloseTo(0.5, offset(EPSILON));
        assertThat(KrakenAnimation.level(KrakenAnimation.END)).isZero();

        TracedStage stage = play(BED, Integer.MAX_VALUE);
        List<TracedStage.Call> swims = stage.sounds("game.neutral.swim");
        assertThat(swims).extracting(TracedStage.Call::tick).startsWith(3, 7, 11).endsWith(111, 115, 115);
        assertThat(volume(swims.getFirst())).isCloseTo(0.16f, offset(1.0E-6f));
        assertThat(volume(swims.get(5))).isEqualTo(0.8f);
        assertThat(swims.subList(0, swims.size() - 1)).allSatisfy(swim -> assertThat(recorded(swim)).isBetween(38, 81));
        assertThat(recorded(swims.getLast())).isEqualTo(31);
        assertThat(volume(swims.getLast())).isEqualTo(1.2f);
        List<TracedStage.Call> laps = stage.sounds("random.splash").stream().filter(splash -> recorded(splash) == 94).toList();
        assertThat(laps).extracting(TracedStage.Call::tick).containsExactly(8, 17, 26, 35, 44, 53, 62, 71, 80, 89, 98, 107, 116);
        assertThat(volume(laps.getFirst())).isCloseTo(0.315f, offset(1.0E-6f));
        assertThat(stage.particles("WATER_WAKE")).hasSize((KrakenAnimation.END - 4) / 3 + 1)
            .allSatisfy(wake -> assertThat(wake.at()).isEqualTo(BED.middle().add(0, KrakenSea.SURFACE, 0)));
    }

    @Test
    void theWaterHazeGrowsAcrossTheBedWhicheverWayItFaces() {
        TracedStage along = play(BED, 40);
        TracedStage across = play(SIDEWAYS, 40);

        assertThat(along.particles("SPELL_MOB").getFirst().count()).isEqualTo(1);
        assertThat(along.particles("SPELL_MOB").getFirst().spread().x()).isCloseTo(0.61, offset(1.0E-9));
        assertThat(along.particles("SPELL_MOB").getLast()).satisfies(haze -> {
            assertThat(haze.count()).isEqualTo(9);
            assertThat(haze.spread().x()).isCloseTo(1.6, offset(EPSILON));
            assertThat(haze.spread().z()).isCloseTo(1.12, offset(EPSILON));
        });
        assertThat(across.particles("SPELL_MOB").getLast().spread().x()).isCloseTo(1.12, offset(EPSILON));
        assertThat(across.particles("SPELL_MOB").getLast().spread().z()).isCloseTo(1.6, offset(EPSILON));
        assertThat(along.particles("WATER_SPLASH")).filteredOn(splash -> splash.count() == 10)
            .hasSize(40 - 7 + 1)
            .allSatisfy(splash -> assertThat(Math.hypot(splash.at().x() - BED.middle().x(), splash.at().z() - BED.middle().z())).isLessThanOrEqualTo(KrakenSea.POOL));
    }

    @Test
    void fourTentaclesBurstOutOfTheFloorAroundTheBedOneAfterTheOther() {
        TracedStage stage = play(BED, 40);

        assertThat(stage.recording.breakEffects).containsExactly(
            new BlockChange(22, 60, 54, Material.BEDROCK, 0),
            new BlockChange(24, 60, 51, Material.BEDROCK, 0),
            new BlockChange(22, 60, 51, Material.BEDROCK, 0),
            new BlockChange(25, 60, 54, Material.BEDROCK, 0));
        assertThat(stage.calls("breakEffect")).extracting(TracedStage.Call::tick).containsExactly(20, 24, 30, 34);
        assertThat(stage.sounds("random.splash")).filteredOn(splash -> recorded(splash) == 44).hasSize(4)
            .allSatisfy(splash -> assertThat(volume(splash)).isEqualTo(1.6f));
        assertThat(stage.particles("BLOCK_CRACK")).hasSize(4);
        assertThat(stage.recording.particleData.get("BLOCK_CRACK")).containsOnly(Kit.blockId(Material.BEDROCK, 0));

        List<RecordingStage.FakeActor> segments = stands(stage).subList(2, stands(stage).size());
        assertThat(segments).hasSize(4 * KrakenTentacle.SEGMENTS);
        for (int tentacle = 0; tentacle < 4; tentacle++) {
            List<RecordingStage.FakeActor> body = segments.subList(tentacle * KrakenTentacle.SEGMENTS, (tentacle + 1) * KrakenTentacle.SEGMENTS);
            assertThat(body).extracting(segment -> segment.equipment.get(Slot.HELMET)).containsOnly(Material.WOOL);
            assertThat(body).extracting(segment -> segment.equipmentData.get(Slot.HELMET)).containsExactly(10, 10, 2, 10, 2, 2, 6);
            assertThat(body).extracting(segment -> segment.style).containsExactly(StandStyle.HIDDEN, StandStyle.HIDDEN, StandStyle.HIDDEN,
                StandStyle.HIDDEN, StandStyle.HIDDEN, StandStyle.HIDDEN_SMALL, StandStyle.HIDDEN_SMALL);
        }
    }

    @Test
    void eachTentacleIsBornBuriedAndRisesBentTowardsItsHalfOfTheBed() {
        RecordingStage stage = new RecordingStage(block -> block[1] <= 59);
        List<KrakenTentacle> tentacles = tentacles(stage, BED);
        double floor = BED.foot().y();

        for (KrakenTentacle tentacle : tentacles) {
            assertThat(tentacle.cube(0).y() - floor).isCloseTo(KrakenTentacle.BURIED, offset(EPSILON));
            assertThat(tentacle.cube(KrakenTentacle.SEGMENTS - 1).y() - floor).isLessThan(1);
        }
        KrakenTentacle first = tentacles.getFirst();
        Vec hole = first.cube(0);
        int risen = KrakenAnimation.HOLES.getFirst().rise() + KrakenTentacle.RISE;
        first.move(risen);
        assertThat(first.cube(0)).isEqualTo(new Vec(hole.x(), floor + KrakenTentacle.LIFT, hole.z()));
        for (int link = 0; link < KrakenTentacle.LINKS.length; link++) {
            assertThat(first.cube(link).distance(first.cube(link + 1))).isCloseTo(KrakenTentacle.LINKS[link], offset(1.0E-9));
        }
        Vec tip = first.cube(KrakenTentacle.SEGMENTS - 1);
        assertThat(tip.y() - floor).isBetween(2.4, 3.2);
        assertThat(Math.hypot(tip.x() - BED.head().x(), tip.z() - BED.head().z())).isLessThan(Math.hypot(hole.x() - BED.head().x(), hole.z() - BED.head().z()));
    }

    @Test
    void halfwayOutTheBuriedPartRisesStraightUpTheHole() {
        RecordingStage stage = new RecordingStage(block -> block[1] <= 59);
        KrakenTentacle first = tentacles(stage, BED).getFirst();
        Vec hole = first.cube(0);
        first.move(KrakenAnimation.HOLES.getFirst().rise() + 2);

        assertThat(first.cube(0).y()).isLessThan(BED.foot().y());
        for (int segment = 0; segment < KrakenTentacle.SEGMENTS && first.cube(segment).y() < BED.foot().y(); segment++) {
            assertThat(first.cube(segment).x()).isEqualTo(hole.x());
            assertThat(first.cube(segment).z()).isEqualTo(hole.z());
        }
        assertThat(first.cube(KrakenTentacle.SEGMENTS - 1).y()).isGreaterThan(BED.foot().y());
    }

    @Test
    void everyWoolBlockSitsOnTheTentacleWithItsHeadTiltedAlongIt() {
        TracedStage stage = play(BED, 90);
        List<RecordingStage.FakeActor> segments = stands(stage).subList(2, 2 + KrakenTentacle.SEGMENTS);
        RecordingStage replay = new RecordingStage(block -> block[1] <= 59);
        KrakenTentacle twin = tentacles(replay, BED).getFirst();
        twin.move(90);

        for (int segment = 0; segment < KrakenTentacle.SEGMENTS; segment++) {
            RecordingStage.FakeActor stand = segments.get(segment);
            double pitch = Math.toRadians(stand.pose.head().x());
            double yaw = Math.toRadians(stand.yaw);
            Vec along = new Vec(-Math.sin(yaw) * Math.sin(pitch), Math.cos(pitch), Math.cos(yaw) * Math.sin(pitch));
            boolean small = segment >= KrakenTentacle.SMALL_FROM;
            Vec cube = stand.position.add(0, small ? KrakenTentacle.SMALL_NECK : KrakenTentacle.NECK, 0)
                .add(along.multiply(small ? KrakenTentacle.SMALL_CROWN : KrakenTentacle.CROWN));
            assertThat(cube.distance(twin.cube(segment))).as("segment " + segment).isLessThan(1.0E-4);
        }
    }

    @Test
    void theTentaclesCurlOverAndPinTheBedThenSqueezeItDown() {
        RecordingStage stage = new RecordingStage(block -> block[1] <= 59);
        List<KrakenTentacle> tentacles = tentacles(stage, BED);
        int gripped = KrakenAnimation.STRIKE + 20;
        double floor = BED.foot().y();

        for (KrakenTentacle tentacle : tentacles) {
            tentacle.move(gripped);
            Vec tip = tentacle.cube(KrakenTentacle.SEGMENTS - 1).subtract(BED.middle());
            assertThat(Math.abs(tip.x() * BED.forward().x() + tip.z() * BED.forward().z())).isLessThan(1);
            assertThat(Math.abs(tip.x() * BED.right().x() + tip.z() * BED.right().z())).isLessThan(0.7);
            assertThat(tip.y()).isBetween(0.1, 0.6);
        }
        KrakenTentacle first = tentacles.getFirst();
        double pinned = first.cube(KrakenTentacle.SEGMENTS - 1).y();
        first.move(KrakenAnimation.SQUEEZES[2] + KrakenTentacle.SQUEEZE_TICKS);
        assertThat(pinned - first.cube(KrakenTentacle.SEGMENTS - 1).y()).isGreaterThan(0.4);
        assertThat(KrakenTentacle.squeezed(KrakenAnimation.SQUEEZES[1] + 1)).isCloseTo(1 + 1 / 3.0, offset(EPSILON));
        assertThat(KrakenTentacle.whip(KrakenAnimation.STRIKE + KrakenTentacle.WHIP_FROM + KrakenTentacle.WHIP_TICKS / 2)).isEqualTo(1);
        assertThat(KrakenTentacle.whip(KrakenAnimation.STRIKE + KrakenTentacle.WHIP_FROM + KrakenTentacle.WHIP_TICKS)).isCloseTo(0, offset(EPSILON));
        assertThat(KrakenTentacle.grip(KrakenTentacle.LINKS.length - 1, KrakenAnimation.STRIKE)).isZero();
        assertThat(KrakenTentacle.grip(0, gripped)).isEqualTo(1);
    }

    @Test
    void theSlapTheRoarAndThreeCrunchesSinkTheBed() {
        TracedStage stage = play(BED, KrakenAnimation.PLUNGE - 1);

        assertThat(stage.sounds("mob.slime.attack")).singleElement().satisfies(slap -> {
            assertThat(slap.tick()).isEqualTo(KrakenAnimation.SLAP);
            assertThat(recorded(slap)).isEqualTo(31);
            assertThat(slap.at()).isEqualTo(BED.middle().add(0, KrakenAnimation.VOICE, 0));
        });
        assertThat(stage.sounds("note.bass")).singleElement().satisfies(bass -> assertThat(bass.tick()).isEqualTo(KrakenAnimation.ROAR));
        assertThat(stage.sounds("mob.enderdragon.growl")).singleElement().satisfies(growl -> {
            assertThat(recorded(growl)).isEqualTo(31);
            assertThat(volume(growl)).isEqualTo(3);
        });
        assertThat(stage.sounds("mob.zombie.woodbreak")).extracting(TracedStage.Call::tick).containsExactly(81, 89, 97);
        assertThat(stage.sounds("mob.zombie.woodbreak")).allSatisfy(crunch -> assertThat(recorded(crunch)).isEqualTo(44));
        assertThat(stage.sounds("random.splash")).filteredOn(splash -> volume(splash) == 2).extracting(TracedStage.Call::tick).containsExactly(81, 89, 97);
        assertThat(KrakenAnimation.INK).isEqualTo(351);
        assertThat(stage.recording.particleData.get("ITEM_CRACK")).containsOnly(KrakenAnimation.INK).hasSize(1 + 2 * 3);
        assertThat(stage.particles("BLOCK_CRACK")).filteredOn(crack -> crack.tick() == KrakenAnimation.SLAP).hasSize(2)
            .allSatisfy(crack -> assertThat((int[]) crack.args().get(5)).containsExactly(Kit.blockId(Material.BED_BLOCK, 0)));
        assertThat(stage.particles("ITEM_CRACK")).filteredOn(ink -> ink.tick() == KrakenAnimation.SQUEEZES[2])
            .extracting(ink -> ink.at().y() - BED.foot().y()).allSatisfy(height -> assertThat(height).isCloseTo(KrakenAnimation.CRUSH - 0.4, offset(EPSILON)));

        List<RecordingStage.FakeActor> halves = stands(stage).subList(0, 2);
        assertThat(halves).allSatisfy(half -> assertThat(half.position.y()).isCloseTo(BED.foot().y() - 3 * KrakenBed.SQUEEZE_SINK, offset(EPSILON)));
        assertThat(Math.abs(halves.getFirst().position.x() - BED.foot().x())).isLessThanOrEqualTo(KrakenBed.SHAKE);
    }

    @Test
    void aLastRoarDragsEverythingUnderAndTheWaterRipplesOut() {
        TracedStage beforeSwallow = play(BED, KrakenAnimation.SWALLOW - 1);
        assertThat(beforeSwallow.recording.actors).filteredOn(actor -> actor.type.startsWith("FallingBlock")).noneMatch(RecordingStage.FakeActor::removed);

        TracedStage stage = play(BED, Integer.MAX_VALUE);
        assertThat(stage.sounds("random.explode")).singleElement().satisfies(blast -> {
            assertThat(blast.tick()).isEqualTo(KrakenAnimation.PLUNGE);
            assertThat(recorded(blast)).isEqualTo(88);
        });
        assertThat(stage.sounds("mob.enderdragon.growl")).last().satisfies(growl -> assertThat(recorded(growl)).isEqualTo(40));
        assertThat(stage.particles("WATER_SPLASH")).filteredOn(ripple -> ripple.count() == 2)
            .hasSize(KrakenAnimation.RIPPLES.length * KrakenSea.RIPPLE_STEPS * KrakenSea.RIPPLE_POINTS)
            .allSatisfy(ripple -> {
                double radius = Math.hypot(ripple.at().x() - BED.middle().x(), ripple.at().z() - BED.middle().z());
                double first = KrakenSea.RIPPLE_START + KrakenSea.RIPPLE_GROWTH * (ripple.tick() - KrakenAnimation.RIPPLES[0]);
                double second = KrakenSea.RIPPLE_START + KrakenSea.RIPPLE_GROWTH * (ripple.tick() - KrakenAnimation.RIPPLES[1]);
                assertThat(Math.min(Math.abs(radius - first), Math.abs(radius - second))).isLessThan(1.0E-9);
            });
        assertThat(stage.particles("DRIP_WATER")).isNotEmpty().allSatisfy(drip -> assertThat(drip.tick()).isBetween(20, KrakenAnimation.PLUNGE - 1));
        assertThat(stage.recording.alive()).isZero();
        assertThat(stage.timeline.length()).isEqualTo(KrakenAnimation.END);
        assertThat(KrakenBed.sink(KrakenAnimation.PLUNGE + 2)).isCloseTo(3 * KrakenBed.SQUEEZE_SINK + 2 * KrakenBed.PLUNGE_SINK, offset(EPSILON));
    }

    @Test
    void theTentaclesSinkBackIntoTheirHolesBeforeTheyVanish() {
        RecordingStage stage = new RecordingStage(block -> block[1] <= 59);
        List<KrakenTentacle> tentacles = tentacles(stage, BED);

        for (KrakenTentacle tentacle : tentacles) {
            tentacle.move(KrakenAnimation.GONE - 1);
            for (int segment = 0; segment < KrakenTentacle.SEGMENTS; segment++) {
                assertThat(tentacle.cube(segment).y()).isLessThan(BED.foot().y());
            }
        }

        TracedStage played = play(BED, KrakenAnimation.GONE - 1);
        assertThat(stands(played).subList(2, stands(played).size())).noneMatch(RecordingStage.FakeActor::removed);
        assertThat(stands(play(BED, KrakenAnimation.GONE)).subList(2, 2 + 4 * KrakenTentacle.SEGMENTS)).allMatch(RecordingStage.FakeActor::removed);
    }

    @Test
    void theTentaclesKeepTheirPlacesAroundABedFacingAnotherWay() {
        TracedStage stage = play(SIDEWAYS, 40);
        Vec middle = SIDEWAYS.middle();
        Vec base = middle.add(SIDEWAYS.forward().multiply(1.28)).add(SIDEWAYS.right().multiply(1.47));

        assertThat(stage.recording.breakEffects.getFirst())
            .isEqualTo(new BlockChange((int) Math.floor(base.x()), 24, (int) Math.floor(base.z()), Material.BEDROCK, 0));
        assertThat(base.x() - middle.x()).isCloseTo(1.28, offset(EPSILON));
        assertThat(base.z() - middle.z()).isCloseTo(1.47, offset(EPSILON));
    }
}
