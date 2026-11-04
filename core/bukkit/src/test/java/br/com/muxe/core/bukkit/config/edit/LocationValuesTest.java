package br.com.muxe.core.bukkit.config.edit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class LocationValuesTest {

    @Test
    void coordinatesAreRoundedToHundredthsAndAnglesToTenths() {
        LocationValues location = LocationValues.of("lobby", 6.123456, 11.0, -21.987654, 179.96f, 10.04f, false);

        assertThat(location.x()).isEqualTo(6.12);
        assertThat(location.y()).isEqualTo(11.0);
        assertThat(location.z()).isEqualTo(-21.99);
        assertThat(location.yaw()).isEqualTo(180.0);
        assertThat(location.pitch()).isEqualTo(10.0);
    }

    @Test
    void centeringPutsHorizontalCoordinatesOnTheMiddleOfTheBlock() {
        LocationValues location = LocationValues.of("lobby", 6.9, 11.0, -21.1, 0f, 0f, true);

        assertThat(location.x()).isEqualTo(6.5);
        assertThat(location.z()).isEqualTo(-21.5);
        assertThat(location.y()).isEqualTo(11.0);
    }

    @Test
    void negativeZeroBecomesZero() {
        LocationValues location = LocationValues.of("lobby", -0.001, 0.0, 0.001, -0.01f, 0f, false);

        assertThat(Double.doubleToRawLongBits(location.x())).isEqualTo(Double.doubleToRawLongBits(0.0));
        assertThat(Double.doubleToRawLongBits(location.yaw())).isEqualTo(Double.doubleToRawLongBits(0.0));
    }

    @Test
    void entriesAreWrittenUnderThePathInStableOrder() {
        LocationValues location = LocationValues.of("lobby", 1.5, 2.0, 3.5, 90f, 0f, false);

        Map<String, Object> withWorld = location.entries("preview.spawn", true);
        Map<String, Object> withoutWorld = location.entries("preview.spawn", false);

        assertThat(withWorld.keySet()).containsExactly(
            "preview.spawn.world", "preview.spawn.x", "preview.spawn.y", "preview.spawn.z", "preview.spawn.yaw", "preview.spawn.pitch");
        assertThat(withWorld).containsEntry("preview.spawn.world", "lobby").containsEntry("preview.spawn.yaw", 90.0);
        assertThat(withoutWorld).doesNotContainKey("preview.spawn.world");
    }

    @Test
    void describeIsAReadableLine() {
        assertThat(LocationValues.of("lobby", 1.5, 2.0, 3.5, 90f, 10f, false).describe()).isEqualTo("lobby 1.5 2.0 3.5 (yaw 90.0, pitch 10.0)");
    }
}
