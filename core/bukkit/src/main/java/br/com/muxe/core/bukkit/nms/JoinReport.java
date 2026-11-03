package br.com.muxe.core.bukkit.nms;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;

public record JoinReport(OptionalLong readyMillis, Map<String, Integer> sent, long bytes, long slowestWriteMillis) {

    public JoinReport {
        sent = Map.copyOf(sent);
    }

    public long kilobytes() {
        return Math.round(bytes / 1024.0);
    }

    public int total() {
        return sent.values().stream().mapToInt(Integer::intValue).sum();
    }

    public List<Map.Entry<String, Integer>> busiest(int limit) {
        return sent.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()).thenComparing(Map.Entry.comparingByKey()))
            .limit(Math.max(0, limit))
            .toList();
    }

    static boolean isMovement(String packetClass) {
        return packetClass.substring(packetClass.lastIndexOf('.') + 1).startsWith("PacketPlayInFlying");
    }

    static String shortName(String packetClass) {
        String simple = packetClass.substring(Math.max(packetClass.lastIndexOf('.'), packetClass.lastIndexOf('$')) + 1);
        return simple.replaceFirst("^PacketPlayOut", "");
    }
}
