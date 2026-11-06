package br.com.muxe.minigame.bedwars.cosmetic.trail;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

final class FakeTrail implements ProjectileTrail {

    static final Stage SILENT_STAGE = (Stage) Proxy.newProxyInstance(Stage.class.getClassLoader(), new Class<?>[] {Stage.class}, (proxy, method, arguments) -> null);

    final List<Flight> ticks = new ArrayList<>();
    final List<Flight> landings = new ArrayList<>();
    private final String id;
    private final int linger;
    private RuntimeException failure;

    FakeTrail(String id, int linger) {
        this.id = id;
        this.linger = linger;
    }

    FakeTrail failingWith(RuntimeException exception) {
        this.failure = exception;
        return this;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void tick(Flight flight, Stage stage, RandomGenerator random) {
        if (failure != null) {
            throw failure;
        }
        ticks.add(flight);
    }

    @Override
    public void land(Flight flight, Stage stage, RandomGenerator random) {
        landings.add(flight);
    }

    @Override
    public int linger() {
        return linger;
    }
}
