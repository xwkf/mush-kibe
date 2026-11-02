package br.com.muxe.minigame.bedwars.stats.application.command;

import br.com.muxe.core.common.cqrs.command.Command;
import br.com.muxe.core.common.result.Result;
import br.com.muxe.minigame.bedwars.stats.domain.MatchOutcome;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record RecordMatchCommand(List<MatchOutcome> outcomes) implements Command<Result<Void>> {

    public RecordMatchCommand {
        outcomes = List.copyOf(Objects.requireNonNull(outcomes, "outcomes"));
        Set<UUID> players = new HashSet<>();
        for (MatchOutcome outcome : outcomes) {
            if (!players.add(outcome.playerId())) {
                throw new IllegalArgumentException("The player " + outcome.playerId() + " has more than one outcome in the match");
            }
        }
    }
}
