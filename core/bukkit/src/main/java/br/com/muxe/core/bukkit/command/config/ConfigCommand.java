package br.com.muxe.core.bukkit.command.config;

import br.com.muxe.core.bukkit.command.framework.annotation.Arg;
import br.com.muxe.core.bukkit.command.framework.annotation.Greedy;
import br.com.muxe.core.bukkit.command.framework.annotation.Subcommand;
import br.com.muxe.core.bukkit.command.framework.annotation.Suggest;
import br.com.muxe.core.bukkit.config.edit.ConfigEditor;
import br.com.muxe.core.bukkit.config.edit.ConfigFlags;
import br.com.muxe.core.bukkit.config.edit.ConfigOutcome;
import br.com.muxe.core.bukkit.config.edit.LocationValues;
import java.util.Optional;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class ConfigCommand {

    public static final String PERMISSION = "muxe.admin.config";

    private static final Set<String> VALUE_FLAGS = Set.of(ConfigFlags.NEW);
    private static final Set<String> LOCATION_FLAGS = Set.of(ConfigFlags.NEW, ConfigFlags.CENTER, ConfigFlags.WORLD);

    private final ConfigEditor editor;
    private final ConfigReplies replies;

    protected ConfigCommand(ConfigEditor editor, ConfigReplies replies) {
        this.editor = editor;
        this.replies = replies;
    }

    protected final ConfigEditor editor() {
        return editor;
    }

    protected final ConfigReplies replies() {
        return replies;
    }

    @Subcommand("ver")
    public void view(CommandSender sender, @Arg("caminho") @Suggest(provider = ConfigPathSuggestions.NAME) Optional<String> path) {
        replies.view(sender, editor.view(path.orElse("")));
    }

    @Subcommand("definir")
    public void set(CommandSender sender, @Arg("caminho") @Suggest(provider = ConfigPathSuggestions.NAME) String path, @Arg("valor") @Greedy String value) {
        ConfigFlags flags = ConfigFlags.parse(value, VALUE_FLAGS);
        if (flags.rest().isEmpty()) {
            replies.send(sender, "config.missing-value");
            return;
        }
        ConfigOutcome outcome = editor.set(path, flags.rest(), flags.has(ConfigFlags.NEW));
        if (replies.refused(sender, outcome)) {
            return;
        }
        replies.send(sender, "config.set", "path", outcome.path(), "value", outcome.detail());
        replies.applied(sender, outcome);
    }

    @Subcommand("adicionar")
    public void add(CommandSender sender, @Arg("caminho") @Suggest(provider = ConfigPathSuggestions.NAME) String path, @Arg("valor") @Greedy String value) {
        ConfigOutcome outcome = editor.add(path, value);
        if (replies.refused(sender, outcome)) {
            return;
        }
        replies.send(sender, "config.list-added", "path", outcome.path(), "value", value.trim());
        replies.applied(sender, outcome);
    }

    @Subcommand("remover")
    public void remove(CommandSender sender, @Arg("caminho") @Suggest(provider = ConfigPathSuggestions.NAME) String path, @Arg("valor") @Greedy String value) {
        ConfigOutcome outcome = editor.remove(path, value);
        if (replies.refused(sender, outcome)) {
            return;
        }
        replies.send(sender, "config.list-removed", "path", outcome.path(), "value", value.trim());
        replies.applied(sender, outcome);
    }

    @Subcommand("local")
    public void location(Player player, @Arg("caminho") @Suggest(provider = ConfigPathSuggestions.NAME) String path, @Arg("opções") @Greedy Optional<String> options) {
        ConfigFlags flags = ConfigFlags.parse(options, LOCATION_FLAGS);
        if (!flags.rest().isEmpty()) {
            replies.send(player, "config.unknown-option", "option", flags.rest(), "options", String.join(" ", LOCATION_FLAGS.stream().sorted().toList()));
            return;
        }
        LocationValues here = here(player.getLocation(), flags.has(ConfigFlags.CENTER));
        ConfigOutcome outcome = editor.setLocation(path, here, flags.has(ConfigFlags.WORLD), flags.has(ConfigFlags.NEW));
        if (replies.refused(player, outcome)) {
            return;
        }
        replies.send(player, "config.location-set", "path", path, "location", here.describe());
        replies.applied(player, outcome);
    }

    @Subcommand("recarregar")
    public void reload(CommandSender sender) {
        replies.reloaded(sender, editor.reloadAll());
    }

    protected static LocationValues here(Location location, boolean centered) {
        return LocationValues.of(location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch(), centered);
    }
}
