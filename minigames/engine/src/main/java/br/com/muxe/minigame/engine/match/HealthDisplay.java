package br.com.muxe.minigame.engine.match;

import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public final class HealthDisplay {

    public static final String BELOW_NAME = "muxe-vida-nome";
    public static final String PLAYER_LIST = "muxe-vida-tab";

    private final String belowNameLabel;
    private final Map<Scoreboard, Set<String>> shown = new WeakHashMap<>();

    public HealthDisplay(String belowNameLabel) {
        this.belowNameLabel = belowNameLabel;
    }

    public void show(Scoreboard board, Map<String, Integer> alive) {
        boolean rebuild = mustRebuild(shown.getOrDefault(board, Set.of()), alive.keySet());
        Objective belowName = objective(board, BELOW_NAME, DisplaySlot.BELOW_NAME, belowNameLabel, rebuild);
        Objective playerList = objective(board, PLAYER_LIST, DisplaySlot.PLAYER_LIST, PLAYER_LIST, rebuild);
        alive.forEach((name, points) -> {
            belowName.getScore(name).setScore(points);
            playerList.getScore(name).setScore(points);
        });
        shown.put(board, Set.copyOf(alive.keySet()));
    }

    public void hide(Scoreboard board) {
        for (String name : new String[] {BELOW_NAME, PLAYER_LIST}) {
            Objective objective = board.getObjective(name);
            if (objective != null) {
                objective.unregister();
            }
        }
        shown.remove(board);
    }

    public static int points(double health) {
        return (int) Math.ceil(health);
    }

    static boolean mustRebuild(Set<String> shown, Set<String> alive) {
        return !alive.containsAll(shown);
    }

    private static Objective objective(Scoreboard board, String name, DisplaySlot slot, String label, boolean rebuild) {
        Objective objective = board.getObjective(name);
        if (objective != null && rebuild) {
            objective.unregister();
            objective = null;
        }
        if (objective == null) {
            objective = board.registerNewObjective(name, "dummy");
            objective.setDisplayName(label);
            objective.setDisplaySlot(slot);
        }
        return objective;
    }
}
