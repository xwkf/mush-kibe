package br.com.muxe.minigame.bedwars.cosmetic.trail.effect;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class StageCalls {

    final List<Emission> particles = new ArrayList<>();
    final List<String> others = new ArrayList<>();
    final Stage stage = (Stage) Proxy.newProxyInstance(Stage.class.getClassLoader(), new Class<?>[] {Stage.class}, (proxy, method, args) -> handle(method, args));

    private Object handle(Method method, Object[] args) {
        if (method.getDeclaringClass() == Object.class) {
            return switch (method.getName()) {
                case "equals" -> args[0] == stage;
                case "hashCode" -> System.identityHashCode(this);
                default -> "StageCalls";
            };
        }
        if (method.getName().equals("particle")) {
            int[] data = (int[]) args[5];
            particles.add(new Emission((String) args[0], (Vec) args[1], (int) args[2], (Vec) args[3], (double) args[4], Arrays.stream(data).boxed().toList()));
            return null;
        }
        others.add(method.getName());
        return nothing(method.getReturnType());
    }

    private static Object nothing(Class<?> type) {
        if (type == boolean.class) {
            return false;
        }
        if (type == int.class) {
            return 0;
        }
        return null;
    }

    record Emission(String type, Vec position, int count, Vec spread, double speed, List<Integer> data) {
    }
}
