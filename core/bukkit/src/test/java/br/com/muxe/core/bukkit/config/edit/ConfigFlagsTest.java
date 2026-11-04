package br.com.muxe.core.bukkit.config.edit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ConfigFlagsTest {

    private static final Set<String> KNOWN = Set.of(ConfigFlags.NEW, ConfigFlags.CENTER);

    @Test
    void flagsAtTheEndAreStrippedFromTheValue() {
        ConfigFlags flags = ConfigFlags.parse("&eOlá mundo --novo", KNOWN);

        assertThat(flags.rest()).isEqualTo("&eOlá mundo");
        assertThat(flags.has(ConfigFlags.NEW)).isTrue();
        assertThat(flags.has(ConfigFlags.CENTER)).isFalse();
    }

    @Test
    void flagsAtTheStartAreAlsoStripped() {
        ConfigFlags flags = ConfigFlags.parse("--novo --centro 12", KNOWN);

        assertThat(flags.rest()).isEqualTo("12");
        assertThat(flags.present()).containsExactlyInAnyOrder(ConfigFlags.NEW, ConfigFlags.CENTER);
    }

    @Test
    void aFlagInTheMiddleOfTheTextBelongsToTheText() {
        ConfigFlags flags = ConfigFlags.parse("use --novo aqui", KNOWN);

        assertThat(flags.rest()).isEqualTo("use --novo aqui");
        assertThat(flags.present()).isEmpty();
    }

    @Test
    void unknownFlagsStayInTheText() {
        assertThat(ConfigFlags.parse("--mundo", KNOWN).rest()).isEqualTo("--mundo");
    }

    @Test
    void anAbsentOrBlankInputIsEmpty() {
        assertThat(ConfigFlags.parse(Optional.empty(), KNOWN).rest()).isEmpty();
        assertThat(ConfigFlags.parse("   ", KNOWN).rest()).isEmpty();
        assertThat(ConfigFlags.parse(Optional.of("--centro"), KNOWN).has(ConfigFlags.CENTER)).isTrue();
    }
}
