package br.com.muxe.minigame.api.cosmetic;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.minigame.api.cosmetic.catalog.Availability;
import br.com.muxe.minigame.api.cosmetic.profile.CosmeticAccess;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CosmeticAccessTest {

    private static CosmeticAccess holding(String... permissions) {
        Set<String> held = Set.of(permissions);
        return CosmeticAccess.of(held::contains);
    }

    @Test
    void thePermissionsOfEachTierAreDocumented() {
        assertThat(CosmeticAccess.VIP_PERMISSION).isEqualTo("muxe.cosmetic.vip");
        assertThat(CosmeticAccess.ULTRA_PERMISSION).isEqualTo("muxe.cosmetic.ultra");
        assertThat(CosmeticAccess.ULTRA_PLUS_PERMISSION).isEqualTo("muxe.cosmetic.ultraplus");
        assertThat(CosmeticAccess.ALL_PERMISSION).isEqualTo("muxe.cosmetic.all");
    }

    @Test
    void theHighestTierWins() {
        assertThat(holding()).isEqualTo(CosmeticAccess.REGULAR);
        assertThat(holding(CosmeticAccess.VIP_PERMISSION)).isEqualTo(CosmeticAccess.VIP);
        assertThat(holding(CosmeticAccess.VIP_PERMISSION, CosmeticAccess.ULTRA_PERMISSION)).isEqualTo(CosmeticAccess.ULTRA);
        assertThat(holding(CosmeticAccess.ULTRA_PERMISSION)).isEqualTo(CosmeticAccess.ULTRA);
        assertThat(holding(CosmeticAccess.ULTRA_PLUS_PERMISSION, CosmeticAccess.VIP_PERMISSION)).isEqualTo(CosmeticAccess.ULTRA_PLUS);
        assertThat(holding(CosmeticAccess.ALL_PERMISSION, CosmeticAccess.ULTRA_PLUS_PERMISSION)).isEqualTo(CosmeticAccess.ALL);
    }

    @Test
    void aTierCoversItsOwnAndTheLowerOnes() {
        assertThat(CosmeticAccess.REGULAR.covers(Availability.DEFAULT)).isTrue();
        assertThat(CosmeticAccess.REGULAR.covers(Availability.VIP)).isFalse();
        assertThat(CosmeticAccess.VIP.covers(Availability.VIP)).isTrue();
        assertThat(CosmeticAccess.VIP.covers(Availability.ULTRA)).isFalse();
        assertThat(CosmeticAccess.ULTRA.covers(Availability.VIP)).isTrue();
        assertThat(CosmeticAccess.ULTRA.covers(Availability.ULTRA)).isTrue();
        assertThat(CosmeticAccess.ULTRA.covers(Availability.ULTRA_PLUS)).isFalse();
        assertThat(CosmeticAccess.ULTRA_PLUS.covers(Availability.ULTRA)).isTrue();
        assertThat(CosmeticAccess.ULTRA_PLUS.covers(Availability.ULTRA_PLUS)).isTrue();
        assertThat(CosmeticAccess.ALL.covers(Availability.ULTRA_PLUS)).isTrue();
    }

    @Test
    void salesAndEventsAreNeverIncludedInATier() {
        for (CosmeticAccess access : CosmeticAccess.values()) {
            assertThat(access.covers(Availability.SALE)).as(access.name()).isFalse();
            assertThat(access.covers(Availability.EVENT)).as(access.name()).isFalse();
        }
    }
}
