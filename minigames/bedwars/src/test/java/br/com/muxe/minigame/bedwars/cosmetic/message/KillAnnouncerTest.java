package br.com.muxe.minigame.bedwars.cosmetic.message;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.minigame.api.cosmetic.message.KillAnnouncer;
import br.com.muxe.minigame.api.cosmetic.message.LifetimeTally;
import br.com.muxe.minigame.api.cosmetic.message.MessageSet;
import br.com.muxe.minigame.api.cosmetic.message.MessageSets;
import br.com.muxe.minigame.bedwars.room.RoomFixtures;
import br.com.muxe.minigame.bedwars.stats.domain.StatCounters;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class KillAnnouncerTest {

    private static final StatCounters STORED = new StatCounters(40, 11_937, 3, 17_088, 12_537, 5, 19_214, 2_536, 1, 20, 20, 0, 4, 900);

    private final MessageSets sets = MessageSets.from(RoomFixtures.CONFIG.getConfigurationSection("kill-messages"));
    private final MessageSet standard = sets.find("padrao").orElseThrow();
    private final MessageSet statistic = sets.find("estatistico").orElseThrow();
    private final Map<UUID, MessageSet> chosen = new HashMap<>();
    private final LifetimeTally tally = new LifetimeTally();
    private final KillAnnouncer announcer = new KillAnnouncer(sets, id -> chosen.getOrDefault(id, standard), tally);
    private final UUID killer = UUID.randomUUID();
    private final UUID victim = UUID.randomUUID();

    @Test
    void aKillIsToldWithTheSetOfTheKiller() {
        chosen.put(killer, statistic);
        chosen.put(victim, standard);
        tally.load(killer, STORED.lifetime());

        assertThat(announcer.kill(killer, "§aAlice", victim, "§cBob", false, false)).isEqualTo("§7§cBob §7foi a §b17.089ª§7 kill de §aAlice§7.");
    }

    @Test
    void theSetOfTheVictimDoesNotChangeAKill() {
        chosen.put(killer, standard);
        chosen.put(victim, statistic);
        tally.load(victim, STORED.lifetime());

        assertThat(announcer.kill(killer, "§aAlice", victim, "§cBob", false, false)).isEqualTo("§7§cBob §7morreu para §aAlice§7.");
    }

    @Test
    void aFinalKillCountsTheFinalKillsOfTheKiller() {
        chosen.put(killer, statistic);
        tally.load(killer, STORED.lifetime());

        assertThat(announcer.kill(killer, "§aAlice", victim, "§cBob", true, false)).isEqualTo("§7§cBob §7foi a §b19.215ª§7 kill final de §aAlice§7.");
        assertThat(announcer.kill(killer, "§aAlice", victim, "§cBob", true, false)).isEqualTo("§7§cBob §7foi a §b19.216ª§7 kill final de §aAlice§7.");
        assertThat(announcer.kill(killer, "§aAlice", victim, "§cBob", false, false)).isEqualTo("§7§cBob §7foi a §b17.089ª§7 kill de §aAlice§7.");
    }

    @Test
    void aVoidKillSaysSoInTheStandardSet() {
        assertThat(announcer.kill(killer, "§aAlice", victim, "§cBob", false, true)).isEqualTo("§7§cBob §7morreu no void para §aAlice§7.");
        assertThat(announcer.kill(killer, "§aAlice", victim, "§cBob", true, true)).isEqualTo("§7§cBob §7morreu no void para §aAlice§7. §b§lKILL FINAL!");
    }

    @Test
    void aDeathWithoutAKillerIsToldWithTheSetOfTheVictim() {
        chosen.put(victim, statistic);
        tally.load(victim, STORED.lifetime());

        assertThat(announcer.death(victim, "§cBob", false, false)).isEqualTo("§7§cBob §7morreu pela §b12.538ª§7 vez§7.");
        assertThat(announcer.death(victim, "§cBob", true, false)).isEqualTo("§7§cBob §7teve sua §b2.537ª§7 morte final§7.");
    }

    @Test
    void aDeathWithoutAKillerKeepsTheGameTextsInTheStandardSet() {
        assertThat(announcer.death(victim, "§cBob", false, false)).isEqualTo("§7§cBob §7morreu§7.");
        assertThat(announcer.death(victim, "§cBob", false, true)).isEqualTo("§7§cBob §7morreu no void§7.");
        assertThat(announcer.death(victim, "§cBob", true, false)).isEqualTo("§7§cBob §7morreu§7. §b§lKILL FINAL!");
        assertThat(announcer.death(victim, "§cBob", true, true)).isEqualTo("§7§cBob §7morreu no void§7. §b§lKILL FINAL!");
    }

    @Test
    void theVictimOfAKillAlsoCountsADeath() {
        chosen.put(victim, statistic);
        tally.load(victim, STORED.lifetime());
        announcer.kill(killer, "§aAlice", victim, "§cBob", false, false);
        announcer.kill(killer, "§aAlice", victim, "§cBob", true, false);

        assertThat(announcer.death(victim, "§cBob", false, false)).isEqualTo("§7§cBob §7morreu pela §b12.539ª§7 vez§7.");
        assertThat(announcer.death(victim, "§cBob", true, false)).isEqualTo("§7§cBob §7teve sua §b2.538ª§7 morte final§7.");
    }

    @Test
    void theBedIsToldWithTheSetOfTheBreakerAndKeepsThePrefix() {
        chosen.put(killer, statistic);
        tally.load(killer, STORED.lifetime());

        assertThat(announcer.bed(killer, "§aAlice", "§c", "Vermelho"))
            .isEqualTo("§f§lCAMA DESTRUÍDA §f» §7A §cCama do Vermelho §7foi a §b11.938ª§7 cama destruída de §aAlice§7.");
        assertThat(announcer.bed(killer, "§aAlice", "§9", "Azul"))
            .isEqualTo("§f§lCAMA DESTRUÍDA §f» §7A §9Cama do Azul §7foi a §b11.939ª§7 cama destruída de §aAlice§7.");
        assertThat(announcer.bed(victim, "§cBob", "§9", "Azul")).isEqualTo("§f§lCAMA DESTRUÍDA §f» §7A §9Cama do Azul §7foi destruída por §cBob§7.");
    }

    @Test
    void whenTheTotalsAreNotLoadedTheStatisticSetFallsBackToTheStandardTexts() {
        chosen.put(killer, statistic);
        chosen.put(victim, statistic);

        assertThat(announcer.kill(killer, "§aAlice", victim, "§cBob", false, false)).isEqualTo("§7§cBob §7morreu para §aAlice§7.");
        assertThat(announcer.death(victim, "§cBob", false, false)).isEqualTo("§7§cBob §7morreu§7.");
        assertThat(announcer.bed(killer, "§aAlice", "§c", "Vermelho")).isEqualTo("§f§lCAMA DESTRUÍDA §f» §7A §cCama do Vermelho §7foi destruída por §aAlice§7.");
    }

    @Test
    void totalsLoadedLaterStillIncludeWhatHappenedBeforeTheyArrived() {
        chosen.put(killer, statistic);
        announcer.kill(killer, "§aAlice", victim, "§cBob", false, false);
        tally.load(killer, STORED.lifetime());

        assertThat(announcer.kill(killer, "§aAlice", victim, "§cBob", false, false)).isEqualTo("§7§cBob §7foi a §b17.090ª§7 kill de §aAlice§7.");
    }
}
