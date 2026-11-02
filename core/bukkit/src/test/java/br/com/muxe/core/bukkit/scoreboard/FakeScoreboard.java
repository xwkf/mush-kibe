package br.com.muxe.core.bukkit.scoreboard;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

final class FakeScoreboard {

    private final Map<String, Integer> scores = new LinkedHashMap<>();
    private final Map<String, Team> teams = new HashMap<>();
    private int scoreWrites;

    Scoreboard scoreboard() {
        return proxy(Scoreboard.class, (method, args) -> switch (method) {
            case "registerNewObjective" -> objective();
            case "getTeam" -> teams.get((String) args[0]);
            case "registerNewTeam" -> {
                Team team = team();
                teams.put((String) args[0], team);
                yield team;
            }
            case "resetScores" -> {
                scores.remove((String) args[0]);
                yield null;
            }
            default -> null;
        });
    }

    Map<String, Integer> scores() {
        return scores;
    }

    int scoreWrites() {
        return scoreWrites;
    }

    private Objective objective() {
        return proxy(Objective.class, (method, args) -> method.equals("getScore") ? score((String) args[0]) : null);
    }

    private Score score(String entry) {
        return proxy(Score.class, (method, args) -> {
            if (method.equals("setScore")) {
                scoreWrites++;
                scores.put(entry, (Integer) args[0]);
            }
            return null;
        });
    }

    private Team team() {
        Set<String> entries = new HashSet<>();
        return proxy(Team.class, (method, args) -> switch (method) {
            case "addEntry" -> entries.add((String) args[0]);
            case "removeEntry" -> entries.remove((String) args[0]);
            default -> null;
        });
    }

    private static <T> T proxy(Class<T> type, Answer answer) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, (proxy, method, args) -> answer.on(method.getName(), args)));
    }

    private interface Answer {

        Object on(String method, Object[] args);
    }
}
