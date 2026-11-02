package br.com.muxe.core.bukkit.nms;

import java.util.Map;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

final class MaskedBlocks {

    private final Map<UUID, Map<Long, Integer>> masks = new ConcurrentHashMap<>();

    static int combinedId(int blockId, int data) {
        return blockId | data << 12;
    }

    void mask(UUID viewer, int x, int y, int z, int combinedId) {
        masks.computeIfAbsent(viewer, id -> new ConcurrentHashMap<>()).put(key(x, y, z), combinedId);
    }

    void unmask(UUID viewer, int x, int y, int z) {
        masks.computeIfPresent(viewer, (id, blocks) -> {
            blocks.remove(key(x, y, z));
            return blocks.isEmpty() ? null : blocks;
        });
    }

    void clear(UUID viewer) {
        masks.remove(viewer);
    }

    boolean any(UUID viewer) {
        return masks.containsKey(viewer);
    }

    OptionalInt find(UUID viewer, int x, int y, int z) {
        Map<Long, Integer> blocks = masks.get(viewer);
        if (blocks == null) {
            return OptionalInt.empty();
        }
        Integer combinedId = blocks.get(key(x, y, z));
        return combinedId == null ? OptionalInt.empty() : OptionalInt.of(combinedId);
    }

    private static long key(int x, int y, int z) {
        return ((long) x & 0x3FFFFFF) << 38 | ((long) y & 0xFFF) << 26 | (long) z & 0x3FFFFFF;
    }
}
