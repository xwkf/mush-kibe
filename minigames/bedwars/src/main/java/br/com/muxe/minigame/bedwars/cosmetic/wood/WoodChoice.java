package br.com.muxe.minigame.bedwars.cosmetic.wood;

import br.com.muxe.core.bukkit.item.ItemBuilder;
import org.bukkit.Material;

public record WoodChoice(Material material, short data) {

    public static final WoodChoice OAK = new WoodChoice(Material.WOOD, (short) 0);

    public static WoodChoice of(ItemBuilder icon) {
        return new WoodChoice(icon.material(), icon.data());
    }
}
