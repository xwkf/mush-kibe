package br.com.muxe.core.bukkit.menu;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class MenuAnimator implements Runnable {

    @Override
    public void run() {
        Set<MenuHolder> open = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof MenuHolder holder) {
                open.add(holder);
            }
        }
        open.forEach(MenuHolder::tick);
    }
}
