package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class SceneAnimations {

    private final Map<String, SceneAnimation> byId = new LinkedHashMap<>();

    public SceneAnimations(List<SceneAnimation> animations) {
        animations.forEach(animation -> byId.put(animation.id(), animation));
    }

    public static SceneAnimations victories() {
        return new SceneAnimations(List.of(new ChristmasGiftsVictory()));
    }

    public static SceneAnimations finalKills() {
        return new SceneAnimations(List.of(new GiftedFinalKill()));
    }

    public Optional<SceneAnimation> find(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    public List<SceneAnimation> all() {
        return List.copyOf(byId.values());
    }
}
