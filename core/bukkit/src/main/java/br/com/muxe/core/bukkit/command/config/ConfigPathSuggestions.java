package br.com.muxe.core.bukkit.command.config;

import br.com.muxe.core.bukkit.command.framework.Commands;
import br.com.muxe.core.bukkit.command.framework.completion.SuggestionProvider;
import br.com.muxe.core.bukkit.config.edit.ConfigEditor;
import java.util.Collection;
import org.bukkit.command.CommandSender;

public final class ConfigPathSuggestions implements SuggestionProvider {

    public static final String NAME = "config-paths";

    private final ConfigEditor editor;

    public ConfigPathSuggestions(ConfigEditor editor) {
        this.editor = editor;
    }

    public static void register(Commands commands, ConfigEditor editor) {
        commands.suggestions(NAME, new ConfigPathSuggestions(editor));
    }

    @Override
    public Collection<String> suggest(CommandSender sender) {
        return editor.suggest("");
    }

    @Override
    public Collection<String> suggest(CommandSender sender, String typed) {
        return editor.suggest(typed);
    }
}
