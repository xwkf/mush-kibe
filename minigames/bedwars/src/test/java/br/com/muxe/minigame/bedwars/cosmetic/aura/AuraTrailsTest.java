package br.com.muxe.minigame.bedwars.cosmetic.aura;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AuraTrailsTest {

    private static final Vec HERE = new Vec(10, 64, 20);

    private final UUID owner = UUID.randomUUID();
    private final AuraTrails trails = new AuraTrails();
    private final List<Spawn> spawns = new ArrayList<>();
    private final AuraSink sink = (particle, at) -> spawns.add(new Spawn(particle, at));

    @Test
    void aPlayerStandingStillGetsOneHelixPointPerTick() {
        for (int tick = 0; tick < 3; tick++) {
            trails.emit(owner, AuraParticle.FLAME, HERE, sink);
        }

        assertThat(spawns).hasSize(3);
        for (int counter = 0; counter < 3; counter++) {
            assertThat(spawns.get(counter).at()).isEqualTo(HERE.add(AuraPattern.helix(counter)));
            assertThat(spawns.get(counter).particle()).isEqualTo(AuraParticle.FLAME);
        }
    }

    @Test
    void theFirstTickNeverCountsAsMovement() {
        trails.emit(owner, AuraParticle.SPELL_WITCH, HERE, sink);

        assertThat(spawns.getFirst().at()).isEqualTo(HERE.add(AuraPattern.helix(0)));
        assertThat(spawns.getFirst().particle()).isEqualTo(AuraParticle.SPELL_WITCH);
    }

    @Test
    void aPlayerWhoMovedGetsOneParticleAtTheFeet() {
        trails.emit(owner, AuraParticle.FLAME, HERE, sink);
        Vec stepped = HERE.add(0.3, 0, 0);
        trails.emit(owner, AuraParticle.FLAME, stepped, sink);

        assertThat(spawns).hasSize(2);
        assertThat(spawns.get(1).at()).isEqualTo(AuraPattern.feet(stepped));
    }

    @Test
    void theCounterKeepsRunningWhileThePlayerMovesSoTheHelixResumesWhereItWouldBe() {
        trails.emit(owner, AuraParticle.FLAME, HERE, sink);
        trails.emit(owner, AuraParticle.FLAME, HERE.add(0.3, 0, 0), sink);
        trails.emit(owner, AuraParticle.FLAME, HERE.add(0.6, 0, 0), sink);
        Vec stopped = HERE.add(0.6, 0, 0);
        trails.emit(owner, AuraParticle.FLAME, stopped, sink);

        assertThat(spawns.get(3).at()).isEqualTo(stopped.add(AuraPattern.helix(3)));
    }

    @Test
    void jumpingOrFallingWithoutMovingSidewaysKeepsTheHelix() {
        trails.emit(owner, AuraParticle.FLAME, HERE, sink);
        Vec jumped = HERE.add(0, 1, 0);
        trails.emit(owner, AuraParticle.FLAME, jumped, sink);

        assertThat(spawns.get(1).at()).isEqualTo(jumped.add(AuraPattern.helix(1)));
    }

    @Test
    void eachPlayerHasHisOwnCounter() {
        UUID other = UUID.randomUUID();
        trails.emit(owner, AuraParticle.FLAME, HERE, sink);
        trails.emit(owner, AuraParticle.FLAME, HERE, sink);
        trails.emit(other, AuraParticle.FLAME, HERE, sink);

        assertThat(spawns.get(2).at()).isEqualTo(HERE.add(AuraPattern.helix(0)));
    }

    @Test
    void aPlayerWhoStopsShowingStartsAgainFromTheBeginning() {
        trails.emit(owner, AuraParticle.FLAME, HERE, sink);
        trails.emit(owner, AuraParticle.FLAME, HERE, sink);
        trails.retain(Set.of());
        trails.emit(owner, AuraParticle.FLAME, HERE, sink);

        assertThat(spawns.get(2).at()).isEqualTo(HERE.add(AuraPattern.helix(0)));
    }

    @Test
    void aPlayerKeptByRetainKeepsTheCounter() {
        trails.emit(owner, AuraParticle.FLAME, HERE, sink);
        trails.retain(Set.of(owner));
        trails.emit(owner, AuraParticle.FLAME, HERE, sink);

        assertThat(spawns.get(1).at()).isEqualTo(HERE.add(AuraPattern.helix(1)));
    }

    private record Spawn(AuraParticle particle, Vec at) {
    }
}
