package br.com.muxe.lobby.listener;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.Test;

class VoidRuleTest {

    private final VoidRule rule = new VoidRule(Set.of("lobby"), 0);

    @Test
    void fallingOutOfTheLobbyWorldSendsThePlayerBack() {
        assertThat(rule.sendsBack("lobby", -1)).isTrue();
        assertThat(rule.sendsBack("lobby", 0)).isFalse();
    }

    @Test
    void worldsCreatedAfterTheLobbyStartedAreLeftToTheirOwners() {
        assertThat(rule.sendsBack("muxe_setup_solo_majestic", -40)).isFalse();
    }
}
