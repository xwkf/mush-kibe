package br.com.muxe.minigame.engine.world.server;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.World;
import org.junit.jupiter.api.Test;

class MatchWorldRulesTest {

    private final List<String> calls = new ArrayList<>();
    private final World world = (World) Proxy.newProxyInstance(World.class.getClassLoader(), new Class<?>[] {World.class}, (proxy, method, args) -> {
        calls.add(method.getName() + (args == null ? "" : List.of(args)));
        return method.getReturnType() == boolean.class ? false : null;
    });

    @Test
    void playersCanHitEachOtherInAMatchWorldEvenWhenTheServerTurnsPvpOff() {
        MatchWorldRules.apply(world);

        assertThat(calls).contains("setPVP[true]");
    }

    @Test
    void theMatchWorldKeepsClearMiddayWithoutMobsOrFireSpread() {
        MatchWorldRules.apply(world);

        assertThat(calls).contains("setStorm[false]", "setThundering[false]", "setTime[6000]", "setGameRuleValue[doDaylightCycle, false]",
            "setGameRuleValue[doMobSpawning, false]", "setGameRuleValue[doFireTick, false]", "setAutoSave[false]");
    }
}
