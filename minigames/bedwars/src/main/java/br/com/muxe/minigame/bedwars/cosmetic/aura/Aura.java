package br.com.muxe.minigame.bedwars.cosmetic.aura;

import java.util.Objects;

public record Aura(String id, AuraParticle particle) {

    public Aura {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(particle, "particle");
    }
}
