package br.com.muxe.minigame.bedwars.matchmaking;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.common.messaging.codec.PacketReader;
import br.com.muxe.core.common.messaging.codec.PacketWriter;
import br.com.muxe.minigame.bedwars.network.JoinRoomReply;
import br.com.muxe.minigame.bedwars.room.RoomFixtures;
import org.junit.jupiter.api.Test;

class JoinNoticesTest {

    @Test
    void joiningAnOpenRoomOnlyAnnouncesTheRoom() {
        Matchmaker.Target target = new Matchmaker.Target("bedwars-1", "2vfvk5");

        assertThat(JoinNotices.beforeRequest(target)).isEmpty();
        assertThat(JoinNotices.afterReply(JoinRoomReply.reserved("2vfvk5", false))).containsExactly("room-sending");
    }

    @Test
    void askingForANewRoomAnnouncesTheRequestTheRoomAndTheCreation() {
        Matchmaker.Target target = new Matchmaker.Target("bedwars-1", "");

        assertThat(JoinNotices.beforeRequest(target)).containsExactly("room-requesting");
        assertThat(JoinNotices.afterReply(JoinRoomReply.reserved("2vfvjv", true))).containsExactly("room-sending", "room-created");
    }

    @Test
    void aNewRoomThatTurnedOutToExistIsNotAnnouncedAsCreated() {
        assertThat(JoinNotices.afterReply(JoinRoomReply.reserved("2vfvjv", false))).containsExactly("room-sending");
    }

    @Test
    void theMessagesFollowTheReference() {
        assertThat(RoomFixtures.CONFIG.getString("messages.room-requesting")).isEqualTo("&aSolicitando nova sala...");
        assertThat(RoomFixtures.CONFIG.getString("messages.room-sending")).isEqualTo("&aEnviando para {room}...");
        assertThat(RoomFixtures.CONFIG.getString("messages.room-created")).isEqualTo("&aSala criada!");
    }

    @Test
    void theReplyCarriesWhetherTheRoomWasCreated() {
        JoinRoomReply reply = JoinRoomReply.reserved("2vfvjv", true);
        PacketWriter out = new PacketWriter();
        JoinRoomReply.TYPE.codec().encode(reply, out);
        PacketReader in = new PacketReader(out.toByteArray());

        assertThat(JoinRoomReply.TYPE.codec().decode(in)).isEqualTo(reply);
        in.ensureFullyRead();
    }
}
