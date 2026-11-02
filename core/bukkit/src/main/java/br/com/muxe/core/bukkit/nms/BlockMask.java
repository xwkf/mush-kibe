package br.com.muxe.core.bukkit.nms;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public final class BlockMask implements Listener {

    private static final String HANDLER_PREFIX = "muxe_block_mask_";
    private static final AtomicInteger NEXT_ID = new AtomicInteger();
    private static final String MINECRAFT_HANDLER = "packet_handler";

    private final Handles handles;
    private final MaskedBlocks masks = new MaskedBlocks();
    private final String handler;

    private BlockMask(Handles handles) {
        this.handles = handles;
        this.handler = handlerName(NEXT_ID.incrementAndGet());
    }

    static String handlerName(int id) {
        return HANDLER_PREFIX + id;
    }

    public static Optional<BlockMask> start(Plugin plugin) {
        Optional<BlockMask> mask = ServerInternals.resolve("Block masks").flatMap(Handles::resolve).map(BlockMask::new);
        mask.ifPresent(started -> {
            plugin.getServer().getPluginManager().registerEvents(started, plugin);
            plugin.getServer().getOnlinePlayers().forEach(started::inject);
        });
        return mask;
    }

    public void stop() {
        Bukkit.getOnlinePlayers().forEach(this::eject);
    }

    @SuppressWarnings("deprecation")
    public void hide(Player viewer, Location at, Material fake, byte data) {
        masks.mask(viewer.getUniqueId(), at.getBlockX(), at.getBlockY(), at.getBlockZ(), MaskedBlocks.combinedId(fake.getId(), data));
        viewer.sendBlockChange(at, fake, data);
    }

    @SuppressWarnings("deprecation")
    public void reveal(Player viewer, Location at) {
        masks.unmask(viewer.getUniqueId(), at.getBlockX(), at.getBlockY(), at.getBlockZ());
        Block real = at.getBlock();
        viewer.sendBlockChange(at, real.getType(), real.getData());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        inject(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        masks.clear(event.getPlayer().getUniqueId());
        eject(event.getPlayer());
    }

    private void inject(Player player) {
        handles.channel(player).ifPresent(channel -> channel.eventLoop().execute(() -> {
            ChannelPipeline pipeline = channel.pipeline();
            if (pipeline.get(handler) == null && pipeline.get(MINECRAFT_HANDLER) != null) {
                pipeline.addBefore(MINECRAFT_HANDLER, handler, new MaskHandler(player));
            }
        }));
    }

    private void eject(Player player) {
        handles.channel(player).ifPresent(channel -> channel.eventLoop().execute(() -> {
            if (channel.pipeline().get(handler) != null) {
                channel.pipeline().remove(handler);
            }
        }));
    }

    private final class MaskHandler extends ChannelDuplexHandler {

        private final Player player;

        private MaskHandler(Player player) {
            this.player = player;
        }

        @Override
        public void write(ChannelHandlerContext context, Object message, ChannelPromise promise) throws Exception {
            super.write(context, masked(message), promise);
        }

        private Object masked(Object message) {
            if (!handles.blockChange.isInstance(message) || !masks.any(player.getUniqueId())) {
                return message;
            }
            try {
                Object position = handles.position.get(message);
                int x = (int) handles.x.invoke(position);
                int y = (int) handles.y.invoke(position);
                int z = (int) handles.z.invoke(position);
                OptionalInt fake = masks.find(player.getUniqueId(), x, y, z);
                if (fake.isEmpty()) {
                    return message;
                }
                Object replacement = handles.emptyBlockChange.newInstance();
                handles.position.set(replacement, position);
                handles.block.set(replacement, handles.byCombinedId.invoke(null, fake.getAsInt()));
                return replacement;
            } catch (ReflectiveOperationException | RuntimeException exception) {
                Bukkit.getLogger().log(Level.FINE, "[Muxe] Could not mask a block change for " + player.getName(), exception);
                return message;
            }
        }
    }

    private static final class Handles {

        private final ServerInternals internals;
        private final Field networkManager;
        private final Field channel;
        private final Class<?> blockChange;
        private final Constructor<?> emptyBlockChange;
        private final Field position;
        private final Field block;
        private final Method x;
        private final Method y;
        private final Method z;
        private final Method byCombinedId;

        private Handles(ServerInternals internals) throws ReflectiveOperationException {
            this.internals = internals;
            Class<?> connection = internals.nms("PlayerConnection");
            Class<?> manager = internals.nms("NetworkManager");
            this.networkManager = ServerInternals.fieldOfType(connection, manager);
            this.channel = ServerInternals.fieldOfType(manager, Channel.class);
            this.blockChange = internals.nms("PacketPlayOutBlockChange");
            this.emptyBlockChange = blockChange.getConstructor();
            Class<?> blockPosition = internals.nms("BlockPosition");
            this.position = ServerInternals.fieldOfType(blockChange, blockPosition);
            this.block = ServerInternals.fieldOfType(blockChange, internals.nms("IBlockData"));
            this.x = blockPosition.getMethod("getX");
            this.y = blockPosition.getMethod("getY");
            this.z = blockPosition.getMethod("getZ");
            this.byCombinedId = internals.nms("Block").getMethod("getByCombinedId", int.class);
        }

        private static Optional<Handles> resolve(ServerInternals internals) {
            try {
                return Optional.of(new Handles(internals));
            } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
                Bukkit.getLogger().log(Level.WARNING, "[Muxe] Block masks are unavailable on this server: " + exception);
                return Optional.empty();
            }
        }

        private Optional<Channel> channel(Player player) {
            try {
                Object manager = networkManager.get(internals.connection(player));
                return Optional.ofNullable((Channel) channel.get(manager));
            } catch (ReflectiveOperationException | RuntimeException exception) {
                Bukkit.getLogger().log(Level.FINE, "[Muxe] Could not reach the connection of " + player.getName(), exception);
                return Optional.empty();
            }
        }
    }
}
