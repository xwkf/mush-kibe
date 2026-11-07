package br.com.muxe.minigame.api.runtime;

import java.util.function.Predicate;
import org.bukkit.Material;

public record MatchRules(boolean respawn, boolean breakOnlyPlaced, boolean hunger, boolean crafting, boolean enterBeds, boolean moveArmor,
                         Predicate<Material> undroppable, String nameTagPrefix) {

    public static MatchRules defaults(String nameTagPrefix) {
        return new MatchRules(false, true, false, false, false, false, material -> false, nameTagPrefix);
    }

    public MatchRules respawning() {
        return new MatchRules(true, breakOnlyPlaced, hunger, crafting, enterBeds, moveArmor, undroppable, nameTagPrefix);
    }

    public MatchRules breakingAnything() {
        return new MatchRules(respawn, false, hunger, crafting, enterBeds, moveArmor, undroppable, nameTagPrefix);
    }

    public MatchRules withHunger() {
        return new MatchRules(respawn, breakOnlyPlaced, true, crafting, enterBeds, moveArmor, undroppable, nameTagPrefix);
    }

    public MatchRules withCrafting() {
        return new MatchRules(respawn, breakOnlyPlaced, hunger, true, enterBeds, moveArmor, undroppable, nameTagPrefix);
    }

    public MatchRules keeping(Predicate<Material> kept) {
        return new MatchRules(respawn, breakOnlyPlaced, hunger, crafting, enterBeds, moveArmor, kept, nameTagPrefix);
    }
}
