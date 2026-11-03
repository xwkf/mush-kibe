package br.com.muxe.core.bukkit.nms;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.OptionalLong;
import org.junit.jupiter.api.Test;

class JoinReportTest {

    @Test
    void addsUpEveryPacketSent() {
        JoinReport report = new JoinReport(OptionalLong.of(320), Map.of("MapChunkBulk", 40, "SpawnEntityLiving", 210, "KeepAlive", 1), 0, 0);

        assertThat(report.total()).isEqualTo(251);
    }

    @Test
    void listsTheMostSentKindsFirst() {
        JoinReport report = new JoinReport(OptionalLong.empty(), Map.of("MapChunkBulk", 40, "SpawnEntityLiving", 210, "KeepAlive", 1, "EntityMetadata", 40), 0, 0);

        assertThat(report.busiest(3)).containsExactly(Map.entry("SpawnEntityLiving", 210), Map.entry("EntityMetadata", 40), Map.entry("MapChunkBulk", 40));
    }

    @Test
    void bytesAreShownInWholeKilobytes() {
        JoinReport report = new JoinReport(OptionalLong.empty(), Map.of(), 3_276_800, 4_100);

        assertThat(report.kilobytes()).isEqualTo(3_200);
        assertThat(report.slowestWriteMillis()).isEqualTo(4_100);
    }

    @Test
    void movementPacketsAreTheFlyingPacketAndItsVariants() {
        assertThat(JoinReport.isMovement("net.minecraft.server.v1_8_R3.PacketPlayInFlying")).isTrue();
        assertThat(JoinReport.isMovement("net.minecraft.server.v1_8_R3.PacketPlayInFlying$PacketPlayInPositionLook")).isTrue();
        assertThat(JoinReport.isMovement("net.minecraft.server.v1_8_R3.PacketPlayInKeepAlive")).isFalse();
    }

    @Test
    void outgoingPacketsAreNamedWithoutTheirPrefix() {
        assertThat(JoinReport.shortName("net.minecraft.server.v1_8_R3.PacketPlayOutMapChunkBulk")).isEqualTo("MapChunkBulk");
        assertThat(JoinReport.shortName("net.minecraft.server.v1_8_R3.PacketPlayOutEntity$PacketPlayOutRelEntityMove")).isEqualTo("RelEntityMove");
    }
}
