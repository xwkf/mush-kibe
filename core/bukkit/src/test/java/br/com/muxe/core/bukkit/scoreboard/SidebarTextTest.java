package br.com.muxe.core.bukkit.scoreboard;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SidebarTextTest {

    @Test
    void shortLinesFitInThePrefix() {
        assertThat(SidebarText.split("§fTag: §7Membro")).isEqualTo(new SidebarText("§fTag: §7Membro", ""));
    }

    @Test
    void longLinesContinueInTheSuffixWithTheSameColor() {
        SidebarText text = SidebarText.split("§fJogadores: §b2211");

        assertThat(text.prefix()).isEqualTo("§fJogadores: §b2");
        assertThat(text.suffix()).isEqualTo("§b211");
    }

    @Test
    void aColorCodeIsNeverCutInHalf() {
        SidebarText text = SidebarText.split("§f1234567890123§a99");

        assertThat(text.prefix()).isEqualTo("§f1234567890123");
        assertThat(text.suffix()).isEqualTo("§a99");
    }

    @Test
    void aSuffixThatStartsWithItsOwnColorDoesNotRepeatTheLastOne() {
        SidebarText text = SidebarText.split("§f12345678901234§a99");

        assertThat(text.prefix()).isEqualTo("§f12345678901234");
        assertThat(text.suffix()).isEqualTo("§a99");
    }

    @Test
    void boldCarriesOverWithTheColor() {
        SidebarText text = SidebarText.split("§e§l1234567890123456");

        assertThat(text.prefix()).isEqualTo("§e§l123456789012");
        assertThat(text.suffix()).isEqualTo("§e§l3456");
    }

    @Test
    void lineBeyondPrefixAndSuffixUsesTheEntryInTheMiddle() {
        String line = "§f[§a▮▮▮▮▮§7▮▮▮▮▮§f] §7(54.9k/55k)";

        SidebarText.SidebarLine laid = SidebarText.layout(line, "§3");

        assertThat(laid.prefix()).isEqualTo("§f[§a▮▮▮▮▮§7▮▮▮▮");
        assertThat(laid.entry()).startsWith("§3§7").hasSizeLessThanOrEqualTo(SidebarText.MAX_ENTRY);
        assertThat(laid.prefix() + laid.entry().substring(2) + laid.suffix()).isEqualTo("§f[§a▮▮▮▮▮§7▮▮▮▮§7▮§f] §7(54.9k/55k)");
    }

    @Test
    void lineThatFitsKeepsTheHiddenEntry() {
        SidebarText.SidebarLine laid = SidebarText.layout("§fJogadores: §b2211", "§3");

        assertThat(laid.entry()).isEqualTo("§3§r");
        assertThat(laid.suffix()).isEqualTo("§b211");
    }

    @Test
    void aLongLineWithoutColorKeepsTheDefaultColorInTheEntry() {
        SidebarText.SidebarLine laid = SidebarText.layout("x".repeat(70), "§3");

        assertThat(laid.entry()).isEqualTo("§3§r" + "x".repeat(SidebarText.MAX_ENTRY - 4));
        assertThat(laid.suffix()).hasSize(SidebarText.MAX_PART);
    }

    @Test
    void bothPartsStayWithinTheProtocolLimit() {
        SidebarText text = SidebarText.split("§f" + "x".repeat(60));

        assertThat(text.prefix()).hasSizeLessThanOrEqualTo(SidebarText.MAX_PART);
        assertThat(text.suffix()).hasSizeLessThanOrEqualTo(SidebarText.MAX_PART).doesNotEndWith("§");
    }
}
