package br.com.muxe.core.common.mission.domain;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public interface MissionRepository {

    Map<String, MissionProgress> find(String game, UUID playerId, Map<String, LocalDate> missions);

    void save(String game, UUID playerId, MissionProgress progress);
}
