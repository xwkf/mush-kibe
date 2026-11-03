package br.com.muxe.core.bukkit.world;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.World;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.junit.jupiter.api.Test;

class ClearMiddayTest {

    private final Map<String, Object> calls = new LinkedHashMap<>();
    private final World world = (World) Proxy.newProxyInstance(World.class.getClassLoader(), new Class<?>[] {World.class}, (proxy, method, args) -> {
        if (method.getName().equals("setGameRuleValue")) {
            calls.put(String.valueOf(args[0]), args[1]);
            return true;
        }
        if (method.getName().startsWith("set")) {
            calls.put(method.getName(), args[0]);
        }
        return method.getReturnType() == boolean.class ? false : null;
    });
    private final ClearMidday climate = new ClearMidday();

    @Test
    void everyWorldStaysAtMiddayWithoutRain() {
        ClearMidday.apply(world);

        assertThat(calls).containsEntry("doDaylightCycle", "false")
            .containsEntry("setTime", 6000L)
            .containsEntry("setStorm", false)
            .containsEntry("setThundering", false);
    }

    @Test
    void rainAndThunderNeverStart() {
        WeatherChangeEvent rain = new WeatherChangeEvent(world, true);
        ThunderChangeEvent thunder = new ThunderChangeEvent(world, true);

        climate.onWeatherChange(rain);
        climate.onThunderChange(thunder);

        assertThat(rain.isCancelled()).isTrue();
        assertThat(thunder.isCancelled()).isTrue();
    }

    @Test
    void theSkyCanStillClearUp() {
        WeatherChangeEvent clear = new WeatherChangeEvent(world, false);
        ThunderChangeEvent calm = new ThunderChangeEvent(world, false);

        climate.onWeatherChange(clear);
        climate.onThunderChange(calm);

        assertThat(clear.isCancelled()).isFalse();
        assertThat(calm.isCancelled()).isFalse();
    }
}
