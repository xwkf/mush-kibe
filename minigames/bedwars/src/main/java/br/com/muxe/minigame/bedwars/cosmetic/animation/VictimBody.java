package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SignedSkin;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.core.bukkit.item.SkullTexture;
import java.util.Optional;

final class VictimBody {

    private VictimBody() {
    }

    static Actor spawn(Stage stage, Scene scene, Vec position, float yaw) {
        return skin(scene).map(skin -> stage.player(position, yaw, skin)).orElseGet(() -> stage.player(position, yaw));
    }

    static Optional<SignedSkin> skin(Scene scene) {
        return scene.skin().map(SkullTexture::value).map(value -> new SignedSkin(value, null));
    }

    static float facingKiller(Scene scene) {
        return Vec.ZERO.yawTowards(scene.facing().multiply(-1));
    }
}
