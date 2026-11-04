package br.com.muxe.core.bukkit.config.edit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ConfigValuesTest {

    @Test
    void newValuesAreInferredFromTheirText() {
        assertThat(ConfigValues.parse("true", null)).contains(true);
        assertThat(ConfigValues.parse("FALSE", null)).contains(false);
        assertThat(ConfigValues.parse("42", null)).contains(42);
        assertThat(ConfigValues.parse("-7", null)).contains(-7);
        assertThat(ConfigValues.parse("3000000000", null)).contains(3_000_000_000L);
        assertThat(ConfigValues.parse("27.5", null)).contains(27.5);
        assertThat(ConfigValues.parse("&eOlá mundo", null)).contains("&eOlá mundo");
    }

    @Test
    void durationsStayAsStrings() {
        assertThat(ConfigValues.parse("60s", null)).contains("60s");
        assertThat(ConfigValues.parse("500ms", null)).contains("500ms");
        assertThat(ConfigValues.parse("2m", 0)).isEmpty();
        assertThat(ConfigValues.parse("2m", "1s")).contains("2m");
    }

    @Test
    void quotesForceTextAndAreRemoved() {
        assertThat(ConfigValues.parse("\"123\"", null)).contains("123");
        assertThat(ConfigValues.parse("\"\"", null)).contains("");
        assertThat(ConfigValues.parse("\"true\"", "x")).contains("true");
    }

    @Test
    void anExistingValueDecidesTheTypeAndRefusesWhatDoesNotFit() {
        assertThat(ConfigValues.parse("true", false)).contains(true);
        assertThat(ConfigValues.parse("yes", false)).isEmpty();
        assertThat(ConfigValues.parse("5", 3)).contains(5);
        assertThat(ConfigValues.parse("5.5", 3)).isEmpty();
        assertThat(ConfigValues.parse("abc", 3)).isEmpty();
        assertThat(ConfigValues.parse("27", 11.0)).contains(27.0);
        assertThat(ConfigValues.parse("27.25", 11.0)).contains(27.25);
        assertThat(ConfigValues.parse("x", 11.0)).isEmpty();
        assertThat(ConfigValues.parse("123", "texto")).contains("123");
        assertThat(ConfigValues.parse("5", 3L)).contains(5L);
    }

    @Test
    void listsSplitOnCommasAndFollowTheTypeOfTheirFirstItem() {
        assertThat(ConfigValues.parseList("a, b ,c", List.of("x"))).contains(List.of("a", "b", "c"));
        assertThat(ConfigValues.parseList("1,2,3", List.of(9))).contains(List.of(1, 2, 3));
        assertThat(ConfigValues.parseList("1,x", List.of(9))).isEmpty();
        assertThat(ConfigValues.parseList("1,2", List.of())).contains(List.of(1, 2));
        assertThat(ConfigValues.parseList("a,,b", List.of("x"))).contains(List.of("a", "b"));
        assertThat(ConfigValues.parseList("[]", List.of("x"))).contains(List.of());
    }

    @Test
    void typesAreNamedInPortuguese() {
        assertThat(ConfigValues.typeName(true)).isEqualTo(ConfigValues.TYPE_BOOLEAN);
        assertThat(ConfigValues.typeName(1)).isEqualTo(ConfigValues.TYPE_INTEGER);
        assertThat(ConfigValues.typeName(1L)).isEqualTo(ConfigValues.TYPE_INTEGER);
        assertThat(ConfigValues.typeName(1.5)).isEqualTo(ConfigValues.TYPE_DECIMAL);
        assertThat(ConfigValues.typeName(List.of())).isEqualTo(ConfigValues.TYPE_LIST);
        assertThat(ConfigValues.typeName("a")).isEqualTo(ConfigValues.TYPE_TEXT);
        Optional<Object> none = ConfigValues.parse("x", null);
        assertThat(none).isPresent();
    }
}
