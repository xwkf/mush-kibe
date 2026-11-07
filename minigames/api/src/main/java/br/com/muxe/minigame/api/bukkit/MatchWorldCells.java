package br.com.muxe.minigame.api.bukkit;

import br.com.muxe.minigame.api.runtime.Cells;
import br.com.muxe.minigame.engine.world.cell.Cell;
import br.com.muxe.minigame.engine.world.map.MapTemplate;
import br.com.muxe.minigame.engine.world.server.MatchWorld;
import java.util.Optional;
import org.bukkit.Location;

public record MatchWorldCells(MatchWorld world) implements Cells {

    @Override
    public Cell allocate(MapTemplate map) {
        return world.allocate(map);
    }

    @Override
    public void release(Cell cell) {
        world.release(cell);
    }

    @Override
    public Optional<Cell> cellAt(Location location) {
        return world.cellAt(location);
    }

    @Override
    public boolean contains(Location location) {
        return world.contains(location.getWorld());
    }
}
