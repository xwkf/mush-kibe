package br.com.muxe.core.common.testing;

import br.com.muxe.core.common.account.domain.Account;
import br.com.muxe.core.common.account.domain.AccountRepository;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryAccountRepository implements AccountRepository {

    private final Map<UUID, Account> accounts = new ConcurrentHashMap<>();

    @Override
    public Optional<Account> findById(UUID id) {
        return Optional.ofNullable(accounts.get(id));
    }

    @Override
    public List<Account> findByIds(Collection<UUID> ids) {
        return ids.stream().distinct().map(accounts::get).filter(Objects::nonNull).toList();
    }

    @Override
    public Optional<Account> findByUsername(String username) {
        String lower = username.toLowerCase(Locale.ROOT);
        return accounts.values().stream()
            .filter(account -> account.username() != null && account.username().toLowerCase(Locale.ROOT).equals(lower))
            .findFirst();
    }

    @Override
    public void save(Account account) {
        accounts.put(account.id(), account);
    }

    public int size() {
        return accounts.size();
    }
}
