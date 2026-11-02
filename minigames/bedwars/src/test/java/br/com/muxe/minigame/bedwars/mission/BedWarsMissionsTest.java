package br.com.muxe.minigame.bedwars.mission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import br.com.muxe.core.bukkit.display.DisplayLabel;
import br.com.muxe.core.bukkit.mission.announce.MissionTexts;
import br.com.muxe.core.bukkit.mission.config.MissionConfig;
import br.com.muxe.core.bukkit.nms.PacketHologramTransport;
import br.com.muxe.core.bukkit.npc.NpcLabelLayout;
import br.com.muxe.core.common.mission.domain.Mission;
import br.com.muxe.core.common.mission.domain.MissionBoard;
import br.com.muxe.core.common.mission.domain.MissionSession;
import br.com.muxe.minigame.bedwars.lobby.settings.StatsNpcSettings;
import br.com.muxe.minigame.bedwars.lobby.top.TopTexts;
import br.com.muxe.minigame.bedwars.lobby.top.TopView;
import br.com.muxe.minigame.bedwars.room.RoomFixtures;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class BedWarsMissionsTest {

    private static final MissionConfig CONFIG = MissionConfig.from(RoomFixtures.CONFIG.getConfigurationSection("missions"), BedWarsGoal::find);
    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");
    private static final Instant NOW = Instant.parse("2026-11-04T15:00:00Z");

    @Test
    void theCatalogHasTheExportedMissionsWithoutHalloween() {
        assertThat(CONFIG.catalog().daily()).extracting(Mission::id).containsExactly("ninguem-me-para", "quem-foi", "protecao-do-fim", "rei-das-pontes",
            "um-aliado-inesperado", "colecionador-de-esmeraldas");
        assertThat(CONFIG.catalog().weekly()).extracting(Mission::id).containsExactly("sou-eu-bola-de-fogo", "fim-da-linha", "olha-a-explosao",
            "ansiedade-mais-de-8000", "artesao");
        assertThat(CONFIG.catalog().creditPath()).extracting(Mission::credits).containsExactly(15L);
        assertThat(CONFIG.catalog().dailyCount()).isEqualTo(3);
        assertThat(CONFIG.texts("fim-da-linha").name()).isEqualTo("Fim da linha");
        assertThat(MissionRules.from(RoomFixtures.CONFIG.getConfigurationSection("missions"))).isEqualTo(MissionRules.DEFAULT);
    }

    @Test
    void completingAMissionShowsTheReferenceLineHoverAndReward() {
        MissionSession session = session();
        List<Mission> completed = session.update(Map.of("KILL_STREAK", 2L));
        MissionBoard.Entry entry = entry(session, "ninguem-me-para");

        MissionTexts.Completion completion = MissionTexts.completion(CONFIG, entry);

        assertThat(completed).extracting(Mission::id).containsExactly("ninguem-me-para");
        assertThat(completion.prefix()).isEqualTo("&b&lMISSÕES &aVocê completou ");
        assertThat(completion.name()).isEqualTo("&eNinguém me para! &7(Diária)");
        assertThat(completion.hover()).containsExactly("&eNinguém me para! &7(Diária)", "", "&7Elimine 2 adversários seguidos em uma partida: &f2/2");
        assertThat(completion.rewards()).containsExactly("&b+625 XP (Bed Wars)");
    }

    @Test
    void theMatchSummaryListsWhatMovedPerCategory() {
        MissionSession session = session();
        session.update(Map.of("KILL_STREAK", 2L, "FINAL_KILLS", 3L, "FAST_WIN", 1L));

        List<String> lines = MissionTexts.summary(CONFIG, session.changes());

        assertThat(lines).containsExactly(
            "&a----------------------------------------",
            "&aMISSÕES DIÁRIAS",
            "",
            " &eNinguém me para!: &72/2 &a(+2) &bCOMPLETO!",
            "",
            "&6MISSÕES SEMANAIS",
            "",
            " &eFim da linha: &73/35 &a(+3)",
            " &eAnsiedade mais de 8000: &71/1 &a(+1) &bCOMPLETO!",
            "",
            "&dCAMINHO DOS CRÉDITOS",
            "",
            " &eComplecionista: &71/5 &a(+1)",
            "",
            "&a----------------------------------------"
        );
    }

    @Test
    void theStatsNpcLinesSitAtTheReferenceHeights() {
        NpcLabelLayout labels = StatsNpcSettings.from(RoomFixtures.CONFIG.getConfigurationSection("lobby.stats-npc")).labels();
        List<String> lines = List.of("a", "b", "c", "d", "e", "f", "g", "h", "i");

        List<Double> heights = labels.place(lines).stream().map(DisplayLabel::height).toList();

        double bottom = heights.getLast();
        assertThat(heights.stream().map(height -> Math.round((height - bottom) * 1e6) / 1e6).toList())
            .containsExactly(2.75, 2.46875, 2.03125, 1.75, 1.46875, 1.1875, 0.75, 0.28125, 0.0);
    }

    @Test
    void theModeNpcLinesSitAtTheReferenceHeights() {
        NpcLabelLayout labels = NpcLabelLayout.from(RoomFixtures.CONFIG.getConfigurationSection("lobby.npc-labels"));

        List<Double> heights = labels.place(List.of("&6&lUTILIZE", "&a/duel <nome>", "&b&l1V1", "&e69 jogando")).stream().map(DisplayLabel::height).toList();

        assertThat(heights.get(2) - heights.get(3)).isCloseTo(0.28125, within(1e-9));
        assertThat(heights.get(1) - heights.get(2)).isCloseTo(0.46875, within(1e-9));
        assertThat(heights.get(0) - heights.get(1)).isCloseTo(0.28125, within(1e-9));
    }

    @Test
    void theTopLinesSitAtTheReferenceHeights() {
        TopTexts texts = TopTexts.from(RoomFixtures.CONFIG.getConfigurationSection("lobby.tops"));
        List<Double> gaps = TopView.gaps(texts.spacing());
        double[] depth = new double[gaps.size() + 1];
        for (int index = 0; index < gaps.size(); index++) {
            depth[index + 1] = depth[index] + gaps.get(index);
        }

        assertThat(texts.labelAboveStand()).isEqualTo(PacketHologramTransport.FULL_STAND_LABEL_HEIGHT);
        assertThat(depth[1]).isEqualTo(79.78125 - 79.5);
        assertThat(depth[2]).isEqualTo(79.78125 - 79.125);
        assertThat(depth[11]).isEqualTo(79.78125 - 76.59375);
        assertThat(depth[12]).isEqualTo(79.78125 - 76.21875);
        assertThat(depth[13]).isEqualTo(79.78125 - 75.875);
    }

    private static MissionSession session() {
        List<Mission> active = List.of(
            mission(CONFIG.catalog().daily(), "ninguem-me-para"),
            mission(CONFIG.catalog().weekly(), "fim-da-linha"),
            mission(CONFIG.catalog().weekly(), "ansiedade-mais-de-8000"),
            CONFIG.catalog().creditPath().getFirst()
        );
        return new MissionSession(MissionBoard.of(active, Map.of(), MissionBoard.starts(active, NOW, ZONE)));
    }

    private static Mission mission(List<Mission> pool, String id) {
        return pool.stream().filter(mission -> mission.id().equals(id)).findFirst().orElseThrow();
    }

    private static MissionBoard.Entry entry(MissionSession session, String id) {
        return session.board().entries().stream().filter(entry -> entry.mission().id().equals(id)).findFirst().orElseThrow();
    }
}
