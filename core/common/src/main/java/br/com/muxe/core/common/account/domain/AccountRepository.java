package br.com.muxe.core.common.account.domain;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {

    Optional<Account> findById(UUID id);

    List<Account> findByIds(Collection<UUID> ids);

    Optional<Account> findByUsername(String username);

    void save(Account account);
}
