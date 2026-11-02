package br.com.muxe.minigame.engine.world.cell;

import br.com.muxe.minigame.engine.world.map.ChunkBounds;
import br.com.muxe.minigame.engine.world.map.ChunkCodec;
import br.com.muxe.minigame.engine.world.map.ChunkData;
import br.com.muxe.minigame.engine.world.map.ChunkSource;
import br.com.muxe.minigame.engine.world.map.MapTemplate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.concurrent.Executor;

public final class MatchCells implements ChunkSource {

    private final int sizeChunks;
    private final int columns;
    private final int marginChunks;
    private final Executor compressor;
    private final Map<Integer, Occupant> occupied = new HashMap<>();
    private final TreeSet<Integer> freed = new TreeSet<>();
    private int nextIndex;

    public MatchCells(int sizeChunks, int columns, int marginChunks) {
        this(sizeChunks, columns, marginChunks, Runnable::run);
    }

    public MatchCells(int sizeChunks, int columns, int marginChunks, Executor compressor) {
        if (sizeChunks < 4 || sizeChunks % 2 != 0) {
            throw new IllegalArgumentException("Cell size must be an even number of at least 4 chunks, got " + sizeChunks);
        }
        if (columns < 1) {
            throw new IllegalArgumentException("Columns must be positive, got " + columns);
        }
        if (marginChunks < 0 || marginChunks * 2 >= sizeChunks) {
            throw new IllegalArgumentException("Margin " + marginChunks + " does not fit a cell of " + sizeChunks + " chunks");
        }
        this.sizeChunks = sizeChunks;
        this.columns = columns;
        this.marginChunks = marginChunks;
        this.compressor = compressor;
    }

    public boolean fits(MapTemplate template) {
        ChunkBounds bounds = template.bounds();
        if (bounds.isEmpty()) {
            return true;
        }
        int half = sizeChunks / 2 - marginChunks;
        return bounds.minX() >= -half && bounds.minZ() >= -half && bounds.maxX() < half && bounds.maxZ() < half;
    }

    public synchronized Cell allocate(MapTemplate template) {
        if (!fits(template)) {
            throw new IllegalArgumentException("Map " + template.bounds() + " does not fit a cell of " + sizeChunks + " chunks with a margin of " + marginChunks);
        }
        int index = freed.isEmpty() ? nextIndex++ : freed.pollFirst();
        Cell cell = new Cell(index, Math.floorMod(index, columns) * sizeChunks, index / columns * sizeChunks, sizeChunks);
        occupied.put(index, new Occupant(cell, template, new HashMap<>()));
        return cell;
    }

    public synchronized void release(Cell cell) {
        if (occupied.remove(cell.index()) != null) {
            freed.add(cell.index());
        }
    }

    public synchronized Optional<Cell> cellAt(int chunkX, int chunkZ) {
        if (chunkX < 0 || chunkZ < 0 || chunkX / sizeChunks >= columns) {
            return Optional.empty();
        }
        return Optional.ofNullable(occupied.get(chunkZ / sizeChunks * columns + chunkX / sizeChunks)).map(Occupant::cell);
    }

    public synchronized int active() {
        return occupied.size();
    }

    public synchronized int changedChunks(Cell cell) {
        Occupant occupant = occupied.get(cell.index());
        return occupant == null ? 0 : occupant.changes().size();
    }

    @Override
    public synchronized Optional<ChunkData> load(int x, int z) {
        Optional<Occupant> occupant = cellAt(x, z).map(cell -> occupied.get(cell.index()));
        if (occupant.isEmpty()) {
            return Optional.empty();
        }
        Occupant current = occupant.get();
        Change changed = current.changes().get(ChunkData.key(x, z));
        if (changed != null) {
            return Optional.of(changed.chunk());
        }
        int dx = current.cell().offsetChunkX();
        int dz = current.cell().offsetChunkZ();
        return current.template().chunk(x - dx, z - dz).map(chunk -> chunk.shiftedBy(dx, dz));
    }

    @Override
    public synchronized void save(ChunkData chunk) {
        Optional<Occupant> found = cellAt(chunk.x(), chunk.z()).map(cell -> occupied.get(cell.index()));
        if (found.isEmpty()) {
            return;
        }
        Occupant occupant = found.get();
        boolean inTemplate = occupant.template().has(chunk.x() - occupant.cell().offsetChunkX(), chunk.z() - occupant.cell().offsetChunkZ());
        if (chunk.isEmpty() && !inTemplate) {
            occupant.changes().remove(chunk.key());
            return;
        }
        Change pending = new Change(chunk, null);
        occupant.changes().put(chunk.key(), pending);
        compressor.execute(() -> {
            byte[] compressed = ChunkCodec.compressFast(chunk);
            compressed(occupant, chunk.key(), pending, compressed);
        });
    }

    public synchronized int pendingChunks(Cell cell) {
        Occupant occupant = occupied.get(cell.index());
        return occupant == null ? 0 : (int) occupant.changes().values().stream().filter(change -> change.compressed() == null).count();
    }

    private synchronized void compressed(Occupant occupant, long key, Change pending, byte[] compressed) {
        if (occupied.get(occupant.cell().index()) == occupant && occupant.changes().get(key) == pending) {
            occupant.changes().put(key, new Change(null, compressed));
        }
    }

    private record Occupant(Cell cell, MapTemplate template, Map<Long, Change> changes) {
    }

    private record Change(ChunkData raw, byte[] compressed) {

        ChunkData chunk() {
            return raw != null ? raw : ChunkCodec.decompress(compressed);
        }
    }
}
