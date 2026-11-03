package br.com.muxe.minigame.bedwars.preferences.domain;

import java.util.Objects;

public record Preferences(boolean showLevel, boolean tntTimer, ChestDeposit chestDeposit) {

    public static final Preferences DEFAULTS = new Preferences(true, true, ChestDeposit.ENABLED);

    public Preferences {
        Objects.requireNonNull(chestDeposit, "chestDeposit");
    }

    public Preferences toggleShowLevel() {
        return new Preferences(!showLevel, tntTimer, chestDeposit);
    }

    public Preferences toggleTntTimer() {
        return new Preferences(showLevel, !tntTimer, chestDeposit);
    }

    public Preferences nextChestDeposit() {
        return new Preferences(showLevel, tntTimer, chestDeposit.next());
    }

    public enum ChestDeposit {
        ENABLED,
        NO_HOLOGRAMS,
        DISABLED;

        public ChestDeposit next() {
            return values()[(ordinal() + 1) % values().length];
        }

        public boolean deposits() {
            return this != DISABLED;
        }

        public boolean showsHolograms() {
            return this == ENABLED;
        }
    }
}
