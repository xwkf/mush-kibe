package br.com.muxe.minigame.bedwars.stats.domain;

public record StatCounters(
    long games,
    long bedsBroken,
    long bedsLost,
    long kills,
    long deaths,
    long assists,
    long finalKills,
    long finalDeaths,
    long finalAssists,
    long wins,
    long losses,
    long winstreak,
    long bestWinstreak,
    long xp
) {

    public static final StatCounters ZERO = new StatCounters(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

    public StatCounters plus(MatchOutcome outcome) {
        long streak = switch (outcome.result()) {
            case WIN -> winstreak + 1;
            case LOSS -> 0;
            case NONE -> winstreak;
        };
        return new StatCounters(
            games + 1,
            bedsBroken + outcome.bedsBroken(),
            bedsLost + outcome.bedsLost(),
            kills + outcome.kills(),
            deaths + outcome.deaths(),
            assists + outcome.assists(),
            finalKills + outcome.finalKills(),
            finalDeaths + outcome.finalDeaths(),
            finalAssists + outcome.finalAssists(),
            wins + (outcome.result() == MatchResult.WIN ? 1 : 0),
            losses + (outcome.result() == MatchResult.LOSS ? 1 : 0),
            streak,
            Math.max(bestWinstreak, streak),
            xp + outcome.xp()
        );
    }
}
