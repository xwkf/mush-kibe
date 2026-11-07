package br.com.muxe.minigame.bedwars.teamcolor.infrastructure.postgres;

import br.com.muxe.core.common.persistence.sql.Column;
import br.com.muxe.core.common.persistence.sql.Select;
import br.com.muxe.core.common.persistence.sql.Sql;
import br.com.muxe.core.common.persistence.sql.Table;
import br.com.muxe.core.common.persistence.sql.Write;
import br.com.muxe.core.common.persistence.sql.type.SqlType;
import br.com.muxe.minigame.bedwars.teamcolor.domain.FavouriteColorRepository;
import br.com.muxe.minigame.engine.team.TeamColor;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public final class PostgresFavouriteColorRepository implements FavouriteColorRepository {

    private static final Table FAVOURITE = Table.of("bedwars", "favourite_team_color");
    private static final Column<UUID> PLAYER_ID = FAVOURITE.column("player_id", SqlType.UUID);
    private static final Column<String> TEAM_COLOR = FAVOURITE.column("team_color", SqlType.TEXT);
    private static final Column<Instant> UPDATED_AT = FAVOURITE.column("updated_at", SqlType.TIMESTAMPTZ);

    private static final Select<String> FIND = Sql.select(TEAM_COLOR).from(FAVOURITE)
        .where(PLAYER_ID.eq())
        .map(row -> row.get(TEAM_COLOR))
        .named("bedwars.team-color.find");

    private static final Write<Void> SAVE = Sql.<Void>insertInto(FAVOURITE)
        .value(PLAYER_ID)
        .value(TEAM_COLOR)
        .value(UPDATED_AT)
        .onConflict(PLAYER_ID).doUpdate(TEAM_COLOR, UPDATED_AT)
        .named("bedwars.team-color.save");

    private static final Write<Void> CLEAR = Sql.deleteFrom(FAVOURITE)
        .where(PLAYER_ID.eq())
        .named("bedwars.team-color.clear");

    @Override
    public Optional<TeamColor> find(UUID playerId) {
        return FIND.first(PLAYER_ID.is(playerId)).flatMap(TeamColor::find);
    }

    @Override
    public void save(UUID playerId, TeamColor color, Instant at) {
        SAVE.execute(PLAYER_ID.is(playerId), TEAM_COLOR.is(color.id()), UPDATED_AT.is(at));
    }

    @Override
    public void clear(UUID playerId) {
        CLEAR.execute(PLAYER_ID.is(playerId));
    }
}
