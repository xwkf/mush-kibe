package br.com.muxe.lobby.launchpad;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public final class PadScanner implements Listener {

    private final PadIndex index;
    private final Material block;
    private final Executor async;
    private final Executor mainThread;
    private final Logger logger;

    public PadScanner(PadIndex index, Material block, Executor async, Executor mainThread, Logger logger) {
        this.index = index;
        this.block = block;
        this.async = async;
        this.mainThread = mainThread;
        this.logger = logger;
    }

    public void scanLoaded(List<World> worlds) {
        worlds.forEach(world -> {
            for (Chunk chunk : world.getLoadedChunks()) {
                scan(chunk);
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        scan(event.getChunk());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkUnload(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();
        index.forgetChunk(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (event.getBlockPlaced().getType() == block) {
            index.add(spotOf(event.getBlockPlaced()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == block) {
            index.remove(spotOf(event.getBlock()));
        }
    }

    @SuppressWarnings("deprecation")
    private void scan(Chunk chunk) {
        ChunkSnapshot snapshot = chunk.getChunkSnapshot(false, false, false);
        int typeId = block.getId();
        CompletableFuture.supplyAsync(() -> ChunkScan.find(new SnapshotBlocks(snapshot), typeId), async)
            .thenAcceptAsync(spots -> {
                if (chunk.isLoaded()) {
                    index.replaceChunk(snapshot.getWorldName(), snapshot.getX(), snapshot.getZ(), spots);
                }
            }, mainThread)
            .exceptionally(error -> {
                logger.log(Level.WARNING, "Could not scan chunk " + snapshot.getX() + "," + snapshot.getZ() + " for launch pads", error);
                return null;
            });
    }

    private static PadSpot spotOf(Block block) {
        return new PadSpot(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
    }

    private record SnapshotBlocks(ChunkSnapshot snapshot) implements ChunkScan.ChunkBlocks {

        @Override
        public String world() {
            return snapshot.getWorldName();
        }

        @Override
        public int chunkX() {
            return snapshot.getX();
        }

        @Override
        public int chunkZ() {
            return snapshot.getZ();
        }

        @Override
        public boolean emptySection(int section) {
            return snapshot.isSectionEmpty(section);
        }

        @Override
        @SuppressWarnings("deprecation")
        public int typeId(int x, int y, int z) {
            return snapshot.getBlockTypeId(x, y, z);
        }
    }
}
