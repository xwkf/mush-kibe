package br.com.muxe.minigame.api.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.BedFrame;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.StandStyle;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import org.bukkit.Material;

public final class HeadLantern {

    public static final double DROP = -0.156;

    private HeadLantern() {
    }

    public static Actor place(Stage stage, BedFrame bed, Vec offset) {
        Actor lantern = stage.armorStand(at(bed, offset), yaw(bed), StandStyle.HIDDEN_SMALL);
        lantern.equip(Slot.HELMET, Material.JACK_O_LANTERN, 0);
        return lantern;
    }

    public static void move(Actor lantern, BedFrame bed, Vec offset) {
        lantern.teleport(at(bed, offset), yaw(bed), 0);
    }

    public static Vec at(BedFrame bed, Vec offset) {
        return bed.head().add(0, DROP, 0).add(offset);
    }

    private static float yaw(BedFrame bed) {
        return bed.yaw() + 180;
    }
}
