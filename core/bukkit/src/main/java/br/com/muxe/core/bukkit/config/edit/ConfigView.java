package br.com.muxe.core.bukkit.config.edit;

import java.util.List;

public sealed interface ConfigView {

    record Missing(String path) implements ConfigView {
    }

    record Hidden(String path) implements ConfigView {
    }

    record Scalar(String path, String value, String type) implements ConfigView {
    }

    record Items(String path, List<String> items) implements ConfigView {

        public Items {
            items = List.copyOf(items);
        }
    }

    record Keys(String path, List<String> keys) implements ConfigView {

        public Keys {
            keys = List.copyOf(keys);
        }
    }
}
