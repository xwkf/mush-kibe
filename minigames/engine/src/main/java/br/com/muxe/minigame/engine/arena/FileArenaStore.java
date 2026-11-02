package br.com.muxe.minigame.engine.arena;

import br.com.muxe.minigame.engine.setup.ArenaDraft;
import br.com.muxe.minigame.engine.setup.ArenaFiles;
import br.com.muxe.minigame.engine.setup.ArenaFiles.StoredArena;
import br.com.muxe.minigame.engine.world.map.MapFiles;
import br.com.muxe.minigame.engine.world.map.MapTemplate;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public final class FileArenaStore implements ArenaStore {

    private final ArenaFiles arenas;
    private final Path maps;

    public FileArenaStore(Path dataFolder) {
        this(new ArenaFiles(dataFolder.resolve("arenas")), dataFolder.resolve("maps"));
    }

    public FileArenaStore(ArenaFiles arenas, Path maps) {
        this.arenas = arenas;
        this.maps = maps;
    }

    @Override
    public List<String> names() throws IOException {
        return arenas.names();
    }

    @Override
    public Optional<StoredArena> read(String name) throws IOException {
        return arenas.read(name);
    }

    @Override
    public Optional<MapTemplate> map(String name) throws IOException {
        Path file = maps.resolve(name + MapFiles.EXTENSION);
        return Files.isRegularFile(file) ? Optional.of(MapFiles.read(file)) : Optional.empty();
    }

    @Override
    public void save(ArenaDraft draft, boolean ready, MapTemplate map) throws IOException {
        MapFiles.write(maps.resolve(draft.name() + MapFiles.EXTENSION), map);
        arenas.write(draft, ready);
    }

    @Override
    public void saveDefinition(ArenaDraft draft, boolean ready) throws IOException {
        arenas.write(draft, ready);
    }
}
