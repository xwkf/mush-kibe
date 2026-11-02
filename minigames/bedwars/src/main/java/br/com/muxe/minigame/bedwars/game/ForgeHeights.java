package br.com.muxe.minigame.bedwars.game;

import br.com.muxe.core.bukkit.display.StandMotion;
import br.com.muxe.core.bukkit.nms.PacketHologramTransport;

public final class ForgeHeights {

    public static final double DROP_ABOVE_POINT = 1.3;
    public static final double BLOCK_ABOVE_DROP = 0.35;
    public static final StandMotion BLOCK_MOTION = new StandMotion(6, 0.08, 45, 0);
    public static final double HOLOGRAM_SPACING = 0.3;
    public static final int HOLOGRAM_LINES = 3;
    public static final double HOLOGRAM_BASE_ABOVE_DROP = 0.5;
    public static final double HOLOGRAM_TOP_ABOVE_DROP = HOLOGRAM_BASE_ABOVE_DROP + (HOLOGRAM_LINES - 1) * HOLOGRAM_SPACING
        + PacketHologramTransport.FULL_STAND_LABEL_HEIGHT;

    private ForgeHeights() {
    }
}
