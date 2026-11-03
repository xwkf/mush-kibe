package br.com.muxe.minigame.bedwars.preferences;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.common.testing.DirectTransactions;
import br.com.muxe.core.common.testing.MutableClock;
import br.com.muxe.minigame.bedwars.preferences.application.command.SavePreferencesCommand;
import br.com.muxe.minigame.bedwars.preferences.application.command.SavePreferencesHandler;
import br.com.muxe.minigame.bedwars.preferences.application.query.FindPreferencesHandler;
import br.com.muxe.minigame.bedwars.preferences.application.query.FindPreferencesQuery;
import br.com.muxe.minigame.bedwars.preferences.domain.Preferences;
import br.com.muxe.minigame.bedwars.preferences.domain.Preferences.ChestDeposit;
import br.com.muxe.minigame.bedwars.preferences.domain.PreferencesRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PreferencesTest {

    private final Map<UUID, Preferences> saved = new HashMap<>();
    private final PreferencesRepository repository = new PreferencesRepository() {
        @Override
        public Optional<Preferences> find(UUID playerId) {
            return Optional.ofNullable(saved.get(playerId));
        }

        @Override
        public void save(UUID playerId, Preferences preferences, Instant at) {
            saved.put(playerId, preferences);
        }
    };
    private final SavePreferencesHandler save = new SavePreferencesHandler(repository, new DirectTransactions(), new MutableClock(Instant.parse("2026-11-03T03:00:00Z")));
    private final FindPreferencesHandler find = new FindPreferencesHandler(repository, new DirectTransactions());
    private final UUID player = new UUID(0, 1);

    @Test
    void everythingStartsOnWithHolograms() {
        assertThat(Preferences.DEFAULTS).isEqualTo(new Preferences(true, true, ChestDeposit.ENABLED));
    }

    @Test
    void togglesFlipOnlyTheirOwnValue() {
        Preferences changed = Preferences.DEFAULTS.toggleShowLevel();

        assertThat(changed.showLevel()).isFalse();
        assertThat(changed.tntTimer()).isTrue();
        assertThat(changed.toggleTntTimer().tntTimer()).isFalse();
    }

    @Test
    void theChestDepositGoesThroughTheThreeOptionsInOrder() {
        assertThat(ChestDeposit.ENABLED.next()).isEqualTo(ChestDeposit.NO_HOLOGRAMS);
        assertThat(ChestDeposit.NO_HOLOGRAMS.next()).isEqualTo(ChestDeposit.DISABLED);
        assertThat(ChestDeposit.DISABLED.next()).isEqualTo(ChestDeposit.ENABLED);
        assertThat(ChestDeposit.NO_HOLOGRAMS.deposits()).isTrue();
        assertThat(ChestDeposit.NO_HOLOGRAMS.showsHolograms()).isFalse();
        assertThat(ChestDeposit.DISABLED.deposits()).isFalse();
    }

    @Test
    void savedPreferencesAreFoundInTheNextMatch() {
        assertThat(find.handle(new FindPreferencesQuery(player))).isEmpty();
        Preferences changed = Preferences.DEFAULTS.toggleShowLevel().nextChestDeposit();

        assertThat(save.handle(new SavePreferencesCommand(player, changed)).isSuccess()).isTrue();

        assertThat(find.handle(new FindPreferencesQuery(player))).contains(changed);
    }
}
