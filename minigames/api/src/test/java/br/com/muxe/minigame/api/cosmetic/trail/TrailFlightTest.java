package br.com.muxe.minigame.api.cosmetic.trail;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class TrailFlightTest {

    private final FakeSource source = new FakeSource();
    private final AtomicInteger closes = new AtomicInteger();

    private TrailFlight flight(FakeTrail trail, ProjectileKind kind) {
        return new TrailFlight(trail, kind, source, FakeTrail.SILENT_STAGE, closes::incrementAndGet, new Random(1));
    }

    @Test
    void everyTickInTheAirGetsTheNextAgeStartingAtZeroAndWhereTheProjectileIsNow() {
        FakeTrail trail = new FakeTrail("rastro", 0);
        TrailFlight flight = flight(trail, ProjectileKind.SNOWBALL);

        for (int tick = 0; tick < 4; tick++) {
            assertThat(flight.advance()).isTrue();
            source.moveOn();
        }

        assertThat(trail.ticks).extracting(Flight::age).containsExactly(0, 1, 2, 3);
        assertThat(trail.ticks).extracting(Flight::position).containsExactly(Vec.ZERO, new Vec(0, 0, 1), new Vec(0, 0, 2), new Vec(0, 0, 3));
        assertThat(trail.ticks).allMatch(seen -> seen.kind() == ProjectileKind.SNOWBALL && seen.velocity().equals(new Vec(0, 0, 1)));
        assertThat(trail.landings).isEmpty();
        assertThat(flight.age()).isEqualTo(4);
        assertThat(closes).hasValue(0);
    }

    @Test
    void whenTheProjectileIsGoneTheTrailLandsOnceWhereItWasAndStaysOpenForItsLinger() {
        FakeTrail trail = new FakeTrail("rastro", 3);
        TrailFlight flight = flight(trail, ProjectileKind.ENDER_PEARL);
        flight.advance();
        flight.advance();
        source.position = new Vec(4, 5, 6);
        source.valid = false;

        assertThat(flight.advance()).isTrue();
        assertThat(trail.landings).containsExactly(new Flight(new Vec(4, 5, 6), new Vec(0, 0, 1), 2, ProjectileKind.ENDER_PEARL));
        assertThat(flight.flying()).isFalse();
        assertThat(flight.advance()).isTrue();
        assertThat(flight.advance()).isTrue();
        assertThat(closes).hasValue(0);
        assertThat(flight.advance()).isFalse();
        assertThat(closes).hasValue(1);
        assertThat(flight.advance()).isFalse();

        assertThat(trail.ticks).hasSize(2);
        assertThat(trail.landings).hasSize(1);
        assertThat(closes).hasValue(1);
    }

    @Test
    void aTrailWithoutLingerClosesOnTheTickItLands() {
        FakeTrail trail = new FakeTrail("rastro", 0);
        TrailFlight flight = flight(trail, ProjectileKind.POTION);
        source.valid = false;

        assertThat(flight.advance()).isFalse();
        assertThat(trail.ticks).isEmpty();
        assertThat(trail.landings).extracting(Flight::age).containsExactly(0);
        assertThat(flight.closed()).isTrue();
        assertThat(closes).hasValue(1);
    }

    @Test
    void anArrowStuckInTheGroundOrAHookLyingStillLandsWhileTheEntityIsStillThere() {
        FakeTrail arrowTrail = new FakeTrail("rastro", 0);
        TrailFlight arrow = flight(arrowTrail, ProjectileKind.ARROW);
        arrow.advance();
        source.onGround = true;
        arrow.advance();

        assertThat(arrowTrail.ticks).hasSize(1);
        assertThat(arrowTrail.landings).hasSize(1);
        assertThat(arrow.closed()).isTrue();

        FakeSource hookSource = new FakeSource();
        FakeTrail hookTrail = new FakeTrail("rastro", 0);
        TrailFlight hook = new TrailFlight(hookTrail, ProjectileKind.FISHING_HOOK, hookSource, FakeTrail.SILENT_STAGE, () -> {
        }, new Random(2));
        hookSource.velocity = Vec.ZERO;
        for (int tick = 0; tick < 10; tick++) {
            hook.advance();
        }
        hookSource.onGround = true;
        hook.advance();

        assertThat(hookTrail.ticks).hasSize(10);
        assertThat(hookTrail.landings).hasSize(1);
    }

    @Test
    void aFlightThatNeverEndsLandsAfterTheMaximumAge() {
        FakeTrail trail = new FakeTrail("rastro", 0);
        TrailFlight flight = flight(trail, ProjectileKind.FISHING_HOOK);

        for (int tick = 0; tick < TrailFlight.MAX_AGE; tick++) {
            flight.advance();
        }
        assertThat(trail.landings).isEmpty();
        flight.advance();

        assertThat(trail.ticks).hasSize(TrailFlight.MAX_AGE);
        assertThat(trail.landings).extracting(Flight::age).containsExactly(TrailFlight.MAX_AGE);
        assertThat(flight.closed()).isTrue();
    }

    @Test
    void closingFromOutsideRemovesTheStageOnceWithoutLanding() {
        FakeTrail trail = new FakeTrail("rastro", 5);
        TrailFlight flight = flight(trail, ProjectileKind.ARROW);
        flight.advance();

        flight.close();
        flight.close();

        assertThat(flight.advance()).isFalse();
        assertThat(trail.ticks).hasSize(1);
        assertThat(trail.landings).isEmpty();
        assertThat(closes).hasValue(1);
    }
}
