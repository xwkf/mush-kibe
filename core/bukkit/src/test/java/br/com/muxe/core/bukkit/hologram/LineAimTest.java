package br.com.muxe.core.bukkit.hologram;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

class LineAimTest {

    @Test
    void lookingStraightAtTheColumnGivesTheEyeHeight() {
        double y = LineAim.heightAt(0, 78.6, 0, 1, 0, 0, 5, 0);

        assertThat(y).isCloseTo(78.6, within(1e-9));
    }

    @Test
    void lookingUpOrDownMovesTheHeightAlongTheRay() {
        double up = LineAim.heightAt(0, 78.6, 0, 4, 1, 0, 4, 0);
        double down = LineAim.heightAt(0, 78.6, 0, 3, -1, 4, 3, 4);

        assertThat(up).isCloseTo(79.6, within(1e-9));
        assertThat(down).isCloseTo(77.6, within(1e-9));
    }

    @Test
    void theRayIsMeasuredAtItsClosestPassByTheColumn() {
        double y = LineAim.heightAt(0, 70, 0, 1, 1, 0, 4, 3);

        assertThat(y).isCloseTo(74, within(1e-9));
    }

    @Test
    void aColumnBehindThePlayerGivesTheEyeHeight() {
        double y = LineAim.heightAt(0, 70, 0, 1, 1, 0, -4, 0);

        assertThat(y).isCloseTo(70, within(1e-9));
    }
}
