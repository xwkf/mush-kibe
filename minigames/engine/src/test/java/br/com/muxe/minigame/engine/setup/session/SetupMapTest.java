package br.com.muxe.minigame.engine.setup.session;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.minigame.engine.setup.session.SetupMap.Origin;
import br.com.muxe.minigame.engine.world.map.MapFixtures;
import br.com.muxe.minigame.engine.world.map.MapTemplate;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SetupMapTest {

    private final MapTemplate built = MapTemplate.builder().put(MapFixtures.chunk(0, 0, MapFixtures.section(4, 1, 0))).build();

    @TempDir
    Path server;

    @Test
    void anArenaSavedEmptyPicksUpAWorldFolderUploadedLater() throws IOException {
        Path folder = worldFolder("solo_majestic");

        SetupMap map = SetupMap.choose(Optional.of(MapTemplate.empty()), folder);

        assertThat(map.origin()).isEqualTo(Origin.CONVERTED);
        assertThat(map.needsSaving()).isTrue();
    }

    @Test
    void aSavedMapWithBlocksWinsOverTheWorldFolder() throws IOException {
        Path folder = worldFolder("solo_majestic");

        SetupMap map = SetupMap.choose(Optional.of(built), folder);

        assertThat(map.origin()).isEqualTo(Origin.LOADED);
        assertThat(map.template()).isSameAs(built);
        assertThat(map.needsSaving()).isFalse();
    }

    @Test
    void aNewArenaWithoutAWorldFolderStartsEmpty() throws IOException {
        SetupMap map = SetupMap.choose(Optional.empty(), server.resolve("solo_majestic"));

        assertThat(map.origin()).isEqualTo(Origin.EMPTY);
        assertThat(map.template().chunkCount()).isZero();
        assertThat(map.needsSaving()).isTrue();
    }

    @Test
    void anArenaSavedEmptyWithoutAWorldFolderKeepsItsMap() throws IOException {
        SetupMap map = SetupMap.choose(Optional.of(MapTemplate.empty()), server.resolve("solo_majestic"));

        assertThat(map.origin()).isEqualTo(Origin.LOADED);
        assertThat(map.needsSaving()).isFalse();
    }

    private Path worldFolder(String name) throws IOException {
        Path folder = server.resolve(name);
        Files.createDirectories(folder.resolve("region"));
        return folder;
    }
}
