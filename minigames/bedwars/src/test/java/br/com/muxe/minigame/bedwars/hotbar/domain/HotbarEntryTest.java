package br.com.muxe.minigame.bedwars.hotbar.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import org.junit.jupiter.api.Test;

class HotbarEntryTest {

    @Test
    void anEntryIsEncodedWithItsKindAndComesBackTheSame() {
        assertThat(HotbarEntry.category("blocos").encoded()).isEqualTo("c:blocos");
        assertThat(HotbarEntry.item("la").encoded()).isEqualTo("i:la");
        assertThat(HotbarEntry.decode("c:blocos")).contains(HotbarEntry.category("blocos"));
        assertThat(HotbarEntry.decode("i:pocao-agilidade")).contains(HotbarEntry.item("pocao-agilidade"));
    }

    @Test
    void textThatIsNotAnEntryIsNotDecoded() {
        assertThat(HotbarEntry.decode("")).isEmpty();
        assertThat(HotbarEntry.decode("x:la")).isEmpty();
        assertThat(HotbarEntry.decode("la")).isEmpty();
        assertThat(HotbarEntry.decode("i:")).isEmpty();
        assertThat(HotbarEntry.decode("i:a:b")).isEmpty();
        assertThat(HotbarEntry.decode("i:" + "a".repeat(HotbarEntry.MAX_ID_LENGTH + 1))).isEmpty();
    }

    @Test
    void anEntryNeedsAnIdThatCanBeStored() {
        assertThatThrownBy(() -> HotbarEntry.item("")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> HotbarEntry.item("a,b")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> HotbarEntry.category("a:b")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void anItemEntryMatchesOnlyThatItemAndACategoryEntryMatchesTheCategoriesOfTheItem() {
        assertThat(HotbarEntry.item("la").matches("la", Set.of("blocos"))).isTrue();
        assertThat(HotbarEntry.item("la").matches("argila", Set.of("blocos"))).isFalse();
        assertThat(HotbarEntry.category("blocos").matches("argila", Set.of("blocos"))).isTrue();
        assertThat(HotbarEntry.category("combate").matches("argila", Set.of("blocos"))).isFalse();
        assertThat(HotbarEntry.category("la").matches("la", Set.of("blocos"))).isFalse();
    }
}
