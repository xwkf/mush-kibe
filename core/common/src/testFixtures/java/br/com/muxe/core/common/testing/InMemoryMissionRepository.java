package br.com.muxe.core.common.testing;

import br.com.muxe.core.common.mission.domain.MissionProgress;
import br.com.muxe.core.common.mission.domain.MissionRepository;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class InMemoryMissionRepository implements MissionRepository {

    private final Map<Key, MissionProgress> rows = new HashMap<>();

    @Override
    public Map<String, MissionProgress> find(String game, UUID playerId, Map<String, LocalDate> missions) {
        Map<String, MissionProgress> found = new HashMap<>();
        missions.forEach((mission, start) -> {
            MissionProgress progress = rows.get(new Key(game, playerId, mission, start));
            if (progress != null) {
                found.put(mission, progress);
            }
        });
        return found;
    }

    @Override
    public void save(String game, UUID playerId, MissionProgress progress) {
        rows.put(new Key(game, playerId, progress.mission(), progress.start()), progress);
    }

    public int size() {
        return rows.size();
    }

    private record Key(String game, UUID playerId, String mission, LocalDate start) {
    }
}
