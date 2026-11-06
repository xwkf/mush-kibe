package br.com.muxe.minigame.bedwars.cosmetic.trail;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class TrailFlightsTest {

    private final List<String> failures = new ArrayList<>();
    private final TrailFlights flights = new TrailFlights((trail, exception) -> failures.add(trail + ": " + exception.getMessage()));
    private final UUID shooter = UUID.randomUUID();
    private final UUID other = UUID.randomUUID();
    private final AtomicInteger closes = new AtomicInteger();

    private TrailFlight flight(FakeTrail trail, FakeSource source) {
        return new TrailFlight(trail, ProjectileKind.ARROW, source, FakeTrail.SILENT_STAGE, closes::incrementAndGet, new Random(3));
    }

    @Test
    void finishedFlightsAreDroppedAndTheOthersKeepFlying() {
        FakeSource landing = new FakeSource();
        FakeSource flying = new FakeSource();
        flights.add(shooter, "AB12", flight(new FakeTrail("a", 0), landing));
        flights.add(other, "AB12", flight(new FakeTrail("b", 0), flying));
        flights.tick((owner, room) -> true);
        landing.valid = false;

        flights.tick((owner, room) -> true);

        assertThat(flights.size()).isEqualTo(1);
        assertThat(closes).hasValue(1);
    }

    @Test
    void aRoomThatIsNoLongerPlayingClosesItsFlightsWithoutLanding() {
        FakeTrail trail = new FakeTrail("a", 5);
        flights.add(shooter, "AB12", flight(trail, new FakeSource()));
        flights.add(other, "CD34", flight(new FakeTrail("b", 0), new FakeSource()));

        flights.tick((owner, room) -> !room.equals("AB12"));

        assertThat(trail.ticks).isEmpty();
        assertThat(trail.landings).isEmpty();
        assertThat(flights.size()).isEqualTo(1);
        assertThat(closes).hasValue(1);
    }

    @Test
    void aPlayerWhoQuitsLosesEveryFlightOfTheirs() {
        flights.add(shooter, "AB12", flight(new FakeTrail("a", 0), new FakeSource()));
        flights.add(shooter, "AB12", flight(new FakeTrail("a", 0), new FakeSource()));
        flights.add(other, "AB12", flight(new FakeTrail("b", 0), new FakeSource()));

        flights.stop(shooter);

        assertThat(flights.size()).isEqualTo(1);
        assertThat(closes).hasValue(2);
        flights.stopAll();
        assertThat(flights.isEmpty()).isTrue();
        assertThat(closes).hasValue(3);
    }

    @Test
    void aTrailThatThrowsIsClosedAndReportedWithoutStoppingTheOthers() {
        FakeTrail healthy = new FakeTrail("b", 0);
        flights.add(shooter, "AB12", flight(new FakeTrail("a", 0).failingWith(new IllegalStateException("boom")), new FakeSource()));
        flights.add(other, "AB12", flight(healthy, new FakeSource()));

        flights.tick((owner, room) -> true);

        assertThat(failures).containsExactly("a: boom");
        assertThat(healthy.ticks).hasSize(1);
        assertThat(flights.size()).isEqualTo(1);
        assertThat(closes).hasValue(1);
    }
}
