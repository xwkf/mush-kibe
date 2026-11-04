package br.com.muxe.core.bukkit.cosmetic.stage;

import br.com.muxe.core.bukkit.cosmetic.animation.BedAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.BedFrame;
import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public final class AnimationPlayer {

    private final Plugin plugin;

    public AnimationPlayer(Plugin plugin) {
        this.plugin = plugin;
    }

    public Playback play(BedAnimation animation, World world, BedFrame bed, Vec breaker, Supplier<Collection<Player>> viewers, Runnable onFinish) {
        return run(animation.id(), world, viewers, onFinish,
            (stage, timeline) -> animation.script(bed, breaker, stage, timeline, ThreadLocalRandom.current()));
    }

    public Playback play(SceneAnimation animation, World world, Scene scene, Supplier<Collection<Player>> viewers, Runnable onFinish) {
        return run(animation.id(), world, viewers, onFinish,
            (stage, timeline) -> animation.script(scene, stage, timeline, ThreadLocalRandom.current()));
    }

    private Playback run(String id, World world, Supplier<Collection<Player>> viewers, Runnable onFinish, BiConsumer<PacketStage, Timeline> script) {
        PacketStage stage = new PacketStage(world, viewers, MountGuard.of(plugin));
        Timeline timeline = new Timeline();
        script.accept(stage, timeline);
        Playback playback = new Playback(stage, new PlaybackLifecycle(timeline.length()));
        playback.task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            try {
                int tick = playback.lifecycle.tick();
                switch (playback.lifecycle.next(stage.watched())) {
                    case PLAY -> {
                        stage.tick();
                        timeline.run(tick);
                    }
                    case FINISH -> {
                        stage.tick();
                        onFinish.run();
                    }
                    case HOLD -> stage.tick();
                    case CLOSE, DONE -> playback.close();
                }
            } catch (RuntimeException exception) {
                if (playback.lifecycle.stopped() || playback.lifecycle.finished()) {
                    playback.close();
                    return;
                }
                plugin.getLogger().log(Level.WARNING, "Animation " + id + " failed", exception);
                playback.lifecycle.stop();
                onFinish.run();
            }
        }, 0L, 1L);
        return playback;
    }

    public static final class Playback {

        private final PacketStage stage;
        private final PlaybackLifecycle lifecycle;
        private BukkitTask task;

        private Playback(PacketStage stage, PlaybackLifecycle lifecycle) {
            this.stage = stage;
            this.lifecycle = lifecycle;
        }

        public boolean finished() {
            return lifecycle.finished() || lifecycle.stopped() || lifecycle.closed();
        }

        public void cancel() {
            lifecycle.stop();
            stage.dismount();
        }

        private void close() {
            lifecycle.close();
            task.cancel();
            stage.close();
        }
    }
}
