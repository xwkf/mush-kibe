package br.com.muxe.minigame.bedwars.cosmetic.trail;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.minigame.api.cosmetic.catalog.CosmeticCatalog;
import br.com.muxe.minigame.api.cosmetic.profile.CosmeticWallet;
import br.com.muxe.minigame.api.cosmetic.trail.FakeTrail;
import br.com.muxe.minigame.api.cosmetic.trail.ProjectileTrail;
import br.com.muxe.minigame.api.cosmetic.trail.ProjectileTrails;
import br.com.muxe.minigame.api.cosmetic.trail.TrailCosmetics;
import br.com.muxe.minigame.bedwars.room.RoomFixtures;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.random.RandomGenerator;
import org.junit.jupiter.api.Test;

class TrailCosmeticsTest {

    private static final RandomGenerator NEVER = () -> {
        throw new AssertionError("The trail must not be drawn again");
    };

    private final ProjectileTrail critical = new FakeTrail("critico", 0);
    private final ProjectileTrail lava = new FakeTrail("lava", 0);
    private final List<ProjectileTrail> registry = List.of(critical, lava);
    private final Function<String, Optional<ProjectileTrail>> find = id -> registry.stream().filter(trail -> trail.id().equals(id)).findFirst();
    private final Map<UUID, CosmeticWallet> wallets = new HashMap<>();
    private final UUID player = UUID.randomUUID();

    private TrailCosmetics cosmetics(Function<String, Optional<ProjectileTrail>> trails) {
        CosmeticCatalog catalog = CosmeticCatalog.from(RoomFixtures.CONFIG.getConfigurationSection("cosmetics.projectile-trail"), "cosmetics", "bedwars-rastro",
            id -> trails.apply(id).isPresent());
        return new TrailCosmetics(catalog, id -> Optional.ofNullable(wallets.get(id)), trails);
    }

    private void own(String selected, String... owned) {
        wallets.put(player, new CosmeticWallet(Set.of(owned), selected, 0, false));
    }

    @Test
    void aChosenTrailIsUsedAsLongAsThePlayerOwnsIt() {
        own("lava", "lava");

        assertThat(cosmetics(find).of(player, "AB12", NEVER)).contains(lava);
    }

    @Test
    void theDefaultTrailIsOwnedByEveryone() {
        own("critico");

        assertThat(cosmetics(find).of(player, "AB12", NEVER)).contains(critical);
    }

    @Test
    void randomDrawsOneOwnedTrailAndKeepsItForTheWholeMatch() {
        own(CosmeticWallet.RANDOM, "lava");
        TrailCosmetics cosmetics = cosmetics(find);
        ProjectileTrail first = cosmetics.of(player, "AB12", new Random(5)).orElseThrow();

        for (int launch = 0; launch < 30; launch++) {
            assertThat(cosmetics.of(player, "AB12", NEVER)).contains(first);
        }
    }

    @Test
    void randomDrawsAgainInTheNextMatchAmongTheOwnedTrails() {
        own(CosmeticWallet.RANDOM, "lava");
        TrailCosmetics cosmetics = cosmetics(find);
        Random random = new Random(9);
        Set<ProjectileTrail> seen = new HashSet<>();

        for (int match = 0; match < 40; match++) {
            seen.add(cosmetics.of(player, "room-" + match, random).orElseThrow());
        }

        assertThat(seen).containsExactlyInAnyOrder(critical, lava);
    }

    @Test
    void aForgottenPlayerDrawsAgain() {
        own(CosmeticWallet.RANDOM, "lava");
        TrailCosmetics cosmetics = cosmetics(find);
        cosmetics.of(player, "AB12", new Random(1));

        cosmetics.forget(player);

        assertThat(cosmetics.of(player, "AB12", new Random(2))).isPresent();
    }

    @Test
    void noneOrAPlayerWithoutProfileHasNoTrail() {
        own(CosmeticWallet.NONE, "lava");
        TrailCosmetics cosmetics = cosmetics(find);

        assertThat(cosmetics.of(player, "AB12", NEVER)).isEmpty();
        assertThat(cosmetics.of(UUID.randomUUID(), "AB12", NEVER)).isEmpty();
    }

    @Test
    void withAnEmptyRegistryNobodyHasATrailAndNothingBreaks() {
        own("lava", "lava", "chamas");
        TrailCosmetics cosmetics = cosmetics(id -> Optional.empty());

        assertThat(cosmetics.of(player, "AB12", new Random(1))).isEmpty();
        own(CosmeticWallet.RANDOM, "lava");
        assertThat(cosmetics.of(player, "CD34", new Random(1))).isEmpty();
        assertThat(cosmetics.find("lava")).isEmpty();
    }

    @Test
    void theRealRegistryOnlyHoldsTrailsOfTheCatalog() {
        CosmeticCatalog catalog = CosmeticCatalog.from(RoomFixtures.CONFIG.getConfigurationSection("cosmetics.projectile-trail"), "cosmetics", "bedwars-rastro",
            id -> true);

        ProjectileTrails.all().forEach(trail -> assertThat(catalog.find(trail.id())).as(trail.id()).isPresent());
    }
}
