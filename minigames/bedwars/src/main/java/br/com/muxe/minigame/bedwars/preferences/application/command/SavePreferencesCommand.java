package br.com.muxe.minigame.bedwars.preferences.application.command;

import br.com.muxe.core.common.cqrs.command.Command;
import br.com.muxe.core.common.result.Result;
import br.com.muxe.minigame.bedwars.preferences.domain.Preferences;
import java.util.Objects;
import java.util.UUID;

public record SavePreferencesCommand(UUID playerId, Preferences preferences) implements Command<Result<Void>> {

    public SavePreferencesCommand {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(preferences, "preferences");
    }
}
