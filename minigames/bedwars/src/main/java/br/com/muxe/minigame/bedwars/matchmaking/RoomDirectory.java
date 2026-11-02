package br.com.muxe.minigame.bedwars.matchmaking;

import br.com.muxe.minigame.bedwars.network.ArenaInfo;
import br.com.muxe.minigame.bedwars.network.RoomInfo;
import br.com.muxe.minigame.bedwars.network.RoomsPacket;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class RoomDirectory {

    private final Clock clock;
    private final Duration timeToLive;
    private final Map<String, Reported> servers = new ConcurrentHashMap<>();

    public RoomDirectory(Clock clock, Duration timeToLive) {
        this.clock = clock;
        this.timeToLive = timeToLive;
    }

    public void update(RoomsPacket packet) {
        servers.put(packet.serverId(), new Reported(packet, clock.instant()));
    }

    public List<RoomsPacket> alive() {
        Instant oldest = clock.instant().minus(timeToLive);
        servers.values().removeIf(reported -> reported.at().isBefore(oldest));
        return servers.values().stream().map(Reported::packet).sorted(Comparator.comparing(RoomsPacket::serverId)).toList();
    }

    public int playersIn(String mode) {
        return alive().stream().flatMap(server -> server.rooms().stream()).filter(room -> room.mode().equals(mode)).mapToInt(RoomInfo::players).sum();
    }

    public int playersIn(Set<String> modes) {
        return modes.stream().mapToInt(this::playersIn).sum();
    }

    public List<ArenaInfo> arenas(String mode) {
        Map<String, ArenaInfo> byName = new LinkedHashMap<>();
        for (RoomsPacket server : alive()) {
            for (ArenaInfo arena : server.arenas()) {
                if (arena.mode().equals(mode)) {
                    byName.putIfAbsent(arena.name(), arena);
                }
            }
        }
        return List.copyOf(byName.values());
    }

    public RoomCount rooms(String mode, String arena) {
        List<RoomInfo> rooms = alive().stream()
            .flatMap(server -> server.rooms().stream())
            .filter(room -> room.mode().equals(mode) && room.arena().equals(arena))
            .collect(Collectors.toList());
        return new RoomCount((int) rooms.stream().filter(RoomInfo::hasRoom).count(), rooms.size());
    }

    public record RoomCount(int available, int total) {
    }

    private record Reported(RoomsPacket packet, Instant at) {
    }
}
