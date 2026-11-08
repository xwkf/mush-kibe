package br.com.muxe.minigame.bedwars.room;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MatchStartedMessageTest {

    @Test
    void theAllianceWarningHasADarkRedBoldHeaderOverALightRedRule() {
        assertThat(RoomFixtures.CONFIG.getStringList("messages.match-started")).containsExactly(
            "&eO jogo iniciou!",
            "",
            "&4&lAVISO:",
            "&c - A aliança com outros jogadores é proibida e resultará em punição."
        );
    }
}
