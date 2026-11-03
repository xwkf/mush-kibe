package br.com.muxe.core.bukkit.command;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class PingListTest {

    @Test
    void listsEachPlayerWithTheirPingInNameOrder() {
        String described = PingList.describe(Map.of("tadeu", 182, "Ana", 35, "lia", 90), "&f{player} &e{ping} ms");

        assertThat(described).isEqualTo("&fAna &e35 ms, &flia &e90 ms, &ftadeu &e182 ms");
    }

    @Test
    void nobodyOnlineGivesAnEmptyList() {
        assertThat(PingList.describe(Map.of(), "&f{player} &e{ping} ms")).isEmpty();
    }
}
