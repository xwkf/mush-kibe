package br.com.muxe.core.common.mission.domain;

import java.util.Objects;

public record MissionGoal(String id, boolean best) {

    public static final MissionGoal WEEKLY_MISSIONS = new MissionGoal("WEEKLY_MISSIONS", true);

    public MissionGoal {
        Objects.requireNonNull(id, "id");
    }

    public long apply(long progress, long amount) {
        return best ? Math.max(progress, amount) : progress + amount;
    }
}
