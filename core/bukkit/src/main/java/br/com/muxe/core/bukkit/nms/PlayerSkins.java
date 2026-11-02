package br.com.muxe.core.bukkit.nms;

import br.com.muxe.core.bukkit.npc.NpcSkin;
import java.util.Collection;
import java.util.Optional;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class PlayerSkins {

    private static final String TEXTURES = "textures";

    private PlayerSkins() {
    }

    public static Optional<NpcSkin> of(Player player) {
        try {
            return fromProfileOwner(player);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            Bukkit.getLogger().log(Level.FINE, "[Muxe] Could not read the skin of " + player.getName(), exception);
            return Optional.empty();
        }
    }

    static Optional<NpcSkin> fromProfileOwner(Object owner) throws ReflectiveOperationException {
        Object profile = owner.getClass().getMethod("getProfile").invoke(owner);
        if (profile == null) {
            return Optional.empty();
        }
        Object properties = profile.getClass().getMethod("getProperties").invoke(profile);
        Object textures = properties.getClass().getMethod("get", Object.class).invoke(properties, TEXTURES);
        for (Object property : (Collection<?>) textures) {
            String value = (String) property.getClass().getMethod("getValue").invoke(property);
            String signature = (String) property.getClass().getMethod("getSignature").invoke(property);
            if (value != null && signature != null) {
                return Optional.of(new NpcSkin(value, signature));
            }
        }
        return Optional.empty();
    }
}
