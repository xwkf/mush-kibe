package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockChange;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockType;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Click;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.bedwars.cosmetic.animation.RecordingStage.FakeActor;
import br.com.muxe.minigame.bedwars.cosmetic.animation.RecordingStage.FakeMount;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class WitchVictoryTest {

    private static final int FLOOR = 27;

    private final UUID winner = UUID.randomUUID();
    private final UUID other = UUID.randomUUID();

    @Test
    void itIsTheBruxaVictoryAndLastsTwoHundredTicks() {
        Timeline timeline = script(stage(), scene(winner));

        assertThat(new WitchVictory().id()).isEqualTo("bruxa");
        assertThat(SceneAnimations.victories().find("bruxa")).isPresent();
        assertThat(timeline.length()).isEqualTo(200);
    }

    @Test
    void holdingWFliesOneBlockPerTickWhereTheBroomFacesAndClimbsAlongTheLook() {
        Vec south = BroomFlight.target(true, 0, 0);
        assertThat(south.x()).isCloseTo(0, offset(1e-9));
        assertThat(south.y()).isCloseTo(0, offset(1e-9));
        assertThat(south.z()).isCloseTo(1, offset(1e-9));
        Vec east = BroomFlight.target(true, -90, 0);
        assertThat(east.x()).isCloseTo(1, offset(1e-9));
        assertThat(east.z()).isCloseTo(0, offset(1e-9));
        assertThat(BroomFlight.target(true, 0, -20).y()).isCloseTo(Math.tan(Math.toRadians(20)), offset(1e-9));
        assertThat(BroomFlight.target(true, 0, 30).y()).isCloseTo(-Math.tan(Math.toRadians(30)), offset(1e-9));
        assertThat(BroomFlight.target(true, 0, 89).y()).isEqualTo(-1);
        assertThat(BroomFlight.target(true, 0, -89).y()).isEqualTo(1);
        assertThat(BroomFlight.target(false, 40, -30)).isEqualTo(Vec.ZERO);
    }

    @Test
    void theBroomSpeedsUpAndTurnsTowardTheLookEasingIn() {
        BroomFlight flight = new BroomFlight(new Vec(0, 30, 0), 0);

        flight.step(true, 0, 0);
        assertThat(flight.velocity().z()).isCloseTo(0.3, offset(1e-9));
        assertThat(flight.velocity().y()).isCloseTo(0.07, offset(1e-9));
        for (int tick = 0; tick < 30; tick++) {
            flight.step(true, 0, 0);
        }
        assertThat(flight.velocity().z()).isCloseTo(1, offset(1e-3));

        flight.step(true, 40, 0);
        assertThat((double) flight.yaw()).isCloseTo(14, offset(1e-4));
        for (int tick = 0; tick < 40; tick++) {
            flight.step(false, 40, 0);
        }
        assertThat((double) flight.yaw()).isCloseTo(40, offset(1e-3));
        assertThat(flight.velocity().length()).isLessThan(1e-4);
    }

    @Test
    void theBroomIsABristleHeadFiveLyingHandleHeadsAndASittingBlackKitten() {
        RecordingStage stage = stage();
        Vec seat = new Vec(10, 30, 10);

        Broom.build(stage, seat, 0);

        List<FakeActor> stands = stage.actors.stream().filter(actor -> actor.type.equals("ArmorStand")).toList();
        assertThat(stands).hasSize(6).allSatisfy(stand -> {
            assertThat(stand.pose.head()).isEqualTo(new Vec(0, 0, 90));
            assertThat(stand.style.invisible()).isTrue();
        });
        assertThat(stands.getFirst().skulls).containsEntry(Slot.HELMET, Broom.BRISTLES);
        assertThat(stands.getFirst().style.small()).isFalse();
        assertThat(stands.getFirst().position).isEqualTo(new Vec(10, 30 - 1.375, 10 - 1.54));
        assertThat(stands.subList(1, 6)).allSatisfy(stand -> {
            assertThat(stand.skulls).containsEntry(Slot.HELMET, Broom.HANDLE);
            assertThat(stand.style.small()).isTrue();
        });
        assertThat(stands.get(1).position.z()).isCloseTo(10 - 0.94, offset(1e-9));
        assertThat(stands.get(5).position.z()).isCloseTo(10 - 0.94 + 4 * 0.406, offset(1e-9));
        assertThat(stands.get(5).position.y()).isCloseTo(30 - 0.688 - 4 * 0.031, offset(1e-9));
        FakeActor cat = stage.actors.getLast();
        assertThat(cat.type).isEqualTo("Ozelot");
        assertThat(cat.watched).containsEntry(12, (byte) -1).containsEntry(16, (byte) 1).containsEntry(18, (byte) 1);
        assertThat(cat.position).isEqualTo(new Vec(10, 30.25, 10 - 0.77));
    }

    @Test
    void everyRiderGetsAWandABroomAndFourWitchesOnTheirOwnBrooms() {
        RecordingStage stage = stage();
        stage.eyesOf.put(other, new Vec(60.5, FLOOR + Riders.EYE_HEIGHT, -40.5));
        stage.lookOf.put(other, new Vec(1, 0, 0));
        Timeline timeline = script(stage, scene(winner, other));

        timeline.run(0);

        assertThat(stage.tools).containsOnlyKeys(winner, other);
        assertThat(stage.mounts).extracting(mount -> mount.riders).containsExactly(List.of(winner), List.of(other));
        assertThat(stage.mounts.getFirst().start).isEqualTo(new Vec(0.5, FLOOR + 0.75, 0.5));
        List<FakeActor> witches = stage.actors.stream().filter(actor -> actor.type.equals("Player")).toList();
        assertThat(witches).hasSize(8).allSatisfy(witch -> {
            assertThat(witch.equipment).containsEntry(Slot.HAND, Material.STICK);
            assertThat(((FakeActor) witch.vehicle).style.marker()).isTrue();
        });
        assertThat(stage.actors.stream().filter(actor -> actor.equipment.get(Slot.HELMET) == Material.HOPPER)).hasSize(8);
        assertThat(stage.actors.stream().filter(actor -> actor.type.equals("Ozelot"))).hasSize(10);
        assertThat(stage.particles).containsEntry("CLOUD", 8);
        assertThat(stage.sounds).containsExactly("mob.enderdragon.growl");
        assertThat(witches.get(0).position).isEqualTo(new Vec(0.5 + 4, FLOOR + 0.75, 0.5 + 4));
        assertThat(witches.get(1).position).isEqualTo(new Vec(0.5 - 4, FLOOR + 0.75, 0.5 + 4));
        assertThat(witches.get(3).position.x()).isCloseTo(0.5 - 7, offset(1e-9));
        assertThat(witches.get(3).position.z()).isCloseTo(0.5 + 3, offset(1e-9));
    }

    @Test
    void theWitchesHoverFirstThenChaseTheirPlaceNoFasterThanOneBlockPerTick() {
        assertThat(Companion.chase(Vec.ZERO, new Vec(0, 0, 2))).isEqualTo(new Vec(0, 0, 2 * 0.18));
        assertThat(Companion.chase(Vec.ZERO, new Vec(0, 0, 40)).length()).isCloseTo(1, offset(1e-9));
        Vec place = Companion.place(new Vec(0, 30, 0), 90, new Companion.Place(4, 7));
        assertThat(place.x()).isCloseTo(-4, offset(1e-9));
        assertThat(place.z()).isCloseTo(-7, offset(1e-9));

        RecordingStage stage = stage();
        Timeline timeline = script(stage, scene(winner));
        FakeMount mount = run(stage, timeline, 0);
        mount.pushing = true;
        FakeActor witch = stage.actors.stream().filter(actor -> actor.type.equals("Player")).findFirst().orElseThrow();
        Vec start = witch.position;
        for (int tick = 1; tick < Companion.FOLLOW_START; tick++) {
            timeline.run(tick);
            assertThat(witch.position.x()).isEqualTo(start.x());
            assertThat(witch.position.z()).isEqualTo(start.z());
        }
        assertThat(witch.position.y()).isGreaterThan(start.y());
        Vec previous = witch.position;
        for (int tick = Companion.FOLLOW_START; tick < 120; tick++) {
            timeline.run(tick);
            assertThat(witch.position.distance(previous)).isLessThanOrEqualTo(1.0 + 1e-9);
            previous = witch.position;
        }
        assertThat(witch.position.z()).isBetween(mount.seat.z() - 6, mount.seat.z() + 4);
        assertThat(witch.position.x()).isCloseTo(mount.seat.x() + 4, offset(0.5));
    }

    @Test
    void theRiderSteersTheBroomAndTheBroomFollowsTheSeat() {
        RecordingStage stage = stage();
        Timeline timeline = script(stage, scene(winner));
        FakeMount mount = run(stage, timeline, 0);
        mount.pitch = -20;
        mount.pushing = true;
        for (int tick = 1; tick <= 60; tick++) {
            timeline.run(tick);
        }

        Vec step = mount.path.getLast().subtract(mount.path.get(mount.path.size() - 2));
        assertThat(step.z()).isCloseTo(1, offset(1e-3));
        assertThat(step.y()).isCloseTo(Math.tan(Math.toRadians(20)), offset(1e-3));
        FakeActor bristles = stage.actors.getFirst();
        assertThat(bristles.position).isEqualTo(Broom.spot(mount.seat, 0, Broom.BRISTLES_FORWARD, Broom.BRISTLES_UP));
    }

    @Test
    void clickingTheWandFiresABeamThatBurstsNineTicksLaterAndRecolorsTheDyedBlocksAround() {
        RecordingStage stage = stage();
        stage.types = block -> {
            if (block[1] < FLOOR) {
                return new BlockType(block[0] == 0 ? Material.STAINED_CLAY : Material.WOOL, 0);
            }
            return new BlockType(block[2] == 40 && block[1] == FLOOR ? Material.STONE : Material.AIR, 0);
        };
        stage.eyes = new Vec(0.5, 40, 0.5);
        stage.look = new Vec(0, -1, 0);
        Timeline timeline = script(stage, scene(winner));
        run(stage, timeline, 0);
        timeline.run(1);
        int before = stage.actors.size();

        stage.tools.get(winner).accept(Click.LEFT);

        List<FakeActor> guardians = stage.actors.subList(before, stage.actors.size());
        assertThat(guardians).extracting(actor -> actor.type).containsExactly("Guardian", "Guardian");
        assertThat(guardians).allSatisfy(guardian -> assertThat(guardian.watched).containsEntry(0, (byte) 32));
        FakeActor caster = guardians.getFirst();
        FakeActor target = guardians.getLast();
        assertThat(caster.target).isSameAs(target);
        assertThat(target.position.y()).isCloseTo(FLOOR - 0.425, offset(0.01));
        assertThat(stage.sounds).contains("mob.guardian.curse");

        for (int tick = 2; tick < 1 + Hex.BURST_AFTER; tick++) {
            timeline.run(tick);
        }
        assertThat(caster.removed).isFalse();
        assertThat(stage.bulk).isEmpty();
        timeline.run(1 + Hex.BURST_AFTER);

        assertThat(caster.removed).isTrue();
        assertThat(target.removed).isTrue();
        assertThat(stage.sounds).contains("random.explode");
        assertThat(stage.particles).containsEntry("EXPLOSION_HUGE", 1);
        assertThat(stage.bulk).isNotEmpty().allSatisfy(change -> {
            assertThat(change.data()).isIn(Hex.COLORS);
            assertThat(new Vec(change.x() + 0.5, change.y() + 0.5, change.z() + 0.5).distance(new Vec(0.5, FLOOR, 0.5))).isLessThanOrEqualTo(Hex.RADIUS + 0.01);
            assertThat(change.y()).isLessThan(FLOOR);
        });
        assertThat(stage.bulk.stream().map(BlockChange::data).distinct()).hasSize(1);
        assertThat(stage.bulk.stream().map(BlockChange::material).distinct()).containsExactlyInAnyOrder(Material.WOOL, Material.STAINED_CLAY);
    }

    @Test
    void onlyWoolClayGlassPanesAndCarpetsAreRecolored() {
        RecordingStage stage = stage();
        stage.types = block -> new BlockType(switch (block[0]) {
            case 0 -> Material.STAINED_GLASS;
            case 1 -> Material.STAINED_GLASS_PANE;
            case 2 -> Material.CARPET;
            case 3 -> Material.STONE;
            default -> Material.AIR;
        }, 5);

        List<BlockChange> changes = Hex.recolor(stage, new Vec(1.5, 30.5, 0.5), 9);

        assertThat(changes).extracting(BlockChange::material).containsOnly(Material.STAINED_GLASS, Material.STAINED_GLASS_PANE, Material.CARPET);
        assertThat(changes).extracting(BlockChange::data).containsOnly(9);
        assertThat(changes).allSatisfy(change -> assertThat(new Vec(change.x() + 0.5, change.y() + 0.5, change.z() + 0.5).distance(new Vec(1.5, 30.5, 0.5))).isLessThanOrEqualTo(5.5));
        assertThat(changes.stream().filter(change -> change.x() == 1 && change.z() == 0)).hasSize(11);
    }

    @Test
    void theWandWaitsThirteenTicksBetweenSpellsAndAimsEightyBlocksIntoTheAirWhenNothingIsHit() {
        assertThat(WitchVictory.ready(Optional.empty(), 0)).isTrue();
        assertThat(WitchVictory.ready(Optional.of(10), 22)).isFalse();
        assertThat(WitchVictory.ready(Optional.of(10), 23)).isTrue();
        RecordingStage sky = new RecordingStage(block -> false);
        assertThat(WitchVictory.aim(sky, new Vec(0, 30, 0), new Vec(0, 0, 2))).isEqualTo(new Vec(0, 30, 80));

        RecordingStage stage = stage();
        stage.eyes = new Vec(0.5, 40, 0.5);
        stage.look = new Vec(0, -1, 0);
        Timeline timeline = script(stage, scene(winner));
        run(stage, timeline, 0);
        timeline.run(1);
        stage.tools.get(winner).accept(Click.LEFT);
        stage.tools.get(winner).accept(Click.RIGHT);
        assertThat(guardians(stage)).isEqualTo(2);
        for (int tick = 2; tick <= 13; tick++) {
            timeline.run(tick);
        }
        stage.tools.get(winner).accept(Click.LEFT);
        assertThat(guardians(stage)).isEqualTo(2);
        timeline.run(14);
        stage.tools.get(winner).accept(Click.RIGHT);
        assertThat(guardians(stage)).isEqualTo(4);
    }

    @Test
    void fireworksLaunchEveryTenTicksAndEverythingIsGoneAtTheEnd() {
        RecordingStage stage = stage();
        stage.eyes = new Vec(0.5, 40, 0.5);
        stage.look = new Vec(0, -1, 0);
        Timeline timeline = script(stage, scene(winner, other));
        for (int tick = 0; tick <= timeline.length(); tick++) {
            timeline.run(tick);
            if (tick == 195) {
                stage.tools.get(winner).accept(Click.LEFT);
            }
        }
        stage.tools.get(winner).accept(Click.LEFT);

        assertThat(stage.sounds.stream().filter("fireworks.launch"::equals).count()).isEqualTo(20);
        assertThat(stage.sounds.stream().filter("random.explode"::equals).count()).isZero();
        assertThat(stage.alive()).isZero();
    }

    private static long guardians(RecordingStage stage) {
        return stage.actors.stream().filter(actor -> actor.type.equals("Guardian")).count();
    }

    private static FakeMount run(RecordingStage stage, Timeline timeline, int tick) {
        timeline.run(tick);
        return stage.mounts.getFirst();
    }

    private static RecordingStage stage() {
        return new RecordingStage(block -> block[1] < FLOOR);
    }

    private static Timeline script(RecordingStage stage, Scene scene) {
        Timeline timeline = new Timeline();
        new WitchVictory().script(scene, stage, timeline, new Random(7));
        return timeline;
    }

    private static Scene scene(UUID... riders) {
        return new Scene(new Vec(0.5, FLOOR, 0.5), new Vec(0, 0, 1), Optional.empty(), List.of(riders));
    }
}
