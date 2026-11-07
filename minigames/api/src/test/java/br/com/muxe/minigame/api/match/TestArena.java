package br.com.muxe.minigame.api.match;

import br.com.muxe.minigame.engine.setup.Point;
import br.com.muxe.minigame.engine.team.TeamColor;
import br.com.muxe.minigame.engine.world.map.MapTemplate;
import java.util.List;

public record TestArena(String name, String displayName, String mode, List<TeamColor> teamColors) implements MatchArena {

    public static final List<TeamColor> RED_BLUE = List.of(TeamColor.VERMELHO, TeamColor.AZUL);
    public static final List<TeamColor> EIGHT = List.of(TeamColor.VERMELHO, TeamColor.AZUL, TeamColor.VERDE, TeamColor.AMARELO,
        TeamColor.CIANO, TeamColor.LARANJA, TeamColor.ROSA, TeamColor.CINZA);

    public static TestArena of(String mode, List<TeamColor> teams) {
        return new TestArena(mode + "_teste", "Teste", mode, teams);
    }

    @Override
    public MapTemplate map() {
        return MapTemplate.empty();
    }

    @Override
    public Point waitingSpawn() {
        return Point.block(0, 120, 0);
    }

    @Override
    public List<Point> waitingArea() {
        return List.of(Point.block(-5, 118, -5), Point.block(5, 125, 5));
    }

    @Override
    public Point spectator() {
        return Point.block(0, 100, 0);
    }

    @Override
    public int voidY() {
        return 40;
    }

    @Override
    public Point spawn(TeamColor team) {
        return Point.block(teamColors.indexOf(team) * 40, 70, 60);
    }
}
