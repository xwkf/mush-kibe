package br.com.muxe.minigame.engine.setup;

import java.util.Set;

public record LinkedBlock(SetupStep step, Set<String> materials, int radius) {

    public LinkedBlock {
        materials = Set.copyOf(materials);
    }
}
