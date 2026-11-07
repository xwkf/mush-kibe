package br.com.muxe.minigame.api.runtime;

import br.com.muxe.minigame.engine.team.TeamColor;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@FunctionalInterface
public interface TeamPreferences {

    TeamPreferences NONE = players -> Map.of();

    Map<UUID, TeamColor> preferred(List<UUID> players);
}
