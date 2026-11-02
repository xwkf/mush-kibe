package br.com.muxe.core.bukkit.npc;

import br.com.muxe.core.bukkit.hologram.HologramPixel;
import java.util.List;
import java.util.Objects;
import org.bukkit.Location;

final class NpcLabels {

    private NpcLabels() {
    }

    static boolean sameSpots(List<HologramPixel> before, List<HologramPixel> after) {
        if (before.size() != after.size()) {
            return false;
        }
        for (int index = 0; index < before.size(); index++) {
            Location a = before.get(index).location();
            Location b = after.get(index).location();
            if (!Objects.equals(a.getWorld(), b.getWorld()) || a.getX() != b.getX() || a.getY() != b.getY() || a.getZ() != b.getZ()) {
                return false;
            }
        }
        return true;
    }
}
