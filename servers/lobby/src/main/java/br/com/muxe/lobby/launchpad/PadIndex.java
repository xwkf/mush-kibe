package br.com.muxe.lobby.launchpad;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PadIndex {

    private final Map<ChunkKey, Set<PadSpot>> chunks = new HashMap<>();

    public void replaceChunk(String world, int chunkX, int chunkZ, Collection<PadSpot> spots) {
        ChunkKey key = new ChunkKey(world, chunkX, chunkZ);
        if (spots.isEmpty()) {
            chunks.remove(key);
        } else {
            chunks.put(key, new HashSet<>(spots));
        }
    }

    public void forgetChunk(String world, int chunkX, int chunkZ) {
        chunks.remove(new ChunkKey(world, chunkX, chunkZ));
    }

    public void add(PadSpot spot) {
        chunks.computeIfAbsent(keyOf(spot), ignored -> new HashSet<>()).add(spot);
    }

    public void remove(PadSpot spot) {
        ChunkKey key = keyOf(spot);
        Set<PadSpot> spots = chunks.get(key);
        if (spots != null && spots.remove(spot) && spots.isEmpty()) {
            chunks.remove(key);
        }
    }

    public List<PadSpot> all() {
        return chunks.values().stream().flatMap(Set::stream).toList();
    }

    public List<PadSpot> near(String world, double x, double y, double z, double radius) {
        return chunks.entrySet().stream()
            .filter(entry -> entry.getKey().world().equals(world))
            .flatMap(entry -> entry.getValue().stream())
            .filter(spot -> spot.within(world, x, y, z, radius))
            .toList();
    }

    private static ChunkKey keyOf(PadSpot spot) {
        return new ChunkKey(spot.world(), spot.chunkX(), spot.chunkZ());
    }

    private record ChunkKey(String world, int x, int z) {
    }
}
