package br.com.muxe.minigame.bedwars.game;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.minigame.engine.setup.Point;
import br.com.muxe.minigame.engine.team.TeamColor;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ChestAccessTest {

    private static final Map<TeamColor, Point> SPAWNS = Map.of(
        TeamColor.VERMELHO, Point.block(0, 70, 0),
        TeamColor.AZUL, Point.block(30, 70, 0));

    @Test
    void aChestBelongsToTheClosestTeamWithinTheIslandRadius() {
        assertThat(ChestAccess.owner(Point.block(4, 70, 3), SPAWNS, 17)).contains(TeamColor.VERMELHO);
        assertThat(ChestAccess.owner(Point.block(20, 70, 0), SPAWNS, 17)).contains(TeamColor.AZUL);
        assertThat(ChestAccess.owner(Point.block(15, 70, 40), SPAWNS, 17)).isEmpty();
    }

    @Test
    void onlyTheOwnersOpenTheirChestUntilTheTeamIsEliminated() {
        Optional<TeamColor> red = Optional.of(TeamColor.VERMELHO);

        assertThat(ChestAccess.canOpen(red, TeamColor.VERMELHO, false)).isTrue();
        assertThat(ChestAccess.canOpen(red, TeamColor.AZUL, false)).isFalse();
        assertThat(ChestAccess.canOpen(red, TeamColor.AZUL, true)).isTrue();
        assertThat(ChestAccess.canOpen(Optional.empty(), TeamColor.AZUL, false)).isTrue();
    }
}
