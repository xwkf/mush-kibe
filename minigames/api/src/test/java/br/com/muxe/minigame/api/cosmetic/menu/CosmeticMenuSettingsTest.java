package br.com.muxe.minigame.api.cosmetic.menu;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class CosmeticMenuSettingsTest {

    private static final String MENUS = """
        categories:
          title: "Cosméticos do Duels"
          rows: 4
          victory-effect:
            slot: 11
            item:
              material: FIREWORK
          team-color:
            slot: 15
            item:
              material: WOOL
          soon:
            "11":
              material: BARRIER
            "13":
              material: BARRIER
            "15":
              material: BARRIER
          back:
            slot: 31
            item:
              material: ARROW
        victory-effect:
          title: "Efeitos de Vitória ({page}/{pages})"
          rows: 6
          slots:
          - 10
          - 11
          none:
            material: BARRIER
          random:
            material: ENDER_PEARL
          back:
            slot: 48
            item:
              material: ARROW
          previous:
            slot: 45
            item:
              material: ARROW
          next:
            slot: 53
            item:
              material: ARROW
          selected:
            slot: 49
            item:
              material: PAPER
          shop:
            slot: 50
            item:
              material: EMERALD
          lore:
            star: "*"
        final-kill:
          title: "Kill Final ({page}/{pages})"
        """;

    @Test
    void onlyTheConfiguredKindsGetAButtonAndEachListStartsFromTheBaseList() throws InvalidConfigurationException {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.loadFromString(MENUS);

        CosmeticMenuSettings settings = CosmeticMenuSettings.from(yaml, "victory-effect", List.of("victory-effect", "final-kill"));

        assertThat(settings.categoriesTitle()).isEqualTo("Cosméticos do Duels");
        assertThat(settings.categoriesRows()).isEqualTo(4);
        assertThat(settings.categories()).containsOnlyKeys("victory-effect");
        assertThat(settings.category("victory-effect").button().slot()).isEqualTo(11);
        assertThat(settings.category("victory-effect").list().title()).isEqualTo("Efeitos de Vitória ({page}/{pages})");
        assertThat(settings.teamColor()).hasValueSatisfying(button -> assertThat(button.slot()).isEqualTo(15));
        assertThat(settings.soon()).containsOnlyKeys(13);
        assertThat(settings.categoriesBack().slot()).isEqualTo(31);
    }
}
