package br.com.muxe.core.bukkit.cosmetic.animation;

import java.util.random.RandomGenerator;

public interface SceneAnimation {

    String id();

    void script(Vec origin, Vec facing, Stage stage, Timeline timeline, RandomGenerator random);
}
