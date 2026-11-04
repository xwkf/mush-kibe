package br.com.muxe.minigame.bedwars.cosmetic.preview.config;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public sealed interface PreviewEdit {

    record Write(Map<String, Object> values, String detail) implements PreviewEdit {

        public Write {
            values = Collections.unmodifiableMap(new LinkedHashMap<>(values));
        }
    }

    record Refused(String message, String value) implements PreviewEdit {
    }
}
