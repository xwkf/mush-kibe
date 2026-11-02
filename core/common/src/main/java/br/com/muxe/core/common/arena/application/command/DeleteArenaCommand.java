package br.com.muxe.core.common.arena.application.command;

import br.com.muxe.core.common.cqrs.command.Command;
import br.com.muxe.core.common.result.Result;
import java.util.Objects;

public record DeleteArenaCommand(String game, String name) implements Command<Result<Void>> {

    public DeleteArenaCommand {
        Objects.requireNonNull(game, "game");
        Objects.requireNonNull(name, "name");
    }
}
