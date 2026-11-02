package br.com.muxe.minigame.bedwars.stats.application.command;

import br.com.muxe.core.common.cqrs.command.Command;
import br.com.muxe.core.common.result.Result;
import java.util.Objects;
import java.util.UUID;

public record ToggleFavouriteMapCommand(UUID playerId, String arena) implements Command<Result<Boolean>> {

    public static final int MAX_ARENA_LENGTH = 64;

    public ToggleFavouriteMapCommand {
        Objects.requireNonNull(playerId, "playerId");
        if (arena == null || arena.isBlank() || arena.length() > MAX_ARENA_LENGTH) {
            throw new IllegalArgumentException("Invalid arena: " + arena);
        }
    }
}
