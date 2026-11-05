package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SignedSkin;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.core.bukkit.item.SkullTexture;
import java.util.Optional;
import org.bukkit.Material;

final class VictimBody {

    static final byte VISIBLE = 0;
    static final byte INVISIBLE = 0x20;
    static final int FLAGS = 0;

    private VictimBody() {
    }

    static Actor spawn(Stage stage, Scene scene, Vec position, float yaw) {
        return skin(scene).map(skin -> stage.player(position, yaw, skin)).orElseGet(() -> stage.player(position, yaw));
    }

    static Optional<SignedSkin> skin(Scene scene) {
        return scene.body().or(() -> scene.skin().map(SkullTexture::value).map(value -> new SignedSkin(value, null)));
    }

    static float facingKiller(Scene scene) {
        return Vec.ZERO.yawTowards(scene.facing().multiply(-1));
    }

    static void wearHead(Scene scene, Actor stand) {
        scene.skin().ifPresentOrElse(skin -> stand.equipSkull(Slot.HELMET, skin), () -> stand.equip(Slot.HELMET, Material.SKULL_ITEM, 3));
    }
}
