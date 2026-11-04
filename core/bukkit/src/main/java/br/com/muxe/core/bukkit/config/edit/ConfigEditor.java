package br.com.muxe.core.bukkit.config.edit;

import br.com.muxe.core.bukkit.config.edit.ConfigOutcome.Status;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import org.bukkit.configuration.ConfigurationSection;

public final class ConfigEditor {

    public static final int SUGGESTION_LIMIT = 100;
    public static final String SECTION_MARK = ".";

    private final ConfigStore store;
    private final ConfigReloads reloads;
    private final Set<String> pendingRestart = new LinkedHashSet<>();

    public ConfigEditor(ConfigStore store, ConfigReloads reloads) {
        this.store = store;
        this.reloads = reloads;
    }

    public ConfigView view(String path) {
        ConfigurationSection config = store.config();
        if (!path.isEmpty() && !ConfigPaths.isValid(path)) {
            return new ConfigView.Missing(path);
        }
        if (!path.isEmpty() && ConfigPaths.isSensitive(path)) {
            return new ConfigView.Hidden(path);
        }
        if (path.isEmpty() || config.isConfigurationSection(path)) {
            ConfigurationSection section = path.isEmpty() ? config : config.getConfigurationSection(path);
            List<String> keys = new ArrayList<>();
            for (String key : keysOf(section)) {
                keys.add(section.get(key) instanceof ConfigurationSection ? key + SECTION_MARK : key);
            }
            return new ConfigView.Keys(path, keys);
        }
        Object value = config.get(path);
        if (value == null) {
            return new ConfigView.Missing(path);
        }
        if (value instanceof List<?> list) {
            return new ConfigView.Items(path, list.stream().map(String::valueOf).toList());
        }
        return new ConfigView.Scalar(path, String.valueOf(value), ConfigValues.typeName(value));
    }

    public ConfigOutcome set(String path, String raw, boolean allowNew) {
        Optional<ConfigOutcome> refusal = check(path, allowNew);
        if (refusal.isPresent()) {
            return refusal.get();
        }
        Object existing = store.config().get(path);
        if (existing instanceof List<?> list) {
            Optional<List<Object>> parsed = ConfigValues.parseList(raw, list);
            return parsed.isEmpty()
                ? ConfigOutcome.refused(Status.TYPE_MISMATCH, path, itemType(list))
                : commit(Map.of(path, parsed.get()));
        }
        Optional<Object> parsed = ConfigValues.parse(raw, existing);
        return parsed.isEmpty()
            ? ConfigOutcome.refused(Status.TYPE_MISMATCH, path, ConfigValues.typeName(existing))
            : commit(Map.of(path, parsed.get()));
    }

    public ConfigOutcome add(String path, String raw) {
        Optional<ConfigOutcome> refusal = check(path, false);
        if (refusal.isPresent()) {
            return refusal.get();
        }
        if (!(store.config().get(path) instanceof List<?> list)) {
            return ConfigOutcome.refused(Status.NOT_A_LIST, path, "");
        }
        Optional<Object> parsed = ConfigValues.parse(raw, list.isEmpty() ? null : list.getFirst());
        if (parsed.isEmpty()) {
            return ConfigOutcome.refused(Status.TYPE_MISMATCH, path, itemType(list));
        }
        List<Object> next = new ArrayList<>(list);
        next.add(parsed.get());
        return commit(Map.of(path, next));
    }

    public ConfigOutcome remove(String path, String raw) {
        Optional<ConfigOutcome> refusal = check(path, false);
        if (refusal.isPresent()) {
            return refusal.get();
        }
        if (!(store.config().get(path) instanceof List<?> list)) {
            return ConfigOutcome.refused(Status.NOT_A_LIST, path, "");
        }
        Optional<Object> parsed = ConfigValues.parse(raw, list.isEmpty() ? null : list.getFirst());
        if (parsed.isEmpty() || !list.contains(parsed.get())) {
            return ConfigOutcome.refused(Status.MISSING_ITEM, path, raw.trim());
        }
        List<Object> next = new ArrayList<>(list);
        next.remove(parsed.get());
        return commit(Map.of(path, next));
    }

    public ConfigOutcome setAll(Map<String, Object> values, boolean allowNew) {
        for (String path : values.keySet()) {
            Optional<ConfigOutcome> refusal = check(path, allowNew);
            if (refusal.isPresent()) {
                return refusal.get();
            }
        }
        return commit(values);
    }

    public ConfigOutcome setLocation(String path, LocationValues location, boolean forceWorld, boolean allowNew) {
        Optional<ConfigOutcome> refusal = check(path, allowNew, true);
        if (refusal.isPresent()) {
            return refusal.get();
        }
        ConfigurationSection config = store.config();
        boolean includeWorld = forceWorld || !config.isConfigurationSection(path) || config.contains(ConfigPaths.join(path, "world"));
        return commit(location.entries(path, includeWorld));
    }

    public ReloadOutcome reloadAll() {
        store.reload();
        ReloadReport report = reloads.applyAll(store.config());
        if (report.failed().isEmpty()) {
            pendingRestart.removeIf(reloads::covers);
        }
        return new ReloadOutcome(report, List.copyOf(pendingRestart));
    }

    public List<String> pendingRestart() {
        return List.copyOf(pendingRestart);
    }

    public List<String> suggest(String typed) {
        ConfigurationSection config = store.config();
        String parent = ConfigPaths.parent(typed);
        ConfigurationSection section = parent.isEmpty() ? config : config.getConfigurationSection(parent);
        if (section == null) {
            return List.of();
        }
        String lower = typed.toLowerCase(Locale.ROOT);
        List<String> found = new ArrayList<>();
        for (String key : keysOf(section)) {
            String full = ConfigPaths.join(parent, key);
            if (ConfigPaths.isSensitive(full) || !full.toLowerCase(Locale.ROOT).startsWith(lower)) {
                continue;
            }
            found.add(section.get(key) instanceof ConfigurationSection ? full + SECTION_MARK : full);
            if (found.size() >= SUGGESTION_LIMIT) {
                break;
            }
        }
        return found;
    }

    private Optional<ConfigOutcome> check(String path, boolean allowNew) {
        return check(path, allowNew, false);
    }

    private Optional<ConfigOutcome> check(String path, boolean allowNew, boolean sectionAllowed) {
        if (!ConfigPaths.isValid(path)) {
            return Optional.of(ConfigOutcome.refused(Status.INVALID_PATH, path, ""));
        }
        if (ConfigPaths.isSensitive(path)) {
            return Optional.of(ConfigOutcome.refused(Status.PROTECTED, path, ""));
        }
        ConfigurationSection config = store.config();
        if (!sectionAllowed && config.isConfigurationSection(path)) {
            return Optional.of(ConfigOutcome.refused(Status.IS_SECTION, path, ""));
        }
        if (sectionAllowed && config.contains(path) && !config.isConfigurationSection(path)) {
            return Optional.of(ConfigOutcome.refused(Status.BLOCKED, path, path));
        }
        if (!allowNew && !config.contains(path)) {
            return Optional.of(ConfigOutcome.refused(Status.UNKNOWN_PATH, path, ""));
        }
        for (String ancestor : ConfigPaths.ancestors(path)) {
            if (config.contains(ancestor) && !config.isConfigurationSection(ancestor)) {
                return Optional.of(ConfigOutcome.refused(Status.BLOCKED, path, ancestor));
            }
        }
        return Optional.empty();
    }

    private ConfigOutcome commit(Map<String, Object> values) {
        ConfigurationSection config = store.config();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            Optional<ConfigOutcome> refusal = checkLeaf(entry.getKey());
            if (refusal.isPresent()) {
                return refusal.get();
            }
        }
        values.forEach(config::set);
        store.save();
        pendingRestart.addAll(values.keySet());
        ReloadReport report = reloads.apply(values.keySet(), config);
        if (report.live()) {
            pendingRestart.removeAll(values.keySet());
        }
        String first = values.keySet().iterator().next();
        return new ConfigOutcome(Status.CHANGED, first, values.size() == 1 ? describe(values.get(first)) : "", report);
    }

    private Optional<ConfigOutcome> checkLeaf(String path) {
        ConfigurationSection config = store.config();
        if (config.isConfigurationSection(path)) {
            return Optional.of(ConfigOutcome.refused(Status.IS_SECTION, path, ""));
        }
        return Optional.empty();
    }

    private static String describe(Object value) {
        return value instanceof List<?> list ? String.join(", ", list.stream().map(String::valueOf).toList()) : String.valueOf(value);
    }

    private static String itemType(List<?> list) {
        return list.isEmpty() ? ConfigValues.TYPE_TEXT : ConfigValues.typeName(list.getFirst());
    }

    private static Set<String> keysOf(ConfigurationSection section) {
        Set<String> keys = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        keys.addAll(section.getKeys(false));
        ConfigurationSection defaults = section.getDefaultSection();
        if (defaults != null) {
            keys.addAll(defaults.getKeys(false));
        }
        return keys;
    }

    public record ReloadOutcome(ReloadReport report, List<String> pendingRestart) {

        public ReloadOutcome {
            pendingRestart = List.copyOf(pendingRestart);
        }
    }
}
