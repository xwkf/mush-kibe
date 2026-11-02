package br.com.muxe.lobby.launchpad;

import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;

public final class PadParticles implements Runnable {

    public static final double EDGE_MARGIN = 0.1;
    public static final double MAX_RISE = 0.3;

    private final Server server;
    private final PadIndex index;
    private final int perBlock;
    private final double viewRadius;

    public PadParticles(Server server, PadIndex index, int perBlock, double viewRadius) {
        this.server = server;
        this.index = index;
        this.perBlock = perBlock;
        this.viewRadius = viewRadius;
    }

    @Override
    public void run() {
        for (PadSpot spot : index.all()) {
            World world = server.getWorld(spot.world());
            if (world == null || !watched(world, spot)) {
                continue;
            }
            ThreadLocalRandom random = ThreadLocalRandom.current();
            for (int count = 0; count < perBlock; count++) {
                Location point = new Location(world,
                    spot.x() + random.nextDouble(EDGE_MARGIN, 1 - EDGE_MARGIN),
                    spot.y() + 1 + random.nextDouble(0, MAX_RISE),
                    spot.z() + random.nextDouble(EDGE_MARGIN, 1 - EDGE_MARGIN));
                world.spigot().playEffect(point, Effect.HAPPY_VILLAGER, 0, 0, 0f, 0f, 0f, 0f, 1, (int) viewRadius);
            }
        }
    }

    private boolean watched(World world, PadSpot spot) {
        for (Player player : world.getPlayers()) {
            Location at = player.getLocation();
            if (spot.within(world.getName(), at.getX(), at.getY(), at.getZ(), viewRadius)) {
                return true;
            }
        }
        return false;
    }
}
