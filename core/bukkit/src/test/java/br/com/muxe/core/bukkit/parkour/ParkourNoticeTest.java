package br.com.muxe.core.bukkit.parkour;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.text.MessageCatalog;
import br.com.muxe.core.common.parkour.domain.ParkourOutcome;
import java.io.File;
import java.time.Duration;
import java.util.Optional;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class ParkourNoticeTest {

    private static final MessageCatalog MESSAGES = MessageCatalog.from(
        YamlConfiguration.loadConfiguration(new File("src/main/resources/config.yml")).getConfigurationSection("messages"));

    @Test
    void finishingSlowerThanTheRecordShowsTheBestTime() {
        ParkourOutcome outcome = new ParkourOutcome(Duration.ofMillis(229_854), Optional.of(Duration.ofMillis(127_652)), false);

        ParkourNotice notice = ParkourNotice.after(outcome).orElseThrow();

        assertThat(MESSAGES.get(notice.key(), notice.placeholders())).isEqualTo("§b§lPARKOUR §eO seu melhor tempo foi de §a2m 7,652s§e.");
    }

    @Test
    void beatingTheRecordShowsTheNewRecord() {
        ParkourOutcome outcome = new ParkourOutcome(Duration.ofMillis(100_000), Optional.of(Duration.ofMillis(127_652)), true);

        ParkourNotice notice = ParkourNotice.after(outcome).orElseThrow();

        assertThat(MESSAGES.get(notice.key(), notice.placeholders())).isEqualTo("§b§lPARKOUR §eVocê atingiu um novo §brecorde! §a2m 7,652s §7-> §a1m 40,000s");
    }

    @Test
    void theFirstFinishShowsNothingElse() {
        assertThat(ParkourNotice.after(new ParkourOutcome(Duration.ofSeconds(90), Optional.empty(), true))).isEmpty();
    }

    @Test
    void theFinishLineMatchesTheReference() {
        assertThat(MESSAGES.get("parkour-finished", "time", "3m 49,854s")).isEqualTo("§b§lPARKOUR §aVocê finalizou o §eparkour §aem §73m 49,854s§a!");
    }
}
