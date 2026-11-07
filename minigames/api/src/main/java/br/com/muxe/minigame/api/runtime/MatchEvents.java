package br.com.muxe.minigame.api.runtime;

import br.com.muxe.core.bukkit.cosmetic.animation.Area;
import br.com.muxe.minigame.engine.team.TeamColor;
import java.util.List;
import org.bukkit.entity.Player;

public interface MatchEvents {

    MatchEvents NONE = new MatchEvents() {
    };

    default void victory(Player celebrant, List<Player> viewers, Area area) {
    }

    default void finalKill(Player killer, Player victim, TeamColor victimTeam, List<Player> viewers) {
    }
}
