package br.com.muxe.minigame.api.arena;

import br.com.muxe.minigame.engine.setup.ArenaDraft;
import br.com.muxe.minigame.engine.setup.Point;
import br.com.muxe.minigame.engine.setup.SetupStep;
import br.com.muxe.minigame.engine.team.TeamColor;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.Function;

public record ArenaView(ArenaDraft draft) {

    public String name() {
        return draft.name();
    }

    public String displayName() {
        return draft.displayName();
    }

    public String mode() {
        return draft.mode();
    }

    public List<TeamColor> teams() {
        return draft.teams();
    }

    public Point point(SetupStep step) {
        return point(step, null);
    }

    public Point point(SetupStep step, TeamColor team) {
        List<Point> points = draft.points(step, team);
        if (points.isEmpty()) {
            throw new IllegalArgumentException("Arena " + draft.name() + " has no " + ArenaDraft.key(step, team));
        }
        return points.getFirst();
    }

    public List<Point> points(SetupStep step) {
        return draft.points(step, null);
    }

    public List<Point> points(SetupStep step, TeamColor team) {
        return draft.points(step, team);
    }

    public int height(SetupStep step) {
        return (int) draft.number(step.id()).orElse(0);
    }

    public OptionalInt optionalHeight(SetupStep step) {
        double value = draft.number(step.id()).orElse(Double.NaN);
        return Double.isNaN(value) ? OptionalInt.empty() : OptionalInt.of((int) value);
    }

    public <T> Map<TeamColor, T> perTeam(Function<TeamColor, T> reader) {
        Map<TeamColor, T> values = new LinkedHashMap<>();
        for (TeamColor team : draft.teams()) {
            values.put(team, reader.apply(team));
        }
        return values;
    }
}
