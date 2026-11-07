package br.com.muxe.auth.credential.infrastructure.postgres;

import br.com.muxe.auth.credential.domain.Credential;
import br.com.muxe.auth.credential.domain.CredentialRepository;
import br.com.muxe.auth.credential.domain.PasswordHash;
import br.com.muxe.core.common.persistence.sql.Column;
import br.com.muxe.core.common.persistence.sql.Select;
import br.com.muxe.core.common.persistence.sql.Sql;
import br.com.muxe.core.common.persistence.sql.Table;
import br.com.muxe.core.common.persistence.sql.Write;
import br.com.muxe.core.common.persistence.sql.type.SqlType;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public final class PostgresCredentialRepository implements CredentialRepository {

    private static final Table CREDENTIALS = Table.of("auth", "credentials");
    private static final Column<UUID> PLAYER_ID = CREDENTIALS.column("player_id", SqlType.UUID);
    private static final Column<PasswordHash> PASSWORD_HASH = CREDENTIALS.column("password_hash", SqlType.TEXT.map(PasswordHash::parse, PasswordHash::encode));
    private static final Column<Instant> CREATED_AT = CREDENTIALS.column("created_at", SqlType.TIMESTAMPTZ);
    private static final Column<Instant> UPDATED_AT = CREDENTIALS.column("updated_at", SqlType.TIMESTAMPTZ);
    private static final Column<Instant> LAST_LOGIN_AT = CREDENTIALS.column("last_login_at", SqlType.TIMESTAMPTZ);

    private static final Select<Credential> FIND = Sql.select(PLAYER_ID, PASSWORD_HASH, CREATED_AT, UPDATED_AT, LAST_LOGIN_AT).from(CREDENTIALS)
        .where(PLAYER_ID.eq())
        .map(row -> new Credential(row.get(PLAYER_ID), row.get(PASSWORD_HASH), row.get(CREATED_AT), row.get(UPDATED_AT), row.get(LAST_LOGIN_AT)))
        .named("auth.find_credential");

    private static final Select<UUID> EXISTS = Sql.select(PLAYER_ID).from(CREDENTIALS)
        .where(PLAYER_ID.eq())
        .map(row -> row.get(PLAYER_ID))
        .named("auth.credential_exists");

    private static final Write<Credential> INSERT = Sql.<Credential>insertInto(CREDENTIALS)
        .value(PLAYER_ID, Credential::playerId)
        .value(PASSWORD_HASH, Credential::passwordHash)
        .value(CREATED_AT, Credential::createdAt)
        .value(UPDATED_AT, Credential::updatedAt)
        .value(LAST_LOGIN_AT, Credential::lastLoginAt)
        .named("auth.insert_credential");

    private static final Write<Credential> UPDATE = Sql.<Credential>update(CREDENTIALS)
        .set(PASSWORD_HASH, Credential::passwordHash)
        .set(UPDATED_AT, Credential::updatedAt)
        .set(LAST_LOGIN_AT, Credential::lastLoginAt)
        .where(PLAYER_ID, Credential::playerId)
        .named("auth.update_credential");

    @Override
    public Optional<Credential> find(UUID playerId) {
        return FIND.first(PLAYER_ID.is(playerId));
    }

    @Override
    public boolean exists(UUID playerId) {
        return EXISTS.exists(PLAYER_ID.is(playerId));
    }

    @Override
    public void insert(Credential credential) {
        INSERT.execute(credential);
    }

    @Override
    public void update(Credential credential) {
        UPDATE.execute(credential);
    }
}
