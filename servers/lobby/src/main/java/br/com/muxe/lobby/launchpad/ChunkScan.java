package br.com.muxe.lobby.launchpad;

import java.util.ArrayList;
import java.util.List;

public final class ChunkScan {

    public static final int SIZE = 16;
    public static final int SECTIONS = 16;

    private ChunkScan() {
    }

    public static List<PadSpot> find(ChunkBlocks blocks, int wantedType) {
        List<PadSpot> found = new ArrayList<>();
        int baseX = blocks.chunkX() * SIZE;
        int baseZ = blocks.chunkZ() * SIZE;
        for (int section = 0; section < SECTIONS; section++) {
            if (blocks.emptySection(section)) {
                continue;
            }
            for (int y = section * SIZE; y < (section + 1) * SIZE; y++) {
                for (int x = 0; x < SIZE; x++) {
                    for (int z = 0; z < SIZE; z++) {
                        if (blocks.typeId(x, y, z) == wantedType) {
                            found.add(new PadSpot(blocks.world(), baseX + x, y, baseZ + z));
                        }
                    }
                }
            }
        }
        return found;
    }

    public interface ChunkBlocks {

        String world();

        int chunkX();

        int chunkZ();

        boolean emptySection(int section);

        int typeId(int x, int y, int z);
    }
}
