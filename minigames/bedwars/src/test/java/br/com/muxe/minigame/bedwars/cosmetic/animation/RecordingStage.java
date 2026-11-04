package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.SignedSkin;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.bukkit.Material;

final class RecordingStage implements Stage {

    final List<FakeActor> actors = new ArrayList<>();
    final Map<String, Integer> particles = new HashMap<>();
    final List<String> sounds = new ArrayList<>();
    final List<SignedSkin> skins = new ArrayList<>();
    final List<Float> pitches = new ArrayList<>();
    final List<String> blocks = new ArrayList<>();
    final List<Long> times = new ArrayList<>();
    final List<FakeMount> mounts = new ArrayList<>();
    final Map<UUID, Consumer<Click>> tools = new HashMap<>();
    final List<Vec> pushes = new ArrayList<>();
    final List<BlockChange> bulk = new ArrayList<>();
    Function<int[], BlockType> types = block -> new BlockType(this.solid(block[0], block[1], block[2]) ? Material.STONE : Material.AIR, 0);
    Vec eyes = new Vec(0, 1.62, 0);
    final Map<UUID, Vec> eyesOf = new HashMap<>();
    final Map<UUID, Vec> lookOf = new HashMap<>();
    Vec look = new Vec(0, 0, 1);
    private final Predicate<int[]> solid;

    RecordingStage(Predicate<int[]> solid) {
        this.solid = solid;
    }

    @Override
    public Actor armorStand(Vec position, float yaw, StandStyle style) {
        FakeActor stand = add("ArmorStand", position);
        stand.style = style;
        return stand;
    }

    @Override
    public Actor mob(String type, Vec position, float yaw) {
        return add(type, position);
    }

    @Override
    public Actor fallingBlock(Vec position, Material material, int data) {
        return add("FallingBlock:" + material + ":" + data, position);
    }

    @Override
    public Actor item(Vec position, Material material, int data) {
        return add("Item:" + material, position);
    }

    @Override
    public Actor skullItem(Vec position, String texture) {
        return add("Skull:" + texture, position);
    }

    @Override
    public Actor firework(Vec position, List<Burst> bursts) {
        return add("Firework", position);
    }

    @Override
    public Actor snowball(Vec position, Vec velocity) {
        return add("Snowball", position);
    }

    @Override
    public boolean solid(int x, int y, int z) {
        return solid.test(new int[] {x, y, z});
    }

    @Override
    public void particle(String type, Vec position, int count, Vec spread, double speed, int... data) {
        particles.merge(type, 1, Integer::sum);
    }

    @Override
    public void sound(String name, Vec position, float volume, float pitch) {
        sounds.add(name);
        pitches.add(pitch);
    }

    @Override
    public void block(int x, int y, int z, Material material, int data) {
        blocks.add(x + "," + y + "," + z + "=" + material);
    }

    @Override
    public void time(long dayTime) {
        times.add(dayTime);
    }

    @Override
    public BlockType type(int x, int y, int z) {
        return types.apply(new int[] {x, y, z});
    }

    @Override
    public void blocks(List<BlockChange> changes) {
        bulk.addAll(changes);
    }

    @Override
    public Mount mount(Vec seat, float yaw, List<UUID> riders) {
        FakeMount mount = new FakeMount(seat);
        mount.riders.addAll(riders);
        mounts.add(mount);
        return mount;
    }

    @Override
    public void give(UUID player, int slot, Material material, String name, Consumer<Click> onClick) {
        tools.put(player, onClick);
    }

    @Override
    public void push(UUID player, Vec velocity) {
        pushes.add(velocity);
    }

    @Override
    public Optional<Vec> eyes(UUID player) {
        return Optional.of(eyesOf.getOrDefault(player, eyes));
    }

    @Override
    public Optional<Vec> look(UUID player) {
        return Optional.of(lookOf.getOrDefault(player, look));
    }

    @Override
    public Actor hook(Vec position, UUID owner) {
        return add("FishingHook", position);
    }

    @Override
    public Actor player(Vec position, float yaw) {
        return add("Player", position);
    }

    @Override
    public Actor player(Vec position, float yaw, SignedSkin skin) {
        skins.add(skin);
        return add("Player", position);
    }

    @Override
    public void lightning(Vec position) {
        sounds.add("lightning");
    }

    long alive() {
        return actors.stream().filter(actor -> !actor.removed()).count();
    }

    private FakeActor add(String type, Vec position) {
        FakeActor actor = new FakeActor(type, position);
        actors.add(actor);
        return actor;
    }

    static final class FakeActor implements Actor {

        final String type;
        Vec position;
        float yaw;
        boolean removed;
        StandStyle style;
        Pose pose;
        Actor target;
        Actor vehicle;
        final Map<Integer, Object> watched = new HashMap<>();
        final Map<Slot, String> skulls = new HashMap<>();
        final Map<Slot, Material> equipment = new HashMap<>();

        FakeActor(String type, Vec position) {
            this.type = type;
            this.position = position;
        }

        @Override
        public void teleport(Vec position, float yaw, float pitch) {
            this.position = position;
            this.yaw = yaw;
        }

        @Override
        public void velocity(Vec perTick) {
        }

        @Override
        public void equip(Slot slot, Material material, int data) {
            equipment.put(slot, material);
        }

        @Override
        public void equipSkull(Slot slot, String texture) {
            skulls.put(slot, texture);
        }

        @Override
        public void watch(int index, Object value) {
            watched.put(index, value);
        }

        @Override
        public void pose(Pose pose) {
            this.pose = pose;
        }

        @Override
        public void ride(Actor vehicle) {
            this.vehicle = vehicle;
        }

        @Override
        public void leash(Actor holder) {
            watched.put(-1, holder);
        }

        @Override
        public void target(Actor target) {
            this.target = target;
        }

        @Override
        public void status(int opcode) {
        }

        @Override
        public void remove() {
            removed = true;
        }

        @Override
        public boolean removed() {
            return removed;
        }
    }

    static final class FakeMount implements Mount {

        final Vec start;
        final List<UUID> riders = new ArrayList<>();
        Vec seat;
        float yaw;
        float pitch;
        boolean pushing;
        final List<Vec> path = new ArrayList<>();

        FakeMount(Vec seat) {
            this.start = seat;
            this.seat = seat;
        }

        @Override
        public Vec seat() {
            return seat;
        }

        @Override
        public float lookYaw() {
            return yaw;
        }

        @Override
        public float lookPitch() {
            return pitch;
        }

        @Override
        public boolean pushing() {
            return pushing;
        }

        @Override
        public void move(Vec seat, float yaw) {
            this.seat = seat;
            path.add(seat);
        }
    }
}
