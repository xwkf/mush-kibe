package br.com.muxe.core.bukkit.nms;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class PlayerPing {

    private final ServerInternals internals;
    private final Field ping;

    private PlayerPing(ServerInternals internals, Field ping) {
        this.internals = internals;
        this.ping = ping;
    }

    public static Optional<PlayerPing> resolve() {
        return ServerInternals.resolve("Player pings").flatMap(internals -> {
            try {
                return Optional.of(new PlayerPing(internals, internals.nms("EntityPlayer").getField("ping")));
            } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
                Bukkit.getLogger().log(Level.WARNING, "[Muxe] Player pings are unavailable on this server: " + exception);
                return Optional.empty();
            }
        });
    }

    public OptionalInt of(Player player) {
        try {
            return OptionalInt.of(ping.getInt(internals.player(player)));
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return OptionalInt.empty();
        }
    }
}
