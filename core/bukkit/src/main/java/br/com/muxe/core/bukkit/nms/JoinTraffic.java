package br.com.muxe.core.bukkit.nms;

import br.com.muxe.core.common.metrics.MetricRegistry;
import br.com.muxe.core.common.metrics.instrument.Timer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public final class JoinTraffic implements Listener {

    public static final Duration WINDOW = Duration.ofSeconds(10);
    private static final String HANDLER = "muxe_join_traffic";
    private static final String BYTES_HANDLER = "muxe_join_bytes";
    private static final String MINECRAFT_HANDLER = "packet_handler";
    private static final long TICKS_PER_SECOND = 20L;

    private final Plugin plugin;
    private final Field networkManager;
    private final Field channel;
    private final ServerInternals internals;
    private final Timer readyTimer;
    private final Map<UUID, Counting> counting = new ConcurrentHashMap<>();
    private final Map<UUID, JoinReport> reports = new ConcurrentHashMap<>();

    private JoinTraffic(Plugin plugin, ServerInternals internals, MetricRegistry metrics) throws ReflectiveOperationException {
        this.plugin = plugin;
        this.internals = internals;
        this.networkManager = ServerInternals.fieldOfType(internals.nms("PlayerConnection"), internals.nms("NetworkManager"));
        this.channel = ServerInternals.fieldOfType(internals.nms("NetworkManager"), Channel.class);
        this.readyTimer = metrics.timer("muxe_player_join_ready_seconds");
    }

    public static Optional<JoinTraffic> start(Plugin plugin, MetricRegistry metrics) {
        return ServerInternals.resolve("Join traffic reports").flatMap(internals -> {
            try {
                JoinTraffic traffic = new JoinTraffic(plugin, internals, metrics);
                plugin.getServer().getPluginManager().registerEvents(traffic, plugin);
                return Optional.of(traffic);
            } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
                Bukkit.getLogger().log(Level.WARNING, "[Muxe] Join traffic reports are unavailable on this server: " + exception);
                return Optional.empty();
            }
        });
    }

    public Optional<JoinReport> report(UUID playerId) {
        Counting current = counting.get(playerId);
        return current != null ? Optional.of(current.report()) : Optional.ofNullable(reports.get(playerId));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Counting current = new Counting(System.nanoTime());
        counting.put(player.getUniqueId(), current);
        channel(player).ifPresent(found -> found.eventLoop().execute(() -> {
            if (found.pipeline().get(HANDLER) == null && found.pipeline().get(MINECRAFT_HANDLER) != null) {
                found.pipeline().addBefore(MINECRAFT_HANDLER, HANDLER, current);
                found.pipeline().addFirst(BYTES_HANDLER, current.wire());
            }
        }));
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> finish(player, current), WINDOW.toSeconds() * TICKS_PER_SECOND);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        counting.remove(id);
        reports.remove(id);
        eject(event.getPlayer());
    }

    private void finish(Player player, Counting current) {
        if (!counting.remove(player.getUniqueId(), current)) {
            return;
        }
        reports.put(player.getUniqueId(), current.report());
        eject(player);
    }

    private void eject(Player player) {
        channel(player).ifPresent(found -> found.eventLoop().execute(() -> {
            if (found.pipeline().get(HANDLER) != null) {
                found.pipeline().remove(HANDLER);
            }
            if (found.pipeline().get(BYTES_HANDLER) != null) {
                found.pipeline().remove(BYTES_HANDLER);
            }
        }));
    }

    private Optional<Channel> channel(Player player) {
        try {
            return Optional.ofNullable((Channel) channel.get(networkManager.get(internals.connection(player))));
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return Optional.empty();
        }
    }

    private final class Counting extends ChannelDuplexHandler {

        private final long joinedAt;
        private final Map<String, Integer> sent = new ConcurrentHashMap<>();
        private final AtomicLong bytes = new AtomicLong();
        private final AtomicLong slowestWrite = new AtomicLong();
        private volatile long readyNanos = -1;

        private Counting(long joinedAt) {
            this.joinedAt = joinedAt;
        }

        @Override
        public void write(ChannelHandlerContext context, Object message, ChannelPromise promise) throws Exception {
            sent.merge(JoinReport.shortName(message.getClass().getName()), 1, Integer::sum);
            super.write(context, message, promise);
        }

        @Override
        public void channelRead(ChannelHandlerContext context, Object message) throws Exception {
            if (readyNanos < 0 && JoinReport.isMovement(message.getClass().getName())) {
                readyNanos = System.nanoTime() - joinedAt;
                readyTimer.record(readyNanos);
            }
            super.channelRead(context, message);
        }

        private ChannelOutboundHandlerAdapter wire() {
            return new ChannelOutboundHandlerAdapter() {
                @Override
                public void write(ChannelHandlerContext context, Object message, ChannelPromise promise) throws Exception {
                    if (message instanceof ByteBuf buffer) {
                        bytes.addAndGet(buffer.readableBytes());
                        if (!promise.isVoid()) {
                            long queuedAt = System.nanoTime();
                            promise.addListener(done -> slowestWrite.accumulateAndGet(System.nanoTime() - queuedAt, Math::max));
                        }
                    }
                    super.write(context, message, promise);
                }
            };
        }

        private JoinReport report() {
            long ready = readyNanos;
            return new JoinReport(ready < 0 ? OptionalLong.empty() : OptionalLong.of(TimeUnit.NANOSECONDS.toMillis(ready)), sent,
                bytes.get(), TimeUnit.NANOSECONDS.toMillis(slowestWrite.get()));
        }
    }
}
