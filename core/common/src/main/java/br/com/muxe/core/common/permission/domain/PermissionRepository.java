package br.com.muxe.core.common.permission.domain;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository {

    PermissionData load();

    PermissionUser user(UUID id);

    void saveGroup(Group group);

    void deleteGroup(String id);

    void saveTag(Tag tag);

    void deleteTag(String id);

    void saveTrack(Track track);

    void deleteTrack(String id);

    void saveUser(PermissionUser user);

    void saveUsers(Collection<PermissionUser> users);

    void selectTag(UUID id, Optional<String> tag);

    void saveMedal(Medal medal);

    void deleteMedal(String id);

    void selectMedal(UUID id, Optional<String> medal);

    List<UUID> usersWith(String key);

    void log(PermissionAction action);

    void logAll(List<PermissionAction> actions);

    List<PermissionAction> recentActions(int limit);
}
