package br.com.muxe.minigame.engine.arena;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.common.arena.ArenaModule;
import br.com.muxe.core.common.cqrs.bus.SimpleCommandBus;
import br.com.muxe.core.common.cqrs.bus.SimpleQueryBus;
import br.com.muxe.core.common.testing.DirectTransactions;
import br.com.muxe.core.common.testing.InMemoryArenaRepository;
import br.com.muxe.core.common.testing.MutableClock;
import br.com.muxe.minigame.engine.setup.ArenaDraft;
import br.com.muxe.minigame.engine.setup.Point;
import br.com.muxe.minigame.engine.world.cell.MatchCells;
import br.com.muxe.minigame.engine.world.map.MapFixtures;
import br.com.muxe.minigame.engine.world.map.MapTemplate;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

class DatabaseArenaStoreTest {

    private final SimpleCommandBus commands = new SimpleCommandBus(Runnable::run, List.of());
    private final SimpleQueryBus queries = new SimpleQueryBus(Runnable::run, List.of());
    private final DatabaseArenaStore bedwars = new DatabaseArenaStore("bedwars", commands, queries);
    private final DatabaseArenaStore skywars = new DatabaseArenaStore("skywars", commands, queries);
    private final MapTemplate map = MapTemplate.builder().put(MapFixtures.chunk(0, 0, MapFixtures.section(4, 1, 0))).build();

    DatabaseArenaStoreTest() {
        ArenaModule.register(commands, queries, new InMemoryArenaRepository(), new DirectTransactions(), MutableClock.startingAt("2026-11-02T12:00:00Z"));
    }

    @Test
    void anArenaSavedOnTheLobbyIsReadByEveryServerOfTheGame() throws IOException {
        ArenaDraft draft = new ArenaDraft("quartetos_teste", "quartetos", "Teste");
        draft.restorePoints("espera", List.of(new Point(1.5, 80, -2.5, 90f, 0f)));

        bedwars.save(draft, true, map);

        assertThat(bedwars.names()).containsExactly("quartetos_teste");
        assertThat(skywars.names()).isEmpty();
        assertThat(bedwars.read("quartetos_teste").orElseThrow().ready()).isTrue();
        assertThat(bedwars.read("quartetos_teste").orElseThrow().draft().allPoints().get("espera")).containsExactly(new Point(1.5, 80, -2.5, 90f, 0f));
        assertThat(bedwars.map("quartetos_teste").orElseThrow().chunk(0, 0).orElseThrow().section(4).orElseThrow().id(5, 5, 5)).isEqualTo(1);
        ArenaCatalog catalog = new ArenaCatalog(bedwars, new MatchCells(64, 4, 12)::fits, Logger.getLogger("test"));
        assertThat(catalog.loadAll()).isEqualTo(1);
    }

    @Test
    void savingOnlyTheDefinitionKeepsTheSavedMap() throws IOException {
        ArenaDraft draft = new ArenaDraft("1v1_slip", "1v1", "Slip");
        bedwars.save(draft, false, map);

        draft.restorePoints("espectador", List.of(new Point(0, 90, 0, 0f, 0f)));
        bedwars.saveDefinition(draft, true);

        assertThat(bedwars.read("1v1_slip").orElseThrow().ready()).isTrue();
        assertThat(bedwars.map("1v1_slip").orElseThrow().chunkCount()).isEqualTo(1);
    }

    @Test
    void anArenaWithoutAMapHasNoMap() throws IOException {
        bedwars.saveDefinition(new ArenaDraft("solo_vazio", "solo", "Vazio"), false);

        assertThat(bedwars.read("solo_vazio")).isPresent();
        assertThat(bedwars.map("solo_vazio")).isEmpty();
    }

    @Test
    void aDeletedArenaLeavesTheStoreAndTheCatalog() throws IOException {
        bedwars.save(new ArenaDraft("majestic_solo", "solo", "Majestic"), true, map);
        skywars.save(new ArenaDraft("majestic_solo", "solo", "Majestic"), true, map);
        ArenaCatalog catalog = new ArenaCatalog(bedwars, new MatchCells(64, 4, 12)::fits, Logger.getLogger("test"));
        catalog.loadAll();

        assertThat(bedwars.delete("majestic_solo")).isTrue();

        assertThat(bedwars.names()).isEmpty();
        assertThat(bedwars.map("majestic_solo")).isEmpty();
        assertThat(catalog.reload("majestic_solo")).isEmpty();
        assertThat(catalog.find("majestic_solo")).isEmpty();
        assertThat(skywars.names()).containsExactly("majestic_solo");
    }

    @Test
    void deletingAnArenaThatDoesNotExistReportsIt() throws IOException {
        assertThat(bedwars.delete("nada_aqui")).isFalse();
    }
}
