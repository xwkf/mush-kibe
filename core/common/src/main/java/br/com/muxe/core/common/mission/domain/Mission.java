package br.com.muxe.core.common.mission.domain;

import java.util.Objects;

public record Mission(String id, MissionCadence cadence, MissionGoal goal, long target, long xp, long credits) {

    public Mission {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(cadence, "cadence");
        Objects.requireNonNull(goal, "goal");
        if (target <= 0) {
            throw new IllegalArgumentException("The mission " + id + " needs a positive target");
        }
    }
}
