package br.com.muxe.minigame.engine.arena;

import br.com.muxe.minigame.engine.setup.ArenaDraft;
import br.com.muxe.minigame.engine.setup.ArenaFiles.StoredArena;
import br.com.muxe.minigame.engine.world.map.MapTemplate;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface ArenaStore {

    List<String> names() throws IOException;

    Optional<StoredArena> read(String name) throws IOException;

    Optional<MapTemplate> map(String name) throws IOException;

    void save(ArenaDraft draft, boolean ready, MapTemplate map) throws IOException;

    void saveDefinition(ArenaDraft draft, boolean ready) throws IOException;

    boolean delete(String name) throws IOException;
}
