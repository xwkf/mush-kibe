package br.com.muxe.core.common.mission.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@FunctionalInterface
public interface MissionRewards {

    MissionRewards NONE = (playerId, completed, at) -> {
    };

    void grant(UUID playerId, List<Mission> completed, Instant at);
}
