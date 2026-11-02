package br.com.muxe.lobby.launchpad;

import java.util.function.DoubleUnaryOperator;

public final class LaunchVelocity {

    public static final double GRAVITY = 0.08;
    public static final double VERTICAL_DRAG = 0.98;
    public static final double AIR_FRICTION = 0.91;
    public static final double SLIME_SLIPPERINESS = 0.8;
    public static final double MAX_COMPONENT = 3.9;

    private static final int SEARCH_STEPS = 60;
    private static final int MAX_TICKS = 600;

    private LaunchVelocity() {
    }

    public static Launch solve(double distance, double height) {
        if (distance <= 0 || height <= 0) {
            throw new IllegalArgumentException("Launch distance and height must be positive");
        }
        double vertical = search(speed -> simulate(0, speed).peak(), height);
        double horizontal = search(speed -> simulate(speed, vertical).distance(), distance);
        return new Launch(horizontal, vertical);
    }

    public static Flight simulate(double horizontal, double vertical) {
        double distance = 0;
        double height = 0;
        double peak = 0;
        double forward = horizontal;
        double up = vertical;
        for (int tick = 1; tick <= MAX_TICKS; tick++) {
            distance += forward;
            height += up;
            peak = Math.max(peak, height);
            up = (up - GRAVITY) * VERTICAL_DRAG;
            forward *= tick == 1 ? SLIME_SLIPPERINESS * AIR_FRICTION : AIR_FRICTION;
            if (height <= 0) {
                return new Flight(distance, peak, tick);
            }
        }
        return new Flight(distance, peak, MAX_TICKS);
    }

    private static double search(DoubleUnaryOperator reach, double target) {
        double low = 0;
        double high = MAX_COMPONENT;
        for (int step = 0; step < SEARCH_STEPS; step++) {
            double middle = (low + high) / 2;
            if (reach.applyAsDouble(middle) < target) {
                low = middle;
            } else {
                high = middle;
            }
        }
        return high;
    }

    public record Launch(double horizontal, double vertical) {

        public Push towards(float yaw) {
            double radians = Math.toRadians(yaw);
            return new Push(-Math.sin(radians) * horizontal, vertical, Math.cos(radians) * horizontal);
        }
    }

    public record Push(double x, double y, double z) {
    }

    public record Flight(double distance, double peak, int ticks) {
    }
}
