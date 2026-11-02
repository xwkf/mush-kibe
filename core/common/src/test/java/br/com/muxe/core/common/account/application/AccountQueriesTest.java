package br.com.muxe.core.common.account.application;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.common.account.application.query.AccountIdentityView;
import br.com.muxe.core.common.account.application.query.AccountProfileView;
import br.com.muxe.core.common.account.application.query.FindAccountByUsernameHandler;
import br.com.muxe.core.common.account.application.query.FindAccountByUsernameQuery;
import br.com.muxe.core.common.account.application.query.FindAccountHandler;
import br.com.muxe.core.common.account.application.query.FindAccountQuery;
import br.com.muxe.core.common.account.domain.Account;
import br.com.muxe.core.common.testing.DirectTransactions;
import br.com.muxe.core.common.testing.InMemoryAccountRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AccountQueriesTest {

    private final InMemoryAccountRepository accounts = new InMemoryAccountRepository();
    private final DirectTransactions transactions = new DirectTransactions();
    private final FindAccountHandler byId = new FindAccountHandler(accounts, transactions);
    private final FindAccountByUsernameHandler byName = new FindAccountByUsernameHandler(accounts, transactions);
    private final UUID player = UUID.randomUUID();
    private final Instant firstSeenAt = Instant.parse("2021-09-10T19:29:25Z");

    @Test
    void theProfileShowsTheNameAndTheFirstLogin() {
        accounts.save(Account.firstLogin(player, "Tadeu", true, firstSeenAt).withLogin("Tadeu", Instant.parse("2026-11-02T12:00:00Z")));

        assertThat(byId.handle(new FindAccountQuery(player))).contains(new AccountProfileView(player, Optional.of("Tadeu"), firstSeenAt));
        assertThat(byId.handle(new FindAccountQuery(UUID.randomUUID()))).isEmpty();
        assertThat(transactions.operations()).contains("account.find");
    }

    @Test
    void aNameResolvesToTheIdentityOfItsAccountIgnoringCase() {
        accounts.save(Account.firstLogin(player, "Tadeu", true, firstSeenAt));

        assertThat(byName.handle(new FindAccountByUsernameQuery("tadeu"))).contains(new AccountIdentityView(player, "Tadeu", true));
        assertThat(byName.handle(new FindAccountByUsernameQuery("Steve"))).isEmpty();
        assertThat(transactions.operations()).contains("account.find_by_username");
    }
}
