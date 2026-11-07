package br.com.muxe.core.common.placement.infrastructure.postgres;

import br.com.muxe.core.common.persistence.sql.Column;
import br.com.muxe.core.common.persistence.sql.Row;
import br.com.muxe.core.common.persistence.sql.RowMapper;
import br.com.muxe.core.common.persistence.sql.Select;
import br.com.muxe.core.common.persistence.sql.Sql;
import br.com.muxe.core.common.persistence.sql.Write;
import br.com.muxe.core.common.persistence.sql.type.SqlType;
import br.com.muxe.core.common.placement.domain.HeldItem;
import br.com.muxe.core.common.placement.domain.HologramPlacement;
import br.com.muxe.core.common.placement.domain.NpcAction;
import br.com.muxe.core.common.placement.domain.NpcPlacement;
import br.com.muxe.core.common.placement.domain.ParkourPoint;
import br.com.muxe.core.common.placement.domain.ParkourPointPlacement;
import br.com.muxe.core.common.placement.domain.Placement;
import br.com.muxe.core.common.placement.domain.PlacementKind;
import br.com.muxe.core.common.placement.domain.PlacementRepository;
import br.com.muxe.core.common.placement.domain.Placements;
import br.com.muxe.core.common.placement.domain.RegisterPlacement;
import br.com.muxe.core.common.placement.domain.SkinTexture;
import br.com.muxe.core.common.placement.domain.SocialHeadPlacement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class PostgresPlacementRepository implements PlacementRepository {

    private static final PlacementTable NPCS = PlacementTable.of("npc_placements");
    private static final Column<String> NPC_NAME = NPCS.table().column("name", SqlType.TEXT);
    private static final Column<NpcAction.Type> NPC_ACTION = NPCS.table().column("action", SqlType.enumByName(NpcAction.Type.class));
    private static final Column<String> NPC_ACTION_TARGET = NPCS.table().column("action_target", SqlType.TEXT);
    private static final Column<String> NPC_SKIN_NAME = NPCS.table().column("skin_name", SqlType.TEXT);
    private static final Column<String> NPC_SKIN_VALUE = NPCS.table().column("skin_value", SqlType.TEXT);
    private static final Column<String> NPC_SKIN_SIGNATURE = NPCS.table().column("skin_signature", SqlType.TEXT);
    private static final Column<List<String>> NPC_LINES = NPCS.table().column("lines", SqlType.TEXT_ARRAY);
    private static final Column<String> NPC_HAND_MATERIAL = NPCS.table().column("hand_material", SqlType.TEXT);
    private static final Column<Integer> NPC_HAND_DATA = NPCS.table().column("hand_data", SqlType.SMALLINT);

    private static final PlacementTable HOLOGRAMS = PlacementTable.of("hologram_placements");
    private static final Column<List<String>> HOLOGRAM_LINES = HOLOGRAMS.table().column("lines", SqlType.TEXT_ARRAY);

    private static final PlacementTable REGISTERS = PlacementTable.of("register_placements");
    private static final PositionColumns SELLER = PositionColumns.of(REGISTERS.table(), "seller_");

    private static final PlacementTable SOCIAL_HEADS = PlacementTable.of("social_head_placements");

    private static final PlacementTable PARKOUR_POINTS = PlacementTable.of("parkour_placements");

    private static final Select<NpcPlacement> FIND_NPCS = find(NPCS,
        List.of(NPC_NAME, NPC_ACTION, NPC_ACTION_TARGET, NPC_SKIN_NAME, NPC_SKIN_VALUE, NPC_SKIN_SIGNATURE, NPC_LINES, NPC_HAND_MATERIAL, NPC_HAND_DATA),
        PostgresPlacementRepository::npc, "placement.find_npcs");

    private static final Select<HologramPlacement> FIND_HOLOGRAMS = find(HOLOGRAMS, List.of(HOLOGRAM_LINES),
        row -> new HologramPlacement(row.get(HOLOGRAMS.group()), row.get(HOLOGRAMS.id()), HOLOGRAMS.position().read(row), row.get(HOLOGRAM_LINES)),
        "placement.find_holograms");

    private static final Select<RegisterPlacement> FIND_REGISTERS = find(REGISTERS, SELLER.all(),
        row -> new RegisterPlacement(row.get(REGISTERS.group()), row.get(REGISTERS.id()), REGISTERS.position().read(row), SELLER.readOptional(row)),
        "placement.find_registers");

    private static final Select<SocialHeadPlacement> FIND_SOCIAL_HEADS = find(SOCIAL_HEADS, List.of(),
        row -> new SocialHeadPlacement(row.get(SOCIAL_HEADS.group()), row.get(SOCIAL_HEADS.id()), SOCIAL_HEADS.position().read(row)),
        "placement.find_social_heads");

    private static final Select<ParkourPointPlacement> FIND_PARKOUR_POINTS = find(PARKOUR_POINTS, List.of(),
        PostgresPlacementRepository::parkourPoint, "placement.find_parkour_points");

    private static final Write<NpcPlacement> SAVE_NPC = NPCS.<NpcPlacement>insert()
        .value(NPC_NAME, NpcPlacement::name)
        .value(NPC_ACTION, npc -> npc.action().type())
        .value(NPC_ACTION_TARGET, npc -> npc.action().target())
        .value(NPC_SKIN_NAME, NpcPlacement::skinName)
        .value(NPC_SKIN_VALUE, npc -> npc.skin().map(SkinTexture::value).orElse(null))
        .value(NPC_SKIN_SIGNATURE, npc -> npc.skin().map(SkinTexture::signature).orElse(null))
        .value(NPC_LINES, NpcPlacement::lines)
        .value(NPC_HAND_MATERIAL, npc -> npc.hand().map(HeldItem::material).orElse(null))
        .value(NPC_HAND_DATA, npc -> npc.hand().map(HeldItem::data).orElse(0))
        .onConflict(NPCS.group(), NPCS.id())
        .doUpdate(updated(NPCS, NPC_NAME, NPC_ACTION, NPC_ACTION_TARGET, NPC_SKIN_NAME, NPC_SKIN_VALUE, NPC_SKIN_SIGNATURE, NPC_LINES, NPC_HAND_MATERIAL, NPC_HAND_DATA))
        .named("placement.save_npc");

    private static final Write<HologramPlacement> SAVE_HOLOGRAM = HOLOGRAMS.<HologramPlacement>insert()
        .value(HOLOGRAM_LINES, HologramPlacement::lines)
        .onConflict(HOLOGRAMS.group(), HOLOGRAMS.id())
        .doUpdate(updated(HOLOGRAMS, HOLOGRAM_LINES))
        .named("placement.save_hologram");

    private static final Write<RegisterPlacement> SAVE_REGISTER = SELLER.values(REGISTERS.<RegisterPlacement>insert(), RegisterPlacement::seller)
        .onConflict(REGISTERS.group(), REGISTERS.id())
        .doUpdate(updated(REGISTERS, SELLER.all().toArray(Column<?>[]::new)))
        .named("placement.save_register");

    private static final Write<SocialHeadPlacement> SAVE_SOCIAL_HEAD = SOCIAL_HEADS.<SocialHeadPlacement>insert()
        .onConflict(SOCIAL_HEADS.group(), SOCIAL_HEADS.id())
        .doUpdate(updated(SOCIAL_HEADS))
        .named("placement.save_social_head");

    private static final Write<ParkourPointPlacement> SAVE_PARKOUR_POINT = PARKOUR_POINTS.<ParkourPointPlacement>insert()
        .onConflict(PARKOUR_POINTS.group(), PARKOUR_POINTS.id())
        .doUpdate(updated(PARKOUR_POINTS))
        .named("placement.save_parkour_point");

    private static final Map<PlacementKind, Write<Void>> REMOVE = removals();

    @Override
    public Placements findByGroup(String group) {
        return new Placements(
            FIND_NPCS.list(NPCS.group().is(group)),
            FIND_HOLOGRAMS.list(HOLOGRAMS.group().is(group)),
            FIND_REGISTERS.list(REGISTERS.group().is(group)),
            FIND_SOCIAL_HEADS.list(SOCIAL_HEADS.group().is(group)),
            FIND_PARKOUR_POINTS.list(PARKOUR_POINTS.group().is(group))
        );
    }

    @Override
    public void save(Placement placement) {
        switch (placement) {
            case NpcPlacement npc -> SAVE_NPC.execute(npc);
            case HologramPlacement hologram -> SAVE_HOLOGRAM.execute(hologram);
            case RegisterPlacement register -> SAVE_REGISTER.execute(register);
            case SocialHeadPlacement head -> SAVE_SOCIAL_HEAD.execute(head);
            case ParkourPointPlacement point -> SAVE_PARKOUR_POINT.execute(point);
        }
    }

    @Override
    public boolean remove(PlacementKind kind, String group, String id) {
        PlacementTable table = table(kind);
        return REMOVE.get(kind).execute(table.group().is(group), table.id().is(id)) > 0;
    }

    private static <R> Select<R> find(PlacementTable table, List<Column<?>> extra, RowMapper<R> mapper, String name) {
        List<Column<?>> columns = new ArrayList<>(List.of(table.group()));
        columns.addAll(table.keyAndPosition());
        columns.addAll(extra);
        return Sql.select(columns).from(table.table())
            .where(table.group().eq())
            .orderBy(table.id().asc())
            .map(mapper)
            .named(name);
    }

    private static Column<?>[] updated(PlacementTable table, Column<?>... extra) {
        List<Column<?>> columns = new ArrayList<>(table.position().all());
        columns.addAll(List.of(extra));
        return columns.toArray(Column<?>[]::new);
    }

    private static NpcPlacement npc(Row row) throws SQLException {
        String value = row.get(NPC_SKIN_VALUE);
        String signature = row.get(NPC_SKIN_SIGNATURE);
        Optional<SkinTexture> skin = value == null || signature == null ? Optional.empty() : Optional.of(new SkinTexture(value, signature));
        String handMaterial = row.get(NPC_HAND_MATERIAL);
        Optional<HeldItem> hand = handMaterial == null ? Optional.empty() : Optional.of(new HeldItem(handMaterial, row.get(NPC_HAND_DATA)));
        return new NpcPlacement(
            row.get(NPCS.group()),
            row.get(NPCS.id()),
            NPCS.position().read(row),
            row.get(NPC_NAME),
            new NpcAction(row.get(NPC_ACTION), row.get(NPC_ACTION_TARGET)),
            row.get(NPC_SKIN_NAME),
            skin,
            row.get(NPC_LINES),
            hand
        );
    }

    private static ParkourPointPlacement parkourPoint(Row row) throws SQLException {
        String id = row.get(PARKOUR_POINTS.id());
        ParkourPoint point = ParkourPoint.parse(id).orElseThrow(() -> new SQLException("Unknown parkour point id: " + id));
        return new ParkourPointPlacement(row.get(PARKOUR_POINTS.group()), point, PARKOUR_POINTS.position().read(row));
    }

    private static PlacementTable table(PlacementKind kind) {
        return switch (kind) {
            case NPC -> NPCS;
            case HOLOGRAM -> HOLOGRAMS;
            case REGISTER -> REGISTERS;
            case SOCIAL_HEAD -> SOCIAL_HEADS;
            case PARKOUR -> PARKOUR_POINTS;
        };
    }

    private static Map<PlacementKind, Write<Void>> removals() {
        Map<PlacementKind, Write<Void>> removals = new EnumMap<>(PlacementKind.class);
        for (PlacementKind kind : PlacementKind.values()) {
            removals.put(kind, table(kind).delete("placement.remove_" + kind.name().toLowerCase(Locale.ROOT)));
        }
        return Map.copyOf(removals);
    }
}
