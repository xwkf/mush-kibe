package br.com.muxe.core.bukkit.effect;

import static org.assertj.core.api.Assertions.assertThat;

import org.bukkit.Sound;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class SoundCueTest {

    @Test
    void aSoundMissingFromAnOlderFileComesFromTheDefaultsOfTheJar() throws InvalidConfigurationException {
        YamlConfiguration jar = new YamlConfiguration();
        jar.loadFromString(String.join("\n",
            "sounds:",
            "  countdown:",
            "    sound: CLICK",
            "    volume: 0.5",
            "    pitch: 2"
        ));
        YamlConfiguration onDisk = new YamlConfiguration();
        onDisk.loadFromString("other: 1");
        onDisk.setDefaults(jar);

        assertThat(SoundCue.from(onDisk.getConfigurationSection("sounds").getConfigurationSection("countdown")))
            .contains(new SoundCue(Sound.CLICK, 0.5f, 2f));
    }

    @Test
    void theFileOnTheServerWinsOverTheDefaults() throws InvalidConfigurationException {
        YamlConfiguration jar = new YamlConfiguration();
        jar.loadFromString(String.join("\n",
            "countdown:",
            "  sound: CLICK"
        ));
        YamlConfiguration onDisk = new YamlConfiguration();
        onDisk.loadFromString(String.join("\n",
            "countdown:",
            "  sound: NOTE_PLING"
        ));
        onDisk.setDefaults(jar);

        assertThat(SoundCue.from(onDisk.getConfigurationSection("countdown"))).contains(new SoundCue(Sound.NOTE_PLING, 1f, 1f));
    }

    @Test
    void anEmptySoundPlaysNothing() throws InvalidConfigurationException {
        YamlConfiguration config = new YamlConfiguration();
        config.loadFromString(String.join("\n",
            "countdown:",
            "  sound: \"\""
        ));

        assertThat(SoundCue.from(config.getConfigurationSection("countdown"))).isEmpty();
    }
}
