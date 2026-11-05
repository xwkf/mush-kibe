package br.com.muxe.minigame.bedwars.cosmetic.animation;

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

final class TracedStage {

    final RecordingStage recording;
    final List<Call> calls = new ArrayList<>();
    final Timeline timeline = new Timeline();
    private final Stage traced;
    private int tick;

    TracedStage(Predicate<int[]> solid) {
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

    TracedStage play(BedAnimation animation, BedFrame bed, Vec breaker, long seed) {
        return play(animation, bed, breaker, seed, Integer.MAX_VALUE);
    }

    TracedStage play(BedAnimation animation, BedFrame bed, Vec breaker, long seed, int last) {
        tick = 0;
        animation.script(bed, breaker, traced, timeline, new Random(seed));
        for (tick = 0; tick <= Math.min(last, timeline.length()); tick++) {
            timeline.run(tick);
        }
        return this;
    }

    List<Call> calls(String method) {
        return calls.stream().filter(call -> call.method().equals(method)).toList();
    }

    List<Call> particles(String type) {
        return calls("particle").stream().filter(call -> call.args().getFirst().equals(type)).toList();
    }

    List<Call> sounds(String name) {
        return calls("sound").stream().filter(call -> call.args().getFirst().equals(name)).toList();
    }

    List<String> schedule() {
        return calls("sound").stream().map(call -> call.tick() + " " + call.args().getFirst()).toList();
    }

    record Call(int tick, String method, List<Object> args) {

        Vec at() {
            return (Vec) args.get(1);
        }

        String name() {
            return (String) args.getFirst();
        }

        float pitch() {
            return (float) args.get(3);
        }

        int pitchByte() {
            return Math.round(pitch() * 63);
        }

        int count() {
            return (int) args.get(2);
        }

        Vec spread() {
            return (Vec) args.get(3);
        }

        double speed() {
            return (double) args.get(4);
        }
    }
}
