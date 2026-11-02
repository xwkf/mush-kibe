package br.com.muxe.minigame.engine.arena;

import br.com.muxe.minigame.engine.setup.ArenaFiles.StoredArena;
import br.com.muxe.minigame.engine.world.map.MapTemplate;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ArenaCatalog {

    private final ArenaStore store;
    private final Predicate<MapTemplate> fits;
    private final Logger logger;
    private final Map<String, LoadedArena> arenas = new ConcurrentHashMap<>();

    public ArenaCatalog(ArenaStore store, Predicate<MapTemplate> fits, Logger logger) {
        this.store = store;
        this.fits = fits;
        this.logger = logger;
    }

    public int loadAll() throws IOException {
        arenas.clear();
        for (String name : store.names()) {
            reload(name);
        }
        return arenas.size();
    }

    public Optional<LoadedArena> reload(String name) {
        arenas.remove(name);
        try {
            Optional<StoredArena> stored = store.read(name);
            if (stored.isEmpty() || !stored.get().ready()) {
                return Optional.empty();
            }
            Optional<MapTemplate> found = store.map(name);
            if (found.isEmpty()) {
                logger.warning("Arena " + name + " is ready but has no map");
                return Optional.empty();
            }
            MapTemplate map = found.get();
            if (!fits.test(map)) {
                logger.warning("Arena " + name + " is too wide for a match cell: " + map.bounds());
                return Optional.empty();
            }
            LoadedArena arena = new LoadedArena(stored.get().draft(), map);
            arenas.put(name, arena);
            return Optional.of(arena);
        } catch (IOException | RuntimeException exception) {
            logger.log(Level.WARNING, "Could not load arena " + name, exception);
            return Optional.empty();
        }
    }

    public Optional<LoadedArena> find(String name) {
        return Optional.ofNullable(arenas.get(name));
    }

    public List<LoadedArena> ofMode(String mode) {
        List<LoadedArena> found = new ArrayList<>();
        for (LoadedArena arena : arenas.values()) {
            if (arena.mode().equals(mode)) {
                found.add(arena);
            }
        }
        found.sort((first, second) -> first.displayName().compareToIgnoreCase(second.displayName()));
        return found;
    }

    public Collection<LoadedArena> all() {
        return List.copyOf(arenas.values());
    }
}
