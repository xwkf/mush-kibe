package br.com.muxe.core.common.testing;

import br.com.muxe.core.common.arena.domain.ArenaRecord;
import br.com.muxe.core.common.arena.domain.ArenaRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public final class InMemoryArenaRepository implements ArenaRepository {

    private final Map<String, ArenaRecord> arenas = new TreeMap<>();

    @Override
    public List<String> names(String game) {
        return arenas.values().stream().filter(arena -> arena.game().equals(game)).map(ArenaRecord::name).toList();
    }

    @Override
    public Optional<ArenaRecord> find(String game, String name) {
        return Optional.ofNullable(arenas.get(game + "/" + name));
    }

    @Override
    public void save(ArenaRecord arena, boolean keepMap) {
        ArenaRecord current = arenas.get(arena.game() + "/" + arena.name());
        ArenaRecord saved = keepMap && current != null
            ? new ArenaRecord(arena.game(), arena.name(), arena.mode(), arena.ready(), arena.definition(), current.map(), arena.updatedAt())
            : arena;
        arenas.put(arena.game() + "/" + arena.name(), saved);
    }
}
