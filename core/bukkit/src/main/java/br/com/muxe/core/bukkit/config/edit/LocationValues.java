package br.com.muxe.core.bukkit.config.edit;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public record LocationValues(String world, double x, double y, double z, double yaw, double pitch) {

    private static final double HUNDREDTHS = 100.0;
    private static final double TENTHS = 10.0;

    public LocationValues {
        Objects.requireNonNull(world, "world");
    }

    public static LocationValues of(String world, double x, double y, double z, float yaw, float pitch, boolean centered) {
        return new LocationValues(
            world,
            centered ? Math.floor(x) + 0.5 : round(x, HUNDREDTHS),
            round(y, HUNDREDTHS),
            centered ? Math.floor(z) + 0.5 : round(z, HUNDREDTHS),
            round(yaw, TENTHS),
            round(pitch, TENTHS)
        );
    }

    public Map<String, Object> entries(String path, boolean includeWorld) {
        Map<String, Object> values = new LinkedHashMap<>();
        if (includeWorld) {
            values.put(ConfigPaths.join(path, "world"), world);
        }
        values.put(ConfigPaths.join(path, "x"), x);
        values.put(ConfigPaths.join(path, "y"), y);
        values.put(ConfigPaths.join(path, "z"), z);
        values.put(ConfigPaths.join(path, "yaw"), yaw);
        values.put(ConfigPaths.join(path, "pitch"), pitch);
        return values;
    }

    public String describe() {
        return String.format(Locale.ROOT, "%s %s %s %s (yaw %s, pitch %s)", world, x, y, z, yaw, pitch);
    }

    private static double round(double value, double scale) {
        double rounded = Math.round(value * scale) / scale;
        return rounded == 0 ? 0.0 : rounded;
    }
}
