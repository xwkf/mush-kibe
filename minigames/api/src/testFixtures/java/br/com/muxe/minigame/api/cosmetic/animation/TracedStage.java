package br.com.muxe.minigame.api.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.BedAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.BedFrame;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public final class TracedStage {

    public final RecordingStage recording;
    public final List<Call> calls = new ArrayList<>();
    public final Timeline timeline = new Timeline();
    private final Stage traced;
    private int tick;

    public TracedStage(Predicate<int[]> solid) {
        recording = new RecordingStage(solid);
        traced = (Stage) Proxy.newProxyInstance(Stage.class.getClassLoader(), new Class<?>[] {Stage.class}, (proxy, method, args) -> {
            calls.add(new Call(tick, method.getName(), args == null ? List.of() : Arrays.asList(args)));
            try {
                return method.invoke(recording, args);
            } catch (InvocationTargetException exception) {
                throw exception.getCause();
            }
        });
    }

    public TracedStage play(BedAnimation animation, BedFrame bed, Vec breaker, long seed) {
        return play(animation, bed, breaker, seed, Integer.MAX_VALUE);
    }

    public TracedStage play(BedAnimation animation, BedFrame bed, Vec breaker, long seed, int last) {
        tick = 0;
        animation.script(bed, breaker, traced, timeline, new Random(seed));
        for (tick = 0; tick <= Math.min(last, timeline.length()); tick++) {
            timeline.run(tick);
        }
        return this;
    }

    public List<Call> calls(String method) {
        return calls.stream().filter(call -> call.method().equals(method)).toList();
    }

    public List<Call> particles(String type) {
        return calls("particle").stream().filter(call -> call.args().getFirst().equals(type)).toList();
    }

    public List<Call> sounds(String name) {
        return calls("sound").stream().filter(call -> call.args().getFirst().equals(name)).toList();
    }

    public List<String> schedule() {
        return calls("sound").stream().map(call -> call.tick() + " " + call.args().getFirst()).toList();
    }

    public record Call(int tick, String method, List<Object> args) {

        public Vec at() {
            return (Vec) args.get(1);
        }

        public String name() {
            return (String) args.getFirst();
        }

        public float pitch() {
            return (float) args.get(3);
        }

        public int pitchByte() {
            return Math.round(pitch() * 63);
        }

        public int count() {
            return (int) args.get(2);
        }

        public Vec spread() {
            return (Vec) args.get(3);
        }

        double speed() {
            return (double) args.get(4);
        }
    }
}
