package br.com.muxe.core.common.mission.application.command;

import br.com.muxe.core.common.cqrs.command.Command;
import br.com.muxe.core.common.mission.domain.Mission;
import br.com.muxe.core.common.result.Result;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record RecordMissionProgressCommand(String game, UUID playerId, Map<String, Long> amounts) implements Command<Result<List<Mission>>> {

    public RecordMissionProgressCommand {
        Objects.requireNonNull(game, "game");
        Objects.requireNonNull(playerId, "playerId");
        amounts = Map.copyOf(amounts);
    }
}
