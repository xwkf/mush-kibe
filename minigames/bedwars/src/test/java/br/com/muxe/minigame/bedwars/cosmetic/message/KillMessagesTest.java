package br.com.muxe.minigame.bedwars.cosmetic.message;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.minigame.api.cosmetic.catalog.CosmeticCatalog;
import br.com.muxe.minigame.api.cosmetic.catalog.CosmeticKind;
import br.com.muxe.minigame.api.cosmetic.message.KillMessages;
import br.com.muxe.minigame.api.cosmetic.message.MessageSet;
import br.com.muxe.minigame.api.cosmetic.message.MessageSets;
import br.com.muxe.minigame.api.cosmetic.profile.CosmeticAccess;
import br.com.muxe.minigame.api.cosmetic.profile.CosmeticWallet;
import br.com.muxe.minigame.bedwars.room.RoomFixtures;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.random.RandomGenerator;
import org.junit.jupiter.api.Test;

class KillMessagesTest {

    private static final RandomGenerator NEVER = () -> {
        throw new AssertionError("The message set must not be drawn again");
    };

    private final MessageSets sets = MessageSets.from(RoomFixtures.KILL_MESSAGES);
    private final CosmeticCatalog catalog = RoomFixtures.COSMETICS.catalog("bedwars", CosmeticKind.KILL_MESSAGE, RoomFixtures.CONFIG.getConfigurationSection("cosmetics"),
        id -> sets.find(id).isPresent());
    private final Map<UUID, CosmeticWallet> wallets = new HashMap<>();
    private final KillMessages messages = new KillMessages(sets, catalog, id -> Optional.ofNullable(wallets.get(id)));
    private final UUID player = UUID.randomUUID();

    private void wallet(String selected, CosmeticAccess access, String... owned) {
        wallets.put(player, new CosmeticWallet(Set.of(owned), selected, 0, access));
    }

    @Test
    void aPlayerWhoseCosmeticsAreNotLoadedUsesTheStandardSet() {
        assertThat(messages.of(player, "AB12", NEVER).id()).isEqualTo("padrao");
    }

    @Test
    void nothingSelectedMeansTheStandardSetBecauseItIsTheDefault() {
        wallet(catalog.defaultId().orElseThrow(), CosmeticAccess.REGULAR);

        assertThat(messages.of(player, "AB12", NEVER).id()).isEqualTo("padrao");
    }

    @Test
    void theStatisticSetIsUsedOnlyByAPlayerWhoOwnsIt() {
        wallet("estatistico", CosmeticAccess.REGULAR, "estatistico");
        assertThat(messages.of(player, "AB12", NEVER).id()).isEqualTo("estatistico");

        wallet("estatistico", CosmeticAccess.ULTRA_PLUS);
        assertThat(messages.of(player, "AB12", NEVER).id()).isEqualTo("padrao");
    }

    @Test
    void opsOwnTheStatisticSet() {
        wallets.put(player, CosmeticWallet.loaded(Set.of(), "estatistico", 0, CosmeticAccess.ALL, catalog));

        assertThat(messages.of(player, "AB12", NEVER).id()).isEqualTo("estatistico");
    }

    @Test
    void randomDrawsOneOwnedSetAndKeepsItForTheWholeMatch() {
        wallet(CosmeticWallet.RANDOM, CosmeticAccess.REGULAR, "estatistico");
        MessageSet first = messages.of(player, "AB12", new Random(11));

        for (int death = 0; death < 50; death++) {
            assertThat(messages.of(player, "AB12", NEVER)).isSameAs(first);
        }
    }

    @Test
    void randomDrawsAgainInTheNextMatchAndEveryOwnedSetCanComeUp() {
        wallet(CosmeticWallet.RANDOM, CosmeticAccess.REGULAR, "estatistico");
        Random random = new Random(7);
        Set<String> seen = new HashSet<>();

        for (int match = 0; match < 40; match++) {
            seen.add(messages.of(player, "room-" + match, random).id());
        }

        assertThat(seen).containsExactlyInAnyOrder("padrao", "estatistico");
    }

    @Test
    void randomNeverDrawsASetThePlayerDoesNotOwn() {
        wallet(CosmeticWallet.RANDOM, CosmeticAccess.REGULAR);
        Random random = new Random(3);

        for (int match = 0; match < 30; match++) {
            assertThat(messages.of(player, "room-" + match, random).id()).isEqualTo("padrao");
        }
    }

    @Test
    void aForgottenPlayerDrawsAgainInTheSameRoom() {
        wallet(CosmeticWallet.RANDOM, CosmeticAccess.REGULAR, "estatistico");
        Set<String> seen = new HashSet<>();
        Random random = new Random(5);

        for (int attempt = 0; attempt < 40; attempt++) {
            seen.add(messages.of(player, "AB12", random).id());
            messages.forget(player);
        }

        assertThat(seen).hasSize(2);
    }
}
