package br.com.muxe.minigame.bedwars.cosmetic.animation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;

final class Patches {

    static final double MIN_RADIUS = 3;
    static final double MAX_RADIUS = 9;
    static final int CELL = 16;

    private final Map<Long, List<Ball>> cells = new HashMap<>();

    Patches(int[] box, int bottom, int top, double perColumn, RandomGenerator random) {
        int width = box[2] - box[0] + 1;
        int depth = box[3] - box[1] + 1;
        long count = Math.round(perColumn * width * depth);
        for (long index = 0; index < count; index++) {
            double x = box[0] + random.nextDouble() * width;
            double y = bottom + random.nextDouble() * (top - bottom + 1);
            double z = box[1] + random.nextDouble() * depth;
            add(new Ball(x, y, z, Kit.between(random, MIN_RADIUS, MAX_RADIUS)));
        }
    }

    Patches(List<Ball> balls) {
        balls.forEach(this::add);
    }

    double depth(int x, int y, int z) {
        double deepest = Double.NEGATIVE_INFINITY;
        for (Ball ball : cells.getOrDefault(cell(Math.floorDiv(x, CELL), Math.floorDiv(z, CELL)), List.of())) {
            double distance = Math.sqrt(square(x + 0.5 - ball.x()) + square(y + 0.5 - ball.y()) + square(z + 0.5 - ball.z()));
            deepest = Math.max(deepest, ball.radius() - distance);
        }
        return deepest;
    }

    private void add(Ball ball) {
        int fromX = Math.floorDiv((int) Math.floor(ball.x() - ball.radius()), CELL);
        int toX = Math.floorDiv((int) Math.floor(ball.x() + ball.radius()), CELL);
        int fromZ = Math.floorDiv((int) Math.floor(ball.z() - ball.radius()), CELL);
        int toZ = Math.floorDiv((int) Math.floor(ball.z() + ball.radius()), CELL);
        for (int cellX = fromX; cellX <= toX; cellX++) {
            for (int cellZ = fromZ; cellZ <= toZ; cellZ++) {
                cells.computeIfAbsent(cell(cellX, cellZ), key -> new ArrayList<>()).add(ball);
            }
        }
    }

    private static long cell(int cellX, int cellZ) {
        return (long) cellX << 32 | (cellZ & 0xFFFFFFFFL);
    }

    private static double square(double value) {
        return value * value;
    }

    record Ball(double x, double y, double z, double radius) {
    }
}
