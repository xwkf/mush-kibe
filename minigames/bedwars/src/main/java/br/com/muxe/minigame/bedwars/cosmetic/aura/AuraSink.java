package br.com.muxe.minigame.bedwars.cosmetic.aura;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;

@FunctionalInterface
public interface AuraSink {

    void spawn(AuraParticle particle, Vec at);
}
