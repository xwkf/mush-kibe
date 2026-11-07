package br.com.muxe.minigame.api.cosmetic.trail.effect;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Material;

final class TrailRecorder {

    final List<Puff> particles = new ArrayList<>();
    final List<Item> items = new ArrayList<>();
    final List<String> others = new ArrayList<>();
    final Stage stage = (Stage) Proxy.newProxyInstance(Stage.class.getClassLoader(), new Class<?>[] {Stage.class}, (proxy, method, args) -> handle(proxy, method, args));

    private Object handle(Object proxy, Method method, Object[] args) {
        if (method.getDeclaringClass() == Object.class) {
            return switch (method.getName()) {
                case "equals" -> proxy == args[0];
                case "hashCode" -> System.identityHashCode(proxy);
                default -> "TrailRecorder";
            };
        }
        return switch (method.getName()) {
            case "particle" -> {
                particles.add(new Puff((String) args[0], (Vec) args[1], (int) args[2], (Vec) args[3], (double) args[4], Arrays.stream((int[]) args[5]).boxed().toList()));
                yield null;
            }
            case "item" -> {
                Item item = new Item((Vec) args[0], (Material) args[1], (int) args[2]);
                items.add(item);
                yield item;
            }
            default -> {
                others.add(method.getName());
                yield null;
            }
        };
    }

    List<Item> alive() {
        return items.stream().filter(item -> !item.removed()).toList();
    }

    record Puff(String type, Vec position, int count, Vec spread, double speed, List<Integer> data) {
    }

    static final class Item implements Actor {

        final Vec position;
        final Material material;
        final int data;
        final List<Vec> velocities = new ArrayList<>();
        int removals;

        Item(Vec position, Material material, int data) {
            this.position = position;
            this.material = material;
            this.data = data;
        }

        @Override
        public void teleport(Vec position, float yaw, float pitch) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void velocity(Vec perTick) {
            velocities.add(perTick);
        }

        @Override
        public void equip(Stage.Slot slot, Material material, int data) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void equipSkull(Stage.Slot slot, String texture) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void watch(int index, Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void pose(Stage.Pose pose) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void ride(Actor vehicle) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void leash(Actor holder) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void target(Actor target) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void status(int opcode) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void remove() {
            removals++;
        }

        @Override
        public boolean removed() {
            return removals > 0;
        }
    }
}
