package br.com.muxe.minigame.bedwars.shop;

import br.com.muxe.core.bukkit.item.ItemBuilder;
import br.com.muxe.core.bukkit.menu.Menu;
import br.com.muxe.core.bukkit.menu.Page;
import br.com.muxe.core.bukkit.prompt.AnvilPrompts;
import br.com.muxe.core.bukkit.text.MessageCatalog;
import br.com.muxe.minigame.bedwars.cosmetic.menu.MenuSlot;
import br.com.muxe.minigame.bedwars.quickbuy.domain.QuickBuy;
import br.com.muxe.minigame.bedwars.shop.ShopCatalog.ShopItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public final class QuickBuyEditor {

    public static final List<Integer> SLOTS = List.of(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34);

    private static final int PANE_RED = 14;

    private final ShopCatalog shop;
    private final Settings settings;
    private final QuickBuys quickBuys;
    private final MessageCatalog messages;
    private final AnvilPrompts anvils;
    private final Executor mainThread;
    private final Logger logger;

    public QuickBuyEditor(ShopCatalog shop, Settings settings, QuickBuys quickBuys, MessageCatalog messages, AnvilPrompts anvils, Executor mainThread,
                          Logger logger) {
        this.shop = shop;
        this.settings = settings;
        this.quickBuys = quickBuys;
        this.messages = messages;
        this.anvils = anvils;
        this.mainThread = mainThread;
        this.logger = logger;
    }

    public void open(Player player, Consumer<Player> back) {
        UUID id = player.getUniqueId();
        QuickBuy layout = quickBuys.of(id);
        Menu.Builder menu = Menu.builder(settings.title()).rows(settings.rows());
        for (int position = 0; position < QuickBuy.SIZE; position++) {
            int chosen = position;
            Optional<ShopItem> item = layout.at(position).map(shop.items()::get);
            ItemBuilder icon = item
                .map(found -> look(found).lore(slotLore(found.editor().lore(), settings)))
                .orElseGet(() -> ItemBuilder.of(Material.STAINED_GLASS_PANE, PANE_RED).name(shop.quickBuyTexts().emptyName()).lore(" ", settings.swap()));
            menu.button(SLOTS.get(position), icon, context -> {
                if (context.isRightClick() && item.isPresent()) {
                    quickBuys.change(id, quickBuys.of(id).remove(item.get().id()));
                    open(context.player(), back);
                    return;
                }
                openPicker(context.player(), chosen, 1, back);
            });
        }
        menu.button(settings.back().slot(), settings.back().item(), context -> back.accept(context.player()));
        menu.button(settings.importButton().slot(), settings.importButton().item(), context -> askImport(context.player(), back));
        menu.build().open(player);
    }

    public void openPicker(Player player, int position, int requested, Consumer<Player> back) {
        Page<String> page = Page.of(shop.sellable(), SLOTS.size(), requested);
        String title = settings.pickerTitle().replace("{page}", Integer.toString(page.number())).replace("{pages}", Integer.toString(page.total()));
        Menu.Builder menu = Menu.builder(title).rows(settings.rows());
        for (int index = 0; index < page.items().size(); index++) {
            ShopItem item = shop.items().get(page.items().get(index));
            menu.button(SLOTS.get(index), look(item).lore(pickerLore(item.editor().lore(), settings)), context -> {
                UUID id = context.player().getUniqueId();
                quickBuys.change(id, quickBuys.of(id).place(position, item.id()));
                open(context.player(), back);
            });
        }
        int number = page.number();
        if (page.hasPrevious()) {
            menu.button(settings.previous().slot(), settings.previous().item().replacing("page", Integer.toString(number - 1)),
                context -> openPicker(context.player(), position, number - 1, back));
        } else {
            menu.button(settings.pickerBack().slot(), settings.pickerBack().item(), context -> open(context.player(), back));
        }
        if (page.hasNext()) {
            menu.button(settings.next().slot(), settings.next().item().replacing("page", Integer.toString(number + 1)),
                context -> openPicker(context.player(), position, number + 1, back));
        }
        menu.build().open(player);
    }

    private void askImport(Player player, Consumer<Player> back) {
        anvils.ask(player, settings.importPrompt(), username -> quickBuys.find(username).whenCompleteAsync((found, error) -> {
            if (!player.isOnline()) {
                return;
            }
            if (error != null) {
                logger.log(Level.WARNING, "Could not find the quick buy of " + username, error);
                player.sendMessage(messages.get("cosmetic-failed"));
                return;
            }
            openImport(player, username, found.orElse(QuickBuy.of(List.of())), back);
        }, mainThread));
    }

    public void openImport(Player player, String username, QuickBuy imported, Consumer<Player> back) {
        Menu.Builder menu = Menu.builder(settings.importTitle()).rows(settings.rows());
        for (int position = 0; position < QuickBuy.SIZE; position++) {
            Optional<ShopItem> item = imported.at(position).map(shop.items()::get);
            menu.button(SLOTS.get(position), item.map(this::look)
                .orElseGet(() -> ItemBuilder.of(Material.STAINED_GLASS_PANE, PANE_RED).name(shop.quickBuyTexts().emptyName())));
        }
        menu.button(settings.cancel().slot(), settings.cancel().item(), context -> open(context.player(), back));
        menu.button(settings.confirm().slot(), settings.confirm().item().replacing("player", username), context -> {
            quickBuys.change(context.player().getUniqueId(), imported);
            open(context.player(), back);
        });
        menu.build().open(player);
    }

    private ItemBuilder look(ShopItem item) {
        return ShopLook.icon(item, settings.name());
    }

    static List<String> slotLore(List<String> description, Settings settings) {
        List<String> lore = described(description);
        lore.add(settings.swap());
        lore.add(settings.remove());
        return lore;
    }

    static List<String> pickerLore(List<String> description, Settings settings) {
        List<String> lore = described(description);
        lore.add(settings.select());
        return lore;
    }

    private static List<String> described(List<String> description) {
        List<String> lore = new ArrayList<>();
        lore.add(" ");
        if (!description.isEmpty()) {
            lore.addAll(description);
            lore.add(" ");
        }
        return lore;
    }

    public record Settings(String title, String pickerTitle, int rows, String name, String swap, String remove, String select,
                           MenuSlot back, MenuSlot pickerBack, MenuSlot previous, MenuSlot next, MenuSlot importButton,
                           String importPrompt, String importTitle, MenuSlot cancel, MenuSlot confirm) {

        public static Settings from(ConfigurationSection section) {
            return new Settings(
                section.getString("title", "Editando compra rápida..."),
                section.getString("picker-title", "Selecionando item... ({page}/{pages})"),
                section.getInt("rows", 5),
                section.getString("name", "&a{name}"),
                section.getString("swap"),
                section.getString("remove"),
                section.getString("select"),
                MenuSlot.from(section(section, "back")),
                MenuSlot.from(section(section, "picker-back")),
                MenuSlot.from(section(section, "previous")),
                MenuSlot.from(section(section, "next")),
                MenuSlot.from(section(section, "import")),
                section.getString("import-prompt", "Insira o usuário"),
                section.getString("import-title", "Importando compra rápida..."),
                MenuSlot.from(section(section, "cancel")),
                MenuSlot.from(section(section, "confirm"))
            );
        }

        private static ConfigurationSection section(ConfigurationSection parent, String path) {
            return Objects.requireNonNull(parent.getConfigurationSection(path), "Missing configuration section " + parent.getCurrentPath() + "." + path);
        }
    }
}
