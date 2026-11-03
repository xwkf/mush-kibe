package br.com.muxe.core.bukkit.prompt;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AnvilTextTest {

    private static final String PLACEHOLDER = "Insira o usuário";

    @Test
    void theTypedNameIsTheResultWithoutColoursOrSpaces() {
        assertThat(AnvilText.typed("§r tadeu ", PLACEHOLDER)).contains("tadeu");
    }

    @Test
    void leavingThePlaceholderOrNothingTypedGivesNoAnswer() {
        assertThat(AnvilText.typed("Insira o usuário", PLACEHOLDER)).isEmpty();
        assertThat(AnvilText.typed("§fInsira o usuário", "§fInsira o usuário")).isEmpty();
        assertThat(AnvilText.typed("   ", PLACEHOLDER)).isEmpty();
        assertThat(AnvilText.typed(null, PLACEHOLDER)).isEmpty();
    }
}
