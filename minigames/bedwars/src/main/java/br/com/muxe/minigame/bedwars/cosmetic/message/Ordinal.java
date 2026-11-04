package br.com.muxe.minigame.bedwars.cosmetic.message;

public final class Ordinal {

    private static final int GROUP = 3;

    private Ordinal() {
    }

    public static String format(long number) {
        if (number < 0) {
            throw new IllegalArgumentException("An ordinal cannot be negative: " + number);
        }
        String digits = Long.toString(number);
        StringBuilder grouped = new StringBuilder();
        for (int index = 0; index < digits.length(); index++) {
            if (index > 0 && (digits.length() - index) % GROUP == 0) {
                grouped.append('.');
            }
            grouped.append(digits.charAt(index));
        }
        return grouped.toString();
    }
}
