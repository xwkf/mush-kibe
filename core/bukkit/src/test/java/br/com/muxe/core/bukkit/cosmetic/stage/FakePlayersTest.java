package br.com.muxe.core.bukkit.cosmetic.stage;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class FakePlayersTest {

    private final UUID winner = UUID.randomUUID();
    private final UUID other = UUID.randomUUID();
    private final UUID stranger = UUID.randomUUID();

    @Test
    void theSkinNeverComesFromSomeoneWatchingTheEffect() {
        Random random = new Random(1);
        for (int draw = 0; draw < 50; draw++) {
            assertThat(FakePlayers.skinOwner(List.of(winner, other, stranger), Set.of(winner, other), random)).contains(stranger);
        }
    }

    @Test
    void withNobodyElseOnlineTheFakePlayerKeepsADefaultSkin() {
        assertThat(FakePlayers.skinOwner(List.of(winner), Set.of(winner), new Random(2))).isEmpty();
        assertThat(FakePlayers.skinOwner(List.of(), Set.of(), new Random(2))).isEmpty();
    }

    @Test
    void theSkinIsDrawnAmongEveryoneElse() {
        Random random = new Random(3);
        UUID fourth = UUID.randomUUID();
        Set<UUID> drawn = new HashSet<>();
        for (int draw = 0; draw < 100; draw++) {
            FakePlayers.skinOwner(List.of(winner, stranger, fourth), Set.of(winner), random).ifPresent(drawn::add);
        }
        assertThat(drawn).containsExactlyInAnyOrder(stranger, fourth);
    }

    @Test
    void aFakePlayerHasAnEightCharacterHexName() {
        String name = FakePlayers.name(new Random(4));

        assertThat(name).hasSize(8).matches("[0-9a-f]{8}");
    }
}
