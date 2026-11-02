package br.com.muxe.core.common.mission.domain;

import java.time.LocalDate;
import java.util.Objects;

public record MissionProgress(String mission, LocalDate start, long progress, boolean completed) {

    public MissionProgress {
        Objects.requireNonNull(mission, "mission");
        Objects.requireNonNull(start, "start");
    }

    public static MissionProgress fresh(String mission, LocalDate start) {
        return new MissionProgress(mission, start, 0, false);
    }
}
