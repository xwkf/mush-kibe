package br.com.muxe.minigame.bedwars.cosmetic.animation;

final class SoundPitch {

    static final float STEPS = 63;

    private SoundPitch() {
    }

    static float of(int recorded) {
        return (recorded + 0.5f) / STEPS;
    }
}
