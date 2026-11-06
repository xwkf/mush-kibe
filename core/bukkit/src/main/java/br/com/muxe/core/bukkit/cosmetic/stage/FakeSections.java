package br.com.muxe.core.bukkit.cosmetic.stage;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockChange;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.server.v1_8_R3.Blocks;
import net.minecraft.server.v1_8_R3.Chunk;
import net.minecraft.server.v1_8_R3.ChunkCoordIntPair;
import net.minecraft.server.v1_8_R3.ChunkSection;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.NibbleArray;
import net.minecraft.server.v1_8_R3.PacketPlayOutMapChunk;
import net.minecraft.server.v1_8_R3.WorldServer;

final class FakeSections {

    static final int SECTIONS = 16;
    static final int SECTION_HEIGHT = 16;
    static final int LIGHT_BYTES = 2048;
    static final byte FULL_LIGHT = (byte) 0xFF;

    private final WorldServer world;
    private final Map<ChunkCoordIntPair, Chunk> fakes = new LinkedHashMap<>();

    FakeSections(WorldServer world) {
        this.world = world;
    }

    Map<ChunkCoordIntPair, PacketPlayOutMapChunk> apply(List<BlockChange> changes, Function<BlockChange, IBlockData> state) {
        Map<ChunkCoordIntPair, Integer> masks = new LinkedHashMap<>();
        for (BlockChange change : changes) {
            int chunkX = change.x() >> 4;
            int chunkZ = change.z() >> 4;
            if (change.y() < 0 || change.y() >= SECTIONS * SECTION_HEIGHT || !world.getWorld().isChunkLoaded(chunkX, chunkZ)) {
                continue;
            }
            ChunkCoordIntPair key = new ChunkCoordIntPair(chunkX, chunkZ);
            Chunk fake = fakes.computeIfAbsent(key, ignored -> copy(world.getChunkAt(chunkX, chunkZ)));
            int index = change.y() / SECTION_HEIGHT;
            section(fake, index).setType(change.x() & 15, change.y() & 15, change.z() & 15, state.apply(change));
            masks.merge(key, 1 << index, (left, right) -> left | right);
        }
        Map<ChunkCoordIntPair, PacketPlayOutMapChunk> packets = new LinkedHashMap<>();
        masks.forEach((key, mask) -> packets.put(key, new PacketPlayOutMapChunk(fakes.get(key), false, mask)));
        return packets;
    }

    int highest(int x, int z) {
        if (!world.getWorld().isChunkLoaded(x >> 4, z >> 4)) {
            return -1;
        }
        ChunkSection[] sections = world.getChunkAt(x >> 4, z >> 4).getSections();
        for (int index = sections.length - 1; index >= 0; index--) {
            ChunkSection section = sections[index];
            if (section == null || section.a()) {
                continue;
            }
            for (int y = SECTION_HEIGHT - 1; y >= 0; y--) {
                if (section.getType(x & 15, y, z & 15).getBlock() != Blocks.AIR) {
                    return index * SECTION_HEIGHT + y;
                }
            }
        }
        return -1;
    }

    void clear() {
        fakes.clear();
    }

    private Chunk copy(Chunk real) {
        Chunk fake = new Chunk(world, real.locX, real.locZ);
        ChunkSection[] sections = new ChunkSection[SECTIONS];
        ChunkSection[] original = real.getSections();
        for (int index = 0; index < SECTIONS; index++) {
            ChunkSection section = original[index];
            if (section != null) {
                ChunkSection copy = new ChunkSection(section.getYPosition(), true, section.getIdArray().clone());
                copy.a(new NibbleArray(section.getEmittedLightArray().a().clone()));
                copy.b(new NibbleArray(section.getSkyLightArray().a().clone()));
                sections[index] = copy;
            }
        }
        fake.a(sections);
        return fake;
    }

    private static ChunkSection section(Chunk fake, int index) {
        ChunkSection[] sections = fake.getSections();
        if (sections[index] == null) {
            ChunkSection empty = new ChunkSection(index * SECTION_HEIGHT, true);
            byte[] light = new byte[LIGHT_BYTES];
            Arrays.fill(light, FULL_LIGHT);
            empty.b(new NibbleArray(light));
            sections[index] = empty;
        }
        return sections[index];
    }
}
