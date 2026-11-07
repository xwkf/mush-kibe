package br.com.muxe.minigame.api.cosmetic.trail;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;

public interface FlightSource {

    Vec position();

    Vec velocity();

    boolean valid();

    boolean onGround();
}
