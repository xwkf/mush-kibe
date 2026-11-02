package br.com.muxe.core.bukkit.nms;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.npc.NpcSkin;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PlayerSkinsTest {

    @Test
    void readsTheSignedTexturesOfTheProfile() throws ReflectiveOperationException {
        Owner owner = new Owner(new Profile(new Properties(Map.of("textures", List.of(new Property("value", "signature"))))));

        assertThat(PlayerSkins.fromProfileOwner(owner)).contains(new NpcSkin("value", "signature"));
    }

    @Test
    void aProfileWithoutSignedTexturesHasNoSkin() throws ReflectiveOperationException {
        Owner unsigned = new Owner(new Profile(new Properties(Map.of("textures", List.of(new Property("value", null))))));
        Owner empty = new Owner(new Profile(new Properties(Map.of())));

        assertThat(PlayerSkins.fromProfileOwner(unsigned)).isEmpty();
        assertThat(PlayerSkins.fromProfileOwner(empty)).isEmpty();
        assertThat(PlayerSkins.fromProfileOwner(new Owner(null))).isEmpty();
    }

    public record Owner(Profile getProfile) {
    }

    public record Profile(Properties getProperties) {
    }

    public record Properties(Map<String, Collection<Property>> values) {

        public Collection<Property> get(Object key) {
            return values.getOrDefault(key, List.of());
        }
    }

    public record Property(String getValue, String getSignature) {
    }
}
