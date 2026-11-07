package br.com.muxe.minigame.api.command;

import br.com.muxe.core.bukkit.command.framework.annotation.Arg;
import br.com.muxe.core.bukkit.command.framework.annotation.Command;
import br.com.muxe.core.bukkit.command.framework.annotation.Default;
import br.com.muxe.core.bukkit.command.framework.annotation.Greedy;
import br.com.muxe.minigame.api.runtime.MatchService;
import org.bukkit.entity.Player;

@Command(name = "g", description = "Talks to every player of the match.", usage = "global-usage")
public final class GlobalChatCommand {

    private final MatchService<?, ?> matches;

    public GlobalChatCommand(MatchService<?, ?> matches) {
        this.matches = matches;
    }

    @Default
    public void say(Player player, @Arg("mensagem") @Greedy String message) {
        String text = message.strip();
        if (text.isEmpty()) {
            return;
        }
        matches.shout(player.getUniqueId(), () -> player.chat(text.startsWith("/") ? " " + text : text));
    }
}
