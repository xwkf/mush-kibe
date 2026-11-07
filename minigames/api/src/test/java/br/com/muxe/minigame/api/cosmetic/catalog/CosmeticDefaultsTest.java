package br.com.muxe.minigame.api.cosmetic.catalog;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.minigame.api.cosmetic.animation.SceneAnimations;
import br.com.muxe.minigame.api.cosmetic.aura.Auras;
import br.com.muxe.minigame.api.cosmetic.message.MessageSets;
import br.com.muxe.minigame.api.cosmetic.trail.ProjectileTrails;
import java.util.function.Predicate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class CosmeticDefaultsTest {

    private final CosmeticDefaults defaults = CosmeticDefaults.load();

    @Test
    void theCatalogComesWithTheApiAndEveryCosmeticPlays() {
        SceneAnimations victories = SceneAnimations.victories();
        SceneAnimations finalKills = SceneAnimations.finalKills();
        MessageSets sets = MessageSets.from(defaults.killMessages(null));

        assertThat(catalog(CosmeticKind.VICTORY_EFFECT, null, id -> victories.find(id).isPresent()).all()).hasSize(22).allMatch(Cosmetic::animated);
        assertThat(catalog(CosmeticKind.FINAL_KILL, null, id -> finalKills.find(id).isPresent()).all()).hasSize(43).allMatch(Cosmetic::animated);
        assertThat(catalog(CosmeticKind.AURA, null, id -> Auras.find(id).isPresent()).all()).hasSize(2).allMatch(Cosmetic::animated);
        assertThat(catalog(CosmeticKind.PROJECTILE_TRAIL, null, id -> ProjectileTrails.find(id).isPresent()).all()).hasSize(23).allMatch(Cosmetic::animated);
        assertThat(catalog(CosmeticKind.KILL_MESSAGE, null, id -> sets.find(id).isPresent()).all()).hasSize(2).allMatch(Cosmetic::animated);
    }

    @Test
    void eachGameOwnsItsCosmeticsInItsOwnCategory() {
        assertThat(catalog(CosmeticKind.VICTORY_EFFECT, null, id -> true).category()).isEqualTo("bedwars-vitoria");
        assertThat(defaults.catalog("duels", CosmeticKind.VICTORY_EFFECT, null, id -> true).category()).isEqualTo("duels-vitoria");
        assertThat(CosmeticKind.FINAL_KILL.category("bedwars")).isEqualTo("bedwars-kill-final");
        assertThat(CosmeticKind.AURA.category("bedwars")).isEqualTo("bedwars-aura");
        assertThat(CosmeticKind.PROJECTILE_TRAIL.category("bedwars")).isEqualTo("bedwars-rastro");
        assertThat(CosmeticKind.KILL_MESSAGE.category("bedwars")).isEqualTo("bedwars-mensagem-kill");
    }

    @Test
    void aGameCanChangeAPriceRenameOrHideACosmeticAndAddItsOwn() throws InvalidConfigurationException {
        YamlConfiguration game = yaml("""
            victory-effect:
              category: outra-coisa
              prices:
                "1": 10
              cosmetics:
                feliz-natal:
                  name: "&aPorco Voador do Jogo"
                tinta:
                  hidden: true
                exclusiva:
                  name: "&6Exclusiva"
                  icon:
                    material: GOLD_BLOCK
                  stars: 1
                  availability: SALE
            """);
        CosmeticCatalog base = catalog(CosmeticKind.VICTORY_EFFECT, null, id -> true);
        CosmeticCatalog changed = defaults.catalog("bedwars", CosmeticKind.VICTORY_EFFECT, game, id -> true);

        assertThat(changed.category()).isEqualTo("bedwars-vitoria");
        assertThat(base.find("tinta")).isPresent();
        assertThat(changed.find("tinta")).isEmpty();
        assertThat(changed.find("exclusiva")).hasValueSatisfying(cosmetic -> assertThat(changed.price(cosmetic)).isEqualTo(10));
        assertThat(changed.all()).hasSize(base.all().size());
    }

    @Test
    void killMessagesOfTheGameOverrideTheDefaultTexts() throws InvalidConfigurationException {
        ConfigurationSection merged = defaults.killMessages(yaml("""
            line-color: "&8"
            """));

        assertThat(merged.getString("line-color")).isEqualTo("&8");
        assertThat(merged.getConfigurationSection("sets")).isNotNull();
        assertThat(defaults.killMessages(null).getString("line-color")).isEqualTo("&7");
    }

    @Test
    void theScenesHaveAPreviewSectionAndTheMessagesDoNot() {
        assertThat(CosmeticKind.VICTORY_EFFECT.previewSection()).contains("victory-preview");
        assertThat(CosmeticKind.KILL_MESSAGE.previewSection()).isEmpty();
    }

    private CosmeticCatalog catalog(CosmeticKind kind, ConfigurationSection game, Predicate<String> animated) {
        return defaults.catalog("bedwars", kind, game, animated);
    }

    private static YamlConfiguration yaml(String text) throws InvalidConfigurationException {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.loadFromString(text);
        return yaml;
    }
}
