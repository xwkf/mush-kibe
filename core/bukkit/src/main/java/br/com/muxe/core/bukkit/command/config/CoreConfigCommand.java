package br.com.muxe.core.bukkit.command.config;

import br.com.muxe.core.bukkit.command.framework.annotation.Command;
import br.com.muxe.core.bukkit.command.framework.annotation.Permission;
import br.com.muxe.core.bukkit.config.edit.ConfigEditor;

@Command(name = "coreconfig", description = "Views and changes the Muxe-Core configuration.")
@Permission(ConfigCommand.PERMISSION)
public final class CoreConfigCommand extends ConfigCommand {

    public CoreConfigCommand(ConfigEditor editor, ConfigReplies replies) {
        super(editor, replies);
    }
}
