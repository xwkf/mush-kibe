package br.com.muxe.minigame.api.match;

import br.com.muxe.minigame.engine.team.TeamColor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class MatchTeam {

    private final TeamColor color;
    private final List<UUID> members = new ArrayList<>();
    private boolean respawns;
    private UUID respawnTakenBy;

    public MatchTeam(TeamColor color, boolean respawns) {
        this.color = color;
        this.respawns = respawns;
    }

    public TeamColor color() {
        return color;
    }

    public List<UUID> members() {
        return Collections.unmodifiableList(members);
    }

    void add(UUID member) {
        members.add(member);
    }

    public boolean respawns() {
        return respawns;
    }

    public Optional<UUID> respawnTakenBy() {
        return Optional.ofNullable(respawnTakenBy);
    }

    public void stopRespawning(UUID takenBy) {
        respawns = false;
        respawnTakenBy = takenBy;
    }
}
