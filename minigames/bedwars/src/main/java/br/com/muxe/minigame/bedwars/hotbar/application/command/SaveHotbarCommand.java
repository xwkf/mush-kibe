package br.com.muxe.minigame.bedwars.hotbar.application.command;

import br.com.muxe.core.common.cqrs.command.Command;
import br.com.muxe.core.common.result.Result;
import br.com.muxe.minigame.bedwars.hotbar.domain.HotbarLayout;
import java.util.Objects;
import java.util.UUID;

public record SaveHotbarCommand(UUID playerId, HotbarLayout layout) implements Command<Result<Void>> {

    public SaveHotbarCommand {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(layout, "layout");
    }
}
