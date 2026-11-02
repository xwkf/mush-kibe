package br.com.muxe.minigame.engine.setup;

import br.com.muxe.minigame.engine.team.TeamColor;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class TeamPicker {

    private static final double SAME_BLOCK_SQUARED = 2.25;

    private TeamPicker() {
    }

    public static Optional<TeamColor> nearest(ArenaDraft draft, SetupStep anchor, Point at) {
        TeamColor best = null;
        double bestDistance = Double.MAX_VALUE;
        for (TeamColor team : draft.teams()) {
            for (Point point : draft.points(anchor, team)) {
                double distance = point.distanceSquared(at);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = team;
                }
            }
        }
        return Optional.ofNullable(best);
    }

    public static Optional<Point> nearestFree(Point from, List<Point> candidates, List<Point> taken) {
        return candidates.stream()
            .filter(candidate -> taken.stream().noneMatch(used -> used.distanceSquared(candidate) <= SAME_BLOCK_SQUARED))
            .min(Comparator.comparingDouble(candidate -> candidate.distanceSquared(from)));
    }
}
