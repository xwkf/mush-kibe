package br.com.muxe.core.bukkit.command;

import java.util.Map;
import java.util.stream.Collectors;

public final class PingList {

    private PingList() {
    }

    public static String describe(Map<String, Integer> pings, String entry) {
        return pings.entrySet().stream()
            .sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
            .map(ping -> entry.replace("{player}", ping.getKey()).replace("{ping}", Integer.toString(ping.getValue())))
            .collect(Collectors.joining(", "));
    }
}
