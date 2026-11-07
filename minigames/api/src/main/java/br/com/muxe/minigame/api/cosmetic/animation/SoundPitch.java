package br.com.muxe.minigame.api.cosmetic.animation;

public final class SoundPitch {

    public static final float STEPS = 63;

    private SoundPitch() {
    }

    public static float of(int recorded) {
        return (recorded + 0.5f) / STEPS;
    }
}
