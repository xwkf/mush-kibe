package br.com.muxe.minigame.bedwars.cosmetic.menu;

import br.com.muxe.core.bukkit.credit.CreditFormat;
import br.com.muxe.core.bukkit.item.ItemBuilder;
import br.com.muxe.minigame.bedwars.cosmetic.catalog.BedCosmetic;
import br.com.muxe.minigame.bedwars.cosmetic.catalog.CosmeticCatalog;
import br.com.muxe.minigame.bedwars.cosmetic.menu.CosmeticMenuSettings.CategoryMenu;
import br.com.muxe.minigame.bedwars.cosmetic.menu.CosmeticMenuSettings.LoreTexts;
import br.com.muxe.minigame.bedwars.cosmetic.profile.CosmeticState;
import br.com.muxe.minigame.bedwars.cosmetic.profile.CosmeticWallet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class CosmeticIcons {

    public static final String BLANK = " ";

    private final CategoryMenu menu;
    private final CosmeticListSettings settings;
    private final CosmeticCatalog catalog;

    public CosmeticIcons(CategoryMenu menu, CosmeticCatalog catalog) {
        this.menu = menu;
        this.settings = menu.list();
        this.catalog = catalog;
    }

    public ItemBuilder cosmetic(BedCosmetic cosmetic, CosmeticWallet wallet) {
        return cosmetic(cosmetic, wallet, cosmetic.description());
    }

    public ItemBuilder cosmetic(BedCosmetic cosmetic, CosmeticWallet wallet, List<String> description) {
        LoreTexts texts = settings.lore();
        CosmeticState state = effective(wallet).state(cosmetic);
        boolean owned = state == CosmeticState.SELECTED || state == CosmeticState.OWNED;
        List<String> lore = new ArrayList<>(texts.header());
        if (cosmetic.stars() > 0) {
            lore.add(stars(cosmetic.stars()));
            lore.add(BLANK);
        }
        if (!description.isEmpty()) {
            lore.addAll(description);
            lore.add(BLANK);
        }
        Optional<String> tag = tag(cosmetic, texts);
        if (tag.isPresent()) {
            lore.add(tag.get());
            lore.add(BLANK);
        }
        long price = state == CosmeticState.BUY ? catalog.price(cosmetic) : 0;
        List<String> block = switch (state) {
            case SELECTED -> texts.selectedWithPreview();
            case OWNED -> texts.select();
            case BUY -> wallet.credits() >= price ? texts.buyAffordable() : texts.buy();
            case UNAVAILABLE -> texts.unavailable();
        };
        String missing = CreditFormat.format(Math.max(0, price - wallet.credits()));
        block.forEach(line -> lore.add(line.replace("{price}", CreditFormat.format(price)).replace("{missing}", missing)));
        String name = (owned ? texts.ownedName() : texts.lockedName()).replace("{name}", cosmetic.name());
        return cosmetic.icon().name(name).lore(lore).glow(state == CosmeticState.SELECTED);
    }

    public ItemBuilder none(CosmeticWallet wallet) {
        boolean chosen = CosmeticWallet.NONE.equals(wallet.selected());
        List<String> lore = new ArrayList<>(settings.none().lore());
        lore.add(BLANK);
        lore.addAll(chosen ? settings.lore().selected() : settings.lore().selectOnly());
        return settings.none().lore(lore).glow(chosen);
    }

    public ItemBuilder random(CosmeticWallet wallet) {
        boolean chosen = CosmeticWallet.RANDOM.equals(wallet.selected());
        List<String> lore = new ArrayList<>(settings.random().lore());
        lore.add(BLANK);
        lore.addAll(chosen ? settings.lore().selected() : settings.lore().selectOnly());
        return settings.random().lore(lore).glow(chosen);
    }

    public List<ItemBuilder> randomFrames(CosmeticWallet wallet) {
        ItemBuilder random = random(wallet);
        return wallet.sorted(catalog).stream()
            .filter(wallet::usable)
            .map(cosmetic -> cosmetic.icon().name(random.name()).lore(random.lore()).glow(random.isGlowing()))
            .toList();
    }

    public ItemBuilder selected(CosmeticWallet wallet) {
        String id = selection(wallet);
        Optional<BedCosmetic> cosmetic = catalog.find(id);
        List<String> lore = switch (id) {
            case CosmeticWallet.RANDOM -> settings.random().lore();
            case CosmeticWallet.NONE -> List.of();
            default -> cosmetic.map(BedCosmetic::description).filter(description -> !description.isEmpty()).orElse(settings.selected().item().lore());
        };
        ItemBuilder item = settings.selected().item().replacing("name", selectedName(wallet));
        if (settings.entries().selectedIcon() && cosmetic.isPresent()) {
            return cosmetic.get().icon().showFlags().name(item.name()).lore(lore);
        }
        return item.lore(lore);
    }

    public ItemBuilder category(CosmeticWallet wallet) {
        long owned = wallet.ownedCount(catalog);
        int total = catalog.all().size();
        long percent = total == 0 ? 0 : owned * 100 / total;
        return menu.button().item().replacing(
            "owned", Long.toString(owned),
            "total", Integer.toString(total),
            "percent", Long.toString(percent),
            "selected", selectedName(wallet)
        );
    }

    public String selectedName(CosmeticWallet wallet) {
        String id = selection(wallet);
        return switch (id) {
            case CosmeticWallet.RANDOM -> plain(settings.random().name());
            case CosmeticWallet.NONE -> plain(settings.none().name());
            default -> catalog.find(id).map(BedCosmetic::name).orElse(plain(settings.random().name()));
        };
    }

    String stars(int count) {
        LoreTexts texts = settings.lore();
        String lit = texts.starsOn() + texts.star().repeat(count);
        int unlit = BedCosmetic.MAX_STARS - count;
        return unlit == 0 ? lit : lit + texts.starsOff() + texts.star().repeat(unlit);
    }

    private CosmeticWallet effective(CosmeticWallet wallet) {
        String id = selection(wallet);
        return id.equals(wallet.selected()) ? wallet : wallet.selecting(id);
    }

    private String selection(CosmeticWallet wallet) {
        boolean missing = CosmeticWallet.RANDOM.equals(wallet.selected()) && !settings.entries().random()
            || CosmeticWallet.NONE.equals(wallet.selected()) && !settings.entries().none();
        return missing ? catalog.defaultId().orElse(wallet.selected()) : wallet.selected();
    }

    static Optional<String> tag(BedCosmetic cosmetic, LoreTexts texts) {
        return switch (cosmetic.availability()) {
            case VIP -> cosmetic.tag().or(() -> Optional.of(texts.vipTag()));
            case ULTRA -> cosmetic.tag().or(() -> Optional.of(texts.ultraTag()));
            case ULTRA_PLUS -> cosmetic.tag().or(() -> Optional.of(texts.ultraPlusTag()));
            case DEFAULT, SALE, EVENT -> cosmetic.tag();
        };
    }

    private static String plain(String colored) {
        return colored == null ? "" : colored.replaceAll("(?i)[§&][0-9a-fk-or]", "");
    }
}
