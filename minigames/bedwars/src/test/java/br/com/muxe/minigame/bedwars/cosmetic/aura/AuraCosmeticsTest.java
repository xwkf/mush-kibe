package br.com.muxe.minigame.bedwars.cosmetic.aura;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.minigame.api.cosmetic.aura.Aura;
import br.com.muxe.minigame.api.cosmetic.aura.AuraCosmetics;
import br.com.muxe.minigame.api.cosmetic.aura.Auras;
import br.com.muxe.minigame.api.cosmetic.catalog.CosmeticCatalog;
import br.com.muxe.minigame.api.cosmetic.catalog.CosmeticKind;
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

class AuraCosmeticsTest {

    private static final RandomGenerator NEVER = () -> {
        throw new AssertionError("The aura must not be drawn again");
    };

    private final CosmeticCatalog catalog = RoomFixtures.COSMETICS.catalog("bedwars", CosmeticKind.AURA, RoomFixtures.CONFIG.getConfigurationSection("cosmetics"),
        id -> Auras.find(id).isPresent());
    private final Map<UUID, CosmeticWallet> wallets = new HashMap<>();
    private final AuraCosmetics cosmetics = new AuraCosmetics(catalog, id -> Optional.ofNullable(wallets.get(id)));
    private final UUID player = UUID.randomUUID();

    private void own(String selected, String... owned) {
        wallets.put(player, new CosmeticWallet(Set.of(owned), selected, 0, false));
    }

    @Test
    void aChosenAuraIsUsedAsLongAsThePlayerOwnsIt() {
        own("liga-diamante", "liga-mestre", "liga-diamante");

        assertThat(cosmetics.of(player, "AB12", NEVER)).contains(Auras.DIAMOND_LEAGUE);
    }

    @Test
    void randomDrawsOneOwnedAuraAndKeepsItForTheWholeMatch() {
        own(CosmeticWallet.RANDOM, "liga-mestre", "liga-diamante");
        Aura first = cosmetics.of(player, "AB12", new Random(11)).orElseThrow();

        for (int tick = 0; tick < 50; tick++) {
            assertThat(cosmetics.of(player, "AB12", NEVER)).contains(first);
        }
    }

    @Test
    void randomDrawsAgainInTheNextMatchAndEveryOwnedAuraCanComeUp() {
        own(CosmeticWallet.RANDOM, "liga-mestre", "liga-diamante");
        Random random = new Random(7);
        Set<Aura> seen = new HashSet<>();

        for (int match = 0; match < 40; match++) {
            seen.add(cosmetics.of(player, "room-" + match, random).orElseThrow());
        }

        assertThat(seen).containsExactlyInAnyOrder(Auras.MASTER_LEAGUE, Auras.DIAMOND_LEAGUE);
    }

    @Test
    void randomOnlyDrawsAmongTheAurasThePlayerOwns() {
        own(CosmeticWallet.RANDOM, "liga-diamante");

        for (int match = 0; match < 20; match++) {
            assertThat(cosmetics.of(player, "room-" + match, new Random(match))).contains(Auras.DIAMOND_LEAGUE);
        }
    }

    @Test
    void nonePicksNothingAndSoDoesARandomWithoutAnyOwnedAura() {
        own(CosmeticWallet.NONE, "liga-mestre");
        assertThat(cosmetics.of(player, "AB12", NEVER)).isEmpty();

        UUID poor = UUID.randomUUID();
        wallets.put(poor, new CosmeticWallet(Set.of(), CosmeticWallet.RANDOM, 0, false));
        assertThat(cosmetics.of(poor, "AB12", new Random(1))).isEmpty();
    }

    @Test
    void anAuraTheyDoNotOwnIsNeverUsedEvenIfSelected() {
        own("liga-mestre");

        assertThat(cosmetics.of(player, "AB12", new Random(1))).isEmpty();
    }

    @Test
    void aWalletThatHasNotLoadedYetIsAskedAgainOnTheNextTick() {
        assertThat(cosmetics.of(player, "AB12", NEVER)).isEmpty();

        own("liga-mestre", "liga-mestre");

        assertThat(cosmetics.of(player, "AB12", NEVER)).contains(Auras.MASTER_LEAGUE);
    }

    @Test
    void aPlayerWhoLeftTheMatchIsForgottenAndDrawsAgainOnReturn() {
        own(CosmeticWallet.RANDOM, "liga-mestre", "liga-diamante");
        cosmetics.of(player, "AB12", new Random(3));
        cosmetics.retain(Set.of());
        own("liga-mestre", "liga-mestre");

        assertThat(cosmetics.of(player, "AB12", NEVER)).contains(Auras.MASTER_LEAGUE);
    }
}
