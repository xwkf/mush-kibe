package br.com.muxe.core.bukkit.menu;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.muxe.core.bukkit.item.ItemBuilder;
import java.util.List;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class MenuButtonTest {

    private final ItemBuilder bed = ItemBuilder.of(Material.BED);
    private final ItemBuilder apple = ItemBuilder.of(Material.APPLE);
    private final ItemBuilder star = ItemBuilder.of(Material.NETHER_STAR);

    @Test
    void aPlainButtonNeverChanges() {
        MenuButton button = MenuButton.of(bed);

        assertThat(button.animated()).isFalse();
        assertThat(button.iconAt(0)).isEqualTo(bed);
        assertThat(button.iconAt(500)).isEqualTo(bed);
    }

    @Test
    void anAnimatedButtonShowsEachFrameForItsPeriodAndLoops() {
        MenuButton button = MenuButton.animated(List.of(bed, apple, star), 10, MenuClick.NONE);

        assertThat(button.animated()).isTrue();
        assertThat(button.icon()).isEqualTo(bed);
        assertThat(button.iconAt(9)).isEqualTo(bed);
        assertThat(button.iconAt(10)).isEqualTo(apple);
        assertThat(button.iconAt(25)).isEqualTo(star);
        assertThat(button.iconAt(30)).isEqualTo(bed);
        assertThat(button.changesAt(20)).isTrue();
        assertThat(button.changesAt(21)).isFalse();
    }

    @Test
    void oneFrameIsJustAPlainButton() {
        assertThat(MenuButton.animated(List.of(bed), 10, MenuClick.NONE).animated()).isFalse();
    }

    @Test
    void anAnimationNeedsFramesAndAPositivePeriod() {
        assertThatThrownBy(() -> MenuButton.animated(List.of(), 10, MenuClick.NONE)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MenuButton.animated(List.of(bed, apple), 0, MenuClick.NONE)).isInstanceOf(IllegalArgumentException.class);
    }
}
