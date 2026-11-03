package br.com.muxe.core.bukkit.cosmetic.animation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.Material;

public interface Stage {

    Actor armorStand(Vec position, float yaw, StandStyle style);

    Actor mob(String type, Vec position, float yaw);

    Actor fallingBlock(Vec position, Material material, int data);

    Actor item(Vec position, Material material, int data);

    Actor skullItem(Vec position, String texture);

    Actor firework(Vec position, List<Burst> bursts);

    Actor snowball(Vec position, Vec velocity);

    boolean solid(int x, int y, int z);

    void particle(String type, Vec position, int count, Vec spread, double speed, int... data);

    void sound(String name, Vec position, float volume, float pitch);

    void block(int x, int y, int z, Material material, int data);

    void time(long dayTime);

    Mount mount(Vec seat, float yaw, List<UUID> riders);

    void give(UUID player, int slot, Material material, String name, Consumer<Click> onClick);

    void push(UUID player, Vec velocity);

    Optional<Vec> eyes(UUID player);

    Optional<Vec> look(UUID player);

    Actor hook(Vec position, UUID owner);

    Actor player(Vec position, float yaw, String name);

    void lightning(Vec position);

    interface Actor {

        void teleport(Vec position, float yaw, float pitch);

        void velocity(Vec perTick);

        void equip(Slot slot, Material material, int data);

        void equipSkull(Slot slot, String texture);

        void watch(int index, Object value);

        void pose(Pose pose);

        void ride(Actor vehicle);

        void leash(Actor holder);

        void status(int opcode);

        void remove();

        boolean removed();
    }

    interface Mount {

        Vec seat();

        float lookYaw();

        float lookPitch();

        boolean pushing();

        void move(Vec seat, float yaw);
    }

    enum Click {
        LEFT,
        RIGHT
    }

    enum Slot {
        HAND,
        BOOTS,
        LEGGINGS,
        CHESTPLATE,
        HELMET
    }

    record StandStyle(boolean invisible, boolean small, boolean arms, boolean basePlate, boolean marker) {

        public static final StandStyle HIDDEN = new StandStyle(true, false, false, false, true);
        public static final StandStyle HIDDEN_SMALL = new StandStyle(true, true, false, false, true);
        public static final StandStyle VISIBLE_ARMS = new StandStyle(false, false, true, false, false);
    }

    record Pose(Vec head, Vec body, Vec leftArm, Vec rightArm, Vec leftLeg, Vec rightLeg) {

        public static final Pose STANDING = new Pose(Vec.ZERO, Vec.ZERO, new Vec(-10, 0, -10), new Vec(-15, 0, 10), new Vec(-1, 0, -1), new Vec(1, 0, 1));

        public Pose withHead(Vec value) {
            return new Pose(value, body, leftArm, rightArm, leftLeg, rightLeg);
        }

        public Pose withRightArm(Vec value) {
            return new Pose(head, body, leftArm, value, leftLeg, rightLeg);
        }
    }

    record Burst(int type, List<Integer> colors, List<Integer> fades, boolean flicker, boolean trail) {

        public Burst {
            colors = List.copyOf(colors);
            fades = List.copyOf(fades);
        }
    }
}
