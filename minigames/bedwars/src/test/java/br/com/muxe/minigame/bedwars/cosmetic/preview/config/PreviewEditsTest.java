package br.com.muxe.minigame.bedwars.cosmetic.preview.config;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.config.edit.LocationValues;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PreviewEditsTest {

    private static final LocationValues HERE = LocationValues.of("previa_nova", 6.5, 11.0, 21.5, 180f, 10f, false);

    @Test
    void thePreviewWordsMatchTheSectionsOfTheConfiguration() {
        assertThat(PreviewKind.words()).containsExactly("cama", "vitoria", "kill-final", "aura");
        assertThat(PreviewKind.BED.section()).isEqualTo("preview");
        assertThat(PreviewKind.VICTORY.section()).isEqualTo("victory-preview");
        assertThat(PreviewKind.FINAL_KILL.section()).isEqualTo("final-kill-preview");
        assertThat(PreviewKind.AURA.section()).isEqualTo("aura-preview");
        assertThat(PreviewKind.parse("KILL-FINAL")).contains(PreviewKind.FINAL_KILL);
        assertThat(PreviewKind.parse("lobby")).isEmpty();
        assertThat(PreviewAction.words()).containsExactly("spawn", "cama", "distancia", "tempo");
        assertThat(PreviewAction.parse("Tempo")).contains(PreviewAction.TIMEOUT);
        assertThat(PreviewAction.parse("x")).isEmpty();
    }

    @Test
    void theSpawnSetsTheWorldNextToTheSectionAndTheLocationWithoutAWorldKey() {
        PreviewEdit edit = PreviewEdits.spawn(PreviewKind.AURA, HERE);

        assertThat(edit).isInstanceOf(PreviewEdit.Write.class);
        Map<String, Object> values = ((PreviewEdit.Write) edit).values();
        assertThat(values).containsEntry("aura-preview.world", "previa_nova")
            .containsEntry("aura-preview.spawn.x", 6.5)
            .containsEntry("aura-preview.spawn.y", 11.0)
            .containsEntry("aura-preview.spawn.z", 21.5)
            .containsEntry("aura-preview.spawn.yaw", 180.0)
            .containsEntry("aura-preview.spawn.pitch", 10.0)
            .doesNotContainKey("aura-preview.spawn.world");
    }

    @Test
    void everyKindHasItsOwnSpawnSection() {
        for (PreviewKind kind : PreviewKind.values()) {
            Map<String, Object> values = ((PreviewEdit.Write) PreviewEdits.spawn(kind, HERE)).values();
            assertThat(values.keySet()).allMatch(path -> path.startsWith(kind.section() + "."));
        }
    }

    @Test
    void theBedBlocksWriteBothHalvesOfTheBedPreviewOnly() {
        BedBlocks bed = BedBlocks.of(6, 11, 17, 2);

        PreviewEdit edit = PreviewEdits.bed(PreviewKind.BED, bed);

        assertThat(((PreviewEdit.Write) edit).values())
            .containsEntry("preview.bed.foot.x", 6)
            .containsEntry("preview.bed.foot.y", 11)
            .containsEntry("preview.bed.foot.z", 17)
            .containsEntry("preview.bed.head.x", 6)
            .containsEntry("preview.bed.head.z", 16)
            .hasSize(5);
        assertThat(PreviewEdits.bed(PreviewKind.AURA, bed)).isEqualTo(new PreviewEdit.Refused(PreviewEdits.NOT_SUPPORTED, "aura"));
    }

    @Test
    void theTargetDistanceBelongsToTheFinalKillPreviewAndMustBePositive() {
        assertThat(((PreviewEdit.Write) PreviewEdits.distance(PreviewKind.FINAL_KILL, "6")).values())
            .containsExactly(Map.entry("final-kill-preview.target.distance", 6));
        assertThat(((PreviewEdit.Write) PreviewEdits.distance(PreviewKind.FINAL_KILL, "4,5")).values())
            .containsExactly(Map.entry("final-kill-preview.target.distance", 4.5));
        assertThat(PreviewEdits.distance(PreviewKind.FINAL_KILL, "0")).isEqualTo(new PreviewEdit.Refused(PreviewEdits.BAD_NUMBER, "0"));
        assertThat(PreviewEdits.distance(PreviewKind.FINAL_KILL, "-2")).isEqualTo(new PreviewEdit.Refused(PreviewEdits.BAD_NUMBER, "-2"));
        assertThat(PreviewEdits.distance(PreviewKind.FINAL_KILL, "perto")).isEqualTo(new PreviewEdit.Refused(PreviewEdits.BAD_NUMBER, "perto"));
        assertThat(PreviewEdits.distance(PreviewKind.FINAL_KILL, "NaN")).isEqualTo(new PreviewEdit.Refused(PreviewEdits.BAD_NUMBER, "NaN"));
        assertThat(PreviewEdits.distance(PreviewKind.FINAL_KILL, "500")).isEqualTo(new PreviewEdit.Refused(PreviewEdits.BAD_NUMBER, "500"));
        assertThat(PreviewEdits.distance(PreviewKind.VICTORY, "4")).isEqualTo(new PreviewEdit.Refused(PreviewEdits.NOT_SUPPORTED, "vitoria"));
    }

    @Test
    void theTimeoutIsAValidDurationKeptAsText() {
        assertThat(((PreviewEdit.Write) PreviewEdits.timeout(PreviewKind.BED, "90s")).values()).containsExactly(Map.entry("preview.timeout", "90s"));
        assertThat(((PreviewEdit.Write) PreviewEdits.timeout(PreviewKind.VICTORY, " 2 M ")).values()).containsExactly(Map.entry("victory-preview.timeout", "2m"));
        assertThat(PreviewEdits.timeout(PreviewKind.AURA, "logo")).isEqualTo(new PreviewEdit.Refused(PreviewEdits.BAD_DURATION, "logo"));
        assertThat(PreviewEdits.timeout(PreviewKind.AURA, "500ms")).isEqualTo(new PreviewEdit.Refused(PreviewEdits.BAD_DURATION, "500ms"));
        assertThat(PreviewEdits.timeout(PreviewKind.AURA, "30")).isEqualTo(new PreviewEdit.Refused(PreviewEdits.BAD_DURATION, "30"));
    }
}
