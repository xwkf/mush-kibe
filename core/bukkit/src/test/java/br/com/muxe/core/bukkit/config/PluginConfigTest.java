package br.com.muxe.core.bukkit.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class PluginConfigTest {

    @Test
    void sectionsAddedInANewVersionAreWrittenIntoAnOlderFileWithoutTouchingItsValues() throws InvalidConfigurationException {
        YamlConfiguration jar = new YamlConfiguration();
        jar.loadFromString(String.join("\n",
            "menus:",
            "  bedwars:",
            "    title: \"Menu do Bed Wars\"",
            "    quick-buy:",
            "      slot: 29",
            "      item:",
            "        material: CHEST",
            "        name: \"&aCompra rápida\"",
            "messages:",
            "  kept: \"&adefault\"",
            "  added-later: \"&enew\""
        ));
        YamlConfiguration onDisk = new YamlConfiguration();
        onDisk.loadFromString(String.join("\n",
            "menus:",
            "  bedwars:",
            "    title: \"Editado\"",
            "messages:",
            "  kept: \"&cedited\""
        ));
        onDisk.setDefaults(jar);

        YamlConfiguration completed = PluginConfig.completed(onDisk);

        assertThat(completed.getString("menus.bedwars.title")).isEqualTo("Editado");
        assertThat(completed.getString("messages.kept")).isEqualTo("&cedited");
        assertThat(completed.getString("menus.bedwars.quick-buy.item.material", "")).isEqualTo("CHEST");
        assertThat(completed.getString("menus.bedwars.quick-buy.item.name")).isEqualTo("&aCompra rápida");
        assertThat(completed.getConfigurationSection("messages").getKeys(false)).containsExactlyInAnyOrder("kept", "added-later");
    }
}
