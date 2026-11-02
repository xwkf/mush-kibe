package br.com.muxe.minigame.engine.arena;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.minigame.engine.setup.ArenaDraft;
import br.com.muxe.minigame.engine.world.map.MapFixtures;
import br.com.muxe.minigame.engine.world.map.MapTemplate;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileArenaStoreTest {

    @TempDir
    Path dataFolder;

    @Test
    void deletingAnArenaRemovesItsDefinitionAndMap() throws IOException {
        FileArenaStore store = new FileArenaStore(dataFolder);
        store.save(new ArenaDraft("solo_majestic", "solo", "Majestic"), true, MapTemplate.builder().put(MapFixtures.chunk(0, 0, MapFixtures.section(4, 1, 0))).build());

        assertThat(store.delete("solo_majestic")).isTrue();

        assertThat(store.names()).isEmpty();
        assertThat(store.read("solo_majestic")).isEmpty();
        assertThat(store.map("solo_majestic")).isEmpty();
        assertThat(store.delete("solo_majestic")).isFalse();
    }
}
