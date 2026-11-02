package br.com.muxe.minigame.engine.arena;

import br.com.muxe.core.common.arena.application.command.DeleteArenaCommand;
import br.com.muxe.core.common.arena.application.command.DeleteArenaHandler;
import br.com.muxe.core.common.arena.application.command.SaveArenaCommand;
import br.com.muxe.core.common.arena.application.query.FindArenaQuery;
import br.com.muxe.core.common.arena.application.query.ListArenasQuery;
import br.com.muxe.core.common.arena.domain.ArenaRecord;
import br.com.muxe.core.common.cqrs.command.CommandBus;
import br.com.muxe.core.common.cqrs.query.QueryBus;
import br.com.muxe.core.common.result.Result;
import br.com.muxe.minigame.engine.setup.ArenaDraft;
import br.com.muxe.minigame.engine.setup.ArenaFiles;
import br.com.muxe.minigame.engine.setup.ArenaFiles.StoredArena;
import br.com.muxe.minigame.engine.world.map.MapFiles;
import br.com.muxe.minigame.engine.world.map.MapTemplate;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionException;

public final class DatabaseArenaStore implements ArenaStore {

    private final String game;
    private final CommandBus commands;
    private final QueryBus queries;

    public DatabaseArenaStore(String game, CommandBus commands, QueryBus queries) {
        this.game = game;
        this.commands = commands;
        this.queries = queries;
    }

    @Override
    public List<String> names() throws IOException {
        return join(() -> queries.ask(new ListArenasQuery(game)).join()).stream().filter(ArenaDraft::isValidName).toList();
    }

    @Override
    public Optional<StoredArena> read(String name) throws IOException {
        Optional<ArenaRecord> record = find(name);
        if (record.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(ArenaFiles.parse(name, record.get().definition(), source(name)));
    }

    @Override
    public Optional<MapTemplate> map(String name) throws IOException {
        Optional<ArenaRecord> record = find(name);
        if (record.isEmpty() || record.get().map().length == 0) {
            return Optional.empty();
        }
        return Optional.of(MapFiles.fromBytes(record.get().map(), source(name)));
    }

    @Override
    public void save(ArenaDraft draft, boolean ready, MapTemplate map) throws IOException {
        dispatch(new SaveArenaCommand(game, draft.name(), draft.mode(), ready, ArenaFiles.render(draft, ready), MapFiles.toBytes(map), false), draft);
    }

    @Override
    public void saveDefinition(ArenaDraft draft, boolean ready) throws IOException {
        dispatch(SaveArenaCommand.definitionOnly(game, draft.name(), draft.mode(), ready, ArenaFiles.render(draft, ready)), draft);
    }

    @Override
    public boolean delete(String name) throws IOException {
        Result<Void> result = join(() -> commands.dispatch(new DeleteArenaCommand(game, name)).join());
        if (result instanceof Result.Failure<Void> failure) {
            if (failure.problem().code().equals(DeleteArenaHandler.NOT_FOUND)) {
                return false;
            }
            throw new IOException("Could not delete arena " + name + " of " + game + ": " + failure.problem().message());
        }
        return true;
    }

    private void dispatch(SaveArenaCommand command, ArenaDraft draft) throws IOException {
        Result<Void> result = join(() -> commands.dispatch(command).join());
        if (!result.isSuccess()) {
            throw new IOException("Could not save arena " + draft.name() + " of " + game);
        }
    }

    private Optional<ArenaRecord> find(String name) throws IOException {
        return join(() -> queries.ask(new FindArenaQuery(game, name)).join());
    }

    private String source(String name) {
        return "arena " + game + "/" + name;
    }

    private static <T> T join(Work<T> work) throws IOException {
        try {
            return work.run();
        } catch (CompletionException exception) {
            throw new IOException(exception.getCause() == null ? exception.getMessage() : exception.getCause().getMessage(), exception);
        }
    }

    @FunctionalInterface
    private interface Work<T> {

        T run();
    }
}
