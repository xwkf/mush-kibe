package br.com.muxe.minigame.bedwars.cosmetic.trail.effect;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

final class CapturedParticles {

    final List<Particle> particles = new ArrayList<>();
    final List<String> otherCalls = new ArrayList<>();
    final Stage stage = (Stage) Proxy.newProxyInstance(Stage.class.getClassLoader(), new Class<?>[] {Stage.class}, (proxy, method, args) -> {
        if (method.getName().equals("particle")) {
            particles.add(new Particle((String) args[0], (Vec) args[1], (int) args[2], (Vec) args[3], (double) args[4]));
        } else {
            otherCalls.add(method.getName());
        }
        return null;
    });

    record Particle(String type, Vec position, int count, Vec spread, double speed) {
    }
}
