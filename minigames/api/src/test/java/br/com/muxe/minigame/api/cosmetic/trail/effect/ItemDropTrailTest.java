package br.com.muxe.minigame.api.cosmetic.trail.effect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.api.cosmetic.trail.Flight;
import br.com.muxe.minigame.api.cosmetic.trail.ProjectileKind;
import java.util.List;
import java.util.SplittableRandom;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ItemDropTrailTest {

    private static final Vec HERE = new Vec(10.625, 20.656, 20.0);

    private final TrailRecorder recorder = new TrailRecorder();

    @Test
    void blueLuckDropsALapisHalfABlockBelowTheProjectileEveryTick() {
        ItemDropTrail.SORTE_AZUL.tick(flight(0), recorder.stage, new SplittableRandom(1));
        ItemDropTrail.SORTE_AZUL.tick(flight(1), recorder.stage, new SplittableRandom(2));

        assertThat(ItemDropTrail.SORTE_AZUL.id()).isEqualTo("sorte-azul");
        assertThat(recorder.items).hasSize(2);
        assertThat(recorder.items.getFirst().position).isEqualTo(HERE.add(0, -0.5, 0));
        assertThat(recorder.items.get(1).position).isEqualTo(flight(1).position().add(0, -0.5, 0));
        assertThat(recorder.items).allSatisfy(item -> {
            assertThat(item.material).isEqualTo(Material.INK_SACK);
            assertThat(item.data).isEqualTo(4);
        });
        assertThat(recorder.particles).isEmpty();
    }

    @Test
    void droppedItemsPopUpAndScatterSideways() {
        SplittableRandom random = new SplittableRandom(5);
        for (int age = 0; age < 400; age++) {
            ItemDropTrail.SORTE_AZUL.tick(flight(age), recorder.stage, random);
        }

        List<Vec> velocities = recorder.items.stream().map(item -> item.velocities).peek(sent -> assertThat(sent).hasSize(1)).map(List::getFirst).toList();
        assertThat(velocities).extracting(Vec::y).containsOnly(0.1);
        assertThat(velocities).extracting(Vec::x).allSatisfy(x -> assertThat(x).isBetween(-0.1, 0.1));
        assertThat(velocities).extracting(Vec::z).allSatisfy(z -> assertThat(z).isBetween(-0.1, 0.1));
        assertThat(velocities).extracting(Vec::x).anySatisfy(x -> assertThat(x).isLessThan(-0.09)).anySatisfy(x -> assertThat(x).isGreaterThan(0.09));
        assertThat(velocities).extracting(Vec::z).anySatisfy(z -> assertThat(z).isLessThan(-0.09)).anySatisfy(z -> assertThat(z).isGreaterThan(0.09));
    }

    @Test
    void eachItemDisappearsFiveTicksAfterItDropped() {
        for (int age = 0; age <= 4; age++) {
            ItemDropTrail.SORTE_AZUL.tick(flight(age), recorder.stage, new SplittableRandom(age));
        }
        assertThat(recorder.alive()).hasSize(5);

        ItemDropTrail.SORTE_AZUL.tick(flight(5), recorder.stage, new SplittableRandom(5));

        assertThat(recorder.items.getFirst().removed()).isTrue();
        assertThat(recorder.alive()).hasSize(5);

        ItemDropTrail.SORTE_AZUL.tick(flight(6), recorder.stage, new SplittableRandom(6));

        assertThat(recorder.items.get(1).removed()).isTrue();
        assertThat(recorder.items).allSatisfy(item -> assertThat(item.removals).isLessThanOrEqualTo(1));
    }

    @Test
    void bonesDropABoneOrBoneMealEverySecondTick() {
        SplittableRandom random = new SplittableRandom(11);
        for (int age = 0; age < 200; age++) {
            ItemDropTrail.OSSOS.tick(flight(age), recorder.stage, random);
        }

        assertThat(ItemDropTrail.OSSOS.id()).isEqualTo("ossos");
        assertThat(recorder.items).hasSize(100);
        assertThat(recorder.items).extracting(item -> item.material + ":" + item.data).containsOnly("BONE:0", "INK_SACK:15").contains("BONE:0", "INK_SACK:15");
        assertThat(recorder.items.get(1).position).isEqualTo(flight(2).position().add(0, -0.5, 0));
        assertThat(recorder.particles).isEmpty();
    }

    @Test
    void landingLeavesTheFallingItemsForTheStageToClear() {
        for (int age = 0; age <= 6; age++) {
            ItemDropTrail.SORTE_AZUL.tick(flight(age), recorder.stage, new SplittableRandom(age));
        }

        ItemDropTrail.SORTE_AZUL.land(flight(7), recorder.stage, new SplittableRandom(7));

        assertThat(recorder.alive()).hasSize(5);
        assertThat(recorder.items).hasSize(7);
        assertThat(ItemDropTrail.SORTE_AZUL.linger()).isEqualTo(5);
        assertThat(ItemDropTrail.OSSOS.linger()).isEqualTo(5);
    }

    @Test
    void twoFlightsKeepTheirOwnItems() {
        TrailRecorder other = new TrailRecorder();
        ItemDropTrail.SORTE_AZUL.tick(flight(0), recorder.stage, new SplittableRandom(1));
        ItemDropTrail.SORTE_AZUL.tick(flight(9), other.stage, new SplittableRandom(1));

        assertThat(recorder.alive()).hasSize(1);
        assertThat(other.alive()).hasSize(1);
    }

    @ParameterizedTest
    @EnumSource(ProjectileKind.class)
    void everyKindOfProjectileGetsTheSameTrail(ProjectileKind kind) {
        ItemDropTrail.SORTE_AZUL.tick(new Flight(HERE, new Vec(0, 0.1, -0.4), 0, kind), recorder.stage, new SplittableRandom(1));

        assertThat(recorder.items).singleElement().satisfies(item -> assertThat(item.position).isEqualTo(HERE.add(0, -0.5, 0)));
    }

    @Test
    void aTrailNeedsSomethingToDropAndAPositiveInterval() {
        List<ItemDropTrail.Drop> bone = List.of(new ItemDropTrail.Drop(Material.BONE, 0));
        assertThatThrownBy(() -> new ItemDropTrail("empty", 1, List.of())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ItemDropTrail("never", 0, bone)).isInstanceOf(IllegalArgumentException.class);
    }

    private static Flight flight(int age) {
        return new Flight(HERE.add(0, 0, -1.47 * age), new Vec(0, 0, -1.47), age, ProjectileKind.SNOWBALL);
    }
}
