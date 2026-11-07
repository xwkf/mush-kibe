package br.com.muxe.minigame.api.arena;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.muxe.minigame.engine.arena.LoadedArena;
import br.com.muxe.minigame.engine.mode.GameMode;
import br.com.muxe.minigame.engine.setup.ArenaDraft;
import br.com.muxe.minigame.engine.setup.LinkedBlock;
import br.com.muxe.minigame.engine.setup.Point;
import br.com.muxe.minigame.engine.setup.SetupStep;
import br.com.muxe.minigame.engine.setup.StepKind;
import br.com.muxe.minigame.engine.setup.TeamRule;
import br.com.muxe.minigame.engine.team.TeamColor;
import br.com.muxe.minigame.engine.world.map.MapTemplate;
import java.util.List;
import java.util.Set;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class ArenaSchemaTest {

    private static final GameMode SOLO = new GameMode("solo", "Solo", 2, 8, 1);
    private static final GameMode DUEL = new GameMode("1v1", "1v1", 2, 2, 1);
    private static final SetupStep CHEST = SetupStep.many("bau", "Baú", StepKind.BLOCK, true, 1, 8, "&6&lBAÚ #{index}", "{team}");
    private static final SetupStep MIDDLE = SetupStep.many("meio", "Baú do meio", StepKind.BLOCK, false, 0, 8, "&6&lMEIO #{index}");
    private static final SetupStep LIMIT = SetupStep.optional("limite", "Altura máxima", StepKind.HEIGHT, false);

    private final ArenaSchema schema = ArenaSchema.builder(List.of(SOLO, DUEL))
        .waiting()
        .spectator()
        .voidHeight()
        .teamSpawn()
        .step(CHEST)
        .stepsFor(Set.of("solo"), MIDDLE)
        .linked(new LinkedBlock(CHEST, Set.of("CHEST"), 10))
        .fixedTeams("1v1", List.of(TeamColor.AZUL, TeamColor.VERMELHO))
        .build();

    @Test
    void theStandardStepsComeFirstAndAModeCanHaveItsOwn() {
        assertThat(schema.steps("1v1")).extracting(SetupStep::id).containsExactly("espera", "area-espera", "espectador", "void", "spawn", "bau");
        assertThat(schema.steps("solo")).extracting(SetupStep::id).endsWith("bau", "meio");
        assertThat(schema.teamAnchor("solo")).contains(ArenaSchema.SPAWN);
        assertThat(schema.linkedBlocks("solo")).singleElement().extracting(LinkedBlock::step).isEqualTo(CHEST);
    }

    @Test
    void modesWithoutFixedTeamsChooseTheirColours() {
        assertThat(schema.teams("solo")).isEqualTo(TeamRule.chosen(8));
        assertThat(schema.teams("1v1").fixed()).containsExactly(TeamColor.AZUL, TeamColor.VERMELHO);
        assertThat(schema.modes()).containsExactly("solo", "1v1");
        assertThat(schema.modeName("1v1")).isEqualTo("1v1");
        assertThat(schema.modeName("x")).isEqualTo("x");
        assertThat(schema.modeFromName("solo_ilhas")).contains("solo");
    }

    @Test
    void fixedTeamsAreReadFromTheModesAndMustMatchTheTeamCount() throws InvalidConfigurationException {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.loadFromString("""
            1v1:
              fixed-teams:
              - azul
              - vermelho
              - verde
            """);

        assertThatThrownBy(() -> ArenaSchema.builder(List.of(DUEL)).fixedTeams(yaml).build()).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void theViewReadsTypedValuesFromTheDraft() {
        ArenaDraft draft = draft();
        ArenaView view = new ArenaView(draft);

        assertThat(view.point(ArenaSchema.SPAWN, TeamColor.AZUL)).isEqualTo(Point.block(10, 70, 0));
        assertThat(view.points(CHEST, TeamColor.AZUL)).containsExactly(Point.block(11, 70, 0), Point.block(12, 70, 0));
        assertThat(view.height(ArenaSchema.VOID)).isEqualTo(40);
        assertThat(view.optionalHeight(LIMIT)).isEmpty();
        assertThat(view.perTeam(team -> view.point(ArenaSchema.SPAWN, team))).containsOnlyKeys(TeamColor.AZUL, TeamColor.VERMELHO);
        assertThatThrownBy(() -> view.point(MIDDLE)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("meio");
    }

    @Test
    void aGameWithOnlyTheStandardStepsUsesTheStandardArena() {
        StandardArena arena = StandardArena.from(new LoadedArena(draft(), MapTemplate.empty()));

        assertThat(arena.mode()).isEqualTo("1v1");
        assertThat(arena.teamColors()).containsExactly(TeamColor.AZUL, TeamColor.VERMELHO);
        assertThat(arena.spawn(TeamColor.VERMELHO)).isEqualTo(Point.block(-10, 70, 0));
        assertThat(arena.waitingArea()).hasSize(2);
        assertThat(arena.voidY()).isEqualTo(40);
    }

    private static ArenaDraft draft() {
        ArenaDraft draft = new ArenaDraft("1v1_ilhas", "1v1", "Ilhas");
        draft.restoreTeams(List.of(TeamColor.AZUL, TeamColor.VERMELHO));
        draft.set(ArenaSchema.WAITING_SPAWN, null, Point.block(0, 120, 0));
        draft.set(ArenaSchema.WAITING_AREA, null, Point.block(-5, 118, -5));
        draft.set(ArenaSchema.WAITING_AREA, null, Point.block(5, 125, 5));
        draft.set(ArenaSchema.SPECTATOR, null, Point.block(0, 100, 0));
        draft.set(ArenaSchema.VOID, null, Point.block(0, 40, 0));
        draft.set(ArenaSchema.SPAWN, TeamColor.AZUL, Point.block(10, 70, 0));
        draft.set(ArenaSchema.SPAWN, TeamColor.VERMELHO, Point.block(-10, 70, 0));
        draft.set(CHEST, TeamColor.AZUL, Point.block(11, 70, 0));
        draft.set(CHEST, TeamColor.AZUL, Point.block(12, 70, 0));
        return draft;
    }
}
