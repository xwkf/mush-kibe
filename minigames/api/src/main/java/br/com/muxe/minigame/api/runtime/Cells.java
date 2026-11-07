package br.com.muxe.minigame.api.runtime;

import br.com.muxe.minigame.engine.world.cell.Cell;
import br.com.muxe.minigame.engine.world.map.MapTemplate;
import java.util.Optional;
import org.bukkit.Location;

public interface Cells {

    Cell allocate(MapTemplate map);

    void release(Cell cell);

    Optional<Cell> cellAt(Location location);

    boolean contains(Location location);
}
