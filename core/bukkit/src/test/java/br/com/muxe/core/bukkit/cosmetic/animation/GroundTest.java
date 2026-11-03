package br.com.muxe.core.bukkit.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class GroundTest {

    private final Ground.Solid floorAt26 = (x, y, z) -> y <= 26;

    @Test
    void anEffectRecordedHigherUpLandsOnTheRealFloor() {
        assertThat(Ground.surface(floorAt26, -19.5, 35, 68.5)).isEqualTo(27);
    }

    @Test
    void aPlayerStandingOnTheFloorStaysWhereTheyAre() {
        assertThat(Ground.surface(floorAt26, 0.5, 27, 0.5)).isEqualTo(27);
    }

    @Test
    void aPlayerInTheAirFindsTheFloorBelowThem() {
        assertThat(Ground.surface(floorAt26, 3, 40.7, 3)).isEqualTo(27);
    }

    @Test
    void aPositionSlightlyInsideTheFloorComesBackUpOnTopOfIt() {
        assertThat(Ground.surface(floorAt26, 0, 25.5, 0)).isEqualTo(27);
    }

    @Test
    void aRoofAboveDoesNotCountAsFloor() {
        Ground.Solid roofed = (x, y, z) -> y <= 26 || y >= 48 && y <= 55;

        assertThat(Ground.surface(roofed, 0, 27, 0)).isEqualTo(27);
    }

    @Test
    void somethingFallingHitsTheFirstBlockBelowIt() {
        Ground.Solid tree = (x, y, z) -> y <= 26 || x == 0 && y <= 31;

        assertThat(Ground.hit(tree, 0.5, 40, 0.5, 64)).hasValue(32);
        assertThat(Ground.hit(tree, 3.5, 40, 0.5, 64)).hasValue(27);
    }

    @Test
    void somethingFallingOverTheVoidHitsNothing() {
        assertThat(Ground.hit((x, y, z) -> false, 0, 40, 0, 64)).isEmpty();
        assertThat(Ground.hit(floorAt26, 0, 40, 0, 10)).isEmpty();
    }

    @Test
    void overTheVoidTheEffectKeepsItsHeight() {
        assertThat(Ground.surface((x, y, z) -> false, 0, 12.5, 0)).isEqualTo(12.5);
    }
}
