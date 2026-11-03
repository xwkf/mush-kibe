package br.com.muxe.minigame.bedwars.teamcolor.application.command;

import br.com.muxe.core.common.cqrs.command.Command;
import br.com.muxe.core.common.result.Result;
import br.com.muxe.minigame.engine.team.TeamColor;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record SaveFavouriteColorCommand(UUID playerId, Optional<TeamColor> color) implements Command<Result<Void>> {

    public SaveFavouriteColorCommand {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(color, "color");
    }
}
