package br.com.muxe.minigame.api.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.api.cosmetic.aura.AuraPattern;
import br.com.muxe.minigame.api.cosmetic.aura.Auras;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AuraAnimationTest {

    private static final double EPSILON = 1.0E-9;

    private final UUID viewer = UUID.randomUUID();
    private final List<Particle> particles = new ArrayList<>();
    private Vec eyes = new Vec(10.5, 20.62, 21.5);
    private final Stage stage = (Stage) Proxy.newProxyInstance(Stage.class.getClassLoader(), new Class<?>[] {Stage.class}, (proxy, method, args) -> {
        if (method.getName().equals("particle")) {
            particles.add(new Particle((String) args[0], (Vec) args[1], (int) args[2], (Vec) args[3], (double) args[4]));
        }
        if (method.getName().equals("eyes")) {
            return Optional.of(eyes);
        }
        return null;
    });

    private Timeline script(String id) {
        Timeline timeline = new Timeline();
        SceneAnimations.auras().find(id).orElseThrow()
            .script(new Scene(new Vec(10.5, 19, 21.5), new Vec(0, 0, 1), Optional.empty(), List.of(viewer)), stage, timeline, new Random(1));
        return timeline;
    }

    @Test
    void itRunsForTenMinutesSoThePreviewTimeoutIsWhatEndsIt() {
        assertThat(script("liga-mestre").length()).isEqualTo(AuraAnimation.TICKS);
    }

    @Test
    void theMasterLeagueSendsOneFlameAtATimeOnTheHelixAroundTheFeet() {
        Timeline timeline = script("liga-mestre");

        timeline.run(0);
        timeline.run(1);

        assertThat(particles).hasSize(2);
        Particle first = particles.get(0);
        assertThat(first.type()).isEqualTo("FLAME");
        assertThat(first.count()).isZero();
        assertThat(first.spread()).isEqualTo(Vec.ZERO);
        assertThat(first.speed()).isEqualTo(1.0);
        Vec feet = new Vec(10.5, 19.0, 21.5);
        assertThat(first.at().x()).isEqualTo(feet.x() + AuraPattern.helix(0).x(), within(EPSILON));
        assertThat(first.at().y()).isEqualTo(feet.y() + AuraPattern.helix(0).y(), within(EPSILON));
        assertThat(first.at().z()).isEqualTo(feet.z() + AuraPattern.helix(0).z(), within(EPSILON));
        assertThat(particles.get(1).at().y()).isEqualTo(feet.y() + AuraPattern.helix(1).y(), within(EPSILON));
    }

    @Test
    void theDiamondLeagueSendsWitchMagic() {
        script("liga-diamante").run(0);

        assertThat(particles.getFirst().type()).isEqualTo(Auras.DIAMOND_LEAGUE.particle().name());
        assertThat(particles.getFirst().type()).isEqualTo("SPELL_WITCH");
    }

    @Test
    void whenTheViewerWalksTheParticleStaysAtTheirFeet() {
        Timeline timeline = script("liga-mestre");
        timeline.run(0);
        eyes = new Vec(11.5, 20.62, 21.5);
        timeline.run(1);
        timeline.run(2);

        assertThat(particles.get(1).at().x()).isEqualTo(11.5, within(EPSILON));
        assertThat(particles.get(1).at().y()).isEqualTo(19.1, within(EPSILON));
        assertThat(particles.get(1).at().z()).isEqualTo(21.5, within(EPSILON));
        assertThat(particles.get(2).at().x()).isEqualTo(11.5 + AuraPattern.helix(2).x(), within(EPSILON));
    }

    private record Particle(String type, Vec at, int count, Vec spread, double speed) {
    }
}
