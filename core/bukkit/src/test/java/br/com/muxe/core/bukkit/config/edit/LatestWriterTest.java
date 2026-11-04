package br.com.muxe.core.bukkit.config.edit;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LatestWriterTest {

    private final Logger logger = Logger.getLogger("test");

    @TempDir
    Path directory;

    @Test
    void theContentReachesTheFileAsUtf8WhenTheExecutorRuns() throws IOException {
        Path file = directory.resolve("config.yml");

        new LatestWriter(file, Runnable::run, logger).write("mensagem: \"Olá\"\n");

        assertThat(Files.readString(file, StandardCharsets.UTF_8)).isEqualTo("mensagem: \"Olá\"\n");
    }

    @Test
    void writesQueuedBehindEachOtherLeaveOnlyTheLatestContentOnDisk() throws IOException {
        Path file = directory.resolve("config.yml");
        List<Runnable> queue = new ArrayList<>();
        LatestWriter writer = new LatestWriter(file, queue::add, logger);

        writer.write("first");
        writer.write("second");
        writer.write("third");
        queue.forEach(Runnable::run);

        assertThat(Files.readString(file)).isEqualTo("third");
    }

    @Test
    void flushingWritesPendingContentAtOnceAndIsHarmlessWhenNothingIsPending() throws IOException {
        Path file = directory.resolve("config.yml");
        List<Runnable> queue = new ArrayList<>();
        LatestWriter writer = new LatestWriter(file, queue::add, logger);

        writer.write("pending");
        writer.flush();
        writer.flush();

        assertThat(Files.readString(file)).isEqualTo("pending");
        queue.forEach(Runnable::run);
        assertThat(Files.readString(file)).isEqualTo("pending");
    }

    @Test
    void aFailedWriteIsLoggedInsteadOfThrown() {
        Path missing = directory.resolve("no-such-folder").resolve("config.yml");

        new LatestWriter(missing, Runnable::run, logger).write("x");

        assertThat(Files.exists(missing)).isFalse();
    }
}
