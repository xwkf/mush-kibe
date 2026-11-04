package br.com.muxe.minigame.bedwars.cosmetic.preview.config;

import br.com.muxe.core.bukkit.config.edit.LocationValues;
import br.com.muxe.core.common.config.source.DurationParser;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class PreviewEdits {

    public static final String NOT_SUPPORTED = "preview-config-not-supported";
    public static final String BAD_NUMBER = "preview-config-bad-number";
    public static final String BAD_DURATION = "preview-config-bad-duration";
    public static final String NOT_A_BED = "preview-config-not-a-bed";
    public static final double MAX_DISTANCE = 64;

    private PreviewEdits() {
    }

    public static PreviewEdit spawn(PreviewKind kind, LocationValues here) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put(kind.path("world"), here.world());
        values.putAll(here.entries(kind.path("spawn"), false));
        return new PreviewEdit.Write(values, here.describe());
    }

    public static PreviewEdit bed(PreviewKind kind, BedBlocks bed) {
        if (!kind.hasBed()) {
            return new PreviewEdit.Refused(NOT_SUPPORTED, kind.word());
        }
        Map<String, Object> values = new LinkedHashMap<>();
        values.put(kind.path("bed.foot.x"), bed.footX());
        values.put(kind.path("bed.foot.y"), bed.footY());
        values.put(kind.path("bed.foot.z"), bed.footZ());
        values.put(kind.path("bed.head.x"), bed.headX());
        values.put(kind.path("bed.head.z"), bed.headZ());
        return new PreviewEdit.Write(values, bed.describe());
    }

    public static PreviewEdit distance(PreviewKind kind, String raw) {
        if (!kind.hasTarget()) {
            return new PreviewEdit.Refused(NOT_SUPPORTED, kind.word());
        }
        double blocks;
        try {
            blocks = Double.parseDouble(raw.trim().replace(',', '.'));
        } catch (NumberFormatException exception) {
            return new PreviewEdit.Refused(BAD_NUMBER, raw);
        }
        if (!Double.isFinite(blocks) || blocks <= 0 || blocks > MAX_DISTANCE) {
            return new PreviewEdit.Refused(BAD_NUMBER, raw);
        }
        Object stored = blocks == Math.floor(blocks) ? (Object) (int) blocks : (Object) blocks;
        return new PreviewEdit.Write(Map.of(kind.path("target.distance"), stored), String.valueOf(stored));
    }

    public static PreviewEdit timeout(PreviewKind kind, String raw) {
        String text = raw.toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
        Duration parsed;
        try {
            parsed = DurationParser.parse(text);
        } catch (IllegalArgumentException exception) {
            return new PreviewEdit.Refused(BAD_DURATION, raw);
        }
        if (parsed.toSeconds() < 1) {
            return new PreviewEdit.Refused(BAD_DURATION, raw);
        }
        return new PreviewEdit.Write(Map.of(kind.path("timeout"), text), text);
    }
}
