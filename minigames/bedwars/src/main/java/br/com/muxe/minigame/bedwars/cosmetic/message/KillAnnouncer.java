package br.com.muxe.minigame.bedwars.cosmetic.message;

import br.com.muxe.minigame.bedwars.cosmetic.message.LifetimeTally.Counter;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.function.Function;

public final class KillAnnouncer {

    private final MessageSets sets;
    private final Function<UUID, MessageSet> chosen;
    private final LifetimeTally tally;

    public KillAnnouncer(MessageSets sets, Function<UUID, MessageSet> chosen, LifetimeTally tally) {
        this.sets = sets;
        this.chosen = chosen;
        this.tally = tally;
    }

    public String kill(UUID killer, String killerName, UUID victim, String victimName, boolean finalKill, boolean voidDeath) {
        OptionalLong kills = tally.record(killer, finalKill ? Counter.FINAL_KILLS : Counter.KILLS);
        tally.record(victim, finalKill ? Counter.FINAL_DEATHS : Counter.DEATHS);
        return sets.render(chosen.apply(killer), MessageKind.kill(finalKill, voidDeath), MessageValues.kill(victimName, killerName, kills));
    }

    public String death(UUID victim, String victimName, boolean finalDeath, boolean voidDeath) {
        OptionalLong deaths = tally.record(victim, finalDeath ? Counter.FINAL_DEATHS : Counter.DEATHS);
        return sets.render(chosen.apply(victim), MessageKind.death(finalDeath, voidDeath), MessageValues.death(victimName, deaths));
    }

    public String bed(UUID breaker, String breakerName, String teamColor, String team) {
        OptionalLong beds = tally.record(breaker, Counter.BEDS);
        return sets.bedPrefix() + sets.render(chosen.apply(breaker), MessageKind.BED, MessageValues.bed(breakerName, teamColor, team, beds));
    }
}
