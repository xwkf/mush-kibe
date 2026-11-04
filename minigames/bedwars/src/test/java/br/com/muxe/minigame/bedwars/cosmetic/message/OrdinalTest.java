package br.com.muxe.minigame.bedwars.cosmetic.message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class OrdinalTest {

    @Test
    void thousandsAreSeparatedByDots() {
        assertThat(Ordinal.format(0)).isEqualTo("0");
        assertThat(Ordinal.format(7)).isEqualTo("7");
        assertThat(Ordinal.format(999)).isEqualTo("999");
        assertThat(Ordinal.format(1_000)).isEqualTo("1.000");
        assertThat(Ordinal.format(2_537)).isEqualTo("2.537");
        assertThat(Ordinal.format(17_089)).isEqualTo("17.089");
        assertThat(Ordinal.format(123_456)).isEqualTo("123.456");
        assertThat(Ordinal.format(1_234_567)).isEqualTo("1.234.567");
    }

    @Test
    void aNegativeNumberIsRefused() {
        assertThatThrownBy(() -> Ordinal.format(-1)).isInstanceOf(IllegalArgumentException.class);
    }
}
