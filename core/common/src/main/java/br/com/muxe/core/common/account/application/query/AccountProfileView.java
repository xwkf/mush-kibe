package br.com.muxe.core.common.account.application.query;

import br.com.muxe.core.common.account.domain.Account;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record AccountProfileView(UUID id, Optional<String> username, Instant firstSeenAt) {

    public AccountProfileView {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(firstSeenAt, "firstSeenAt");
    }

    static AccountProfileView of(Account account) {
        return new AccountProfileView(account.id(), account.currentUsername(), account.firstSeenAt());
    }
}
