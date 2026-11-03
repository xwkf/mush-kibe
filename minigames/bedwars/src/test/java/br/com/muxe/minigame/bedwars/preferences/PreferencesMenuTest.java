package br.com.muxe.minigame.bedwars.preferences;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.minigame.bedwars.preferences.domain.Preferences.ChestDeposit;
import br.com.muxe.minigame.bedwars.preferences.menu.PreferencesMenu;
import br.com.muxe.minigame.bedwars.preferences.menu.PreferencesMenu.Settings;
import br.com.muxe.minigame.bedwars.room.RoomFixtures;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class PreferencesMenuTest {

    private final Settings settings = Settings.from(RoomFixtures.CONFIG.getConfigurationSection("preferences"));

    @Test
    void theMenuFollowsTheExport() {
        assertThat(settings.title()).isEqualTo("Preferências do Bed Wars");
        assertThat(settings.rows()).isEqualTo(4);
        assertThat(settings.showLevel().slot()).isEqualTo(11);
        assertThat(settings.showLevel().statusSlot()).isEqualTo(20);
        assertThat(settings.showLevel().material()).isEqualTo(Material.EXP_BOTTLE);
        assertThat(settings.tntTimer().slot()).isEqualTo(13);
        assertThat(settings.tntTimer().material()).isEqualTo(Material.WATCH);
        assertThat(settings.chestDeposit().slot()).isEqualTo(15);
        assertThat(settings.chestDeposit().statusSlot()).isEqualTo(24);
        assertThat(settings.back().slot()).isEqualTo(31);
        assertThat(settings.depositOptions().get(ChestDeposit.NO_HOLOGRAMS).data()).isEqualTo(12);
    }

    @Test
    void anOptionThatIsOnOffersToTurnItOff() {
        assertThat(PreferencesMenu.toggleLore(settings.showLevel().description(), true, settings)).containsExactly(
            "&7Escolha se o seu nível", "&7é mostrado no chat.", " ", "&7Valor: &aAtivado", " ", "&eClique para desativar.");
        assertThat(PreferencesMenu.toggleLore(settings.tntTimer().description(), false, settings)).containsExactly(
            "&7Escolha se o holograma", "&7temporizador da TNT aparece.", " ", "&7Valor: &cDesativado", " ", "&eClique para ativar.");
    }

    @Test
    void theChestOptionListsTheThreeChoicesAndTheNextOne() {
        assertThat(PreferencesMenu.chestLore(settings.chestDeposit().description(), ChestDeposit.NO_HOLOGRAMS, settings)).containsExactly(
            "&7Escolha se deseja guardar",
            "&7itens clicando no baú.",
            " ",
            "&7- &aAtivado",
            "&a- &bSem hologramas&7 (Selecionado)",
            "&7- &cDesativado",
            " ",
            "&eClique para trocar para &cDesativado."
        );
    }
}
