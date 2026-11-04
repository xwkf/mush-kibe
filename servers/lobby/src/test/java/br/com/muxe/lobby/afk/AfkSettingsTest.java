package br.com.muxe.lobby.afk;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class AfkSettingsTest {

    @Test
    void readsTheAfkSection() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("afk.enabled", false);
        config.set("afk.idle-after", "2m");
        config.set("afk.server-group", "ausente");
        config.set("afk.check-interval", "2s");
        config.set("afk.origin-ttl", "1d");

        assertThat(AfkSettings.from(config.getConfigurationSection("afk")))
            .isEqualTo(new AfkSettings(false, Duration.ofMinutes(2), "ausente", Duration.ofSeconds(2), Duration.ofDays(1)));
    }

    @Test
    void theDefaultConfigSendsNonVipsToTheAfkServerAfterFiveMinutes() throws Exception {
        YamlConfiguration config;
        try (Reader reader = new InputStreamReader(Objects.requireNonNull(AfkSettingsTest.class.getResourceAsStream("/config.yml")), StandardCharsets.UTF_8)) {
            config = YamlConfiguration.loadConfiguration(reader);
        }

        assertThat(AfkSettings.from(config.getConfigurationSection("afk")))
            .isEqualTo(new AfkSettings(true, Duration.ofMinutes(5), "afk", Duration.ofSeconds(1), Duration.ofHours(12)));
    }

    @Test
    void withoutTheSectionNonVipsGoToTheAfkServerAfterFiveMinutes() {
        assertThat(AfkSettings.from(null))
            .isEqualTo(new AfkSettings(true, Duration.ofMinutes(5), "afk", Duration.ofSeconds(1), Duration.ofHours(12)));
    }
}
