package br.com.muxe.minigame.api.cosmetic.message;

import java.util.OptionalLong;

public record MessageValues(String victim, String killer, String player, String teamColor, String team, OptionalLong count) {

    public static MessageValues kill(String victim, String killer, OptionalLong count) {
        return new MessageValues(victim, killer, "", "", "", count);
    }

    public static MessageValues death(String victim, OptionalLong count) {
        return new MessageValues(victim, "", "", "", "", count);
    }

    public static MessageValues bed(String player, String teamColor, String team, OptionalLong count) {
        return new MessageValues("", "", player, teamColor, team, count);
    }
}
