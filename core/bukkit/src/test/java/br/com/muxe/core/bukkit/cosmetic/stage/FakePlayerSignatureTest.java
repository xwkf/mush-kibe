package br.com.muxe.core.bukkit.cosmetic.stage;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.npc.NpcSkin;
import java.util.List;
import org.junit.jupiter.api.Test;

class FakePlayerSignatureTest {

    @Test
    void aTextureWornBySomeoneOnlineIsSignedWithTheirSignature() {
        List<NpcSkin> online = List.of(new NpcSkin("other", "other-signature"), new NpcSkin("victim", "victim-signature"));

        assertThat(FakePlayers.signature("victim", online)).contains("victim-signature");
    }

    @Test
    void aTextureNobodyOnlineWearsHasNoSignature() {
        assertThat(FakePlayers.signature("victim", List.of(new NpcSkin("other", "other-signature")))).isEmpty();
        assertThat(FakePlayers.signature("victim", List.of())).isEmpty();
    }
}
