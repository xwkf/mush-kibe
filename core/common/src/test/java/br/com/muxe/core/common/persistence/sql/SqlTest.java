package br.com.muxe.core.common.persistence.sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.muxe.core.common.persistence.sql.type.SqlType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SqlTest {

    private static final Table PLAYERS = Table.of("core", "players");
    private static final Column<UUID> ID = PLAYERS.column("id", SqlType.UUID);
    private static final Column<String> NAME = PLAYERS.column("name", SqlType.TEXT);
    private static final Column<Long> COINS = PLAYERS.column("coins", SqlType.BIGINT);
    private static final Column<Optional<Instant>> BANNED_AT = PLAYERS.column("banned_at", SqlType.TIMESTAMPTZ.optional());

    private static final Table HOMES = Table.of("core", "homes");
    private static final Column<UUID> OWNER = HOMES.column("owner", SqlType.UUID);
    private static final Column<String> WORLD = HOMES.column("world", SqlType.TEXT);

    private static final Column<Integer> LIMIT = Sql.parameter("limit", SqlType.INTEGER);

    @Test
    void aSelectListsItsColumnsConditionsOrderAndLimit() {
        Select<String> select = Sql.select(ID, NAME).from(PLAYERS)
            .where(NAME.eqIgnoreCase())
            .where(BANNED_AT.isNull())
            .orderBy(COINS.desc(), ID.asc())
            .limit(LIMIT)
            .map(row -> row.get(NAME))
            .named("test.find");

        assertThat(select.name()).isEqualTo("test.find");
        assertThat(select.sql()).isEqualTo("SELECT id, name FROM core.players WHERE LOWER(name) = LOWER(?) AND banned_at IS NULL ORDER BY coins DESC, id LIMIT ?");
    }

    @Test
    void aSelectWithAJoinQualifiesEveryColumn() {
        Select<String> select = Sql.selectDistinct(NAME, WORLD).from(PLAYERS)
            .leftJoin(HOMES, OWNER.eq(ID))
            .where(ID.in())
            .map(row -> row.get(NAME))
            .named("test.homes");

        assertThat(select.sql()).isEqualTo(
            "SELECT DISTINCT players.name, homes.world FROM core.players LEFT JOIN core.homes ON homes.owner = players.id WHERE players.id = ANY(?)");
    }

    @Test
    void anUpsertUpdatesOnlyTheChosenColumns() {
        Write<Object> upsert = Sql.insertInto(PLAYERS)
            .value(ID)
            .value(NAME)
            .valueSql(COINS, "0")
            .onConflict(ID).doUpdate(NAME)
            .named("test.save");

        assertThat(upsert.sql()).isEqualTo("INSERT INTO core.players (id, name, coins) VALUES (?, ?, 0) ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name");
    }

    @Test
    void anUpsertCanAddToTheStoredValueUnderAConditionWithAnAlias() {
        Write<Object> upsert = Sql.insertInto(PLAYERS)
            .as("p")
            .value(ID)
            .value(COINS)
            .onConflict(ID).doUpdate(COINS, "p.coins + EXCLUDED.coins")
            .where("p.banned_at IS NULL")
            .named("test.add");

        assertThat(upsert.sql()).isEqualTo(
            "INSERT INTO core.players AS p (id, coins) VALUES (?, ?) ON CONFLICT (id) DO UPDATE SET coins = p.coins + EXCLUDED.coins WHERE p.banned_at IS NULL");
    }

    @Test
    void anInsertCanIgnoreConflicts() {
        Write<Object> insert = Sql.insertInto(HOMES).value(OWNER).value(WORLD).onConflict().doNothing().named("test.home");

        assertThat(insert.sql()).isEqualTo("INSERT INTO core.homes (owner, world) VALUES (?, ?) ON CONFLICT DO NOTHING");
    }

    @Test
    void anUpdateSetsValuesFromTheEntityAndFiltersByItsKey() {
        Write<Player> update = Sql.<Player>update(PLAYERS)
            .set(NAME, Player::name)
            .setSql(COINS, "coins + 1")
            .where(ID, Player::id)
            .named("test.rename");

        assertThat(update.sql()).isEqualTo("UPDATE core.players SET name = ?, coins = coins + 1 WHERE id = ?");
    }

    @Test
    void aDeleteNeedsACondition() {
        assertThat(Sql.deleteFrom(HOMES).where(OWNER.eq().and(WORLD.eq())).named("test.delete").sql())
            .isEqualTo("DELETE FROM core.homes WHERE owner = ? AND world = ?");
        assertThatThrownBy(() -> Sql.deleteFrom(HOMES).named("test.delete_all")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void handWrittenConditionsAndQueriesMustMatchTheirParameters() {
        assertThat(Sql.select(ID).from(PLAYERS).where(Condition.sql("coins >= ? AND coins < ?", COINS, COINS)).map(row -> row.get(ID)).named("test.range").sql())
            .isEqualTo("SELECT id FROM core.players WHERE (coins >= ? AND coins < ?)");
        assertThatThrownBy(() -> Condition.sql("coins >= ?", COINS, COINS)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Sql.query("SELECT 1 WHERE ? = ?").parameters(ID).map(row -> 1).named("test.query"))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Sql.statement("SELECT pg_sleep(?)").named("test.sleep")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void statementsAreCheckedWhenTheyAreDeclared() {
        assertThatThrownBy(() -> Sql.select(ID).from(PLAYERS).map(row -> 1).named(" ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Sql.select(ID).map(row -> 1).named("test.no_table")).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> Sql.select(ID).from(PLAYERS).named("test.no_mapper")).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> Sql.insertInto(PLAYERS).value(WORLD)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Sql.insertInto(PLAYERS).value(ID).value(ID)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Sql.insertInto(PLAYERS).value(ID).onConflict(ID).named("test.no_action")).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> Sql.insertInto(PLAYERS).value(ID).doNothing()).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> Sql.update(PLAYERS).set(NAME).named("test.update_all")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void tablesAndColumnsUseSafeIdentifiers() {
        assertThatThrownBy(() -> Table.of("core", "players; DROP TABLE x")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PLAYERS.column("Name", SqlType.TEXT)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PLAYERS.column("name", SqlType.TEXT)).isInstanceOf(IllegalArgumentException.class);
        assertThat(PLAYERS.columns()).containsExactly(ID, NAME, COINS, BANNED_AT);
        assertThat(PLAYERS.qualifiedName()).isEqualTo("core.players");
        assertThat(LIMIT.table()).isEmpty();
    }

    @Test
    void anArgumentRemembersItsColumnAndValue() {
        UUID id = UUID.randomUUID();

        assertThat(ID.is(id).value()).isEqualTo(id);
        assertThat(ID.is(id).many()).isFalse();
        assertThat(ID.in(List.of(id)).value()).isEqualTo(List.of(id));
        assertThat(ID.in(List.of(id)).many()).isTrue();
    }

    private record Player(UUID id, String name) {
    }
}
