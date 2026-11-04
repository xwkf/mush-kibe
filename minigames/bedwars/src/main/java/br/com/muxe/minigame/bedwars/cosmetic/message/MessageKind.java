package br.com.muxe.minigame.bedwars.cosmetic.message;

import java.util.Arrays;
import java.util.Optional;

public enum MessageKind {
    KILL("kill", null),
    FINAL_KILL("final-kill", null),
    VOID_KILL("void-kill", KILL),
    VOID_FINAL_KILL("void-final-kill", FINAL_KILL),
    DEATH("death", null),
    FINAL_DEATH("final-death", null),
    VOID_DEATH("void-death", DEATH),
    VOID_FINAL_DEATH("void-final-death", FINAL_DEATH),
    BED("bed", null);

    private final String key;
    private final MessageKind fallback;

    MessageKind(String key, MessageKind fallback) {
        this.key = key;
        this.fallback = fallback;
    }

    public String key() {
        return key;
    }

    public Optional<MessageKind> fallback() {
        return Optional.ofNullable(fallback);
    }

    public static Optional<MessageKind> fromKey(String key) {
        return Arrays.stream(values()).filter(kind -> kind.key.equals(key)).findFirst();
    }

    public static MessageKind kill(boolean finalKill, boolean voidDeath) {
        if (voidDeath) {
            return finalKill ? VOID_FINAL_KILL : VOID_KILL;
        }
        return finalKill ? FINAL_KILL : KILL;
    }

    public static MessageKind death(boolean finalDeath, boolean voidDeath) {
        if (voidDeath) {
            return finalDeath ? VOID_FINAL_DEATH : VOID_DEATH;
        }
        return finalDeath ? FINAL_DEATH : DEATH;
    }
}
