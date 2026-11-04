package br.com.muxe.lobby.command;

import br.com.muxe.core.bukkit.command.config.ConfigCommand;
import br.com.muxe.core.bukkit.command.config.ConfigReplies;
import br.com.muxe.core.bukkit.command.framework.annotation.Command;
import br.com.muxe.core.bukkit.command.framework.annotation.Permission;
import br.com.muxe.core.bukkit.config.edit.ConfigEditor;

@Command(name = "lobbyconfig", description = "Views and changes the lobby configuration.")
@Permission(ConfigCommand.PERMISSION)
public final class LobbyConfigCommand extends ConfigCommand {

    public LobbyConfigCommand(ConfigEditor editor, ConfigReplies replies) {
        super(editor, replies);
    }
}
