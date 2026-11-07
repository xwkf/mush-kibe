package br.com.muxe.minigame.api.cosmetic.trail.effect;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.api.cosmetic.trail.Flight;
import br.com.muxe.minigame.api.cosmetic.trail.ProjectileTrail;
import java.util.random.RandomGenerator;

public final class MusicalNotesTrail implements ProjectileTrail {

    public static final String ID = "notas-musicais";
    static final int EVERY = 2;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public void tick(Flight flight, Stage stage, RandomGenerator random) {
        if (flight.age() % EVERY != 0) {
            return;
        }
        Vec spread = new Vec(random.nextDouble(), random.nextDouble(), random.nextDouble());
        stage.particle("NOTE", flight.position(), 1, spread, flight.age() / EVERY);
    }
}
