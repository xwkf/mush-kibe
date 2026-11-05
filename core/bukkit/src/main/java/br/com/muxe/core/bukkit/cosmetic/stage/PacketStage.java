package br.com.muxe.core.bukkit.cosmetic.stage;

import br.com.muxe.core.bukkit.cosmetic.animation.SignedSkin;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.core.bukkit.item.ItemBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.ChunkCoordIntPair;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.EntityFallingBlock;
import net.minecraft.server.v1_8_R3.EntityFireworks;
import net.minecraft.server.v1_8_R3.EntityFishingHook;
import net.minecraft.server.v1_8_R3.EntityItem;
import net.minecraft.server.v1_8_R3.EntityLightning;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.EntitySnowball;
import net.minecraft.server.v1_8_R3.EntityTypes;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutBlockChange;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_8_R3.PacketPlayOutMapChunk;
import net.minecraft.server.v1_8_R3.PacketPlayOutMultiBlockChange.MultiBlockChangeInfo;
import net.minecraft.server.v1_8_R3.PacketPlayOutMultiBlockChange;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedSoundEffect;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R3.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntity;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityWeather;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import net.minecraft.server.v1_8_R3.PlayerInteractManager;
import net.minecraft.server.v1_8_R3.Scoreboard;
import net.minecraft.server.v1_8_R3.ScoreboardTeam;
import net.minecraft.server.v1_8_R3.ScoreboardTeamBase.EnumNameTagVisibility;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

public final class PacketStage implements Stage, AutoCloseable {

    private static final int OBJECT_ITEM = 2;
    private static final int OBJECT_FALLING_BLOCK = 70;
    private static final int OBJECT_FIREWORK = 76;
    private static final int OBJECT_SNOWBALL = 61;
    private static final int OBJECT_FISHING_HOOK = 90;
    private static final int SKIN_LAYERS = 10;
    private static final int TAB_LIST_TICKS = 40;
    private static final String TEXTURES = "textures";
    private static final String HIDDEN_NAMES = "muxe_npc_";
    private static final AtomicInteger NEXT_TEAM = new AtomicInteger();
    private static final int TEAM_CREATE = 0;
    private static final int TEAM_REMOVE = 1;
    private static final int TEAM_JOIN = 3;
    private static final int WORLD_HEIGHT = 256;
    private static final int FULL_CHUNK = 65535;
    private static final Field MULTI_BLOCK_CHUNK = field(ChunkCoordIntPair.class);
    private static final Field MULTI_BLOCK_INFOS = field(MultiBlockChangeInfo[].class);

    private final WorldServer world;
    private final Supplier<Collection<Player>> viewers;
    private final List<PacketActor> actors = new ArrayList<>();
    private final Map<BlockPosition, Boolean> changed = new LinkedHashMap<>();
    private final List<Player> timed = new ArrayList<>();
    private final MountGuard guard;
    private final List<Seats> mounts = new ArrayList<>();
    private final Map<UUID, Tool> tools = new LinkedHashMap<>();
    private final Map<EntityPlayer, Integer> listed = new LinkedHashMap<>();
    private final Set<ChunkCoordIntPair> bulkChunks = new LinkedHashSet<>();
    private ScoreboardTeam hiddenNames;
    private boolean closed;

    public PacketStage(World world, Supplier<Collection<Player>> viewers, MountGuard guard) {
        this.world = ((CraftWorld) world).getHandle();
        this.viewers = viewers;
        this.guard = guard;
    }

    @Override
    public Actor armorStand(Vec position, float yaw, StandStyle style) {
        EntityArmorStand stand = new EntityArmorStand(world, position.x(), position.y(), position.z());
        stand.setLocation(position.x(), position.y(), position.z(), yaw, 0);
        stand.setInvisible(style.invisible());
        stand.setSmall(style.small());
        stand.setArms(style.arms());
        stand.setBasePlate(!style.basePlate());
        stand.setGravity(true);
        stand.n(style.marker());
        return living(stand, yaw);
    }

    @Override
    public Actor mob(String type, Vec position, float yaw) {
        Entity entity = Objects.requireNonNull(EntityTypes.createEntityByName(type, world), "Unknown entity type " + type);
        if (!(entity instanceof EntityLiving living)) {
            throw new IllegalArgumentException(type + " is not a living entity");
        }
        living.setLocation(position.x(), position.y(), position.z(), yaw, 0);
        return living(living, yaw);
    }

    @Override
    public Actor fallingBlock(Vec position, Material material, int data) {
        IBlockData block = state(material, data);
        EntityFallingBlock falling = new EntityFallingBlock(world, position.x(), position.y(), position.z(), block);
        return object(falling, new PacketPlayOutSpawnEntity(falling, OBJECT_FALLING_BLOCK, Block.getCombinedId(block)), false);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Actor item(Vec position, Material material, int data) {
        EntityItem item = new EntityItem(world, position.x(), position.y(), position.z(), CraftItemStack.asNMSCopy(new ItemStack(material, 1, (short) data)));
        return object(item, new PacketPlayOutSpawnEntity(item, OBJECT_ITEM, 1), true);
    }

    @Override
    public Actor skullItem(Vec position, String texture) {
        ItemStack skull = ItemBuilder.skull().skullTexture(texture).build();
        EntityItem item = new EntityItem(world, position.x(), position.y(), position.z(), CraftItemStack.asNMSCopy(skull));
        return object(item, new PacketPlayOutSpawnEntity(item, OBJECT_ITEM, 1), true);
    }

    @Override
    public Actor firework(Vec position, List<Burst> bursts) {
        ItemStack rocket = new ItemStack(Material.FIREWORK);
        FireworkMeta meta = (FireworkMeta) rocket.getItemMeta();
        for (Burst burst : bursts) {
            meta.addEffect(FireworkEffect.builder()
                .with(FireworkEffect.Type.values()[Math.clamp(burst.type(), 0, FireworkEffect.Type.values().length - 1)])
                .withColor(burst.colors().stream().map(Color::fromRGB).toList())
                .withFade(burst.fades().stream().map(Color::fromRGB).toList())
                .flicker(burst.flicker())
                .trail(burst.trail())
                .build());
        }
        meta.setPower(1);
        rocket.setItemMeta(meta);
        EntityFireworks firework = new EntityFireworks(world, position.x(), position.y(), position.z(), CraftItemStack.asNMSCopy(rocket));
        return object(firework, new PacketPlayOutSpawnEntity(firework, OBJECT_FIREWORK), true);
    }

    @Override
    public Actor snowball(Vec position, Vec velocity) {
        EntitySnowball snowball = new EntitySnowball(world, position.x(), position.y(), position.z());
        Actor actor = object(snowball, new PacketPlayOutSpawnEntity(snowball, OBJECT_SNOWBALL), false);
        actor.velocity(velocity);
        return actor;
    }

    @Override
    public boolean solid(int x, int y, int z) {
        return world.getType(new BlockPosition(x, y, z)).getBlock().getMaterial().isSolid();
    }

    @Override
    public void particle(String type, Vec position, int count, Vec spread, double speed, int... data) {
        send(new PacketPlayOutWorldParticles(EnumParticle.valueOf(type), true, (float) position.x(), (float) position.y(), (float) position.z(),
            (float) spread.x(), (float) spread.y(), (float) spread.z(), (float) speed, count, data));
    }

    @Override
    public void sound(String name, Vec position, float volume, float pitch) {
        send(new PacketPlayOutNamedSoundEffect(name, position.x(), position.y(), position.z(), volume, pitch));
    }

    @Override
    public void block(int x, int y, int z, Material material, int data) {
        BlockPosition position = new BlockPosition(x, y, z);
        changed.putIfAbsent(position, Boolean.TRUE);
        PacketPlayOutBlockChange packet = new PacketPlayOutBlockChange(world, position);
        packet.block = state(material, data);
        send(packet);
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockType type(int x, int y, int z) {
        if (y < 0 || y >= WORLD_HEIGHT || !world.getWorld().isChunkLoaded(x >> 4, z >> 4)) {
            return new BlockType(Material.AIR, 0);
        }
        IBlockData state = world.getType(new BlockPosition(x, y, z));
        Block block = state.getBlock();
        Material material = Material.getMaterial(Block.getId(block));
        return new BlockType(material == null ? Material.AIR : material, block.toLegacyData(state));
    }

    @Override
    public void blocks(List<BlockChange> changes) {
        Map<ChunkCoordIntPair, List<BlockChange>> byChunk = new LinkedHashMap<>();
        for (BlockChange change : changes) {
            if (change.y() >= 0 && change.y() < WORLD_HEIGHT && world.getWorld().isChunkLoaded(change.x() >> 4, change.z() >> 4)) {
                byChunk.computeIfAbsent(new ChunkCoordIntPair(change.x() >> 4, change.z() >> 4), key -> new ArrayList<>()).add(change);
            }
        }
        byChunk.forEach((chunk, inChunk) -> {
            bulkChunks.add(chunk);
            PacketPlayOutMultiBlockChange packet = new PacketPlayOutMultiBlockChange();
            MultiBlockChangeInfo[] infos = new MultiBlockChangeInfo[inChunk.size()];
            for (int index = 0; index < infos.length; index++) {
                BlockChange change = inChunk.get(index);
                short position = (short) ((change.x() & 15) << 12 | (change.z() & 15) << 8 | change.y());
                infos[index] = packet.new MultiBlockChangeInfo(position, state(change.material(), change.data()));
            }
            if (MULTI_BLOCK_CHUNK != null && MULTI_BLOCK_INFOS != null) {
                try {
                    MULTI_BLOCK_CHUNK.set(packet, chunk);
                    MULTI_BLOCK_INFOS.set(packet, infos);
                    send(packet);
                } catch (IllegalAccessException exception) {
                    inChunk.forEach(change -> block(change.x(), change.y(), change.z(), change.material(), change.data()));
                }
            } else {
                inChunk.forEach(change -> block(change.x(), change.y(), change.z(), change.material(), change.data()));
            }
        });
    }

    @Override
    public void time(long dayTime) {
        for (Player viewer : viewers.get()) {
            if (viewer.isOnline()) {
                viewer.setPlayerTime(dayTime, false);
                if (!timed.contains(viewer)) {
                    timed.add(viewer);
                }
            }
        }
    }

    @Override
    public Mount mount(Vec seat, float yaw, List<UUID> riders) {
        Seats mount = Seats.create(world.getWorld(), guard, this::send, seat, yaw, riders);
        mounts.add(mount);
        return mount;
    }

    @Override
    public void give(UUID player, int slot, Material material, String name, Consumer<Click> onClick) {
        Player holder = Bukkit.getPlayer(player);
        if (holder == null || !holder.isOnline()) {
            return;
        }
        ItemStack item = ItemBuilder.of(material).name(name).build();
        holder.getInventory().setItem(slot, item);
        holder.getInventory().setHeldItemSlot(slot);
        guard.tool(player, slot, onClick);
        tools.put(player, new Tool(slot, item));
    }

    @Override
    public void push(UUID player, Vec velocity) {
        Player pushed = Bukkit.getPlayer(player);
        if (pushed != null && pushed.isOnline()) {
            pushed.setFallDistance(0);
            pushed.setVelocity(new Vector(velocity.x(), velocity.y(), velocity.z()));
        }
    }

    @Override
    public Optional<Vec> eyes(UUID player) {
        return Optional.ofNullable(Bukkit.getPlayer(player)).filter(Player::isOnline)
            .map(Player::getEyeLocation).map(at -> new Vec(at.getX(), at.getY(), at.getZ()));
    }

    @Override
    public Optional<Vec> look(UUID player) {
        return Optional.ofNullable(Bukkit.getPlayer(player)).filter(Player::isOnline)
            .map(found -> found.getLocation().getDirection()).map(direction -> new Vec(direction.getX(), direction.getY(), direction.getZ()));
    }

    @Override
    public Actor hook(Vec position, UUID owner) {
        Player holder = Bukkit.getPlayer(owner);
        EntityFishingHook hook = new EntityFishingHook(world);
        hook.setPosition(position.x(), position.y(), position.z());
        int ownerId = holder == null ? 0 : holder.getEntityId();
        return object(hook, new PacketPlayOutSpawnEntity(hook, OBJECT_FISHING_HOOK, ownerId), false);
    }

    @Override
    public Actor player(Vec position, float yaw) {
        Set<UUID> watching = viewers.get().stream().map(Player::getUniqueId).collect(Collectors.toSet());
        List<UUID> online = Bukkit.getOnlinePlayers().stream()
            .filter(other -> !((CraftPlayer) other).getProfile().getProperties().get(TEXTURES).isEmpty())
            .map(Player::getUniqueId).toList();
        Optional<Property> skin = FakePlayers.skinOwner(online, watching, ThreadLocalRandom.current()).map(Bukkit::getPlayer)
            .map(owner -> ((CraftPlayer) owner).getProfile().getProperties().get(TEXTURES).iterator().next());
        return fakePlayer(position, yaw, skin);
    }

    @Override
    public Actor player(Vec position, float yaw, SignedSkin skin) {
        String signature = skin.signature() != null ? skin.signature() : FakePlayers.signature(skin.value(), onlineSkins()).orElse(null);
        return fakePlayer(position, yaw, Optional.of(new Property(TEXTURES, skin.value(), signature)));
    }

    private List<SignedSkin> onlineSkins() {
        return Bukkit.getOnlinePlayers().stream()
            .flatMap(online -> ((CraftPlayer) online).getProfile().getProperties().get(TEXTURES).stream())
            .map(texture -> new SignedSkin(texture.getValue(), texture.getSignature()))
            .toList();
    }

    private Actor fakePlayer(Vec position, float yaw, Optional<Property> skin) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), FakePlayers.name(ThreadLocalRandom.current()));
        skin.ifPresent(texture -> profile.getProperties().put(TEXTURES, texture));
        hideName(profile.getName());
        EntityPlayer body = new EntityPlayer(MinecraftServer.getServer(), world, profile, new PlayerInteractManager(world));
        body.setLocation(position.x(), position.y(), position.z(), yaw, 0);
        body.getDataWatcher().watch(SKIN_LAYERS, (byte) 127);
        send(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, body));
        PacketActor actor = new PacketActor(this, body);
        send(new PacketPlayOutNamedEntitySpawn(body));
        actor.headYaw(yaw);
        actors.add(actor);
        listed.put(body, TAB_LIST_TICKS);
        return actor;
    }

    private void hideName(String name) {
        if (hiddenNames == null) {
            hiddenNames = new ScoreboardTeam(new Scoreboard(), HIDDEN_NAMES + NEXT_TEAM.incrementAndGet());
            hiddenNames.setNameTagVisibility(EnumNameTagVisibility.NEVER);
            send(new PacketPlayOutScoreboardTeam(hiddenNames, TEAM_CREATE));
        }
        hiddenNames.getPlayerNameSet().add(name);
        send(new PacketPlayOutScoreboardTeam(hiddenNames, List.of(name), TEAM_JOIN));
    }

    @Override
    public void lightning(Vec position) {
        send(new PacketPlayOutSpawnEntityWeather(new EntityLightning(world, position.x(), position.y(), position.z(), true)));
        sound("ambient.weather.thunder", position, 2, 1);
    }

    void tick() {
        listed.entrySet().removeIf(entry -> {
            int left = entry.getValue() - 1;
            if (left > 0) {
                entry.setValue(left);
                return false;
            }
            send(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entry.getKey()));
            return true;
        });
    }

    public boolean isClosed() {
        return closed;
    }

    public boolean watched() {
        return viewers.get().stream().anyMatch(viewer -> viewer.isOnline() && viewer.getWorld().equals(world.getWorld()));
    }

    public void dismount() {
        mounts.forEach(Seats::remove);
        mounts.clear();
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        for (PacketActor actor : actors) {
            actor.remove();
        }
        for (BlockPosition position : changed.keySet()) {
            sendInWorld(new PacketPlayOutBlockChange(world, position));
        }
        for (ChunkCoordIntPair chunk : bulkChunks) {
            if (world.getWorld().isChunkLoaded(chunk.x, chunk.z)) {
                sendInWorld(new PacketPlayOutMapChunk(world.getChunkAt(chunk.x, chunk.z), true, FULL_CHUNK));
            }
        }
        bulkChunks.clear();
        listed.keySet().forEach(body -> send(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, body)));
        listed.clear();
        if (hiddenNames != null) {
            send(new PacketPlayOutScoreboardTeam(hiddenNames, TEAM_REMOVE));
            hiddenNames = null;
        }
        dismount();
        tools.forEach((player, tool) -> {
            guard.dropTool(player);
            Player holder = Bukkit.getPlayer(player);
            if (holder != null && holder.isOnline() && tool.item().equals(holder.getInventory().getItem(tool.slot()))) {
                holder.getInventory().setItem(tool.slot(), null);
            }
        });
        tools.clear();
        for (Player viewer : timed) {
            if (viewer.isOnline()) {
                viewer.resetPlayerTime();
            }
        }
        actors.clear();
        changed.clear();
        timed.clear();
    }

    void send(Packet<?> packet) {
        for (Player viewer : viewers.get()) {
            if (viewer.isOnline()) {
                ((CraftPlayer) viewer).getHandle().playerConnection.sendPacket(packet);
            }
        }
    }

    private void sendInWorld(Packet<?> packet) {
        for (Player viewer : viewers.get()) {
            if (viewer.isOnline() && viewer.getWorld().equals(world.getWorld())) {
                ((CraftPlayer) viewer).getHandle().playerConnection.sendPacket(packet);
            }
        }
    }

    private record Tool(int slot, ItemStack item) {
    }

    private Actor living(EntityLiving entity, float yaw) {
        PacketActor actor = new PacketActor(this, entity);
        send(new PacketPlayOutSpawnEntityLiving(entity));
        actor.headYaw(yaw);
        actors.add(actor);
        return actor;
    }

    private Actor object(Entity entity, Packet<?> spawn, boolean metadata) {
        PacketActor actor = new PacketActor(this, entity);
        send(spawn);
        if (metadata) {
            send(new PacketPlayOutEntityMetadata(entity.getId(), entity.getDataWatcher(), true));
        }
        actors.add(actor);
        return actor;
    }

    private static Field field(Class<?> type) {
        for (Field candidate : PacketPlayOutMultiBlockChange.class.getDeclaredFields()) {
            if (candidate.getType() == type) {
                candidate.setAccessible(true);
                return candidate;
            }
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    private static IBlockData state(Material material, int data) {
        return Block.getById(material.getId()).fromLegacyData(data);
    }
}
