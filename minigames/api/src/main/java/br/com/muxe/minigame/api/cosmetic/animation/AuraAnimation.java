package br.com.muxe.minigame.api.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.api.cosmetic.aura.Aura;
import br.com.muxe.minigame.api.cosmetic.aura.AuraSink;
import br.com.muxe.minigame.api.cosmetic.aura.AuraTrails;
import java.util.UUID;
import java.util.random.RandomGenerator;

public final class AuraAnimation implements SceneAnimation {

    public static final int TICKS = 12_000;
    public static final double EYE_HEIGHT = 1.62;
    private static final double SPEED = 1.0;

    private final Aura aura;

    public AuraAnimation(Aura aura) {
        this.aura = aura;
    }

    @Override
    public String id() {
        return aura.id();
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        AuraTrails trails = new AuraTrails();
        AuraSink sink = (particle, at) -> stage.particle(particle.name(), at, 0, Vec.ZERO, SPEED);
        UUID owner = scene.riders().isEmpty() ? UUID.randomUUID() : scene.riders().getFirst();
        timeline.repeat(0, TICKS, 1, tick -> {
            Vec feet = stage.eyes(owner).map(eyes -> eyes.add(0, -EYE_HEIGHT, 0)).orElse(scene.origin());
            trails.emit(owner, aura.particle(), feet, sink);
        });
    }
}
