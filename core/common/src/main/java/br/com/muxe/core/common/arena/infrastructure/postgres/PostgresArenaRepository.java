package br.com.muxe.core.common.arena.infrastructure.postgres;

import br.com.muxe.core.common.arena.domain.ArenaRecord;
import br.com.muxe.core.common.arena.domain.ArenaRepository;
import br.com.muxe.core.common.persistence.sql.Column;
import br.com.muxe.core.common.persistence.sql.InsertBuilder;
import br.com.muxe.core.common.persistence.sql.Select;
import br.com.muxe.core.common.persistence.sql.Sql;
import br.com.muxe.core.common.persistence.sql.Table;
import br.com.muxe.core.common.persistence.sql.Write;
import br.com.muxe.core.common.persistence.sql.type.SqlType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public final class PostgresArenaRepository implements ArenaRepository {

    private static final Table ARENAS = Table.of("core", "arenas");
    private static final Column<String> GAME = ARENAS.column("game", SqlType.TEXT);
    private static final Column<String> NAME = ARENAS.column("name", SqlType.TEXT);
    private static final Column<String> MODE = ARENAS.column("mode", SqlType.TEXT);
    private static final Column<Boolean> READY = ARENAS.column("ready", SqlType.BOOLEAN);
    private static final Column<String> DEFINITION = ARENAS.column("definition", SqlType.TEXT);
    private static final Column<byte[]> MAP = ARENAS.column("map", SqlType.BYTEA);
    private static final Column<Instant> UPDATED_AT = ARENAS.column("updated_at", SqlType.TIMESTAMPTZ);

    private static final Select<String> NAMES = Sql.select(NAME).from(ARENAS)
        .where(GAME.eq())
        .orderBy(NAME.asc())
        .map(row -> row.get(NAME))
        .named("arenas.names");

    private static final Select<ArenaRecord> FIND = Sql.select(GAME, NAME, MODE, READY, DEFINITION, MAP, UPDATED_AT).from(ARENAS)
        .where(GAME.eq().and(NAME.eq()))
        .map(row -> new ArenaRecord(row.get(GAME), row.get(NAME), row.get(MODE), row.get(READY), row.get(DEFINITION), row.get(MAP), row.get(UPDATED_AT)))
        .named("arenas.find");

    private static final Write<ArenaRecord> SAVE = insert()
        .doUpdate(MODE, READY, DEFINITION, MAP, UPDATED_AT)
        .named("arenas.save");

    private static final Write<ArenaRecord> SAVE_DEFINITION = insert()
        .doUpdate(MODE, READY, DEFINITION, UPDATED_AT)
        .named("arenas.save_definition");

    private static final Write<Void> DELETE = Sql.deleteFrom(ARENAS)
        .where(GAME.eq().and(NAME.eq()))
        .named("arenas.delete");

    @Override
    public List<String> names(String game) {
        return NAMES.list(GAME.is(game));
    }

    @Override
    public Optional<ArenaRecord> find(String game, String name) {
        return FIND.first(GAME.is(game), NAME.is(name));
    }

    @Override
    public void save(ArenaRecord arena, boolean keepMap) {
        (keepMap ? SAVE_DEFINITION : SAVE).execute(arena);
    }

    @Override
    public boolean delete(String game, String name) {
        return DELETE.execute(GAME.is(game), NAME.is(name)) > 0;
    }

    private static InsertBuilder<ArenaRecord> insert() {
        return Sql.<ArenaRecord>insertInto(ARENAS)
            .value(GAME, ArenaRecord::game)
            .value(NAME, ArenaRecord::name)
            .value(MODE, ArenaRecord::mode)
            .value(READY, ArenaRecord::ready)
            .value(DEFINITION, ArenaRecord::definition)
            .value(MAP, ArenaRecord::map)
            .value(UPDATED_AT, ArenaRecord::updatedAt)
            .onConflict(GAME, NAME);
    }
}
