package br.com.muxe.minigame.api.match;

import br.com.muxe.minigame.engine.mode.GameMode;
import br.com.muxe.minigame.engine.team.TeamColor;
import br.com.muxe.minigame.engine.world.cell.Cell;
import java.util.List;
import java.util.UUID;

public final class MatchFixtures {

    public static final GameMode SOLO = new GameMode("solo", "Solo", 6, 8, 1);
    public static final GameMode ONE_V_ONE = new GameMode("1v1", "1v1", 2, 2, 1);
    public static final GameMode DOUBLES = new GameMode("duplas", "Duplas", 2, 4, 2);

    private MatchFixtures() {
    }

    public static Match match(GameMode mode, List<TeamColor> teams, boolean respawns) {
        return new Match("abc123", mode, TestArena.of(mode.id(), teams), new Cell(0, 0, 0, 64), respawns);
    }

    public static UUID join(Match match, String name) {
        UUID id = UUID.nameUUIDFromBytes(name.getBytes());
        match.join(id, name);
        return id;
    }
}
