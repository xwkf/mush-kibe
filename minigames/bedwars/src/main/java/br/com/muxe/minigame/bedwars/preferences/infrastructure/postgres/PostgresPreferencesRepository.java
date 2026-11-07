package br.com.muxe.minigame.bedwars.preferences.infrastructure.postgres;

import br.com.muxe.core.common.persistence.sql.Column;
import br.com.muxe.core.common.persistence.sql.Select;
import br.com.muxe.core.common.persistence.sql.Sql;
import br.com.muxe.core.common.persistence.sql.Table;
import br.com.muxe.core.common.persistence.sql.Write;
import br.com.muxe.core.common.persistence.sql.type.SqlType;
import br.com.muxe.minigame.bedwars.preferences.domain.Preferences;
import br.com.muxe.minigame.bedwars.preferences.domain.Preferences.ChestDeposit;
import br.com.muxe.minigame.bedwars.preferences.domain.PreferencesRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public final class PostgresPreferencesRepository implements PreferencesRepository {

    private static final Table PREFERENCES = Table.of("bedwars", "preferences");
    private static final Column<UUID> PLAYER_ID = PREFERENCES.column("player_id", SqlType.UUID);
    private static final Column<Boolean> SHOW_LEVEL = PREFERENCES.column("show_level", SqlType.BOOLEAN);
    private static final Column<Boolean> TNT_TIMER = PREFERENCES.column("tnt_timer", SqlType.BOOLEAN);
    private static final Column<ChestDeposit> CHEST_DEPOSIT = PREFERENCES.column("chest_deposit", SqlType.enumByName(ChestDeposit.class));
    private static final Column<Instant> UPDATED_AT = PREFERENCES.column("updated_at", SqlType.TIMESTAMPTZ);

    private static final Select<Preferences> FIND = Sql.select(SHOW_LEVEL, TNT_TIMER, CHEST_DEPOSIT).from(PREFERENCES)
        .where(PLAYER_ID.eq())
        .map(row -> new Preferences(row.get(SHOW_LEVEL), row.get(TNT_TIMER), row.get(CHEST_DEPOSIT)))
        .named("bedwars.preferences.find");

    private static final Write<Void> SAVE = Sql.<Void>insertInto(PREFERENCES)
        .value(PLAYER_ID)
        .value(SHOW_LEVEL)
        .value(TNT_TIMER)
        .value(CHEST_DEPOSIT)
        .value(UPDATED_AT)
        .onConflict(PLAYER_ID).doUpdate(SHOW_LEVEL, TNT_TIMER, CHEST_DEPOSIT, UPDATED_AT)
        .named("bedwars.preferences.save");

    @Override
    public Optional<Preferences> find(UUID playerId) {
        return FIND.first(PLAYER_ID.is(playerId));
    }

    @Override
    public void save(UUID playerId, Preferences preferences, Instant at) {
        SAVE.execute(PLAYER_ID.is(playerId), SHOW_LEVEL.is(preferences.showLevel()), TNT_TIMER.is(preferences.tntTimer()),
            CHEST_DEPOSIT.is(preferences.chestDeposit()), UPDATED_AT.is(at));
    }
}
