package br.com.muxe.minigame.api.settings;

import org.bukkit.configuration.ConfigurationSection;

public record Timings(int respawnSeconds, int rejoinSeconds, int restartSeconds, int respawnProtectionSeconds, int emptyRoomSeconds) {

    public static Timings from(ConfigurationSection section) {
        if (section == null) {
            return new Timings(5, 180, 30, 3, 60);
        }
        return new Timings(
            section.getInt("respawn-seconds", 5),
            section.getInt("rejoin-seconds", 180),
            section.getInt("restart-seconds", 30),
            section.getInt("respawn-protection-seconds", 3),
            section.getInt("empty-room-seconds", 60)
        );
    }
}
