package br.com.muxe.core.common.permission.infrastructure.postgres;

import br.com.muxe.core.common.permission.domain.Group;
import br.com.muxe.core.common.permission.domain.Medal;
import br.com.muxe.core.common.permission.domain.PermissionAction;
import br.com.muxe.core.common.permission.domain.PermissionData;
import br.com.muxe.core.common.permission.domain.PermissionNode;
import br.com.muxe.core.common.permission.domain.PermissionRepository;
import br.com.muxe.core.common.permission.domain.PermissionUser;
import br.com.muxe.core.common.permission.domain.Tag;
import br.com.muxe.core.common.permission.domain.Track;
import br.com.muxe.core.common.persistence.sql.Column;
import br.com.muxe.core.common.persistence.sql.Select;
import br.com.muxe.core.common.persistence.sql.Sql;
import br.com.muxe.core.common.persistence.sql.Table;
import br.com.muxe.core.common.persistence.sql.Write;
import br.com.muxe.core.common.persistence.sql.type.SqlType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class PostgresPermissionRepository implements PermissionRepository {

    private static final Table TAGS = Table.of("core", "perm_tags");
    private static final Column<String> TAG_ID = TAGS.column("id", SqlType.TEXT);
    private static final Column<String> TAG_NAME = TAGS.column("name", SqlType.TEXT);
    private static final Column<String> TAG_COLOR = TAGS.column("color", SqlType.TEXT);
    private static final Column<Integer> TAG_WEIGHT = TAGS.column("weight", SqlType.INTEGER);
    private static final Column<Optional<String>> TAG_PREFIX = TAGS.column("prefix", SqlType.TEXT.optional());
    private static final Column<Boolean> TAG_DECORATIVE = TAGS.column("decorative", SqlType.BOOLEAN);

    private static final Table GROUPS = Table.of("core", "perm_groups");
    private static final Column<String> GROUP_ID = GROUPS.column("id", SqlType.TEXT);
    private static final Column<String> GROUP_DISPLAY_NAME = GROUPS.column("display_name", SqlType.TEXT);
    private static final Column<Integer> GROUP_WEIGHT = GROUPS.column("weight", SqlType.INTEGER);
    private static final Column<Optional<String>> GROUP_TAG = GROUPS.column("tag_id", SqlType.TEXT.optional());

    private static final Table GROUP_NODES = Table.of("core", "perm_group_nodes");
    private static final Column<String> NODE_GROUP = GROUP_NODES.column("group_id", SqlType.TEXT);
    private static final NodeColumns GROUP_NODE = NodeColumns.of(GROUP_NODES);

    private static final Table USER_NODES = Table.of("core", "perm_user_nodes");
    private static final Column<UUID> NODE_PLAYER = USER_NODES.column("player_id", SqlType.UUID);
    private static final NodeColumns USER_NODE = NodeColumns.of(USER_NODES);

    private static final Table TRACKS = Table.of("core", "perm_tracks");
    private static final Column<String> TRACK_ID = TRACKS.column("id", SqlType.TEXT);
    private static final Column<List<String>> TRACK_GROUPS = TRACKS.column("groups", SqlType.TEXT_ARRAY);

    private static final Table PLAYER_TAGS = Table.of("core", "player_tags");
    private static final Column<UUID> TAG_PLAYER = PLAYER_TAGS.column("player_id", SqlType.UUID);
    private static final Column<String> PLAYER_TAG = PLAYER_TAGS.column("tag_id", SqlType.TEXT);
    private static final Column<Instant> TAG_UPDATED_AT = PLAYER_TAGS.column("updated_at", SqlType.TIMESTAMPTZ);

    private static final Table MEDALS = Table.of("core", "perm_medals");
    private static final Column<String> MEDAL_ID = MEDALS.column("id", SqlType.TEXT);
    private static final Column<String> MEDAL_NAME = MEDALS.column("name", SqlType.TEXT);
    private static final Column<String> MEDAL_SYMBOL = MEDALS.column("symbol", SqlType.TEXT);
    private static final Column<String> MEDAL_COLOR = MEDALS.column("color", SqlType.TEXT);
    private static final Column<Integer> MEDAL_WEIGHT = MEDALS.column("weight", SqlType.INTEGER);

    private static final Table PLAYER_MEDALS = Table.of("core", "player_medals");
    private static final Column<UUID> MEDAL_PLAYER = PLAYER_MEDALS.column("player_id", SqlType.UUID);
    private static final Column<String> PLAYER_MEDAL = PLAYER_MEDALS.column("medal_id", SqlType.TEXT);
    private static final Column<Instant> MEDAL_UPDATED_AT = PLAYER_MEDALS.column("updated_at", SqlType.TIMESTAMPTZ);

    private static final Table ACTIONS = Table.of("core", "perm_actions");
    private static final Column<Long> ACTION_ID = ACTIONS.column("id", SqlType.BIGINT);
    private static final Column<Instant> ACTION_AT = ACTIONS.column("at", SqlType.TIMESTAMPTZ);
    private static final Column<String> ACTION_ACTOR = ACTIONS.column("actor", SqlType.TEXT);
    private static final Column<String> ACTION_DESCRIPTION = ACTIONS.column("description", SqlType.TEXT);

    private static final Column<Integer> LIMIT = Sql.parameter("limit", SqlType.INTEGER);

    private static final Select<Tag> FIND_TAGS = Sql.select(TAG_ID, TAG_NAME, TAG_COLOR, TAG_WEIGHT, TAG_PREFIX, TAG_DECORATIVE).from(TAGS)
        .map(row -> new Tag(row.get(TAG_ID), row.get(TAG_NAME), row.get(TAG_COLOR), row.get(TAG_WEIGHT), row.get(TAG_PREFIX), row.get(TAG_DECORATIVE)))
        .named("permission.find_tags");

    private static final Select<GroupRow> FIND_GROUPS = Sql.select(GROUP_ID, GROUP_DISPLAY_NAME, GROUP_WEIGHT, GROUP_TAG).from(GROUPS)
        .map(row -> new GroupRow(row.get(GROUP_ID), row.get(GROUP_DISPLAY_NAME), row.get(GROUP_WEIGHT), row.get(GROUP_TAG)))
        .named("permission.find_groups");

    private static final Select<GroupNode> FIND_GROUP_NODES = Sql.select(withNode(NODE_GROUP, GROUP_NODE)).from(GROUP_NODES)
        .map(row -> new GroupNode(row.get(NODE_GROUP), GROUP_NODE.read(row)))
        .named("permission.find_group_nodes");

    private static final Select<Track> FIND_TRACKS = Sql.select(TRACK_ID, TRACK_GROUPS).from(TRACKS)
        .map(row -> new Track(row.get(TRACK_ID), row.get(TRACK_GROUPS)))
        .named("permission.find_tracks");

    private static final Select<Medal> FIND_MEDALS = Sql.select(MEDAL_ID, MEDAL_NAME, MEDAL_SYMBOL, MEDAL_COLOR, MEDAL_WEIGHT).from(MEDALS)
        .map(row -> new Medal(row.get(MEDAL_ID), row.get(MEDAL_NAME), row.get(MEDAL_SYMBOL), row.get(MEDAL_COLOR), row.get(MEDAL_WEIGHT)))
        .named("permission.find_medals");

    private static final Select<PermissionNode> FIND_USER_NODES = Sql.select(USER_NODE.all()).from(USER_NODES)
        .where(NODE_PLAYER.eq())
        .map(USER_NODE::read)
        .named("permission.find_user_nodes");

    private static final Select<String> FIND_USER_TAG = Sql.select(PLAYER_TAG).from(PLAYER_TAGS)
        .where(TAG_PLAYER.eq())
        .map(row -> row.get(PLAYER_TAG))
        .named("permission.find_user_tag");

    private static final Select<String> FIND_USER_MEDAL = Sql.select(PLAYER_MEDAL).from(PLAYER_MEDALS)
        .where(MEDAL_PLAYER.eq())
        .map(row -> row.get(PLAYER_MEDAL))
        .named("permission.find_user_medal");

    private static final Select<UUID> FIND_USERS_WITH = Sql.selectDistinct(NODE_PLAYER).from(USER_NODES)
        .where(USER_NODE.permission().eq())
        .orderBy(NODE_PLAYER.asc())
        .map(row -> row.get(NODE_PLAYER))
        .named("permission.find_users_with");

    private static final Select<PermissionAction> RECENT = Sql.select(ACTION_AT, ACTION_ACTOR, ACTION_DESCRIPTION).from(ACTIONS)
        .orderBy(ACTION_ID.desc())
        .limit(LIMIT)
        .map(row -> new PermissionAction(row.get(ACTION_AT), row.get(ACTION_ACTOR), row.get(ACTION_DESCRIPTION)))
        .named("permission.recent_actions");

    private static final Write<Tag> SAVE_TAG = Sql.<Tag>insertInto(TAGS)
        .value(TAG_ID, Tag::id)
        .value(TAG_NAME, Tag::name)
        .value(TAG_COLOR, Tag::color)
        .value(TAG_WEIGHT, Tag::weight)
        .value(TAG_PREFIX, Tag::prefix)
        .value(TAG_DECORATIVE, Tag::decorative)
        .onConflict(TAG_ID).doUpdate(TAG_NAME, TAG_COLOR, TAG_WEIGHT, TAG_PREFIX, TAG_DECORATIVE)
        .named("permission.save_tag");

    private static final Write<Group> SAVE_GROUP = Sql.<Group>insertInto(GROUPS)
        .value(GROUP_ID, Group::id)
        .value(GROUP_DISPLAY_NAME, Group::displayName)
        .value(GROUP_WEIGHT, Group::weight)
        .value(GROUP_TAG, Group::tag)
        .onConflict(GROUP_ID).doUpdate(GROUP_DISPLAY_NAME, GROUP_WEIGHT, GROUP_TAG)
        .named("permission.save_group");

    private static final Write<GroupNode> SAVE_GROUP_NODE = GROUP_NODE.values(Sql.<GroupNode>insertInto(GROUP_NODES).value(NODE_GROUP, GroupNode::group), GroupNode::node)
        .named("permission.save_group_nodes");

    private static final Write<UserNode> SAVE_USER_NODE = USER_NODE.values(Sql.<UserNode>insertInto(USER_NODES).value(NODE_PLAYER, UserNode::player), UserNode::node)
        .named("permission.save_user_nodes");

    private static final Write<Track> SAVE_TRACK = Sql.<Track>insertInto(TRACKS)
        .value(TRACK_ID, Track::id)
        .value(TRACK_GROUPS, Track::groups)
        .onConflict(TRACK_ID).doUpdate(TRACK_GROUPS)
        .named("permission.save_track");

    private static final Write<Medal> SAVE_MEDAL = Sql.<Medal>insertInto(MEDALS)
        .value(MEDAL_ID, Medal::id)
        .value(MEDAL_NAME, Medal::name)
        .value(MEDAL_SYMBOL, Medal::symbol)
        .value(MEDAL_COLOR, Medal::color)
        .value(MEDAL_WEIGHT, Medal::weight)
        .onConflict(MEDAL_ID).doUpdate(MEDAL_NAME, MEDAL_SYMBOL, MEDAL_COLOR, MEDAL_WEIGHT)
        .named("permission.save_medal");

    private static final Write<Choice> SELECT_TAG = Sql.<Choice>insertInto(PLAYER_TAGS)
        .value(TAG_PLAYER, Choice::player)
        .value(PLAYER_TAG, Choice::id)
        .valueSql(TAG_UPDATED_AT, "now()")
        .onConflict(TAG_PLAYER).doUpdate(PLAYER_TAG, TAG_UPDATED_AT)
        .named("permission.select_tag");

    private static final Write<Choice> SELECT_MEDAL = Sql.<Choice>insertInto(PLAYER_MEDALS)
        .value(MEDAL_PLAYER, Choice::player)
        .value(PLAYER_MEDAL, Choice::id)
        .valueSql(MEDAL_UPDATED_AT, "now()")
        .onConflict(MEDAL_PLAYER).doUpdate(PLAYER_MEDAL, MEDAL_UPDATED_AT)
        .named("permission.select_medal");

    private static final Write<PermissionAction> LOG = Sql.<PermissionAction>insertInto(ACTIONS)
        .value(ACTION_AT, PermissionAction::at)
        .value(ACTION_ACTOR, PermissionAction::actor)
        .value(ACTION_DESCRIPTION, PermissionAction::description)
        .named("permission.log");

    private static final Write<Void> DELETE_GROUP = Sql.deleteFrom(GROUPS).where(GROUP_ID.eq()).named("permission.delete_group");
    private static final Write<Void> DELETE_GROUP_NODES = Sql.deleteFrom(GROUP_NODES).where(NODE_GROUP.eq()).named("permission.delete_group_nodes");
    private static final Write<Void> DELETE_TAG = Sql.deleteFrom(TAGS).where(TAG_ID.eq()).named("permission.delete_tag");
    private static final Write<Void> DELETE_TRACK = Sql.deleteFrom(TRACKS).where(TRACK_ID.eq()).named("permission.delete_track");
    private static final Write<Void> DELETE_MEDAL = Sql.deleteFrom(MEDALS).where(MEDAL_ID.eq()).named("permission.delete_medal");
    private static final Write<Void> DELETE_USER_NODES = Sql.deleteFrom(USER_NODES).where(NODE_PLAYER.in()).named("permission.delete_user_nodes");
    private static final Write<Void> CLEAR_TAGS = Sql.deleteFrom(PLAYER_TAGS).where(TAG_PLAYER.in()).named("permission.clear_tags");
    private static final Write<Void> CLEAR_MEDALS = Sql.deleteFrom(PLAYER_MEDALS).where(MEDAL_PLAYER.in()).named("permission.clear_medals");

    @Override
    public PermissionData load() {
        List<Tag> tags = FIND_TAGS.list();
        Map<String, List<PermissionNode>> nodes = new HashMap<>();
        for (GroupNode node : FIND_GROUP_NODES.list()) {
            nodes.computeIfAbsent(node.group(), ignored -> new ArrayList<>()).add(node.node());
        }
        List<Group> groups = new ArrayList<>();
        for (GroupRow group : FIND_GROUPS.list()) {
            groups.add(new Group(group.id(), group.displayName(), group.weight(), group.tag(), nodes.getOrDefault(group.id(), List.of())));
        }
        return PermissionData.of(groups, tags, FIND_TRACKS.list(), FIND_MEDALS.list());
    }

    @Override
    public PermissionUser user(UUID id) {
        List<PermissionNode> nodes = FIND_USER_NODES.list(NODE_PLAYER.is(id));
        Optional<String> tag = FIND_USER_TAG.first(TAG_PLAYER.is(id));
        Optional<String> medal = FIND_USER_MEDAL.first(MEDAL_PLAYER.is(id));
        return new PermissionUser(id, nodes, tag, medal);
    }

    @Override
    public void saveGroup(Group group) {
        SAVE_GROUP.execute(group);
        DELETE_GROUP_NODES.execute(NODE_GROUP.is(group.id()));
        SAVE_GROUP_NODE.batch(group.nodes().stream().map(node -> new GroupNode(group.id(), node)).toList());
    }

    @Override
    public void deleteGroup(String id) {
        DELETE_GROUP.execute(GROUP_ID.is(id));
    }

    @Override
    public void saveTag(Tag tag) {
        SAVE_TAG.execute(tag);
    }

    @Override
    public void deleteTag(String id) {
        DELETE_TAG.execute(TAG_ID.is(id));
    }

    @Override
    public void saveTrack(Track track) {
        SAVE_TRACK.execute(track);
    }

    @Override
    public void deleteTrack(String id) {
        DELETE_TRACK.execute(TRACK_ID.is(id));
    }

    @Override
    public void saveUser(PermissionUser user) {
        saveUsers(List.of(user));
    }

    @Override
    public void saveUsers(Collection<PermissionUser> users) {
        Map<UUID, PermissionUser> latest = new LinkedHashMap<>();
        users.forEach(user -> latest.put(user.id(), user));
        if (latest.isEmpty()) {
            return;
        }
        DELETE_USER_NODES.execute(NODE_PLAYER.in(latest.keySet()));
        SAVE_USER_NODE.batch(latest.values().stream().flatMap(user -> user.nodes().stream().map(node -> new UserNode(user.id(), node))).toList());
        choose(latest.values().stream().map(user -> new Choice(user.id(), user.selectedTag())).toList(), SELECT_TAG, CLEAR_TAGS, TAG_PLAYER);
        choose(latest.values().stream().map(user -> new Choice(user.id(), user.selectedMedal())).toList(), SELECT_MEDAL, CLEAR_MEDALS, MEDAL_PLAYER);
    }

    @Override
    public void selectTag(UUID id, Optional<String> tag) {
        choose(List.of(new Choice(id, tag)), SELECT_TAG, CLEAR_TAGS, TAG_PLAYER);
    }

    @Override
    public void saveMedal(Medal medal) {
        SAVE_MEDAL.execute(medal);
    }

    @Override
    public void deleteMedal(String id) {
        DELETE_MEDAL.execute(MEDAL_ID.is(id));
    }

    @Override
    public void selectMedal(UUID id, Optional<String> medal) {
        choose(List.of(new Choice(id, medal)), SELECT_MEDAL, CLEAR_MEDALS, MEDAL_PLAYER);
    }

    @Override
    public List<UUID> usersWith(String key) {
        return FIND_USERS_WITH.list(USER_NODE.permission().is(key));
    }

    @Override
    public void log(PermissionAction action) {
        LOG.execute(action);
    }

    @Override
    public void logAll(List<PermissionAction> actions) {
        LOG.batch(actions);
    }

    @Override
    public List<PermissionAction> recentActions(int limit) {
        return RECENT.list(LIMIT.is(limit));
    }

    private static void choose(List<Choice> choices, Write<Choice> select, Write<Void> clear, Column<UUID> player) {
        List<UUID> cleared = choices.stream().filter(choice -> choice.chosen().isEmpty()).map(Choice::player).toList();
        if (!cleared.isEmpty()) {
            clear.execute(player.in(cleared));
        }
        select.batch(choices.stream().filter(choice -> choice.chosen().isPresent()).toList());
    }

    private static List<Column<?>> withNode(Column<?> owner, NodeColumns node) {
        List<Column<?>> columns = new ArrayList<>(List.of(owner));
        columns.addAll(node.all());
        return columns;
    }

    private record GroupRow(String id, String displayName, int weight, Optional<String> tag) {
    }

    private record GroupNode(String group, PermissionNode node) {
    }

    private record UserNode(UUID player, PermissionNode node) {
    }

    private record Choice(UUID player, Optional<String> chosen) {

        String id() {
            return chosen.orElseThrow();
        }
    }
}
