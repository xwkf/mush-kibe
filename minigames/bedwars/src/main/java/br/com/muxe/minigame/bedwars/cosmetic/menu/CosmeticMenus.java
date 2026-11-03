package br.com.muxe.minigame.bedwars.cosmetic.menu;

import br.com.muxe.core.bukkit.credit.CreditFormat;
import br.com.muxe.core.bukkit.item.ItemBuilder;
import br.com.muxe.core.bukkit.menu.ClickContext;
import br.com.muxe.core.bukkit.menu.Menu;
import br.com.muxe.core.bukkit.menu.MenuButton;
import br.com.muxe.core.bukkit.menu.MenuClick;
import br.com.muxe.core.bukkit.menu.Page;
import br.com.muxe.core.bukkit.text.MessageCatalog;
import br.com.muxe.core.common.cosmetic.application.command.BuyCosmeticCommand;
import br.com.muxe.core.common.cosmetic.application.command.BuyCosmeticHandler;
import br.com.muxe.core.common.cosmetic.application.command.SelectCosmeticCommand;
import br.com.muxe.core.common.cqrs.command.CommandBus;
import br.com.muxe.core.common.result.Result;
import br.com.muxe.minigame.bedwars.cosmetic.catalog.BedCosmetic;
import br.com.muxe.minigame.bedwars.cosmetic.profile.CosmeticAccess;
import br.com.muxe.minigame.bedwars.cosmetic.profile.CosmeticState;
import br.com.muxe.minigame.bedwars.cosmetic.profile.CosmeticWallet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.Player;

public final class CosmeticMenus {

    private static final int SPECIAL_ENTRIES = 2;
    static final int RANDOM_FRAME_TICKS = 10;

    private final CosmeticMenuSettings settings;
    private final Map<String, CosmeticCategory> categories = new LinkedHashMap<>();
    private final CommandBus commands;
    private final Executor mainThread;
    private final MessageCatalog messages;
    private final Logger logger;
    private final Map<UUID, Boolean> categoriesFromMenu = new ConcurrentHashMap<>();
    private BiConsumer<Player, Consumer<Player>> quickBuy = (player, back) -> {
    };
    private BiConsumer<Player, Consumer<Player>> preferences = (player, back) -> {
    };
    private BiConsumer<Player, Optional<Consumer<Player>>> missions = (player, back) -> {
    };

    public CosmeticMenus(CosmeticMenuSettings settings, List<CosmeticCategory> categories, CommandBus commands, Executor mainThread, MessageCatalog messages, Logger logger) {
        this.settings = settings;
        categories.forEach(category -> this.categories.put(category.key(), category));
        this.commands = commands;
        this.mainThread = mainThread;
        this.messages = messages;
        this.logger = logger;
    }

    public void onMissions(BiConsumer<Player, Optional<Consumer<Player>>> opener) {
        this.missions = opener;
    }

    public void onQuickBuy(BiConsumer<Player, Consumer<Player>> opener) {
        this.quickBuy = opener;
    }

    public void onPreferences(BiConsumer<Player, Consumer<Player>> opener) {
        this.preferences = opener;
    }

    public void openBedWars(Player player) {
        Menu.Builder menu = Menu.builder(settings.bedwarsTitle()).rows(settings.bedwarsRows());
        settings.bedwarsItems().forEach(menu::button);
        settings.missions().ifPresent(button -> menu.button(button.slot(), button.item(),
            context -> missions.accept(context.player(), Optional.of(this::openBedWars))));
        settings.quickBuy().ifPresent(button -> menu.button(button.slot(), button.item(),
            context -> quickBuy.accept(context.player(), this::openBedWars)));
        settings.preferences().ifPresent(button -> menu.button(button.slot(), button.item(),
            context -> preferences.accept(context.player(), this::openBedWars)));
        settings.bedwarsSoon().forEach((slot, item) -> menu.button(slot, item, context -> context.player().sendMessage(messages.get("coming-soon"))));
        menu.button(settings.cosmetics().slot(), settings.cosmetics().item(), context -> openCategories(context.player(), true))
            .build()
            .open(player);
    }

    public void openCategories(Player player) {
        openCategories(player, false);
    }

    void openCategories(Player player, boolean fromBedWarsMenu) {
        categoriesFromMenu.put(player.getUniqueId(), fromBedWarsMenu);
        withWallets(player, wallets -> {
            Menu.Builder menu = Menu.builder(settings.categoriesTitle()).rows(settings.categoriesRows());
            for (CosmeticCategory category : categories.values()) {
                CosmeticWallet wallet = wallets.getOrDefault(category.key(), CosmeticWallet.EMPTY);
                menu.button(category.menu().button().slot(), category.icons().category(wallet), context -> openList(context.player(), category.key(), 1));
            }
            settings.soon().forEach((slot, item) -> menu.button(slot, item, context -> context.player().sendMessage(messages.get("coming-soon"))));
            if (fromBedWarsMenu) {
                menu.button(settings.categoriesBack().slot(), settings.categoriesBack().item(), context -> openBedWars(context.player()));
            }
            menu.build().open(player);
        });
    }

    public void openList(Player player, String key, int page) {
        CosmeticCategory category = categories.get(key);
        if (category == null) {
            return;
        }
        withWallet(player, category, wallet -> list(category, wallet, page).open(player));
    }

    Menu list(CosmeticCategory category, CosmeticWallet wallet, int requested) {
        CosmeticIcons icons = category.icons();
        CosmeticListSettings list = category.list();
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(icons.none(wallet), List.of(), context -> choose(context, category, CosmeticWallet.NONE, icons.selectedName(wallet.selecting(CosmeticWallet.NONE)))));
        entries.add(new Entry(icons.random(wallet), icons.randomFrames(wallet), context -> choose(context, category, CosmeticWallet.RANDOM, icons.selectedName(wallet.selecting(CosmeticWallet.RANDOM)))));
        for (BedCosmetic cosmetic : wallet.sorted(category.catalog())) {
            entries.add(new Entry(icons.cosmetic(cosmetic, wallet), List.of(), context -> clicked(context, category, cosmetic)));
        }
        Page<Entry> page = Page.of(entries, list.slots().size(), requested);
        String title = list.title().replace("{page}", Integer.toString(page.number())).replace("{pages}", Integer.toString(page.total()));
        Menu.Builder menu = Menu.builder(title).rows(list.rows());
        for (int index = 0; index < page.items().size(); index++) {
            Entry entry = page.items().get(index);
            MenuClick click = entry.click()::accept;
            menu.button(list.slots().get(index), entry.frames().isEmpty() ? MenuButton.of(entry.icon(), click) : MenuButton.animated(entry.frames(), RANDOM_FRAME_TICKS, click));
        }
        int number = page.number();
        String key = category.key();
        if (page.hasPrevious()) {
            menu.button(list.previous().slot(), list.previous().item().replacing("page", Integer.toString(number - 1)), context -> openList(context.player(), key, number - 1));
        } else {
            menu.button(list.back().slot(), list.back().item(), context -> openCategories(context.player(), categoriesFromMenu.getOrDefault(context.player().getUniqueId(), false)));
        }
        if (page.hasNext()) {
            menu.button(list.next().slot(), list.next().item().replacing("page", Integer.toString(number + 1)), context -> openList(context.player(), key, number + 1));
        }
        menu.button(list.selected().slot(), icons.selected(wallet));
        menu.button(list.shop().slot(), list.shop().item(), context -> context.player().sendMessage(messages.get("coming-soon")));
        return menu.build();
    }

    static int pageOf(int entryIndex, int perPage) {
        return entryIndex / perPage + 1;
    }

    private void clicked(ClickContext context, CosmeticCategory category, BedCosmetic cosmetic) {
        Player player = context.player();
        CosmeticWallet wallet = category.profiles().of(player.getUniqueId()).orElse(CosmeticWallet.EMPTY);
        if (context.isRightClick()) {
            if (!cosmetic.animated()) {
                player.sendMessage(messages.get("cosmetic-no-preview"));
                return;
            }
            int page = currentPage(player, category, cosmetic.id());
            player.closeInventory();
            category.previews().preview(player, cosmetic, viewer -> openList(viewer, category.key(), page));
            return;
        }
        CosmeticState state = wallet.state(cosmetic);
        switch (state) {
            case OWNED -> choose(context, category, cosmetic.id(), cosmetic.name());
            case BUY -> buy(context, category, cosmetic);
            case UNAVAILABLE -> player.sendMessage(messages.get("cosmetic-unavailable"));
            case SELECTED -> {
            }
        }
    }

    private void choose(ClickContext context, CosmeticCategory category, String id, String name) {
        Player player = context.player();
        int page = currentPage(player, category, id);
        commands.dispatch(new SelectCosmeticCommand(player.getUniqueId(), category.catalog().category(), id)).whenCompleteAsync((result, error) -> {
            if (!player.isOnline()) {
                return;
            }
            if (error != null || result.isFailure()) {
                failed(player, error);
                return;
            }
            category.profiles().of(player.getUniqueId()).ifPresent(wallet -> category.profiles().update(player.getUniqueId(), wallet.selecting(id)));
            player.sendMessage(messages.get("cosmetic-selected", "name", name));
            openList(player, category.key(), page);
        }, mainThread);
    }

    private void buy(ClickContext context, CosmeticCategory category, BedCosmetic cosmetic) {
        Player player = context.player();
        long price = category.catalog().price(cosmetic);
        int page = currentPage(player, category, cosmetic.id());
        commands.dispatch(new BuyCosmeticCommand(player.getUniqueId(), category.catalog().category(), cosmetic.id(), price)).whenCompleteAsync((result, error) -> {
            if (!player.isOnline()) {
                return;
            }
            if (error != null) {
                failed(player, error);
                return;
            }
            if (result instanceof Result.Failure<Long> failure) {
                String messageKey = BuyCosmeticHandler.ALREADY_OWNED.equals(failure.problem().code()) ? "cosmetic-already-owned" : "cosmetic-not-enough";
                player.sendMessage(messages.get(messageKey));
                return;
            }
            long balance = result.orElseThrow();
            categories.values().forEach(other -> other.profiles().of(player.getUniqueId())
                .ifPresent(wallet -> other.profiles().update(player.getUniqueId(), other == category ? wallet.buying(cosmetic.id(), balance) : wallet.withCredits(balance))));
            player.sendMessage(messages.get("cosmetic-bought", "name", cosmetic.name(), "price", CreditFormat.format(price)));
            openList(player, category.key(), page);
        }, mainThread);
    }

    private int currentPage(Player player, CosmeticCategory category, String id) {
        CosmeticWallet wallet = category.profiles().of(player.getUniqueId()).orElse(CosmeticWallet.EMPTY);
        List<BedCosmetic> sorted = wallet.sorted(category.catalog());
        for (int index = 0; index < sorted.size(); index++) {
            if (sorted.get(index).id().equals(id)) {
                return pageOf(index + SPECIAL_ENTRIES, category.list().slots().size());
            }
        }
        return 1;
    }

    private void withWallet(Player player, CosmeticCategory category, Consumer<CosmeticWallet> action) {
        category.profiles().load(player.getUniqueId(), CosmeticAccess.of(player::hasPermission)).whenCompleteAsync((wallet, error) -> {
            if (!player.isOnline()) {
                return;
            }
            if (error != null) {
                failed(player, error);
                return;
            }
            action.accept(wallet);
        }, mainThread);
    }

    private void withWallets(Player player, Consumer<Map<String, CosmeticWallet>> action) {
        CosmeticAccess access = CosmeticAccess.of(player::hasPermission);
        Map<String, CompletableFuture<CosmeticWallet>> loading = new LinkedHashMap<>();
        categories.forEach((key, category) -> loading.put(key, category.profiles().load(player.getUniqueId(), access)));
        CompletableFuture.allOf(loading.values().toArray(CompletableFuture[]::new)).whenCompleteAsync((ignored, error) -> {
            if (!player.isOnline()) {
                return;
            }
            if (error != null) {
                failed(player, error);
                return;
            }
            Map<String, CosmeticWallet> wallets = new LinkedHashMap<>();
            loading.forEach((key, future) -> wallets.put(key, future.join()));
            action.accept(wallets);
        }, mainThread);
    }

    private void failed(Player player, Throwable error) {
        if (error != null) {
            logger.log(Level.WARNING, "A cosmetic action of " + player.getName() + " failed", error);
        }
        player.sendMessage(messages.get("cosmetic-failed"));
    }

    @FunctionalInterface
    public interface PreviewGate {

        void preview(Player player, BedCosmetic cosmetic, Consumer<Player> back);
    }

    private record Entry(ItemBuilder icon, List<ItemBuilder> frames, Consumer<ClickContext> click) {
    }
}
