package br.com.muxe.core.bukkit.scoreboard;

import org.bukkit.ChatColor;

public record SidebarText(String prefix, String suffix) {

    public static final int MAX_PART = 16;
    public static final int MAX_ENTRY = 40;

    public static SidebarText split(String line) {
        SidebarLine laid = layout(line, "");
        return new SidebarText(laid.prefix(), laid.suffix());
    }

    public static SidebarLine layout(String line, String hidden) {
        String plainEntry = hidden + ChatColor.RESET;
        if (line.length() <= MAX_PART) {
            return new SidebarLine(line, plainEntry, "");
        }
        int cut = MAX_PART;
        if (line.charAt(cut - 1) == ChatColor.COLOR_CHAR) {
            cut--;
        }
        String prefix = line.substring(0, cut);
        String remainder = line.substring(cut);
        String carried = startsWithColor(remainder) ? "" : ChatColor.getLastColors(prefix);
        if (carried.length() + remainder.length() <= MAX_PART || hidden.isEmpty()) {
            return new SidebarLine(prefix, plainEntry, trim(carried + remainder));
        }
        String lead = hidden + (carried.isEmpty() ? ChatColor.RESET.toString() : carried);
        int room = Math.min(remainder.length(), MAX_ENTRY - lead.length());
        if (room > 0 && room < remainder.length() && remainder.charAt(room - 1) == ChatColor.COLOR_CHAR) {
            room--;
        }
        return new SidebarLine(prefix, lead + remainder.substring(0, room), trim(remainder.substring(room)));
    }

    private static boolean startsWithColor(String text) {
        if (text.length() < 2 || text.charAt(0) != ChatColor.COLOR_CHAR) {
            return false;
        }
        ChatColor color = ChatColor.getByChar(text.charAt(1));
        return color != null && color.isColor();
    }

    private static String trim(String text) {
        String trimmed = text.length() > MAX_PART ? text.substring(0, MAX_PART) : text;
        while (!trimmed.isEmpty() && trimmed.charAt(trimmed.length() - 1) == ChatColor.COLOR_CHAR) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    public record SidebarLine(String prefix, String entry, String suffix) {
    }
}
