package br.com.muxe.minigame.engine.setup.session;

import br.com.muxe.minigame.engine.world.anvil.AnvilImporter;
import br.com.muxe.minigame.engine.world.map.MapTemplate;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

public record SetupMap(MapTemplate template, Origin origin) {

    public static SetupMap choose(Optional<MapTemplate> saved, Path worldFolder) throws IOException {
        if (saved.isPresent() && saved.get().chunkCount() > 0) {
            return new SetupMap(saved.get(), Origin.LOADED);
        }
        if (AnvilImporter.isWorldFolder(worldFolder)) {
            return new SetupMap(AnvilImporter.importWorld(worldFolder), Origin.CONVERTED);
        }
        return new SetupMap(saved.orElseGet(MapTemplate::empty), saved.isPresent() ? Origin.LOADED : Origin.EMPTY);
    }

    public boolean needsSaving() {
        return origin != Origin.LOADED;
    }

    public enum Origin {
        LOADED("setup-loaded"),
        CONVERTED("setup-converted"),
        EMPTY("setup-created-empty");

        private final String messageKey;

        Origin(String messageKey) {
            this.messageKey = messageKey;
        }

        public String messageKey() {
            return messageKey;
        }
    }
}
