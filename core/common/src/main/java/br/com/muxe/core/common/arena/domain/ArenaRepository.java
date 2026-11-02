package br.com.muxe.core.common.arena.domain;

import java.util.List;
import java.util.Optional;

public interface ArenaRepository {

    List<String> names(String game);

    Optional<ArenaRecord> find(String game, String name);

    void save(ArenaRecord arena, boolean keepMap);

    boolean delete(String game, String name);
}
