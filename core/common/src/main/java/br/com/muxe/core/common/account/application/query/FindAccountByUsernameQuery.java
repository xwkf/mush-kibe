package br.com.muxe.core.common.account.application.query;

import br.com.muxe.core.common.cqrs.query.Query;
import java.util.Objects;
import java.util.Optional;

public record FindAccountByUsernameQuery(String username) implements Query<Optional<AccountIdentityView>> {

    public FindAccountByUsernameQuery {
        Objects.requireNonNull(username, "username");
    }
}
