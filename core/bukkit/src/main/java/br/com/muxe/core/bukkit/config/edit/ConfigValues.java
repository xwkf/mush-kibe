package br.com.muxe.core.bukkit.config.edit;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

public final class ConfigValues {

    public static final String TYPE_BOOLEAN = "booleano";
    public static final String TYPE_INTEGER = "inteiro";
    public static final String TYPE_DECIMAL = "decimal";
    public static final String TYPE_TEXT = "texto";
    public static final String TYPE_LIST = "lista";

    private static final Pattern INTEGER = Pattern.compile("-?\\d{1,18}");
    private static final Pattern DECIMAL = Pattern.compile("-?\\d+\\.\\d+");
    private static final String QUOTE = "\"";
    private static final String EMPTY_LIST = "[]";

    private ConfigValues() {
    }

    public static Optional<Object> parse(String raw, Object existing) {
        String text = raw.trim();
        if (existing instanceof Boolean) {
            return bool(text).map(value -> (Object) value);
        }
        if (existing instanceof Integer || existing instanceof Long || existing instanceof Short || existing instanceof Byte) {
            return INTEGER.matcher(text).matches() ? Optional.of(whole(text, existing instanceof Long)) : Optional.empty();
        }
        if (existing instanceof Double || existing instanceof Float) {
            return INTEGER.matcher(text).matches() || DECIMAL.matcher(text).matches() ? Optional.of(Double.parseDouble(text)) : Optional.empty();
        }
        if (existing instanceof String) {
            return Optional.of(unquote(text));
        }
        return Optional.of(infer(text));
    }

    public static Object infer(String text) {
        Optional<Boolean> bool = bool(text);
        if (bool.isPresent()) {
            return bool.get();
        }
        if (INTEGER.matcher(text).matches()) {
            return whole(text, false);
        }
        if (DECIMAL.matcher(text).matches()) {
            return Double.parseDouble(text);
        }
        return unquote(text);
    }

    public static Optional<List<Object>> parseList(String raw, List<?> existing) {
        Object hint = existing.isEmpty() ? null : existing.getFirst();
        List<Object> values = new ArrayList<>();
        String trimmed = raw.trim();
        if (trimmed.equals(EMPTY_LIST)) {
            return Optional.of(values);
        }
        for (String part : trimmed.split(",")) {
            if (part.isBlank()) {
                continue;
            }
            Optional<Object> value = parse(part, hint);
            if (value.isEmpty()) {
                return Optional.empty();
            }
            values.add(value.get());
        }
        return Optional.of(values);
    }

    public static String typeName(Object value) {
        if (value instanceof Boolean) {
            return TYPE_BOOLEAN;
        }
        if (value instanceof Integer || value instanceof Long || value instanceof Short || value instanceof Byte) {
            return TYPE_INTEGER;
        }
        if (value instanceof Number) {
            return TYPE_DECIMAL;
        }
        if (value instanceof List<?>) {
            return TYPE_LIST;
        }
        return TYPE_TEXT;
    }

    private static Optional<Boolean> bool(String text) {
        return switch (text.toLowerCase(Locale.ROOT)) {
            case "true" -> Optional.of(Boolean.TRUE);
            case "false" -> Optional.of(Boolean.FALSE);
            default -> Optional.empty();
        };
    }

    private static Object whole(String text, boolean wide) {
        long value = Long.parseLong(text);
        if (!wide && value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) {
            return (int) value;
        }
        return value;
    }

    private static String unquote(String text) {
        return text.length() >= 2 && text.startsWith(QUOTE) && text.endsWith(QUOTE) ? text.substring(1, text.length() - 1) : text;
    }
}
