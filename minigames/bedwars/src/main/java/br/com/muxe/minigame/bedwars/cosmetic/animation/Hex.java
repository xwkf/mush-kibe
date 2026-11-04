package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockChange;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockType;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.bukkit.Material;

final class Hex {

    static final int BURST_AFTER = 9;
    static final double CASTER_BEHIND = 1.0;
    static final double CASTER_BELOW = 0.8;
    static final double BEAM_HEIGHT = 0.425;
    static final double RADIUS = 5.5;
    static final byte INVISIBLE = 32;
    static final List<Integer> COLORS = List.of(1, 2, 3, 6, 9, 10, 12, 13, 14);
    static final Set<Material> DYED = Set.of(Material.WOOL, Material.STAINED_CLAY, Material.STAINED_GLASS, Material.STAINED_GLASS_PANE, Material.CARPET);

    private final Actor caster;
    private final Actor target;
    private final Vec hit;
    private final int cast;
    private final int color;

    private Hex(Actor caster, Actor target, Vec hit, int cast, int color) {
        this.caster = caster;
        this.target = target;
        this.hit = hit;
        this.cast = cast;
        this.color = color;
    }

    static Hex cast(Stage stage, Vec seat, float yaw, Vec hit, int tick, int color) {
        Vec from = casterSpot(seat, yaw);
        Actor caster = guardian(stage, from);
        Actor target = guardian(stage, hit.add(0, -BEAM_HEIGHT, 0));
        caster.target(target);
        stage.sound("mob.guardian.curse", from, 1, 1);
        return new Hex(caster, target, hit, tick, color);
    }

    static Vec casterSpot(Vec seat, float yaw) {
        return Broom.spot(seat, yaw, -CASTER_BEHIND, -CASTER_BELOW);
    }

    static List<BlockChange> recolor(Stage stage, Vec center, int color) {
        List<BlockChange> changes = new ArrayList<>();
        int reach = (int) Math.ceil(RADIUS);
        int baseX = (int) Math.floor(center.x());
        int baseY = (int) Math.floor(center.y());
        int baseZ = (int) Math.floor(center.z());
        for (int x = baseX - reach; x <= baseX + reach; x++) {
            for (int y = baseY - reach; y <= baseY + reach; y++) {
                for (int z = baseZ - reach; z <= baseZ + reach; z++) {
                    if (center.distance(new Vec(x + 0.5, y + 0.5, z + 0.5)) > RADIUS) {
                        continue;
                    }
                    BlockType type = stage.type(x, y, z);
                    if (DYED.contains(type.material())) {
                        changes.add(new BlockChange(x, y, z, type.material(), color));
                    }
                }
            }
        }
        return changes;
    }

    boolean step(Stage stage, Vec seat, float yaw, int tick) {
        if (tick - cast < BURST_AFTER) {
            caster.teleport(casterSpot(seat, yaw), 0, 0);
            return false;
        }
        fade();
        stage.sound("random.explode", hit, 16, 1);
        stage.particle("EXPLOSION_HUGE", hit, 1, Vec.ZERO, 1);
        List<BlockChange> changes = recolor(stage, hit, color);
        if (!changes.isEmpty()) {
            stage.blocks(changes);
        }
        return true;
    }

    void fade() {
        caster.remove();
        target.remove();
    }

    private static Actor guardian(Stage stage, Vec position) {
        Actor guardian = stage.mob("Guardian", position, 0);
        guardian.watch(0, INVISIBLE);
        return guardian;
    }
}
