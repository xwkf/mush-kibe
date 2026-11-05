package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import org.bukkit.Material;

final class VictimBody {

    static final byte VISIBLE = 0;
    static final byte INVISIBLE = 0x20;
    static final int FLAGS = 0;

    private VictimBody() {
    }

    static Actor spawn(Stage stage, Scene scene, Vec position, float yaw) {
        return scene.body().map(skin -> stage.player(position, yaw, skin)).orElseGet(() -> stage.player(position, yaw));
    }

    static float towardKiller(Scene scene) {
        return scene.origin().yawTowards(scene.origin().subtract(scene.facing()));
    }

    static void wearHead(Scene scene, Actor stand) {
        scene.skin().ifPresentOrElse(skin -> stand.equipSkull(Slot.HELMET, skin), () -> stand.equip(Slot.HELMET, Material.SKULL_ITEM, 3));
    }
}
