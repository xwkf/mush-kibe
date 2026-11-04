package br.com.muxe.afk.config;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.text.MessageCatalog;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class AfkServerSettingsTest {

    private static final YamlConfiguration CONFIG = defaultConfig();

    @Test
    void playersGoBackToTheMainLobbyWhenTheirServerIsGone() {
        AfkServerSettings settings = AfkServerSettings.from(CONFIG);

        assertThat(settings.fallbackGroup()).isEqualTo("lobby");
        assertThat(settings.arrivalGrace()).isEqualTo(Duration.ofSeconds(1));
        assertThat(settings.retryAfter()).isEqualTo(Duration.ofSeconds(5));
    }

    @Test
    void theSpawnFacesTheSignOfTheMap() {
        assertThat(CONFIG.getString("spawn.world")).isEqualTo("afk");
        assertThat(CONFIG.getDouble("spawn.x")).isEqualTo(8.5);
        assertThat(CONFIG.getDouble("spawn.y")).isEqualTo(63);
        assertThat(CONFIG.getDouble("spawn.z")).isEqualTo(8.5);
        assertThat(CONFIG.getDouble("spawn.yaw")).isZero();
    }

    @Test
    void messagesMatchTheReferenceScreenshots() {
        MessageCatalog messages = MessageCatalog.from(CONFIG.getConfigurationSection("messages"));

        assertThat(messages.get("title")).isEqualTo("§c§lVOCÊ ESTÁ AFK");
        assertThat(messages.get("subtitle")).isEqualTo("§7Mova-se para voltar ao lobby.");
        assertThat(messages.lines("arrived")).containsExactly("§c§lVOCÊ ESTÁ AFK!", "§7Mova-se para voltar ao lobby.");
    }

    private static YamlConfiguration defaultConfig() {
        try (Reader reader = new InputStreamReader(Objects.requireNonNull(AfkServerSettingsTest.class.getResourceAsStream("/config.yml")), StandardCharsets.UTF_8)) {
            return YamlConfiguration.loadConfiguration(reader);
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}
