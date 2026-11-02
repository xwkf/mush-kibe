package br.com.muxe.core.bukkit.hologram;

import java.util.List;

public interface SpawnedHologram {

    SpawnedHologram NONE = new SpawnedHologram() {
        @Override
        public void resend() {
        }

        @Override
        public void remove() {
        }
    };

    void resend();

    void remove();

    default boolean owns(int entityId) {
        return false;
    }

    default boolean rename(List<String> names) {
        return false;
    }
}
