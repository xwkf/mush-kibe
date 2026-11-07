package br.com.muxe.minigame.api.match;

import br.com.muxe.minigame.engine.setup.Point;
import br.com.muxe.minigame.engine.team.TeamColor;
import br.com.muxe.minigame.engine.world.map.MapTemplate;
import java.util.List;

public interface MatchArena {

    String name();

    String displayName();

    String mode();

    MapTemplate map();

    List<TeamColor> teamColors();

    Point waitingSpawn();

    List<Point> waitingArea();

    Point spectator();

    int voidY();

    Point spawn(TeamColor team);
}
