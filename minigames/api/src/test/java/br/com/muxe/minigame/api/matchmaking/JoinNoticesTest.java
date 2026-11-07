package br.com.muxe.minigame.api.matchmaking;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.common.messaging.codec.PacketReader;
import br.com.muxe.core.common.messaging.codec.PacketWriter;
import br.com.muxe.minigame.api.network.JoinRoomReply;
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
    void theReplyCarriesWhetherTheRoomWasCreated() {
        JoinRoomReply reply = JoinRoomReply.reserved("2vfvjv", true);
        PacketWriter out = new PacketWriter();
        JoinRoomReply.CODEC.encode(reply, out);
        PacketReader in = new PacketReader(out.toByteArray());

        assertThat(JoinRoomReply.CODEC.decode(in)).isEqualTo(reply);
        in.ensureFullyRead();
    }
}
