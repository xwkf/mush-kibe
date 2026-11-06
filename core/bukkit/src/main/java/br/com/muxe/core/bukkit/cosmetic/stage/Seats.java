package br.com.muxe.core.bukkit.cosmetic.stage;

import br.com.muxe.core.bukkit.cosmetic.animation.Ground;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Mount;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

final class Seats implements Mount {

    static final double SPACING = 1.1;

    private final World world;
    private final MountGuard guard;
    private final Consumer<Packet<?>> broadcast;
    private final List<Seat> seats = new ArrayList<>();
    private Vec seat;
    private float yaw;

    private Seats(World world, MountGuard guard, Consumer<Packet<?>> broadcast, Vec seat, float yaw) {
        this.world = world;
        this.guard = guard;
        this.broadcast = broadcast;
        this.seat = seat;
        this.yaw = yaw;
    }

    static Seats create(World world, MountGuard guard, Consumer<Packet<?>> broadcast, Vec seat, float yaw, List<UUID> riders) {
        Seats mount = new Seats(world, guard, broadcast, seat, yaw);
        for (UUID rider : riders) {
            Player player = Bukkit.getPlayer(rider);
            if (player == null || !player.isOnline() || !player.getWorld().equals(world)) {
                continue;
            }
            Location at = mount.location(mount.seats.size());
            ArmorStand stand = world.spawn(at, ArmorStand.class);
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setMarker(true);
            stand.setBasePlate(false);
            guard.protect(stand);
            player.teleport(at);
            stand.setPassenger(player);
            mount.seats.add(new Seat(player, stand));
        }
        return mount;
    }

    @Override
    public Vec seat() {
        return seat;
    }

    @Override
    public float lookYaw() {
        return leader().map(player -> player.getLocation().getYaw()).orElse(yaw);
    }

    @Override
    public float lookPitch() {
        return leader().map(player -> player.getLocation().getPitch()).orElse(0f);
    }

    @Override
    public boolean pushing() {
        return leader().map(player -> ((CraftPlayer) player).getHandle().ba > 0).orElse(false);
    }

    @Override
    public float strafing() {
        return leader().map(player -> ((CraftPlayer) player).getHandle().aZ).orElse(0f);
    }

    @Override
    public void move(Vec seat, float yaw) {
        this.seat = seat;
        this.yaw = yaw;
        for (int index = 0; index < seats.size(); index++) {
            Seat place = seats.get(index);
            if (place.stand().isDead()) {
                continue;
            }
            Location at = location(index);
            EntityArmorStand handle = ((CraftArmorStand) place.stand()).getHandle();
            handle.setPositionRotation(at.getX(), at.getY(), at.getZ(), at.getYaw(), 0);
            broadcast.accept(new PacketPlayOutEntityTeleport(handle));
            if (place.stand().getPassenger() == null && place.rider().isOnline() && place.rider().getWorld().equals(world)) {
                place.stand().setPassenger(place.rider());
            }
        }
    }

    void remove() {
        for (Seat place : seats) {
            Location at = place.stand().getLocation();
            place.stand().eject();
            guard.release(place.stand());
            place.stand().remove();
            Player rider = place.rider();
            if (rider.isOnline() && rider.getWorld().equals(world)) {
                double ground = Ground.surface((x, y, z) -> world.getBlockAt(x, y, z).getType().isSolid(), at.getX(), at.getY(), at.getZ());
                rider.teleport(new Location(world, at.getX(), ground, at.getZ(), rider.getLocation().getYaw(), rider.getLocation().getPitch()));
                rider.setFallDistance(0);
            }
        }
        seats.clear();
    }

    private Optional<Player> leader() {
        return seats.isEmpty() ? Optional.empty() : Optional.of(seats.getFirst().rider()).filter(Player::isOnline);
    }

    private Location location(int index) {
        double radians = Math.toRadians(yaw);
        double back = index * SPACING;
        return new Location(world, seat.x() + Math.sin(radians) * back, seat.y(), seat.z() - Math.cos(radians) * back, yaw, 0);
    }

    private record Seat(Player rider, ArmorStand stand) {
    }
}
