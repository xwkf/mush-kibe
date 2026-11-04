package br.com.muxe.core.common.activity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DisplacementTest {

    @Test
    void walkingSidewaysOrForwardIsMovement() {
        assertThat(Displacement.between(8.5, 63, 8.5, 8.6, 63, 8.5).walked()).isTrue();
        assertThat(Displacement.between(8.5, 63, 8.5, 8.5, 63, 8.4).walked()).isTrue();
    }

    @Test
    void jumpingIsMovement() {
        assertThat(Displacement.between(8.5, 63, 8.5, 8.5, 63.42, 8.5).walked()).isTrue();
    }

    @Test
    void onlyTurningTheHeadOrFallingStraightDownIsNot() {
        assertThat(Displacement.between(8.5, 63, 8.5, 8.5, 63, 8.5).walked()).isFalse();
        assertThat(Displacement.between(8.5, 63.5, 8.5, 8.5, 63, 8.5).walked()).isFalse();
    }
}
