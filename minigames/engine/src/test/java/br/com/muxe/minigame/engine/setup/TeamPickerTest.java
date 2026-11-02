package br.com.muxe.minigame.engine.setup;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.minigame.engine.team.TeamColor;
import java.util.List;
import org.junit.jupiter.api.Test;

class TeamPickerTest {

    private final TestSchema schema = new TestSchema();

    @Test
    void aTeamStepGoesToTheTeamWithTheNearestSpawn() {
        ArenaDraft draft = ArenaDraft.create("solo_slip", "solo", schema);
        draft.addTeam(TeamColor.AMARELO, schema.teams("solo"));
        draft.addTeam(TeamColor.VERDE, schema.teams("solo"));
        draft.set(TestSchema.SPAWN, TeamColor.AMARELO, new Point(100, 70, 0, 0, 0));
        draft.set(TestSchema.SPAWN, TeamColor.VERDE, new Point(-100, 70, 0, 0, 0));

        assertThat(TeamPicker.nearest(draft, TestSchema.SPAWN, new Point(80, 65, 10, 0, 0))).contains(TeamColor.AMARELO);
        assertThat(TeamPicker.nearest(draft, TestSchema.SPAWN, new Point(-70, 65, 10, 0, 0))).contains(TeamColor.VERDE);
    }

    @Test
    void withoutAnySpawnThereIsNoTeamToPick() {
        ArenaDraft draft = ArenaDraft.create("solo_slip", "solo", schema);
        draft.addTeam(TeamColor.AMARELO, schema.teams("solo"));

        assertThat(TeamPicker.nearest(draft, TestSchema.SPAWN, new Point(0, 70, 0, 0, 0))).isEmpty();
    }

    @Test
    void theNearestBedSkipsBothHalvesOfABedAnotherTeamOwns() {
        Point spawn = Point.block(0, 70, 0);
        Point otherHead = Point.block(2, 70, 0);
        Point otherFoot = Point.block(3, 70, 0);
        Point ownHead = Point.block(-6, 70, 0);
        Point ownFoot = Point.block(-7, 70, 0);

        assertThat(TeamPicker.nearestFree(spawn, List.of(otherHead, otherFoot, ownHead, ownFoot), List.of(otherHead))).contains(ownHead);
        assertThat(TeamPicker.nearestFree(spawn, List.of(otherHead, otherFoot), List.of(otherHead))).isEmpty();
    }
}
