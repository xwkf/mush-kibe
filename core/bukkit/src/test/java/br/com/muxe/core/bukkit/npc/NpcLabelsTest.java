package br.com.muxe.core.bukkit.npc;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.hologram.HologramPixel;
import java.util.List;
import org.bukkit.Location;
import org.junit.jupiter.api.Test;

class NpcLabelsTest {

    @Test
    void linesThatOnlyChangeTextKeepTheirSpots() {
        List<HologramPixel> before = List.of(new HologramPixel(new Location(null, 0.5, 79.4, 5.5), "§f§lNOVO MODO"), new HologramPixel(new Location(null, 0.5, 79.1, 5.5), "§b§lPILARES"));
        List<HologramPixel> after = List.of(new HologramPixel(new Location(null, 0.5, 79.4, 5.5), "§d§lNOVO MODO"), new HologramPixel(new Location(null, 0.5, 79.1, 5.5), "§b§lPILARES"));

        assertThat(NpcLabels.sameSpots(before, after)).isTrue();
    }

    @Test
    void anExtraLineOrAMovedLineNeedsANewHologram() {
        List<HologramPixel> one = List.of(new HologramPixel(new Location(null, 0.5, 79.4, 5.5), "a"));
        List<HologramPixel> two = List.of(new HologramPixel(new Location(null, 0.5, 79.4, 5.5), "a"), new HologramPixel(new Location(null, 0.5, 79.1, 5.5), "b"));
        List<HologramPixel> moved = List.of(new HologramPixel(new Location(null, 0.5, 79.7, 5.5), "a"));

        assertThat(NpcLabels.sameSpots(one, two)).isFalse();
        assertThat(NpcLabels.sameSpots(one, moved)).isFalse();
    }
}
