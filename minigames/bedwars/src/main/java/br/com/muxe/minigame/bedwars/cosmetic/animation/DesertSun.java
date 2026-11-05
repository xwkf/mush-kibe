package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class DesertSun {

    static final double SPACING = 0.09;
    static final int RINGS = 10;
    static final double RADIUS = SPACING * RINGS;
    static final int RAYS = 12;
    static final double RAY_FROM = 1.0;
    static final double LONG_RAY = 1.65;
    static final double SHORT_RAY = 1.4;
    static final double RAY_STEP = 0.08;
    static final double FILL = 0.045;
    static final double LENS_U = 0.2;
    static final double LENS_V = 0.12;
    static final double LENS_WIDTH = 0.16;
    static final double LENS_HEIGHT = 0.11;
    static final double BRIDGE_HALF = 0.06;
    static final double BRIDGE_V = 0.17;
    static final double BRIDGE_THICKNESS = 0.03;
    static final double SMILE_V = 0.02;
    static final double SMILE_INNER = 0.3;
    static final double SMILE_OUTER = 0.36;
    static final double SMILE_FROM = 210;
    static final double SMILE_UNTIL = 330;
    static final double CHEEK_U = 0.42;
    static final double CHEEK_V = -0.08;
    static final double CHEEK_RADIUS = 0.07;
    static final double SHINE_RADIUS = 0.035;
    static final double SHINE_SHIFT = 0.05;
    static final Dot.Tint CENTER = new Dot.Tint(1, 0.95, 0.55);
    static final Dot.Tint EDGE = new Dot.Tint(1, 0.67, 0.05);
    static final Dot.Tint RIM = new Dot.Tint(1, 0.6, 0);
    static final Dot.Tint GLASSES = new Dot.Tint(0.08, 0.08, 0.08);
    static final Dot.Tint SHINE = new Dot.Tint(1, 1, 1);
    static final Dot.Tint SMILE = new Dot.Tint(0.75, 0.22, 0.17);
    static final Dot.Tint CHEEK = new Dot.Tint(1, 0.75, 0.88);
    static final Dot.Tint LONG_RAY_TINT = new Dot.Tint(1, 0.82, 0);
    static final Dot.Tint SHORT_RAY_TINT = new Dot.Tint(1, 0.55, 0);
    static final List<Dot> FACE = face();

    private DesertSun() {
    }

    static void draw(Stage stage, Vec center, Vec right, double turn) {
        FACE.forEach(dot -> dot.show(stage, center, right));
        rays(turn).forEach(dot -> dot.show(stage, center, right));
    }

    static List<Dot> rays(double turn) {
        List<Dot> dots = new ArrayList<>();
        for (int ray = 0; ray < RAYS; ray++) {
            double radians = Math.toRadians(turn + ray * 360.0 / RAYS);
            boolean longRay = ray % 2 == 0;
            double length = longRay ? LONG_RAY : SHORT_RAY;
            for (double radius = RAY_FROM; radius <= length + 1e-9; radius += RAY_STEP) {
                dots.add(new Dot(Math.cos(radians) * radius, Math.sin(radians) * radius, longRay ? LONG_RAY_TINT : SHORT_RAY_TINT));
            }
        }
        return dots;
    }

    static List<Dot> face() {
        List<Dot> dots = new ArrayList<>();
        dots.add(new Dot(0, 0, CENTER));
        for (int ring = 1; ring <= RINGS; ring++) {
            double radius = ring * SPACING;
            int points = (int) Math.round(2 * Math.PI * radius / SPACING);
            for (int point = 0; point < points; point++) {
                double radians = 2 * Math.PI * point / points;
                double u = Math.cos(radians) * radius;
                double v = Math.sin(radians) * radius;
                if (!feature(u, v)) {
                    dots.add(new Dot(u, v, ring == RINGS ? RIM : CENTER.towards(EDGE, (double) ring / RINGS)));
                }
            }
        }
        int cells = (int) Math.round(RADIUS / FILL);
        for (int column = -cells; column <= cells; column++) {
            for (int row = -cells; row <= cells; row++) {
                double u = column * FILL;
                double v = row * FILL;
                featureTint(u, v).ifPresent(tint -> dots.add(new Dot(u, v, tint)));
            }
        }
        return List.copyOf(dots);
    }

    static boolean feature(double u, double v) {
        return featureTint(u, v).isPresent();
    }

    static Optional<Dot.Tint> featureTint(double u, double v) {
        for (int side = -1; side <= 1; side += 2) {
            double lensU = side * LENS_U;
            if (Math.hypot(u - lensU + side * SHINE_SHIFT, v - LENS_V - SHINE_SHIFT) <= SHINE_RADIUS) {
                return Optional.of(SHINE);
            }
            double across = (u - lensU) / LENS_WIDTH;
            double down = (v - LENS_V) / LENS_HEIGHT;
            if (across * across + down * down <= 1) {
                return Optional.of(GLASSES);
            }
            if (Math.hypot(u - side * CHEEK_U, v - CHEEK_V) <= CHEEK_RADIUS) {
                return Optional.of(CHEEK);
            }
        }
        if (Math.abs(u) <= BRIDGE_HALF && Math.abs(v - BRIDGE_V) <= BRIDGE_THICKNESS) {
            return Optional.of(GLASSES);
        }
        double smile = Math.hypot(u, v - SMILE_V);
        double degrees = (Math.toDegrees(Math.atan2(v - SMILE_V, u)) + 360) % 360;
        if (smile >= SMILE_INNER && smile <= SMILE_OUTER && degrees >= SMILE_FROM && degrees <= SMILE_UNTIL) {
            return Optional.of(SMILE);
        }
        return Optional.empty();
    }

    record Dot(double u, double v, Tint tint) {

        void show(Stage stage, Vec center, Vec right) {
            Kit.dust(stage, center.add(right.multiply(u)).add(0, v, 0), tint.red(), tint.green(), tint.blue());
        }

        record Tint(double red, double green, double blue) {

            Tint towards(Tint other, double progress) {
                return new Tint(red + (other.red - red) * progress, green + (other.green - green) * progress, blue + (other.blue - blue) * progress);
            }
        }
    }
}
