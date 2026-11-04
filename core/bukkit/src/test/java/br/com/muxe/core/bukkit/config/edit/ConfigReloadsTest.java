package br.com.muxe.core.bukkit.config.edit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class ConfigReloadsTest {

    private final List<String> calls = new ArrayList<>();
    private final YamlConfiguration config = new YamlConfiguration();

    @Test
    void onlyComponentsCoveringAChangedPathAreCalled() {
        ConfigReloads reloads = new ConfigReloads()
            .register("preview", "preview", section -> calls.add("preview"))
            .register("victory", "victory-preview", section -> calls.add("victory"));

        ReloadReport report = reloads.apply(List.of("preview.spawn.x"), config);

        assertThat(calls).containsExactly("preview");
        assertThat(report.applied()).containsExactly("preview");
        assertThat(report.live()).isTrue();
    }

    @Test
    void changingAWholeSectionAboveAComponentCallsIt() {
        ConfigReloads reloads = new ConfigReloads().register("spawn", "preview.spawn", section -> calls.add("spawn"));

        reloads.apply(List.of("preview"), config);

        assertThat(calls).containsExactly("spawn");
    }

    @Test
    void severalPathsOfOneComponentCallItOnce() {
        ConfigReloads reloads = new ConfigReloads().register("preview", "preview", section -> calls.add("preview"));

        reloads.apply(List.of("preview.world", "preview.spawn.x", "preview.timeout"), config);

        assertThat(calls).containsExactly("preview");
    }

    @Test
    void aPathNobodyCoversProducesAnEmptyReport() {
        ConfigReloads reloads = new ConfigReloads().register("preview", "preview", section -> calls.add("preview"));

        ReloadReport report = reloads.apply(List.of("lobby.menu"), config);

        assertThat(report.covered()).isFalse();
        assertThat(report.live()).isFalse();
        assertThat(calls).isEmpty();
        assertThat(reloads.covers("lobby.menu")).isFalse();
        assertThat(reloads.covers("preview.world")).isTrue();
    }

    @Test
    void aFailureIsReportedWithoutStoppingTheOtherComponents() {
        ConfigReloads reloads = new ConfigReloads()
            .register("broken", "a", section -> {
                throw new IllegalArgumentException("bad value");
            })
            .register("silent", "a", section -> {
                throw new IllegalStateException();
            })
            .register("fine", "a", section -> calls.add("fine"));

        ReloadReport report = reloads.apply(List.of("a.b"), config);

        assertThat(calls).containsExactly("fine");
        assertThat(report.applied()).containsExactly("fine");
        assertThat(report.failed()).containsEntry("broken", "bad value").containsEntry("silent", "IllegalStateException");
        assertThat(report.live()).isFalse();
        assertThat(report.covered()).isTrue();
    }

    @Test
    void applyingEverythingCallsEveryComponentInRegistrationOrderWithTheGivenConfig() {
        config.set("marker", "seen");
        ConfigReloads reloads = new ConfigReloads()
            .register("one", "a", section -> calls.add("one " + section.getString("marker")))
            .register("two", "b", section -> calls.add("two"));

        ReloadReport report = reloads.applyAll(config);

        assertThat(calls).containsExactly("one seen", "two");
        assertThat(report.applied()).containsExactly("one", "two");
        assertThat(reloads.names()).containsExactly("one", "two");
    }
}
