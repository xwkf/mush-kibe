package br.com.muxe.core.bukkit.config.edit;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.config.edit.ConfigEditor.ReloadOutcome;
import br.com.muxe.core.bukkit.config.edit.ConfigOutcome.Status;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConfigEditorTest {

    private static final String DEFAULTS = String.join("\n",
        "preview:",
        "  world: previa",
        "  timeout: 120s",
        "  enabled: true",
        "  count: 3",
        "  spawn:",
        "    x: 6.5",
        "    y: 11.0",
        "    z: 21.5",
        "    yaw: 180.0",
        "    pitch: 10.0",
        "  names:",
        "    - Ana",
        "    - Bia",
        "  levels:",
        "    - 1",
        "    - 2",
        "spawn:",
        "  world: lobby",
        "  x: 0.5",
        "database:",
        "  password: env:SECRET",
        "other:",
        "  value: 1",
        "text: hello",
        "");

    private MemoryConfigStore store;
    private ConfigReloads reloads;
    private ConfigEditor editor;
    private final List<String> applied = new ArrayList<>();

    @BeforeEach
    void create() {
        store = new MemoryConfigStore(DEFAULTS);
        reloads = new ConfigReloads()
            .register("preview", "preview", config -> applied.add("preview " + config.getString("preview.timeout")))
            .register("spawn", "spawn", config -> applied.add("spawn"));
        editor = new ConfigEditor(store, reloads);
    }

    private List<Object> levels() {
        return new ArrayList<>(store.config().getList("preview.levels"));
    }

    @Test
    void viewingAScalarShowsItsValueAndType() {
        assertThat(editor.view("preview.timeout")).isEqualTo(new ConfigView.Scalar("preview.timeout", "120s", ConfigValues.TYPE_TEXT));
        assertThat(editor.view("preview.count")).isEqualTo(new ConfigView.Scalar("preview.count", "3", ConfigValues.TYPE_INTEGER));
        assertThat(editor.view("preview.spawn.y")).isEqualTo(new ConfigView.Scalar("preview.spawn.y", "11.0", ConfigValues.TYPE_DECIMAL));
        assertThat(editor.view("preview.enabled")).isEqualTo(new ConfigView.Scalar("preview.enabled", "true", ConfigValues.TYPE_BOOLEAN));
    }

    @Test
    void viewingASectionListsItsChildKeysMarkingSubsections() {
        assertThat(editor.view("preview")).isEqualTo(new ConfigView.Keys("preview",
            List.of("count", "enabled", "levels", "names", "spawn.", "timeout", "world")));
        assertThat(editor.view("")).isEqualTo(new ConfigView.Keys("", List.of("database.", "other.", "preview.", "spawn.", "text")));
    }

    @Test
    void viewingAListShowsItsItems() {
        assertThat(editor.view("preview.names")).isEqualTo(new ConfigView.Items("preview.names", List.of("Ana", "Bia")));
    }

    @Test
    void viewingMissingInvalidOrSecretPathsRevealsNothing() {
        assertThat(editor.view("preview.nothing")).isEqualTo(new ConfigView.Missing("preview.nothing"));
        assertThat(editor.view("preview..x")).isEqualTo(new ConfigView.Missing("preview..x"));
        assertThat(editor.view("database.password")).isEqualTo(new ConfigView.Hidden("database.password"));
    }

    @Test
    void settingAKnownValueKeepsItsTypeSavesAndRestOfFileIntact() {
        ConfigOutcome outcome = editor.set("preview.timeout", "90s", false);

        assertThat(outcome.status()).isEqualTo(Status.CHANGED);
        assertThat(outcome.detail()).isEqualTo("90s");
        assertThat(store.config().getString("preview.timeout")).isEqualTo("90s");
        assertThat(store.saves()).isEqualTo(1);
        assertThat(store.onDisk().getString("preview.world")).isEqualTo("previa");
        assertThat(store.onDisk().getDouble("preview.spawn.x")).isEqualTo(6.5);
        assertThat(store.onDisk().getStringList("preview.names")).containsExactly("Ana", "Bia");
        assertThat(store.onDisk().getString("database.password")).isEqualTo("env:SECRET");
    }

    @Test
    void numbersAndBooleansAreCheckedAgainstTheExistingType() {
        assertThat(editor.set("preview.count", "5", false).changed()).isTrue();
        assertThat(store.config().getInt("preview.count")).isEqualTo(5);
        assertThat(editor.set("preview.spawn.y", "27", false).changed()).isTrue();
        assertThat(store.config().get("preview.spawn.y")).isEqualTo(27.0);
        assertThat(editor.set("preview.enabled", "false", false).changed()).isTrue();
        assertThat(store.config().getBoolean("preview.enabled")).isFalse();

        ConfigOutcome wrong = editor.set("preview.count", "muitos", false);
        assertThat(wrong.status()).isEqualTo(Status.TYPE_MISMATCH);
        assertThat(wrong.detail()).isEqualTo(ConfigValues.TYPE_INTEGER);
        assertThat(editor.set("preview.enabled", "talvez", false).status()).isEqualTo(Status.TYPE_MISMATCH);
        assertThat(store.config().getInt("preview.count")).isEqualTo(5);
    }

    @Test
    void aRefusedChangeDoesNotSaveOrReload() {
        editor.set("preview.count", "muitos", false);
        editor.set("preview.nothing", "1", false);

        assertThat(store.saves()).isZero();
        assertThat(applied).isEmpty();
    }

    @Test
    void unknownPathsNeedTheNewFlagToBeCreated() {
        ConfigOutcome refused = editor.set("preview.extra", "7", false);
        assertThat(refused.status()).isEqualTo(Status.UNKNOWN_PATH);
        assertThat(store.config().contains("preview.extra")).isFalse();

        ConfigOutcome created = editor.set("preview.extra", "7", true);
        assertThat(created.changed()).isTrue();
        assertThat(store.config().get("preview.extra")).isEqualTo(7);
    }

    @Test
    void pathsKnownOnlyFromTheDefaultsCanBeSet() {
        MemoryConfigStore partial = new MemoryConfigStore(DEFAULTS, "text: hello\n");
        ConfigEditor partialEditor = new ConfigEditor(partial, new ConfigReloads());

        ConfigOutcome outcome = partialEditor.set("preview.timeout", "5s", false);

        assertThat(outcome.changed()).isTrue();
        assertThat(partial.onDisk().getString("preview.timeout")).isEqualTo("5s");
    }

    @Test
    void aSectionIsNeverReplacedByAScalar() {
        ConfigOutcome outcome = editor.set("preview.spawn", "10", true);

        assertThat(outcome.status()).isEqualTo(Status.IS_SECTION);
        assertThat(store.config().isConfigurationSection("preview.spawn")).isTrue();
        assertThat(store.saves()).isZero();
    }

    @Test
    void aScalarIsNeverTurnedIntoASectionByANewChildPath() {
        ConfigOutcome outcome = editor.set("preview.timeout.inner", "1", true);

        assertThat(outcome.status()).isEqualTo(Status.BLOCKED);
        assertThat(outcome.detail()).isEqualTo("preview.timeout");
        assertThat(store.config().getString("preview.timeout")).isEqualTo("120s");
    }

    @Test
    void secretsAndBadPathsAreRefused() {
        assertThat(editor.set("database.password", "x", true).status()).isEqualTo(Status.PROTECTED);
        assertThat(editor.set("preview..x", "x", true).status()).isEqualTo(Status.INVALID_PATH);
        assertThat(editor.set("", "x", true).status()).isEqualTo(Status.INVALID_PATH);
        assertThat(store.config().getString("database.password")).isEqualTo("env:SECRET");
    }

    @Test
    void listsAreSetFromCommaSeparatedValuesKeepingTheirItemType() {
        assertThat(editor.set("preview.names", "Caio, Duda", false).changed()).isTrue();
        assertThat(store.config().getStringList("preview.names")).containsExactly("Caio", "Duda");

        assertThat(editor.set("preview.levels", "5,6,7", false).changed()).isTrue();
        assertThat(levels()).containsExactly(5, 6, 7);

        assertThat(editor.set("preview.levels", "5,x", false).status()).isEqualTo(Status.TYPE_MISMATCH);
        assertThat(levels()).containsExactly(5, 6, 7);
    }

    @Test
    void itemsCanBeAddedToAndRemovedFromAList() {
        assertThat(editor.add("preview.names", "Caio").changed()).isTrue();
        assertThat(store.config().getStringList("preview.names")).containsExactly("Ana", "Bia", "Caio");

        assertThat(editor.remove("preview.names", "Ana").changed()).isTrue();
        assertThat(store.config().getStringList("preview.names")).containsExactly("Bia", "Caio");

        assertThat(editor.remove("preview.names", "Zeca").status()).isEqualTo(Status.MISSING_ITEM);
        assertThat(editor.add("preview.levels", "tres").status()).isEqualTo(Status.TYPE_MISMATCH);
        assertThat(editor.add("preview.timeout", "x").status()).isEqualTo(Status.NOT_A_LIST);
        assertThat(editor.remove("preview.timeout", "x").status()).isEqualTo(Status.NOT_A_LIST);
        assertThat(editor.add("preview.nothing", "x").status()).isEqualTo(Status.UNKNOWN_PATH);
    }

    @Test
    void listItemsKeepTheirNumericType() {
        editor.add("preview.levels", "3");
        editor.remove("preview.levels", "1");

        assertThat(levels()).containsExactly(2, 3);
    }

    @Test
    void aLocationIsWrittenUnderTheSectionWithTheWorldOnlyWhenTheSectionHasOne() {
        LocationValues here = LocationValues.of("previa_nova", 1.234, 5.0, 9.876, 90f, 5f, false);

        ConfigOutcome withWorld = editor.setLocation("spawn", here, false, false);
        ConfigOutcome withoutWorld = editor.setLocation("preview.spawn", here, false, false);

        assertThat(withWorld.changed()).isTrue();
        assertThat(store.config().getString("spawn.world")).isEqualTo("previa_nova");
        assertThat(store.config().getDouble("spawn.x")).isEqualTo(1.23);
        assertThat(withoutWorld.changed()).isTrue();
        assertThat(store.config().contains("preview.spawn.world")).isFalse();
        assertThat(store.config().getDouble("preview.spawn.z")).isEqualTo(9.88);
        assertThat(store.config().getDouble("preview.spawn.yaw")).isEqualTo(90.0);
        assertThat(store.config().getDouble("preview.spawn.pitch")).isEqualTo(5.0);
    }

    @Test
    void theWorldFlagForcesTheWorldIntoASectionThatLacksIt() {
        LocationValues here = LocationValues.of("previa_nova", 1, 2, 3, 0f, 0f, false);

        editor.setLocation("preview.spawn", here, true, false);

        assertThat(store.config().getString("preview.spawn.world")).isEqualTo("previa_nova");
    }

    @Test
    void aLocationNeedsAKnownSectionOrTheNewFlagAndNeverReplacesAScalar() {
        LocationValues here = LocationValues.of("lobby", 1, 2, 3, 0f, 0f, false);

        assertThat(editor.setLocation("brand.new", here, false, false).status()).isEqualTo(Status.UNKNOWN_PATH);
        assertThat(editor.setLocation("brand.new", here, false, true).changed()).isTrue();
        assertThat(store.config().getString("brand.new.world")).isEqualTo("lobby");
        assertThat(editor.setLocation("preview.timeout", here, false, true).status()).isEqualTo(Status.BLOCKED);
        assertThat(editor.setLocation("text", here, false, true).status()).isEqualTo(Status.BLOCKED);
        assertThat(editor.setLocation("database.password", here, false, true).status()).isEqualTo(Status.PROTECTED);
    }

    @Test
    void aMultiValueChangeSavesOnceAndAppliesEachComponentOnce() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("preview.world", "outra");
        values.put("preview.spawn.x", 1.5);
        values.put("preview.timeout", "30s");

        ConfigOutcome outcome = editor.setAll(values, false);

        assertThat(outcome.changed()).isTrue();
        assertThat(store.saves()).isEqualTo(1);
        assertThat(applied).containsExactly("preview 30s");
        assertThat(outcome.reload().applied()).containsExactly("preview");
    }

    @Test
    void aMultiValueChangeIsRejectedAsAWholeWhenOneEntryIsInvalid() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("preview.world", "outra");
        values.put("preview.nothing", 1);

        ConfigOutcome outcome = editor.setAll(values, false);

        assertThat(outcome.status()).isEqualTo(Status.UNKNOWN_PATH);
        assertThat(store.config().getString("preview.world")).isEqualTo("previa");
        assertThat(store.saves()).isZero();
    }

    @Test
    void aChangeIsAppliedLiveOnlyToTheComponentsCoveringItsPath() {
        ConfigOutcome preview = editor.set("preview.timeout", "10s", false);
        ConfigOutcome other = editor.set("other.value", "2", false);

        assertThat(preview.reload().live()).isTrue();
        assertThat(applied).containsExactly("preview 10s");
        assertThat(other.reload().covered()).isFalse();
        assertThat(editor.pendingRestart()).containsExactly("other.value");
    }

    @Test
    void aFailingComponentIsReportedAndTheChangeStaysPendingForARestart() {
        reloads.register("broken", "text", config -> {
            throw new IllegalStateException("no world folder");
        });

        ConfigOutcome outcome = editor.set("text", "bye", false);

        assertThat(outcome.changed()).isTrue();
        assertThat(outcome.reload().failed()).containsEntry("broken", "no world folder");
        assertThat(outcome.reload().live()).isFalse();
        assertThat(editor.pendingRestart()).containsExactly("text");
        assertThat(store.onDisk().getString("text")).isEqualTo("bye");
    }

    @Test
    void reloadingRereadsTheFileAppliesEveryComponentAndListsWhatNeedsARestart() {
        editor.set("other.value", "2", false);
        store.edit(store.disk().replace("120s", "45s"));

        ReloadOutcome outcome = editor.reloadAll();

        assertThat(store.reloads()).isEqualTo(1);
        assertThat(outcome.report().applied()).containsExactly("preview", "spawn");
        assertThat(applied).containsExactly("preview 45s", "spawn");
        assertThat(outcome.pendingRestart()).containsExactly("other.value");
    }

    @Test
    void reloadingClearsPendingPathsThatALaterComponentCovers() {
        editor.set("text", "bye", false);
        reloads.register("text", "text", config -> applied.add("text"));

        ReloadOutcome outcome = editor.reloadAll();

        assertThat(outcome.pendingRestart()).isEmpty();
    }

    @Test
    void suggestionsWalkTheSectionTreeOneLevelAtATime() {
        assertThat(editor.suggest("")).containsExactly("database.", "other.", "preview.", "spawn.", "text");
        assertThat(editor.suggest("pre")).containsExactly("preview.");
        assertThat(editor.suggest("preview.")).containsExactly(
            "preview.count", "preview.enabled", "preview.levels", "preview.names", "preview.spawn.", "preview.timeout", "preview.world");
        assertThat(editor.suggest("preview.sp")).containsExactly("preview.spawn.");
        assertThat(editor.suggest("preview.spawn.")).hasSize(5).contains("preview.spawn.x");
        assertThat(editor.suggest("nothing.")).isEmpty();
    }

    @Test
    void suggestionsNeverOfferSecrets() {
        assertThat(editor.suggest("database.")).isEmpty();
    }
}
