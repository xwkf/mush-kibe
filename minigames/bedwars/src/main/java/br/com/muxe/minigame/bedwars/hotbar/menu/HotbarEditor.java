package br.com.muxe.minigame.bedwars.hotbar.menu;

import br.com.muxe.core.bukkit.item.ItemBuilder;
import br.com.muxe.core.bukkit.menu.ClickContext;
import br.com.muxe.core.bukkit.menu.Menu;
import br.com.muxe.core.bukkit.menu.MenuButton;
import br.com.muxe.core.bukkit.menu.MenuClick;
import br.com.muxe.core.bukkit.menu.Page;
import br.com.muxe.core.bukkit.text.MessageCatalog;
import br.com.muxe.minigame.bedwars.hotbar.HotbarChoices;
import br.com.muxe.minigame.bedwars.hotbar.HotbarStore;
import br.com.muxe.minigame.bedwars.hotbar.domain.HotbarEntry;
import br.com.muxe.minigame.bedwars.hotbar.domain.HotbarLayout;
import br.com.muxe.minigame.bedwars.hotbar.menu.HotbarMenuSettings.Adding;
import br.com.muxe.minigame.bedwars.hotbar.menu.HotbarMenuSettings.Editing;
import br.com.muxe.minigame.bedwars.hotbar.menu.HotbarMenuSettings.Removing;
import br.com.muxe.minigame.bedwars.shop.ShopService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public final class HotbarEditor {

    public static final int FIRST_SLOT = 9;
    public static final List<Integer> ADDING_SLOTS = ShopService.ITEM_SLOTS;
    public static final List<Integer> REMOVING_SLOTS = List.of(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25);

    private static final int PANE_RED = 14;
    private static final int PANE_GREEN = 13;
    private static final int PANE_GRAY = 7;
    private static final int PANE_FIRST = 9;
    private static final int PANE_COUNT = 9;

    private final HotbarMenuSettings settings;
    private final HotbarChoices choices;
    private final HotbarIcons icons;
    private final HotbarStore hotbars;
    private final MessageCatalog messages;
    private final BiConsumer<Player, Menu> opener;

    public HotbarEditor(HotbarMenuSettings settings, HotbarChoices choices, HotbarIcons icons, HotbarStore hotbars, MessageCatalog messages) {
        this(settings, choices, icons, hotbars, messages, (player, menu) -> menu.open(player));
    }

    HotbarEditor(HotbarMenuSettings settings, HotbarChoices choices, HotbarIcons icons, HotbarStore hotbars, MessageCatalog messages,
                 BiConsumer<Player, Menu> opener) {
        this.settings = settings;
        this.choices = choices;
        this.icons = icons;
        this.hotbars = hotbars;
        this.messages = messages;
        this.opener = opener;
    }

    public void openFromBedWarsMenu(Player player, Consumer<Player> back) {
        open(player, new Back(settings.bedwarsMenu(), back));
    }

    public void openFromShop(Player player, Consumer<Player> back) {
        open(player, new Back(settings.shop(), back));
    }

    void open(Player player, Back back) {
        opener.accept(player, editing(player.getUniqueId(), back));
    }

    Menu editing(UUID id, Back back) {
        Editing texts = settings.editing();
        HotbarLayout layout = hotbars.of(id);
        Menu.Builder menu = Menu.builder(texts.title()).rows(texts.rows());
        for (int slot = 0; slot < HotbarLayout.SLOTS; slot++) {
            int chosen = slot;
            MenuClick click = context -> clickedSlot(context, chosen, back);
            List<HotbarEntry> entries = layout.entries(slot);
            if (entries.isEmpty()) {
                menu.button(FIRST_SLOT + slot, MenuButton.of(emptySlot(slot), click));
            } else {
                menu.button(FIRST_SLOT + slot, MenuButton.animated(frames(slot, entries), settings.frameTicks(), click));
            }
        }
        menu.button(texts.back().slot(), texts.back().item().replacing("destination", back.destination()), context -> back.action().accept(context.player()));
        return menu.build();
    }

    Menu adding(int slot, AddingTab tab, int requested, Back back) {
        Adding texts = settings.adding();
        Menu.Builder menu = Menu.builder(texts.title().replace("{slot}", Integer.toString(slot + 1))).rows(texts.rows());
        boolean categories = tab == AddingTab.CATEGORIES;
        menu.button(texts.categoriesTab().slot(), tabIcon(texts.categoriesTab().item(), categories), context -> show(context, adding(slot, AddingTab.CATEGORIES, 1, back)));
        menu.button(texts.itemsTab().slot(), tabIcon(texts.itemsTab().item(), !categories), context -> show(context, adding(slot, AddingTab.ITEMS, 1, back)));
        int openColumn = (categories ? texts.categoriesTab() : texts.itemsTab()).slot();
        for (int column = 0; column < PANE_COUNT; column++) {
            menu.button(PANE_FIRST + column, ItemBuilder.of(Material.STAINED_GLASS_PANE, column == openColumn ? PANE_GREEN : PANE_GRAY).name(" "));
        }
        Page<HotbarEntry> page = Page.of(categories ? categoryEntries() : itemEntries(), ADDING_SLOTS.size(), requested);
        for (int index = 0; index < page.items().size(); index++) {
            HotbarEntry entry = page.items().get(index);
            menu.button(ADDING_SLOTS.get(index), icons.described(entry, texts.add()), context -> choose(context, slot, entry, back));
        }
        int number = page.number();
        if (page.hasPrevious()) {
            menu.button(texts.previous().slot(), texts.previous().item().replacing("page", Integer.toString(number - 1)),
                context -> show(context, adding(slot, tab, number - 1, back)));
        } else {
            menu.button(texts.back().slot(), texts.back().item(), context -> open(context.player(), back));
        }
        if (page.hasNext()) {
            menu.button(texts.next().slot(), texts.next().item().replacing("page", Integer.toString(number + 1)),
                context -> show(context, adding(slot, tab, number + 1, back)));
        }
        return menu.build();
    }

    Menu removing(UUID id, int slot, Back back) {
        Removing texts = settings.removing();
        Menu.Builder menu = Menu.builder(texts.title().replace("{slot}", Integer.toString(slot + 1))).rows(texts.rows());
        List<HotbarEntry> entries = hotbars.of(id).entries(slot);
        for (int index = 0; index < entries.size() && index < REMOVING_SLOTS.size(); index++) {
            HotbarEntry entry = entries.get(index);
            menu.button(REMOVING_SLOTS.get(index), icons.described(entry, texts.remove()), context -> remove(context, slot, entry, back));
        }
        menu.button(texts.back().slot(), texts.back().item(), context -> open(context.player(), back));
        return menu.build();
    }

    public static List<String> slotLore(List<String> names, int shown, Editing texts) {
        List<String> lore = new ArrayList<>();
        lore.add(" ");
        lore.add(names.size() == 1 ? texts.selectedOne() : texts.selectedMany());
        for (int index = 0; index < names.size(); index++) {
            lore.add((index == shown ? texts.entryShown() : texts.entryOther()).replace("{name}", names.get(index)));
        }
        lore.add(" ");
        lore.add(texts.add());
        lore.add(texts.remove());
        return lore;
    }

    private List<ItemBuilder> frames(int slot, List<HotbarEntry> entries) {
        Editing texts = settings.editing();
        List<String> names = entries.stream().map(icons::name).toList();
        String title = texts.slotName().replace("{slot}", Integer.toString(slot + 1));
        List<ItemBuilder> frames = new ArrayList<>(entries.size());
        for (int shown = 0; shown < entries.size(); shown++) {
            frames.add(icons.icon(entries.get(shown)).name(title).lore(slotLore(names, shown, texts)));
        }
        return frames;
    }

    private ItemBuilder emptySlot(int slot) {
        Editing texts = settings.editing();
        return ItemBuilder.of(Material.STAINED_GLASS_PANE, PANE_RED)
            .name(texts.slotName().replace("{slot}", Integer.toString(slot + 1)))
            .lore(" ", texts.add())
            .showFlags();
    }

    private ItemBuilder tabIcon(ItemBuilder icon, boolean open) {
        return open ? icon : icon.lore(settings.adding().otherTab());
    }

    private List<HotbarEntry> categoryEntries() {
        return choices.categories().stream().map(category -> HotbarEntry.category(category.id())).toList();
    }

    private List<HotbarEntry> itemEntries() {
        return choices.items().stream().map(item -> HotbarEntry.item(item.id())).toList();
    }

    private void clickedSlot(ClickContext context, int slot, Back back) {
        Player player = context.player();
        HotbarLayout layout = hotbars.of(player.getUniqueId());
        if (context.isRightClick()) {
            if (!layout.entries(slot).isEmpty()) {
                opener.accept(player, removing(player.getUniqueId(), slot, back));
            }
            return;
        }
        if (!context.isLeftClick()) {
            return;
        }
        if (layout.isFull(slot)) {
            player.sendMessage(messages.get("hotbar-slot-full"));
            return;
        }
        opener.accept(player, adding(slot, AddingTab.CATEGORIES, 1, back));
    }

    private void choose(ClickContext context, int slot, HotbarEntry entry, Back back) {
        Player player = context.player();
        UUID id = player.getUniqueId();
        HotbarLayout layout = hotbars.of(id);
        if (!layout.entries(slot).contains(entry) && layout.isFull(slot)) {
            player.sendMessage(messages.get("hotbar-slot-full"));
            return;
        }
        HotbarLayout changed = layout.add(slot, entry);
        if (!changed.equals(layout)) {
            hotbars.change(id, changed);
        }
        open(player, back);
    }

    private void remove(ClickContext context, int slot, HotbarEntry entry, Back back) {
        Player player = context.player();
        UUID id = player.getUniqueId();
        HotbarLayout layout = hotbars.of(id);
        HotbarLayout changed = layout.remove(slot, entry);
        if (!changed.equals(layout)) {
            hotbars.change(id, changed);
        }
        if (changed.entries(slot).isEmpty()) {
            open(player, back);
        } else {
            opener.accept(player, removing(id, slot, back));
        }
    }

    private void show(ClickContext context, Menu menu) {
        opener.accept(context.player(), menu);
    }

    enum AddingTab {
        CATEGORIES,
        ITEMS
    }

    record Back(String destination, Consumer<Player> action) {
    }
}
