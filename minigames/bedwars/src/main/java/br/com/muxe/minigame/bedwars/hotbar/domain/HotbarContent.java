package br.com.muxe.minigame.bedwars.hotbar.domain;

public record HotbarContent(Kind kind, int amount, int maxStack) {

    public static final HotbarContent EMPTY = new HotbarContent(Kind.EMPTY, 0, 0);
    public static final HotbarContent OTHER = new HotbarContent(Kind.OTHER, 0, 0);

    public static HotbarContent same(int amount, int maxStack) {
        return new HotbarContent(Kind.SAME, amount, maxStack);
    }

    public boolean canTake(int incoming) {
        return switch (kind) {
            case EMPTY -> true;
            case SAME -> amount + incoming <= maxStack;
            case OTHER -> false;
        };
    }

    public enum Kind {
        EMPTY,
        SAME,
        OTHER
    }
}
