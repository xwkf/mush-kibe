package br.com.muxe.minigame.bedwars.cosmetic.trail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import org.junit.jupiter.api.Test;

class FlightTest {

    @Test
    void aFlightKeepsWhereTheProjectileIsAndHowFastItGoes() {
        Flight flight = new Flight(new Vec(1, 2, 3), new Vec(0, 0.5, 1), 4, ProjectileKind.ARROW);

        assertThat(flight.position()).isEqualTo(new Vec(1, 2, 3));
        assertThat(flight.velocity()).isEqualTo(new Vec(0, 0.5, 1));
        assertThat(flight.age()).isEqualTo(4);
        assertThat(flight.kind()).isEqualTo(ProjectileKind.ARROW);
    }

    @Test
    void aFlightCannotHaveANegativeAge() {
        assertThatThrownBy(() -> new Flight(Vec.ZERO, Vec.ZERO, -1, ProjectileKind.SNOWBALL)).isInstanceOf(IllegalArgumentException.class);
    }
}
