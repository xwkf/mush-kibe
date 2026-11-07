package br.com.muxe.minigame.api.arena;

import br.com.muxe.minigame.api.match.MatchArena;
import br.com.muxe.minigame.engine.arena.LoadedArena;
import br.com.muxe.minigame.engine.setup.Point;
import br.com.muxe.minigame.engine.team.TeamColor;
import br.com.muxe.minigame.engine.world.map.MapTemplate;
import java.util.List;
import java.util.Map;

public record StandardArena(String name, String displayName, String mode, MapTemplate map, Point waitingSpawn, List<Point> waitingArea, Point spectator,
                            int voidY, Map<TeamColor, Point> spawns, ArenaView view) implements MatchArena {

    public StandardArena {
        waitingArea = List.copyOf(waitingArea);
        spawns = Map.copyOf(spawns);
    }

    public static StandardArena from(LoadedArena loaded) {
        ArenaView view = new ArenaView(loaded.arena());
        return new StandardArena(
            view.name(),
            view.displayName(),
            view.mode(),
            loaded.map(),
            view.point(ArenaSchema.WAITING_SPAWN),
            view.points(ArenaSchema.WAITING_AREA),
            view.point(ArenaSchema.SPECTATOR),
            view.height(ArenaSchema.VOID),
            view.perTeam(team -> view.point(ArenaSchema.SPAWN, team)),
            view
        );
    }

    @Override
    public List<TeamColor> teamColors() {
        return view.teams();
    }

    @Override
    public Point spawn(TeamColor team) {
        return spawns.get(team);
    }
}
