package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.List;

final class Rainbow {

    static final List<Band> BANDS = List.of(
        new Band(-0.6, 1, 0, 0),
        new Band(-0.4, 1, 0.5, 0),
        new Band(-0.2, 1, 1, 0),
        new Band(0, 0, 1, 0),
        new Band(0.2, 0, 0, 1),
        new Band(0.4, 0.29, 0, 0.51),
        new Band(0.6, 0.58, 0, 0.83)
    );
    static final double SPACING = 0.2;
    static final double QUARTER = Math.PI / 2;
    private static final double SLACK = 1e-9;

    private final Vec foot;
    private final Vec along;
    private final double span;

    Rainbow(Vec foot, Vec along, double span) {
        this.foot = foot;
        this.along = along.normalize();
        this.span = span;
    }

    List<Vec> band(Band band, double fraction) {
        double radius = span - band.offset();
        Vec centre = foot.add(along.multiply(span));
        double limit = QUARTER * Math.clamp(fraction, 0, 1) + SLACK;
        List<Vec> points = new ArrayList<>();
        for (int step = 0; step * SPACING / radius <= limit; step++) {
            double angle = step * SPACING / radius;
            points.add(centre.add(along.multiply(-radius * Math.cos(angle))).add(0, radius * Math.sin(angle), 0));
        }
        return points;
    }

    void draw(Stage stage, double fraction) {
        for (Band band : BANDS) {
            for (Vec point : band(band, fraction)) {
                Kit.dust(stage, point, band.red(), band.green(), band.blue());
            }
        }
    }

    record Band(double offset, double red, double green, double blue) {
    }
}
