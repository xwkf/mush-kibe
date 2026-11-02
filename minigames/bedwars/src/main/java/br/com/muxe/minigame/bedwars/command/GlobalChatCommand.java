package br.com.muxe.minigame.bedwars.command;

import br.com.muxe.core.bukkit.command.framework.annotation.Arg;
import br.com.muxe.core.bukkit.command.framework.annotation.Command;
import br.com.muxe.core.bukkit.command.framework.annotation.Default;
import br.com.muxe.core.bukkit.command.framework.annotation.Greedy;
import br.com.muxe.minigame.bedwars.room.RoomService;
import org.bukkit.entity.Player;

@Command(name = "g", description = "Talks to every player of the match.", usage = "global-usage")
public final class GlobalChatCommand {

    private final RoomService rooms;

    public GlobalChatCommand(RoomService rooms) {
        this.rooms = rooms;
    }

    @Default
    public void say(Player player, @Arg("mensagem") @Greedy String message) {
        rooms.shout(player, message);
    }
}
