package br.com.muxe.core.bukkit.config.edit;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class ConfigPaths {

    public static final int MAX_LENGTH = 200;
    public static final String SEPARATOR = ".";

    private static final Pattern VALID = Pattern.compile("[\\p{L}\\p{N}_-]+(\\.[\\p{L}\\p{N}_-]+)*");
    private static final List<String> SENSITIVE_WORDS = List.of("password", "senha", "secret", "token", "credential");

    private ConfigPaths() {
    }

    public static boolean isValid(String path) {
        return path != null && path.length() <= MAX_LENGTH && VALID.matcher(path).matches();
    }

    public static boolean isSensitive(String path) {
        String lower = path.toLowerCase(Locale.ROOT);
        return SENSITIVE_WORDS.stream().anyMatch(lower::contains);
    }

    public static boolean covers(String prefix, String path) {
        return prefix.isEmpty()
            || path.equals(prefix)
            || path.startsWith(prefix + SEPARATOR)
            || prefix.startsWith(path + SEPARATOR);
    }

    public static String parent(String path) {
        int cut = path.lastIndexOf('.');
        return cut < 0 ? "" : path.substring(0, cut);
    }

    public static String join(String parent, String key) {
        return parent.isEmpty() ? key : parent + SEPARATOR + key;
    }

    public static List<String> ancestors(String path) {
        List<String> found = new ArrayList<>();
        String current = parent(path);
        while (!current.isEmpty()) {
            found.add(current);
            current = parent(current);
        }
        return found.reversed();
    }
}
