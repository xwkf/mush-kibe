package br.com.muxe.minigame.bedwars.game;

import br.com.muxe.minigame.engine.setup.Point;
import br.com.muxe.minigame.engine.team.TeamColor;
import java.util.Map;
import java.util.Optional;

public final class ChestAccess {

    private ChestAccess() {
    }

    public static Optional<TeamColor> owner(Point chest, Map<TeamColor, Point> spawns, int islandRadius) {
        double limit = (double) islandRadius * islandRadius;
        TeamColor owner = null;
        double closest = Double.MAX_VALUE;
        for (Map.Entry<TeamColor, Point> entry : spawns.entrySet()) {
            double distance = entry.getValue().distanceSquared(chest);
            if (distance <= limit && distance < closest) {
                closest = distance;
                owner = entry.getKey();
            }
        }
        return Optional.ofNullable(owner);
    }

    public static boolean canOpen(Optional<TeamColor> owner, TeamColor viewer, boolean ownerEliminated) {
        return owner.isEmpty() || owner.get() == viewer || ownerEliminated;
    }
}
