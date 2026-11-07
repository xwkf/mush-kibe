package br.com.muxe.minigame.api.cosmetic.message;

import br.com.muxe.core.bukkit.text.Colors;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.bukkit.configuration.ConfigurationSection;

public final class MessageSets {

    private final Map<String, MessageSet> sets;
    private final MessageSet fallback;
    private final String bedPrefix;

    public MessageSets(List<MessageSet> sets, String fallbackId, String bedPrefix) {
        Map<String, MessageSet> byId = new LinkedHashMap<>();
        sets.forEach(set -> byId.put(set.id(), set));
        MessageSet defaultSet = byId.get(fallbackId);
        if (defaultSet == null) {
            throw new IllegalArgumentException("The default message set " + fallbackId + " is not defined");
        }
        for (MessageKind kind : MessageKind.values()) {
            if (!defaultSet.defines(kind) || defaultSet.needsCount(kind)) {
                throw new IllegalArgumentException("The default message set " + fallbackId + " needs a message for " + kind.key() + " without counters");
            }
        }
        this.sets = byId;
        this.fallback = defaultSet;
        this.bedPrefix = bedPrefix;
    }

    public static MessageSets from(ConfigurationSection section) {
        String lineColor = Colors.translate(section.getString("line-color", "&7"));
        ConfigurationSection setSection = required(section, "sets");
        List<MessageSet> sets = new ArrayList<>();
        for (String id : setSection.getKeys(false)) {
            ConfigurationSection own = required(setSection, id);
            ConfigurationSection texts = required(own, "messages");
            Map<MessageKind, String> templates = new EnumMap<>(MessageKind.class);
            for (String key : texts.getKeys(false)) {
                MessageKind kind = MessageKind.fromKey(key).orElseThrow(() -> new IllegalArgumentException("Unknown message " + key + " in the set " + id));
                templates.put(kind, Colors.translate(texts.getString(key)));
            }
            List<MessageKind> preview = own.getStringList("preview").stream()
                .map(key -> MessageKind.fromKey(key).orElseThrow(() -> new IllegalArgumentException("Unknown preview message " + key + " in the set " + id)))
                .toList();
            sets.add(new MessageSet(id, lineColor, templates, preview));
        }
        return new MessageSets(sets, Objects.requireNonNull(section.getString("default"), "Missing kill-messages.default"),
            Colors.translate(section.getString("bed-prefix", "")));
    }

    public Optional<MessageSet> find(String id) {
        return Optional.ofNullable(sets.get(id));
    }

    public List<MessageSet> all() {
        return List.copyOf(sets.values());
    }

    public MessageSet fallback() {
        return fallback;
    }

    public String bedPrefix() {
        return bedPrefix;
    }

    public String render(MessageSet set, MessageKind kind, MessageValues values) {
        boolean usable = set.defines(kind) && (values.count().isPresent() || !set.needsCount(kind));
        return (usable ? set : fallback).render(kind, values);
    }

    private static ConfigurationSection required(ConfigurationSection section, String path) {
        return Objects.requireNonNull(section.getConfigurationSection(path), "Missing configuration section " + section.getCurrentPath() + "." + path);
    }
}
