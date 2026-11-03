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
    void overTheVoidTheEffectKeepsItsHeight() {
        assertThat(Ground.surface((x, y, z) -> false, 0, 12.5, 0)).isEqualTo(12.5);
    }
}
