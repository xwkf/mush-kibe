package br.com.muxe.core.common.account.application.query;

import br.com.muxe.core.common.account.domain.Account;
import java.util.Objects;
import java.util.UUID;

public record AccountIdentityView(UUID id, String username, boolean premium) {

    public AccountIdentityView {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(username, "username");
    }

    static AccountIdentityView of(Account account, String searchedName) {
        return new AccountIdentityView(account.id(), account.currentUsername().orElse(searchedName), account.premium());
    }
}
