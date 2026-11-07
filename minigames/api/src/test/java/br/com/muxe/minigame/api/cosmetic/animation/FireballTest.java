package br.com.muxe.minigame.api.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class FireballTest {

    private static final double PRECISION = 1e-9;

    @Test
    void itLeavesAtTwoBlocksPerTickTowardsTheTarget() {
        Fireball fireball = new Fireball(new Vec(0, 10, 0), new Vec(30, 10, 40), new Vec(0, 0, 1));

        assertThat(fireball.motion().length()).isCloseTo(Fireball.SPEED, within(PRECISION));
        assertThat(fireball.motion().x()).isCloseTo(1.2, within(PRECISION));
        assertThat(fireball.motion().z()).isCloseTo(1.6, within(PRECISION));
    }

    @Test
    void eachTickItMovesThenBendsTowardsItsHeadingAndSlowsDown() {
        Fireball fireball = new Fireball(new Vec(0, 100, 0), new Vec(0, 100, 50), new Vec(1, 0, 0));

        Optional<Fireball.Impact> impact = fireball.step((x, y, z) -> false, Map.of());

        assertThat(impact).isEmpty();
        assertThat(fireball.position().z()).isCloseTo(2, within(PRECISION));
        assertThat(fireball.motion().x()).isCloseTo(0.1 * 0.95, within(PRECISION));
        assertThat(fireball.motion().z()).isCloseTo(2 * 0.95, within(PRECISION));
    }

    @Test
    void itHitsARiderOnItsPathAndExplodesWhereItWas() {
        UUID rider = UUID.randomUUID();
        Fireball fireball = new Fireball(new Vec(0, 11.6, 0), new Vec(0, 11.6, 10), new Vec(0, 0, 1));

        Optional<Fireball.Impact> impact = fireball.step((x, y, z) -> false, Map.of(rider, new Vec(0.2, 10, 1.8)));

        assertThat(impact).isPresent();
        assertThat(impact.get().rider()).contains(rider);
        assertThat(impact.get().at()).isEqualTo(new Vec(0, 11.6, 0));
    }

    @Test
    void itStopsAtTheFirstSolidBlockOnItsPath() {
        Fireball fireball = new Fireball(new Vec(0.5, 10.5, 0.5), new Vec(0.5, 10.5, 10.5), new Vec(0, 0, 1));

        Optional<Fireball.Impact> impact = fireball.step((x, y, z) -> z == 2, Map.of());

        assertThat(impact).isPresent();
        assertThat(impact.get().rider()).isEmpty();
    }

    @Test
    void itMissesRidersOutsideTheirBody() {
        assertThat(Fireball.touches(new Vec(0.5, 11, 0), new Vec(0, 10, 0))).isTrue();
        assertThat(Fireball.touches(new Vec(0.7, 11, 0), new Vec(0, 10, 0))).isFalse();
        assertThat(Fireball.touches(new Vec(0, 12.2, 0), new Vec(0, 10, 0))).isFalse();
        assertThat(Fireball.touches(new Vec(0, 9.6, 0), new Vec(0, 10, 0))).isFalse();
    }
}
