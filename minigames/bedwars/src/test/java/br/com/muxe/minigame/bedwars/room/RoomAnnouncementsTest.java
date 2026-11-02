package br.com.muxe.minigame.bedwars.room;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.text.MessageCatalog;
import org.junit.jupiter.api.Test;

class RoomAnnouncementsTest {

    private static final MessageCatalog MESSAGES = MessageCatalog.from(RoomFixtures.CONFIG.getConfigurationSection("messages"));

    @Test
    void joiningTheRoomShowsTheNameInTheTagColourAndHowFullTheRoomIs() {
        assertThat(RoomAnnouncements.joined(MESSAGES, "§5tadeu", 3, 8)).isEqualTo("§5tadeu §eentrou na sala (§b3§e/§b8§e).");
    }

    @Test
    void leavingTheRoomTellsWhoLeft() {
        assertThat(RoomAnnouncements.left(MESSAGES, "§5tadeu")).isEqualTo("§5tadeu §esaiu da sala.");
    }
}
