package br.com.muxe.minigame.bedwars.room;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RoomReservations {

    private final Duration timeToLive;
    private final Map<UUID, Reservation> byPlayer = new ConcurrentHashMap<>();

    public RoomReservations(Duration timeToLive) {
        this.timeToLive = timeToLive;
    }

    public void hold(UUID playerId, String code, String mode, Optional<String> arena, Instant now) {
        byPlayer.put(playerId, new Reservation(code, mode, arena, now.plus(timeToLive)));
    }

    public Optional<Reservation> take(UUID playerId, Instant now) {
        Reservation reservation = byPlayer.remove(playerId);
        return reservation == null || reservation.expiredAt(now) ? Optional.empty() : Optional.of(reservation);
    }

    public int pending(String code, Instant now) {
        return (int) byPlayer.values().stream().filter(reservation -> reservation.code().equals(code) && !reservation.expiredAt(now)).count();
    }

    public int purge(Instant now) {
        int before = byPlayer.size();
        byPlayer.values().removeIf(reservation -> reservation.expiredAt(now));
        return before - byPlayer.size();
    }

    public int size() {
        return byPlayer.size();
    }

    public record Reservation(String code, String mode, Optional<String> arena, Instant expiresAt) {

        boolean expiredAt(Instant now) {
            return now.isAfter(expiresAt);
        }
    }
}
