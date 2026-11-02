package br.com.muxe.core.bukkit.hologram;

import br.com.muxe.core.bukkit.nms.PacketHologramTransport;
import br.com.muxe.core.bukkit.text.Colors;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class TextHolograms {

    public static final double DEFAULT_LINE_SPACING = 0.28;

    private final HologramTransport transport;
    private final double lineSpacing;

    public TextHolograms(HologramTransport transport, double lineSpacing) {
        if (lineSpacing <= 0) {
            throw new IllegalArgumentException("Line spacing must be positive");
        }
        this.transport = transport;
        this.lineSpacing = lineSpacing;
    }

    public static TextHolograms standard() {
        return standard(DEFAULT_LINE_SPACING);
    }

    public static TextHolograms standard(double lineSpacing) {
        return new TextHolograms(PacketHologramTransport.instance(), lineSpacing);
    }

    public double height(int lines) {
        return Math.max(0, lines - 1) * lineSpacing;
    }

    public List<HologramPixel> layout(Location top, List<String> lines) {
        return layout(top, lines, List.of());
    }

    public List<HologramPixel> layout(Location top, List<String> lines, List<Double> gaps) {
        List<HologramPixel> labels = new ArrayList<>(lines.size());
        double depth = 0;
        for (int index = 0; index < lines.size(); index++) {
            if (index > 0) {
                depth += gap(gaps, index - 1);
            }
            String text = Colors.translate(lines.get(index));
            if (!text.isEmpty()) {
                labels.add(new HologramPixel(top.clone().subtract(0, depth, 0), text));
            }
        }
        return labels;
    }

    public double depth(List<Double> gaps, int line) {
        double depth = 0;
        for (int index = 0; index < line; index++) {
            depth += gap(gaps, index);
        }
        return depth;
    }

    public SpawnedHologram show(Player viewer, Location top, List<String> lines) {
        return transport.spawn(viewer, layout(top, lines));
    }

    public SpawnedHologram show(Player viewer, Location top, List<String> lines, List<Double> gaps) {
        return transport.spawn(viewer, layout(top, lines, gaps));
    }

    private double gap(List<Double> gaps, int index) {
        return index < gaps.size() ? gaps.get(index) : lineSpacing;
    }
}
