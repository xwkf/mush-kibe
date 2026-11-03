package br.com.muxe.minigame.bedwars.teamcolor;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.minigame.engine.team.TeamColor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TeamColorStoreTest {

    private final UUID first = new UUID(0, 1);
    private final UUID second = new UUID(0, 2);
    private final UUID third = new UUID(0, 3);
    private final Map<UUID, TeamColor> colors = new HashMap<>();
    private final TeamColorStore store = new TeamColorStore() {

        @Override
        public Optional<TeamColor> of(UUID playerId) {
            return Optional.ofNullable(colors.get(playerId));
        }

        @Override
        public void change(UUID playerId, Optional<TeamColor> color) {
            color.ifPresentOrElse(chosen -> colors.put(playerId, chosen), () -> colors.remove(playerId));
        }
    };

    @Test
    void thePreferredColoursOfARoomSkipPlayersWithoutOne() {
        store.change(first, Optional.of(TeamColor.CIANO));
        store.change(third, Optional.of(TeamColor.ROSA));

        assertThat(store.preferred(List.of(first, second, third))).containsOnly(Map.entry(first, TeamColor.CIANO), Map.entry(third, TeamColor.ROSA));
    }

    @Test
    void nobodyWithAColourGivesAnEmptyMap() {
        assertThat(store.preferred(List.of(first, second))).isEmpty();
        assertThat(store.preferred(List.of())).isEmpty();
    }

    @Test
    void aPlayerWhoClearedTheColourIsNotPreferred() {
        store.change(first, Optional.of(TeamColor.CIANO));
        store.change(first, Optional.empty());

        assertThat(store.preferred(List.of(first))).isEmpty();
    }
}
