package br.com.muxe.core.common.mission.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MissionSession {

    private final Map<String, Long> start = new HashMap<>();
    private final Map<String, MissionGoal> goals = new HashMap<>();
    private final Map<String, Long> sent = new HashMap<>();
    private final Map<String, Long> pending = new HashMap<>();
    private MissionBoard board;

    public MissionSession(MissionBoard board) {
        this.board = board;
        for (MissionBoard.Entry entry : board.entries()) {
            start.put(entry.mission().id(), entry.progress().progress());
            goals.put(entry.mission().goal().id(), entry.mission().goal());
        }
    }

    public MissionBoard board() {
        return board;
    }

    public List<Mission> update(Map<String, Long> totals) {
        Map<String, Long> amounts = new HashMap<>();
        totals.forEach((goalId, total) -> {
            MissionGoal goal = goals.get(goalId);
            if (goal == null) {
                return;
            }
            long previous = sent.getOrDefault(goalId, 0L);
            long amount = goal.best() ? (total > previous ? total : 0) : total - previous;
            if (amount > 0) {
                amounts.put(goalId, amount);
                sent.put(goalId, total);
                pending.merge(goalId, amount, goal::apply);
            }
        });
        if (amounts.isEmpty()) {
            return List.of();
        }
        MissionBoard.Update update = board.apply(amounts);
        board = update.board();
        return update.completed();
    }

    public Map<String, Long> drain() {
        Map<String, Long> drained = Map.copyOf(pending);
        pending.clear();
        return drained;
    }

    public List<Change> changes() {
        List<Change> changes = new ArrayList<>();
        for (MissionBoard.Entry entry : board.entries()) {
            long before = start.getOrDefault(entry.mission().id(), 0L);
            long now = entry.progress().progress();
            if (now > before) {
                changes.add(new Change(entry, now - before));
            }
        }
        return changes;
    }

    public record Change(MissionBoard.Entry entry, long added) {
    }
}
