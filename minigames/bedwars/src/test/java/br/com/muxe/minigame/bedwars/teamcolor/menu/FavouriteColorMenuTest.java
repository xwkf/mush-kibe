package br.com.muxe.minigame.bedwars.teamcolor.menu;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.item.ItemBuilder;
import br.com.muxe.core.bukkit.menu.ClickContext;
import br.com.muxe.core.bukkit.menu.Menu;
import br.com.muxe.core.bukkit.menu.MenuButton;
import br.com.muxe.core.bukkit.text.MessageCatalog;
import br.com.muxe.minigame.bedwars.room.RoomFixtures;
import br.com.muxe.minigame.bedwars.teamcolor.TeamColorStore;
import br.com.muxe.minigame.engine.team.TeamColor;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.junit.jupiter.api.Test;

class FavouriteColorMenuTest {

    private static final UUID PLAYER = new UUID(0, 9);
    private static final List<String> CHOOSE = List.of(" ", "§eClique para selecionar.");
    private static final List<String> CHOSEN = List.of(" ", "§aSelecionada!");

    private final FavouriteColorMenuSettings settings = FavouriteColorMenuSettings.from(RoomFixtures.CONFIG.getConfigurationSection("favourite-color"));
    private final MessageCatalog messages = MessageCatalog.from(RoomFixtures.CONFIG.getConfigurationSection("messages"));
    private final MemoryStore store = new MemoryStore();
    private final List<Menu> opened = new ArrayList<>();
    private final List<String> sent = new ArrayList<>();
    private final List<Player> returned = new ArrayList<>();
    private final Player player = player(sent);
    private final FavouriteColorMenu menu = new FavouriteColorMenu(settings, store, messages, (viewer, built) -> opened.add(built));

    private Menu built() {
        return menu.build(PLAYER, returned::add);
    }

    private static ItemBuilder icon(Menu menu, int slot) {
        return menu.button(slot).orElseThrow(() -> new AssertionError("No button at slot " + slot)).icon();
    }

    private void click(Menu menu, int slot) {
        MenuButton button = menu.button(slot).orElseThrow(() -> new AssertionError("No button at slot " + slot));
        button.click().click(new ClickContext(player, ClickType.LEFT, slot, null));
    }

    @Test
    void theMenuFollowsTheReference() {
        Menu menu = built();

        assertThat(menu.title()).isEqualTo("Cor preferida (1/1)");
        assertThat(menu.rows()).isEqualTo(6);
        assertThat(settings.colors().keySet()).containsExactly(TeamColor.VERMELHO, TeamColor.AZUL, TeamColor.VERDE, TeamColor.AMARELO,
            TeamColor.CIANO, TeamColor.BRANCO, TeamColor.LARANJA, TeamColor.ROSA, TeamColor.CINZA);
        assertThat(menu.buttons().keySet()).containsExactlyInAnyOrder(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 48);
    }

    @Test
    void everyColourIsAWoolOfItsTeamWithItsColouredName() {
        Menu menu = built();
        Map<Integer, String> names = Map.of(11, "§cVermelho", 12, "§9Azul", 13, "§aVerde", 14, "§eAmarelo", 15, "§bCiano",
            16, "§fBranco", 19, "§6Laranja", 20, "§dRosa", 21, "§8Cinza");
        Map<Integer, Integer> data = Map.of(11, 14, 12, 11, 13, 5, 14, 4, 15, 9, 16, 0, 19, 1, 20, 6, 21, 7);

        names.forEach((slot, name) -> {
            ItemBuilder wool = icon(menu, slot);

            assertThat(wool.material()).isEqualTo(Material.WOOL);
            assertThat(wool.data()).isEqualTo(data.get(slot).shortValue());
            assertThat(wool.name()).isEqualTo(name);
        });
    }

    @Test
    void withNoChoiceNenhumaIsTheSelectedOne() {
        Menu menu = built();

        ItemBuilder none = icon(menu, 10);
        assertThat(none.material()).isEqualTo(Material.BARRIER);
        assertThat(none.name()).isEqualTo("§aNenhuma");
        assertThat(none.lore()).isEqualTo(CHOSEN);
        assertThat(none.isGlowing()).isTrue();
        assertThat(none.hidesFlags()).isFalse();
        for (int slot : List.of(11, 12, 13, 14, 15, 16, 19, 20, 21)) {
            assertThat(icon(menu, slot).lore()).isEqualTo(CHOOSE);
            assertThat(icon(menu, slot).isGlowing()).isFalse();
            assertThat(icon(menu, slot).hidesFlags()).isFalse();
        }
    }

    @Test
    void theChosenColourGlowsAndNenhumaInvitesToClick() {
        store.colors.put(PLAYER, TeamColor.ROSA);

        Menu menu = built();

        assertThat(icon(menu, 20).lore()).isEqualTo(CHOSEN);
        assertThat(icon(menu, 20).isGlowing()).isTrue();
        assertThat(icon(menu, 10).lore()).isEqualTo(CHOOSE);
        assertThat(icon(menu, 10).isGlowing()).isFalse();
        assertThat(icon(menu, 11).isGlowing()).isFalse();
    }

    @Test
    void clickingAColourSavesItTellsThePlayerAndRefreshesTheMenu() {
        click(built(), 13);

        assertThat(store.colors).containsEntry(PLAYER, TeamColor.VERDE);
        assertThat(store.changes).containsExactly(Optional.of(TeamColor.VERDE));
        assertThat(sent).containsExactly("§aVocê selecionou §f§aVerde§a.");
        assertThat(opened).hasSize(1);
        assertThat(icon(opened.getFirst(), 13).lore()).isEqualTo(CHOSEN);
        assertThat(icon(opened.getFirst(), 10).lore()).isEqualTo(CHOOSE);
    }

    @Test
    void clickingNenhumaForgetsTheColour() {
        store.colors.put(PLAYER, TeamColor.AZUL);

        click(built(), 10);

        assertThat(store.colors).doesNotContainKey(PLAYER);
        assertThat(store.changes).containsExactly(Optional.empty());
        assertThat(sent).containsExactly("§aVocê selecionou §f§aNenhuma§a.");
        assertThat(icon(opened.getFirst(), 10).lore()).isEqualTo(CHOSEN);
    }

    @Test
    void clickingTheColourThatIsAlreadyChosenDoesNothing() {
        store.colors.put(PLAYER, TeamColor.AZUL);

        click(built(), 12);

        assertThat(store.changes).isEmpty();
        assertThat(sent).isEmpty();
        assertThat(opened).isEmpty();
    }

    @Test
    void switchingFromOneColourToAnotherKeepsOnlyTheLastOne() {
        click(built(), 11);
        click(opened.getLast(), 14);

        assertThat(store.colors).containsEntry(PLAYER, TeamColor.AMARELO);
        assertThat(icon(opened.getLast(), 11).isGlowing()).isFalse();
        assertThat(icon(opened.getLast(), 14).isGlowing()).isTrue();
    }

    @Test
    void theBackButtonLeadsToTheCosmeticsMenu() {
        Menu menu = built();

        ItemBuilder back = icon(menu, 48);
        assertThat(back.material()).isEqualTo(Material.ARROW);
        assertThat(back.name()).isEqualTo("§aVoltar");
        assertThat(back.lore()).containsExactly("§7Para Cosméticos do Bed Wars");
        click(menu, 48);
        assertThat(returned).containsExactly(player);
    }

    private static Player player(List<String> messages) {
        return (Player) Proxy.newProxyInstance(FavouriteColorMenuTest.class.getClassLoader(), new Class<?>[] {Player.class}, (proxy, method, args) -> {
            switch (method.getName()) {
                case "getUniqueId" -> {
                    return PLAYER;
                }
                case "sendMessage" -> {
                    if (args[0] instanceof String text) {
                        messages.add(text);
                    }
                    return null;
                }
                case "hashCode" -> {
                    return PLAYER.hashCode();
                }
                case "equals" -> {
                    return proxy == args[0];
                }
                case "toString" -> {
                    return "FakePlayer";
                }
                default -> throw new UnsupportedOperationException(method.getName());
            }
        });
    }

    private static final class MemoryStore implements TeamColorStore {

        private final Map<UUID, TeamColor> colors = new HashMap<>();
        private final List<Optional<TeamColor>> changes = new ArrayList<>();

        @Override
        public Optional<TeamColor> of(UUID playerId) {
            return Optional.ofNullable(colors.get(playerId));
        }

        @Override
        public void change(UUID playerId, Optional<TeamColor> color) {
            changes.add(color);
            color.ifPresentOrElse(chosen -> colors.put(playerId, chosen), () -> colors.remove(playerId));
        }
    }
}
