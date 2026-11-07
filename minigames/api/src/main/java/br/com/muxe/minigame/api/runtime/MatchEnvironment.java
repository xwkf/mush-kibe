package br.com.muxe.minigame.api.runtime;

import br.com.muxe.core.bukkit.text.MessageCatalog;
import br.com.muxe.minigame.api.settings.MatchSettings;
import java.time.Clock;
import java.util.random.RandomGenerator;

public record MatchEnvironment(MatchSettings settings, MatchRules rules, MessageCatalog messages, Clock clock, RandomGenerator random, MatchEvents events,
                               TeamPreferences preferences) {
}
