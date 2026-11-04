package br.com.muxe.core.common.config.live;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class LiveValueTest {

    @Test
    void swappingReplacesTheValueForLaterReadersAndReturnsThePreviousOne() {
        LiveValue<List<String>> value = new LiveValue<>(List.of("old"));
        List<String> snapshot = value.get();

        List<String> previous = value.swap(List.of("new"));

        assertThat(previous).isEqualTo(snapshot);
        assertThat(snapshot).containsExactly("old");
        assertThat(value.get()).containsExactly("new");
    }

    @Test
    void reloadingKeepsTheCurrentValueWhenTheLoaderFails() {
        LiveValue<String> value = new LiveValue<>("kept");

        assertThatThrownBy(() -> value.reload(() -> {
            throw new IllegalArgumentException("bad config");
        })).isInstanceOf(IllegalArgumentException.class);

        assertThat(value.get()).isEqualTo("kept");
    }

    @Test
    void reloadingStoresWhatTheLoaderReturns() {
        LiveValue<String> value = new LiveValue<>("a");

        value.reload(() -> "b");

        assertThat(value.get()).isEqualTo("b");
    }

    @Test
    void neitherTheInitialNorTheNextValueMayBeNull() {
        assertThatThrownBy(() -> new LiveValue<String>(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new LiveValue<>("a").swap(null)).isInstanceOf(NullPointerException.class);
    }
}
