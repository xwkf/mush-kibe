package br.com.muxe.afk.session;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AfkSessions {

    private final Clock clock;
    private final Duration grace;
    private final Duration retryAfter;
    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();

    public AfkSessions(Clock clock, Duration grace, Duration retryAfter) {
        this.clock = clock;
        this.grace = grace;
        this.retryAfter = retryAfter;
    }

    public void arrive(UUID player) {
        sessions.put(player, new Session(clock.instant(), null));
    }

    public synchronized boolean startReturn(UUID player) {
        Instant now = clock.instant();
        Session session = sessions.get(player);
        if (session == null || !session.mayReturn(now, grace, retryAfter)) {
            return false;
        }
        sessions.put(player, new Session(session.arrivedAt(), now));
        return true;
    }

    public synchronized void returnFailed(UUID player) {
        Session session = sessions.get(player);
        if (session != null) {
            sessions.put(player, new Session(session.arrivedAt(), null));
        }
    }

    public void leave(UUID player) {
        sessions.remove(player);
    }

    private record Session(Instant arrivedAt, Instant returningSince) {

        boolean mayReturn(Instant now, Duration grace, Duration retryAfter) {
            if (now.isBefore(arrivedAt.plus(grace))) {
                return false;
            }
            return returningSince == null || !now.isBefore(returningSince.plus(retryAfter));
        }
    }
}
