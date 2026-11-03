package br.com.muxe.core.bukkit.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.api.Test;

class AreaTest {

    @Test
    void pointsAreDrawnAllOverTheArea() {
        Area area = new Area(-50, 10, 50, 110);
        Random random = new Random(1);
        double minX = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        for (int draw = 0; draw < 2000; draw++) {
            Vec point = area.pick(random, 64);
            assertThat(point.x()).isBetween(-50.0, 50.0);
            assertThat(point.z()).isBetween(10.0, 110.0);
            assertThat(point.y()).isEqualTo(64);
            minX = Math.min(minX, point.x());
            maxX = Math.max(maxX, point.x());
        }
        assertThat(minX).isLessThan(-45);
        assertThat(maxX).isGreaterThan(45);
    }

    @Test
    void aroundAPointIsASquareCentredOnIt() {
        assertThat(Area.around(new Vec(10, 64, -4), 16)).isEqualTo(new Area(-6, -20, 26, 12));
    }

    @Test
    void aBigMapGetsTwiceAsManyFallingThingsAndASmallOneTheRecordedAmount() {
        assertThat(new Area(0, 0, 100, 100).copies()).isEqualTo(Area.MAX_COPIES);
        assertThat(Area.around(Vec.ZERO, 16).copies()).isEqualTo(1);
        assertThat(new Area(0, 0, 5, 5).copies()).isEqualTo(1);
    }

    @Test
    void anAreaCannotBeInsideOut() {
        assertThatThrownBy(() -> new Area(10, 0, 0, 10)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aSceneWithoutAnAreaCoversTheSurroundingsOfItsOrigin() {
        Vec origin = new Vec(100, 30, 100);

        assertThat(new Scene(origin, new Vec(0, 0, 1), Optional.empty(), List.of()).area()).isEqualTo(Area.around(origin, Scene.REACH));
    }
}
