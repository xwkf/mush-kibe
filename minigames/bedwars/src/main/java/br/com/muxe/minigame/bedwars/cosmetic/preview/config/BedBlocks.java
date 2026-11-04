package br.com.muxe.minigame.bedwars.cosmetic.preview.config;

import br.com.muxe.core.bukkit.cosmetic.animation.BedFrame;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;

public record BedBlocks(int footX, int footY, int footZ, int headX, int headZ) {

    public static BedBlocks of(int x, int y, int z, int data) {
        BedFrame frame = BedFrame.ofBlock(x, y, z, data);
        Vec foot = frame.foot();
        Vec head = frame.head();
        return new BedBlocks(floor(foot.x()), y, floor(foot.z()), floor(head.x()), floor(head.z()));
    }

    public String describe() {
        return footX + " " + footY + " " + footZ + " -> " + headX + " " + footY + " " + headZ;
    }

    private static int floor(double value) {
        return (int) Math.floor(value);
    }
}
