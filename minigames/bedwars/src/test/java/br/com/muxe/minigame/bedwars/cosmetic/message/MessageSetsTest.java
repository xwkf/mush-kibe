package br.com.muxe.minigame.bedwars.cosmetic.message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.muxe.minigame.api.cosmetic.catalog.Cosmetic;
import br.com.muxe.minigame.api.cosmetic.catalog.CosmeticCatalog;
import br.com.muxe.minigame.api.cosmetic.catalog.CosmeticKind;
import br.com.muxe.minigame.api.cosmetic.message.MessageKind;
import br.com.muxe.minigame.api.cosmetic.message.MessagePreview;
import br.com.muxe.minigame.api.cosmetic.message.MessageSet;
import br.com.muxe.minigame.api.cosmetic.message.MessageSets;
import br.com.muxe.minigame.api.cosmetic.message.MessageValues;
import br.com.muxe.minigame.bedwars.room.RoomFixtures;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import org.junit.jupiter.api.Test;

class MessageSetsTest {

    private static final String VIEWER = "§8tadeu";
    private static final String KILLER = "§aAlice";
    private static final String VICTIM = "§cBob";

    private final MessageSets sets = MessageSets.from(RoomFixtures.KILL_MESSAGES);
    private final MessagePreview preview = MessagePreview.from(RoomFixtures.KILL_MESSAGES.getConfigurationSection("preview"));
    private final MessageSet standard = sets.find("padrao").orElseThrow();
    private final MessageSet statistic = sets.find("estatistico").orElseThrow();

    @Test
    void theConfigurationDefinesTheStandardAndTheStatisticSetsAndStandardIsTheFallback() {
        assertThat(sets.all().stream().map(MessageSet::id)).containsExactly("padrao", "estatistico");
        assertThat(sets.fallback()).isSameAs(standard);
        assertThat(sets.bedPrefix()).isEqualTo("§f§lCAMA DESTRUÍDA §f» ");
    }

    @Test
    void everyCosmeticOfTheKillMessageCatalogHasASetAndTheOtherWayAround() {
        CosmeticCatalog catalog = RoomFixtures.COSMETICS.catalog("bedwars", CosmeticKind.KILL_MESSAGE, RoomFixtures.CONFIG.getConfigurationSection("cosmetics"),
            id -> sets.find(id).isPresent());

        assertThat(catalog.category()).isEqualTo("bedwars-mensagem-kill");
        assertThat(catalog.all().stream().map(Cosmetic::id)).containsExactlyElementsOf(sets.all().stream().map(MessageSet::id).toList());
        assertThat(catalog.all()).allMatch(Cosmetic::animated);
        assertThat(catalog.defaultId()).contains(sets.fallback().id());
    }

    @Test
    void theStandardSetKeepsTheTextsOfTheGame() {
        MessageValues kill = MessageValues.kill(VICTIM, KILLER, OptionalLong.empty());
        MessageValues death = MessageValues.death(VICTIM, OptionalLong.empty());

        assertThat(standard.render(MessageKind.KILL, kill)).isEqualTo("§7§cBob §7morreu para §aAlice§7.");
        assertThat(standard.render(MessageKind.FINAL_KILL, kill)).isEqualTo("§7§cBob §7morreu para §aAlice§7. §b§lKILL FINAL!");
        assertThat(standard.render(MessageKind.VOID_KILL, kill)).isEqualTo("§7§cBob §7morreu no void para §aAlice§7.");
        assertThat(standard.render(MessageKind.VOID_FINAL_KILL, kill)).isEqualTo("§7§cBob §7morreu no void para §aAlice§7. §b§lKILL FINAL!");
        assertThat(standard.render(MessageKind.DEATH, death)).isEqualTo("§7§cBob §7morreu§7.");
        assertThat(standard.render(MessageKind.FINAL_DEATH, death)).isEqualTo("§7§cBob §7morreu§7. §b§lKILL FINAL!");
        assertThat(standard.render(MessageKind.VOID_DEATH, death)).isEqualTo("§7§cBob §7morreu no void§7.");
        assertThat(standard.render(MessageKind.VOID_FINAL_DEATH, death)).isEqualTo("§7§cBob §7morreu no void§7. §b§lKILL FINAL!");
        assertThat(standard.render(MessageKind.BED, MessageValues.bed(KILLER, "§c", "Vermelho", OptionalLong.empty())))
            .isEqualTo("§7A §cCama do Vermelho §7foi destruída por §aAlice§7.");
    }

    @Test
    void theStatisticSetCountsWithOrdinalsAndThousandSeparators() {
        MessageValues kill = MessageValues.kill(VICTIM, KILLER, OptionalLong.of(17_089));
        MessageValues finalKill = MessageValues.kill(VICTIM, KILLER, OptionalLong.of(19_215));

        assertThat(statistic.render(MessageKind.KILL, kill)).isEqualTo("§7§cBob §7foi a §b17.089ª§7 kill de §aAlice§7.");
        assertThat(statistic.render(MessageKind.FINAL_KILL, finalKill)).isEqualTo("§7§cBob §7foi a §b19.215ª§7 kill final de §aAlice§7.");
        assertThat(statistic.render(MessageKind.DEATH, MessageValues.death(VICTIM, OptionalLong.of(12_538)))).isEqualTo("§7§cBob §7morreu pela §b12.538ª§7 vez§7.");
        assertThat(statistic.render(MessageKind.FINAL_DEATH, MessageValues.death(VICTIM, OptionalLong.of(2_537)))).isEqualTo("§7§cBob §7teve sua §b2.537ª§7 morte final§7.");
        assertThat(statistic.render(MessageKind.BED, MessageValues.bed(KILLER, "§c", "Vermelho", OptionalLong.of(11_938))))
            .isEqualTo("§7A §cCama do Vermelho §7foi a §b11.938ª§7 cama destruída de §aAlice§7.");
    }

    @Test
    void theStatisticSetUsesTheSameTextForVoidAndNormalDeaths() {
        MessageValues kill = MessageValues.kill(VICTIM, KILLER, OptionalLong.of(5));
        MessageValues death = MessageValues.death(VICTIM, OptionalLong.of(6));

        assertThat(statistic.render(MessageKind.VOID_KILL, kill)).isEqualTo(statistic.render(MessageKind.KILL, kill));
        assertThat(statistic.render(MessageKind.VOID_FINAL_KILL, kill)).isEqualTo(statistic.render(MessageKind.FINAL_KILL, kill));
        assertThat(statistic.render(MessageKind.VOID_DEATH, death)).isEqualTo(statistic.render(MessageKind.DEATH, death));
        assertThat(statistic.render(MessageKind.VOID_FINAL_DEATH, death)).isEqualTo(statistic.render(MessageKind.FINAL_DEATH, death));
    }

    @Test
    void aCountingMessageWithoutACounterFallsBackToTheStandardSet() {
        MessageValues kill = MessageValues.kill(VICTIM, KILLER, OptionalLong.empty());

        assertThat(sets.render(statistic, MessageKind.KILL, kill)).isEqualTo(sets.render(standard, MessageKind.KILL, kill));
        assertThat(sets.render(statistic, MessageKind.KILL, MessageValues.kill(VICTIM, KILLER, OptionalLong.of(3))))
            .isEqualTo("§7§cBob §7foi a §b3ª§7 kill de §aAlice§7.");
        assertThatThrownBy(() -> statistic.render(MessageKind.KILL, kill)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void thePreviewOfTheStandardSetLooksLikeTheRecording() {
        assertThat(preview.lines(standard, VIEWER)).containsExactly(
            "§7Prévia:",
            "§7§cFulano §7morreu para §8tadeu§7.",
            "§7§cFulano §7morreu no void para §8tadeu§7.",
            "§7§8tadeu §7morreu§7.",
            "§7§8tadeu §7morreu no void§7.",
            "§7A §cCama do Vermelho §7foi destruída",
            "§7por §8tadeu§7."
        );
    }

    @Test
    void thePreviewOfTheStatisticSetLooksLikeTheRecording() {
        assertThat(preview.lines(statistic, VIEWER)).containsExactly(
            "§7Prévia:",
            "§7§cFulano §7foi a §b17.089ª§7 kill de §8tadeu§7.",
            "§7§cFulano §7foi a §b19.215ª§7 kill final",
            "§7de §8tadeu§7.",
            "§7§8tadeu §7morreu pela §b12.538ª§7 vez§7.",
            "§7§8tadeu §7teve sua §b2.537ª§7 morte final§7.",
            "§7A §cCama do Vermelho §7foi a §b11.938ª§7",
            "§7cama destruída de §8tadeu§7."
        );
    }

    @Test
    void thePreviewIsBuiltFromTheTemplatesOfTheGameSoItNeverDrifts() {
        for (MessageSet set : sets.all()) {
            for (MessageKind kind : set.preview()) {
                MessageValues values = preview.values(kind, VIEWER);
                List<String> lines = set.previewLines(kind, values);
                String joined = lines.size() == 1 ? lines.getFirst() : lines.getFirst() + " " + lines.get(1).substring(set.lineColor().length());

                assertThat(joined).as(set.id() + " " + kind.key()).isEqualTo(set.render(kind, values));
            }
        }
    }

    @Test
    void aSetNeedsOnlyTheMessagesItChangesButTheDefaultNeedsAll() {
        Map<MessageKind, String> partial = new EnumMap<>(MessageKind.class);
        partial.put(MessageKind.KILL, "{victim} morreu");

        MessageSet incomplete = new MessageSet("padrao", "§7", partial, List.of());

        assertThat(incomplete.defines(MessageKind.VOID_KILL)).isTrue();
        assertThat(incomplete.defines(MessageKind.DEATH)).isFalse();
        assertThatThrownBy(() -> new MessageSets(List.of(incomplete), "padrao", "")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void theDefaultSetCannotDependOnACounter() {
        Map<MessageKind, String> counting = new EnumMap<>(MessageKind.class);
        for (MessageKind kind : MessageKind.values()) {
            counting.put(kind, "{victim} {count}");
        }

        assertThatThrownBy(() -> new MessageSets(List.of(new MessageSet("padrao", "§7", counting, List.of())), "padrao", ""))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
