package br.com.muxe.core.bukkit.config.edit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ConfigPathsTest {

    @Test
    void validPathsAreWordsJoinedByDots() {
        assertThat(ConfigPaths.isValid("preview")).isTrue();
        assertThat(ConfigPaths.isValid("preview.spawn.x")).isTrue();
        assertThat(ConfigPaths.isValid("kill-messages.sets.padrao_2")).isTrue();
        assertThat(ConfigPaths.isValid("previsão.local")).isTrue();
    }

    @Test
    void emptyDottedAndOddPathsAreRejected() {
        assertThat(ConfigPaths.isValid(null)).isFalse();
        assertThat(ConfigPaths.isValid("")).isFalse();
        assertThat(ConfigPaths.isValid(".preview")).isFalse();
        assertThat(ConfigPaths.isValid("preview.")).isFalse();
        assertThat(ConfigPaths.isValid("preview..x")).isFalse();
        assertThat(ConfigPaths.isValid("preview x")).isFalse();
        assertThat(ConfigPaths.isValid("preview/x")).isFalse();
        assertThat(ConfigPaths.isValid("a".repeat(ConfigPaths.MAX_LENGTH + 1))).isFalse();
    }

    @Test
    void secretsAreRecognisedAnywhereInThePathIgnoringCase() {
        assertThat(ConfigPaths.isSensitive("database.password")).isTrue();
        assertThat(ConfigPaths.isSensitive("redis.Password")).isTrue();
        assertThat(ConfigPaths.isSensitive("messaging.secret")).isTrue();
        assertThat(ConfigPaths.isSensitive("api.access-token")).isTrue();
        assertThat(ConfigPaths.isSensitive("preview.timeout")).isFalse();
    }

    @Test
    void aPrefixCoversItsOwnSubtreeAndTheSectionsAboveIt() {
        assertThat(ConfigPaths.covers("preview", "preview")).isTrue();
        assertThat(ConfigPaths.covers("preview", "preview.spawn.x")).isTrue();
        assertThat(ConfigPaths.covers("preview.spawn", "preview")).isTrue();
        assertThat(ConfigPaths.covers("preview", "previews.spawn")).isFalse();
        assertThat(ConfigPaths.covers("preview.spawn", "preview.bed.foot.x")).isFalse();
        assertThat(ConfigPaths.covers("", "anything")).isTrue();
    }

    @Test
    void parentsAndAncestorsWalkUpTheDots() {
        assertThat(ConfigPaths.parent("a.b.c")).isEqualTo("a.b");
        assertThat(ConfigPaths.parent("a")).isEmpty();
        assertThat(ConfigPaths.ancestors("a.b.c")).containsExactly("a", "a.b");
        assertThat(ConfigPaths.ancestors("a")).isEmpty();
        assertThat(ConfigPaths.join("", "a")).isEqualTo("a");
        assertThat(ConfigPaths.join("a", "b")).isEqualTo("a.b");
    }
}
