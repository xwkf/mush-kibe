package br.com.muxe.core.common.arena.application.command;

import br.com.muxe.core.common.cqrs.command.Command;
import br.com.muxe.core.common.result.Result;
import java.util.Objects;

public record SaveArenaCommand(String game, String name, String mode, boolean ready, String definition, byte[] map, boolean keepMap) implements Command<Result<Void>> {

    public SaveArenaCommand {
        Objects.requireNonNull(game, "game");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(mode, "mode");
        Objects.requireNonNull(definition, "definition");
        map = map == null ? new byte[0] : map.clone();
    }

    public static SaveArenaCommand definitionOnly(String game, String name, String mode, boolean ready, String definition) {
        return new SaveArenaCommand(game, name, mode, ready, definition, new byte[0], true);
    }

    @Override
    public byte[] map() {
        return map.clone();
    }
}
