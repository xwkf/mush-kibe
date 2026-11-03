package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class FlightTest {

    @Test
    void theLookDirectionPointsWhereThePlayerFacesAndIsAUnitVector() {
        assertThat(Flight.direction(0, 0).z()).isCloseTo(1, offset(1e-9));
        assertThat(Flight.direction(90, 0).x()).isCloseTo(-1, offset(1e-9));
        assertThat(Flight.direction(0, -30).y()).isCloseTo(0.5, offset(1e-9));
        assertThat(Flight.direction(0, 90).y()).isCloseTo(-1, offset(1e-9));
        assertThat(Flight.direction(37, -22).length()).isCloseTo(1, offset(1e-9));
    }

    @Test
    void holdingForwardRampsTheSpeedUpAsOneMinusZeroPointSevenToTheN() {
        Flight flight = new Flight(Vec.ZERO, 0);

        for (int tick = 1; tick <= 12; tick++) {
            flight.step(true, 0, 0);
            assertThat(flight.velocity().z()).isCloseTo(1 - Math.pow(0.7, tick), offset(1e-9));
            assertThat(flight.velocity().x()).isCloseTo(0, offset(1e-9));
        }
    }

    @Test
    void releasingForwardShrinksTheHorizontalSpeedBySeventyPercentEachTick() {
        Flight flight = new Flight(Vec.ZERO, 0);
        for (int tick = 0; tick < 80; tick++) {
            flight.step(true, 0, 0);
        }

        double speed = flight.velocity().z();
        assertThat(speed).isCloseTo(1, offset(1e-6));
        for (int tick = 0; tick < 8; tick++) {
            flight.step(false, 0, 0);
            speed *= 0.7;
            assertThat(flight.velocity().z()).isCloseTo(speed, offset(1e-9));
        }
    }

    @Test
    void theFlightStartsRisingSlowlyAndNeverMovesSidewaysWhenIdle() {
        Flight flight = new Flight(new Vec(5, 30, 7), 0);

        assertThat(flight.velocity()).isEqualTo(new Vec(0, 0.1, 0));
        for (int tick = 0; tick < 300; tick++) {
            flight.step(false, 123, 45);
        }
        assertThat(flight.position().x()).isEqualTo(5);
        assertThat(flight.position().z()).isEqualTo(7);
    }

    @Test
    void theBobIsAThirtyOneTickCosineWithATenthOfABlockPerTickPeak() {
        assertThat(Flight.bob(-1)).isCloseTo(0.1, offset(1e-12));
        assertThat(Flight.bob(0)).isCloseTo(0.1 * Math.cos(0.2), offset(1e-12));
        double period = 2 * Math.PI / Flight.BOB_RATE;
        assertThat(period).isBetween(31.3, 31.5);
        assertThat(Flight.bob(10)).isCloseTo(Flight.bob(10 + (int) Math.round(period)), offset(0.01));
    }

    @Test
    void idleTheFlightBobsAboutAHalfBlockEachWayEveryThirtyOneTicks() {
        Flight flight = new Flight(Vec.ZERO, 0);
        List<Double> heights = new ArrayList<>();
        for (int tick = 0; tick < 400; tick++) {
            flight.step(false, 0, 0);
            heights.add(flight.position().y());
        }

        List<Integer> peaks = new ArrayList<>();
        for (int index = 1; index < heights.size() - 1; index++) {
            if (heights.get(index) > heights.get(index - 1) && heights.get(index) >= heights.get(index + 1)) {
                peaks.add(index);
            }
        }
        assertThat(peaks.size()).isGreaterThanOrEqualTo(10);
        for (int index = 1; index < peaks.size(); index++) {
            assertThat(peaks.get(index) - peaks.get(index - 1)).isBetween(30, 33);
        }
        List<Double> settled = heights.subList(100, heights.size());
        double spread = settled.stream().mapToDouble(Double::doubleValue).max().orElseThrow() - settled.stream().mapToDouble(Double::doubleValue).min().orElseThrow();
        assertThat(spread).isBetween(0.8, 1.1);
    }

    @Test
    void pitchingUpWhileHoldingForwardClimbs() {
        Flight flight = new Flight(Vec.ZERO, 0);

        for (int tick = 0; tick < 40; tick++) {
            flight.step(true, 0, -30);
        }

        assertThat(flight.velocity().y()).isGreaterThan(0.35);
        assertThat(flight.position().y()).isGreaterThan(10);
    }

    @Test
    void turningDriftsTheHeadingBehindTheLook() {
        Flight flight = new Flight(Vec.ZERO, 0);
        for (int tick = 0; tick < 40; tick++) {
            flight.step(true, 0, 0);
        }

        flight.step(true, 90, 0);

        assertThat(flight.velocity().z()).isCloseTo(0.7, offset(1e-3));
        assertThat(flight.velocity().x()).isBetween(-0.4, -0.2);
    }

    @Test
    void theModelYawEasesTwentyPercentOfTheWayToTheRiderYaw() {
        assertThat(Flight.easeYaw(0, 90)).isCloseTo(18, offset(1e-9));
        assertThat(Flight.easeYaw(18, 90)).isCloseTo(32.4, offset(1e-9));
    }

    @Test
    void theModelYawTurnsTheShortWayAcrossTheHalfTurn() {
        assertThat(Flight.easeYaw(170, -170)).isCloseTo(174, offset(1e-9));
        assertThat(Flight.easeYaw(-170, 170)).isCloseTo(-174, offset(1e-9));
        assertThat(Flight.wrap(350)).isCloseTo(-10, offset(1e-9));
        assertThat(Flight.wrap(-190)).isCloseTo(170, offset(1e-9));
    }

    @Test
    void theFlightYawConvergesOnTheRiderYaw() {
        Flight flight = new Flight(Vec.ZERO, 0);

        for (int tick = 0; tick < 60; tick++) {
            flight.step(false, 90, 0);
        }

        assertThat(flight.yaw()).isCloseTo(90f, offset(0.1f));
        assertThat(flight.ticks()).isEqualTo(60);
    }
}
