package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.BedAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.BedFrame;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.api.cosmetic.animation.Kit;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

public final class AbandonedRoomAnimation implements BedAnimation {

    static final int BATS = 16;
    static final int LEAVE = 55;
    static final double SQUEAK_CHANCE = 0.3;
    static final double MIN_SQUEAK = 0.78;
    static final double MAX_SQUEAK = 0.97;

    @Override
    public String id() {
        return "quarto-abandonado";
    }

    @Override
    public void script(BedFrame bed, Vec breaker, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec middle = bed.middle();
        List<BatFlight> flights = new ArrayList<>();
        List<Actor> bats = new ArrayList<>();
        for (int index = 0; index < BATS; index++) {
            flights.add(new BatFlight(middle, random.nextDouble() * 360));
            bats.add(stage.mob("Bat", middle, 0));
        }
        timeline.at(0, () -> {
            stage.particle("SMOKE_LARGE", middle, 60, new Vec(1, 1, 1), 0);
            for (int index = 0; index < BATS; index++) {
                stage.sound("mob.bat.takeoff", bed.head(), 0.05f, (float) Kit.between(random, 0.9, 1.1));
            }
        });
        timeline.repeat(1, LEAVE - 1, 1, tick -> {
            for (int index = 0; index < BATS; index++) {
                BatFlight flight = flights.get(index);
                bats.get(index).teleport(flight.step(random), flight.yaw(), 0);
            }
            if (random.nextDouble() < SQUEAK_CHANCE) {
                Vec squeaker = flights.get(random.nextInt(BATS)).position();
                stage.sound("mob.bat.idle", squeaker, 0.1f, (float) Kit.between(random, MIN_SQUEAK, MAX_SQUEAK));
            }
        });
        timeline.at(LEAVE, () -> bats.forEach(Actor::remove));
    }
}
