package br.com.muxe.minigame.api.cosmetic.catalog;

import java.util.Optional;

public enum CosmeticKind {
    VICTORY_EFFECT("victory-effect", "vitoria", "victory-preview"),
    FINAL_KILL("final-kill", "kill-final", "final-kill-preview"),
    AURA("aura", "aura", "aura-preview"),
    PROJECTILE_TRAIL("projectile-trail", "rastro", "projectile-trail-preview"),
    KILL_MESSAGE("kill-message", "mensagem-kill", null);

    private final String key;
    private final String suffix;
    private final String previewSection;

    CosmeticKind(String key, String suffix, String previewSection) {
        this.key = key;
        this.suffix = suffix;
        this.previewSection = previewSection;
    }

    public String key() {
        return key;
    }

    public String category(String game) {
        return game + "-" + suffix;
    }

    public Optional<String> previewSection() {
        return Optional.ofNullable(previewSection);
    }
}
