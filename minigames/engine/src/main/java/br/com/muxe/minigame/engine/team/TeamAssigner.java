package br.com.muxe.minigame.engine.team;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

public final class TeamAssigner {

    private TeamAssigner() {
    }

    public static Map<TeamColor, List<UUID>> assign(List<TeamColor> teams, int teamSize, List<UUID> players, List<List<UUID>> parties) {
        return assign(teams, teamSize, players, parties, Map.of(), ThreadLocalRandom.current());
    }

    public static Map<TeamColor, List<UUID>> assign(List<TeamColor> teams, int teamSize, List<UUID> players, List<List<UUID>> parties,
                                                    Map<UUID, TeamColor> preferences, RandomGenerator random) {
        Map<TeamColor, List<UUID>> result = new LinkedHashMap<>();
        teams.forEach(team -> result.put(team, new ArrayList<>()));
        Set<UUID> placed = new HashSet<>();
        for (List<UUID> group : partyGroups(players, parties, placed)) {
            placeParty(result, teamSize, group, preferences);
        }
        placePreferred(result, teamSize, players, placed, preferences, random);
        placeRest(result, teamSize, players, placed);
        return result;
    }

    private static List<List<UUID>> partyGroups(List<UUID> players, List<List<UUID>> parties, Set<UUID> placed) {
        List<List<UUID>> groups = new ArrayList<>();
        for (List<UUID> party : parties) {
            List<UUID> present = party.stream().filter(players::contains).filter(placed::add).toList();
            if (present.size() > 1) {
                groups.add(present);
            } else {
                present.forEach(placed::remove);
            }
        }
        groups.sort(Comparator.comparingInt((List<UUID> group) -> group.size()).reversed());
        return groups;
    }

    private static void placeParty(Map<TeamColor, List<UUID>> teams, int teamSize, List<UUID> group, Map<UUID, TeamColor> preferences) {
        List<UUID> remaining = new ArrayList<>(group);
        TeamColor target = commonPreference(group, preferences).filter(color -> teams.containsKey(color) && teams.get(color).isEmpty()).orElse(null);
        while (!remaining.isEmpty()) {
            if (target == null) {
                target = emptiest(teams, teamSize);
            }
            if (target == null) {
                break;
            }
            List<UUID> members = teams.get(target);
            while (!remaining.isEmpty() && members.size() < teamSize) {
                members.add(remaining.removeFirst());
            }
            target = null;
        }
    }

    private static Optional<TeamColor> commonPreference(List<UUID> group, Map<UUID, TeamColor> preferences) {
        Map<TeamColor, Integer> votes = new LinkedHashMap<>();
        group.stream().map(preferences::get).filter(color -> color != null).forEach(color -> votes.merge(color, 1, Integer::sum));
        TeamColor best = null;
        int most = 0;
        for (Map.Entry<TeamColor, Integer> vote : votes.entrySet()) {
            if (vote.getValue() > most) {
                best = vote.getKey();
                most = vote.getValue();
            }
        }
        return Optional.ofNullable(best);
    }

    private static void placePreferred(Map<TeamColor, List<UUID>> teams, int teamSize, List<UUID> players, Set<UUID> placed,
                                       Map<UUID, TeamColor> preferences, RandomGenerator random) {
        for (Map.Entry<TeamColor, List<UUID>> team : teams.entrySet()) {
            List<UUID> candidates = players.stream().filter(player -> !placed.contains(player)).filter(player -> preferences.get(player) == team.getKey()).toList();
            int free = teamSize - team.getValue().size();
            if (candidates.isEmpty() || free <= 0) {
                continue;
            }
            List<UUID> chosen = candidates.size() <= free ? candidates : draw(candidates, free, random);
            team.getValue().addAll(chosen);
            placed.addAll(chosen);
        }
    }

    private static List<UUID> draw(List<UUID> candidates, int seats, RandomGenerator random) {
        List<UUID> shuffled = new ArrayList<>(candidates);
        Collections.shuffle(shuffled, random);
        Set<UUID> winners = new HashSet<>(shuffled.subList(0, seats));
        return candidates.stream().filter(winners::contains).toList();
    }

    private static void placeRest(Map<TeamColor, List<UUID>> teams, int teamSize, List<UUID> players, Set<UUID> placed) {
        for (UUID player : players) {
            if (placed.contains(player)) {
                continue;
            }
            TeamColor target = emptiest(teams, teamSize);
            if (target == null) {
                break;
            }
            teams.get(target).add(player);
            placed.add(player);
        }
    }

    private static TeamColor emptiest(Map<TeamColor, List<UUID>> teams, int teamSize) {
        TeamColor best = null;
        int fewest = Integer.MAX_VALUE;
        for (Map.Entry<TeamColor, List<UUID>> team : teams.entrySet()) {
            int size = team.getValue().size();
            if (size < teamSize && size < fewest) {
                best = team.getKey();
                fewest = size;
            }
        }
        return best;
    }
}
