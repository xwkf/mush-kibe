package br.com.muxe.minigame.engine.match;

import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public final class HealthBelowName {

    public static final String OBJECTIVE = "muxe-vida-nome";

    private final String label;
    private final Map<Scoreboard, Set<String>> shown = new WeakHashMap<>();

    public HealthBelowName(String label) {
        this.label = label;
    }

    public void show(Scoreboard board, Map<String, Integer> alive) {
        Objective objective = board.getObjective(OBJECTIVE);
        if (objective != null && mustRebuild(shown.getOrDefault(board, Set.of()), alive.keySet())) {
            objective.unregister();
            objective = null;
        }
        if (objective == null) {
            objective = board.registerNewObjective(OBJECTIVE, "dummy");
            objective.setDisplayName(label);
            objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
        }
        Objective target = objective;
        alive.forEach((name, points) -> target.getScore(name).setScore(points));
        shown.put(board, Set.copyOf(alive.keySet()));
    }

    public void hide(Scoreboard board) {
        Objective objective = board.getObjective(OBJECTIVE);
        if (objective != null) {
            objective.unregister();
        }
        shown.remove(board);
    }

    public static int points(double health) {
        return (int) Math.ceil(health);
    }

    static boolean mustRebuild(Set<String> shown, Set<String> alive) {
        return !alive.containsAll(shown);
    }
}
