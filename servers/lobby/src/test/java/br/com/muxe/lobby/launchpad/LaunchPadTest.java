package br.com.muxe.lobby.launchpad;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import br.com.muxe.core.common.testing.MutableClock;
import br.com.muxe.lobby.config.LobbySettings;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class LaunchPadTest {

    @Test
    void theDefaultLaunchReachesTwentyBlocksAwayAndFiveBlocksUp() {
        LaunchPadSettings settings = LobbySettings.from(defaultConfig()).launchPad();

        LaunchVelocity.Launch launch = settings.launch();
        LaunchVelocity.Flight flight = LaunchVelocity.simulate(launch.horizontal(), launch.vertical());

        assertThat(settings.enabled()).isTrue();
        assertThat(settings.block()).isEqualTo(Material.SLIME_BLOCK);
        assertThat(flight.distance()).isCloseTo(20, within(0.05));
        assertThat(flight.peak()).isCloseTo(5, within(0.05));
        assertThat(launch.horizontal()).isLessThan(LaunchVelocity.MAX_COMPONENT);
        assertThat(LobbySettings.ticks(settings.particleInterval())).isEqualTo(5);
    }

    @Test
    void thePushFollowsWhereThePlayerLooks() {
        LaunchVelocity.Launch launch = new LaunchVelocity.Launch(2, 1);

        LaunchVelocity.Push south = launch.towards(0);
        LaunchVelocity.Push west = launch.towards(90);

        assertThat(south.x()).isCloseTo(0, within(1e-9));
        assertThat(south.z()).isCloseTo(2, within(1e-9));
        assertThat(south.y()).isEqualTo(1);
        assertThat(west.x()).isCloseTo(-2, within(1e-9));
        assertThat(west.z()).isCloseTo(0, within(1e-9));
    }

    @Test
    void anUnreachableDistanceIsCappedAtTheLargestVelocityTheClientAccepts() {
        LaunchVelocity.Launch launch = LaunchVelocity.solve(500, 5);

        assertThat(launch.horizontal()).isEqualTo(LaunchVelocity.MAX_COMPONENT);
    }

    @Test
    void aChunkScanFindsEveryPadAndSkipsEmptySections() {
        FakeChunk chunk = new FakeChunk(1, -1, 3, 70, 5);

        List<PadSpot> spots = ChunkScan.find(chunk, 165);

        assertThat(spots).containsExactly(new PadSpot("lobby", 19, 70, -11));
        assertThat(chunk.lookups).isEqualTo(ChunkScan.SIZE * ChunkScan.SIZE * ChunkScan.SIZE);
    }

    @Test
    void theIndexFollowsChunksAndPlacedOrBrokenPads() {
        PadIndex index = new PadIndex();
        PadSpot first = new PadSpot("lobby", 19, 70, -11);
        PadSpot second = new PadSpot("lobby", 20, 70, -11);
        PadSpot far = new PadSpot("lobby", 200, 70, 200);

        index.replaceChunk("lobby", 1, -1, List.of(first));
        index.add(second);
        index.add(far);

        assertThat(index.near("lobby", 20, 71, -10, 32)).containsExactlyInAnyOrder(first, second);
        assertThat(index.near("outro", 20, 71, -10, 32)).isEmpty();

        index.remove(second);
        index.forgetChunk("lobby", 1, -1);

        assertThat(index.all()).containsExactly(far);
    }

    @Test
    void aPlayerIsLaunchedOnlyOnceUntilTheCooldownEnds() {
        MutableClock clock = MutableClock.startingAt("2026-11-02T12:00:00Z");
        LaunchCooldown cooldown = new LaunchCooldown(clock, Duration.ofSeconds(1));
        UUID player = UUID.randomUUID();

        assertThat(cooldown.tryUse(player)).isTrue();
        assertThat(cooldown.tryUse(player)).isFalse();

        clock.advance(Duration.ofSeconds(1));

        assertThat(cooldown.tryUse(player)).isTrue();
    }

    private static YamlConfiguration defaultConfig() {
        try (Reader reader = new InputStreamReader(Objects.requireNonNull(LaunchPadTest.class.getResourceAsStream("/config.yml")), StandardCharsets.UTF_8)) {
            return YamlConfiguration.loadConfiguration(reader);
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static final class FakeChunk implements ChunkScan.ChunkBlocks {

        private final int chunkX;
        private final int chunkZ;
        private final int padX;
        private final int padY;
        private final int padZ;
        private int lookups;

        private FakeChunk(int chunkX, int chunkZ, int padX, int padY, int padZ) {
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.padX = padX;
            this.padY = padY;
            this.padZ = padZ;
        }

        @Override
        public String world() {
            return "lobby";
        }

        @Override
        public int chunkX() {
            return chunkX;
        }

        @Override
        public int chunkZ() {
            return chunkZ;
        }

        @Override
        public boolean emptySection(int section) {
            return section != padY / ChunkScan.SIZE;
        }

        @Override
        public int typeId(int x, int y, int z) {
            lookups++;
            return x == padX && y == padY && z == padZ ? 165 : 0;
        }
    }
}
