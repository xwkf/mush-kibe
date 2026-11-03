package br.com.muxe.core.bukkit.cosmetic.stage;

import br.com.muxe.core.bukkit.cosmetic.animation.BedAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.BedFrame;
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

    public Playback play(SceneAnimation animation, World world, Vec origin, Vec facing, Supplier<Collection<Player>> viewers, Runnable onFinish) {
        return run(animation.id(), world, viewers, onFinish,
            (stage, timeline) -> animation.script(origin, facing, stage, timeline, ThreadLocalRandom.current()));
    }

    private Playback run(String id, World world, Supplier<Collection<Player>> viewers, Runnable onFinish, BiConsumer<PacketStage, Timeline> script) {
        PacketStage stage = new PacketStage(world, viewers);
        Timeline timeline = new Timeline();
        script.accept(stage, timeline);
        Playback playback = new Playback(stage, onFinish);
        playback.task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            try {
                timeline.run(playback.tick);
            } catch (RuntimeException exception) {
                plugin.getLogger().log(Level.WARNING, "Animation " + id + " failed", exception);
                playback.finish();
                return;
            }
            playback.tick++;
            if (playback.tick > timeline.length()) {
                playback.finish();
            }
        }, 0L, 1L);
        return playback;
    }

    public static final class Playback {

        private final PacketStage stage;
        private final Runnable onFinish;
        private BukkitTask task;
        private int tick;
        private boolean finished;

        private Playback(PacketStage stage, Runnable onFinish) {
            this.stage = stage;
            this.onFinish = onFinish;
        }

        public boolean finished() {
            return finished;
        }

        public void cancel() {
            if (!finished) {
                finished = true;
                task.cancel();
                stage.close();
            }
        }

        private void finish() {
            if (!finished) {
                finished = true;
                task.cancel();
                stage.close();
                onFinish.run();
            }
        }
    }
}
