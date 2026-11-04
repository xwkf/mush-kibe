package br.com.muxe.core.bukkit.config.edit;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class LatestWriter {

    private final Path file;
    private final Executor io;
    private final Logger logger;
    private final AtomicReference<String> pending = new AtomicReference<>();

    public LatestWriter(Path file, Executor io, Logger logger) {
        this.file = file;
        this.io = io;
        this.logger = logger;
    }

    public void write(String content) {
        pending.set(content);
        io.execute(this::flush);
    }

    public synchronized void flush() {
        String content = pending.getAndSet(null);
        if (content == null) {
            return;
        }
        try {
            Files.writeString(file, content, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            logger.log(Level.WARNING, "Could not save " + file, exception);
        }
    }
}
