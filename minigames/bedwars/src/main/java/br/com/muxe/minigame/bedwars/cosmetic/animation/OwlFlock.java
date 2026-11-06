package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

final class OwlFlock {

    static final int SIZE = 12;
    static final int FIRST = 1;
    static final int GATHER_EVERY = 4;
    static final double SPAWN_RADIUS = 12.5;
    static final double LOWEST_ABOVE = 0.75;
    static final double HIGHEST_ABOVE = 6.75;
    static final int MIN_LIFE = 50;
    static final int MAX_LIFE = 120;
    static final double PUFF_ABOVE = 3.125;
    static final Vec CLOUD_SPREAD = new Vec(0.4, 0.4, 0.4);
    static final int CLOUDS = 15;
    static final double CLOUD_SPEED = 0.05;
    static final int SPARKS = 8;
    static final double SPARK_SPEED = 0.1;

    private final Stage stage;
    private final RandomGenerator random;
    private final List<Member> members = new ArrayList<>();
    private int gathered;

    OwlFlock(Stage stage, RandomGenerator random) {
        this.stage = stage;
        this.random = random;
    }

    void step(Vec around, int tick) {
        for (int index = 0; index < members.size(); index++) {
            Member member = members.get(index);
            member.life--;
            if (member.life <= 0) {
                puff(stage, member.glide.position());
                member.owl.remove();
                members.set(index, spawn(around));
                continue;
            }
            Vec position = member.glide.step(random);
            member.owl.teleport(position, member.glide.yaw(), 0);
        }
        if (gathered < SIZE && tick >= FIRST + gathered * GATHER_EVERY) {
            members.add(spawn(around));
            gathered++;
        }
    }

    void remove() {
        members.forEach(member -> member.owl.remove());
        members.clear();
    }

    List<Vec> positions() {
        return members.stream().map(member -> member.glide.position()).toList();
    }

    static Vec spot(Vec around, RandomGenerator random) {
        double radius = SPAWN_RADIUS * Math.sqrt(random.nextDouble());
        double above = LOWEST_ABOVE + random.nextDouble() * (HIGHEST_ABOVE - LOWEST_ABOVE);
        return Kit.horizontal(random.nextDouble() * 360, radius).add(around.x(), around.y() + above, around.z());
    }

    static void puff(Stage stage, Vec owl) {
        Vec above = owl.add(0, PUFF_ABOVE, 0);
        stage.particle("CLOUD", above, CLOUDS, CLOUD_SPREAD, CLOUD_SPEED);
        stage.particle("FIREWORKS_SPARK", above, SPARKS, Vec.ZERO, SPARK_SPEED);
    }

    private Member spawn(Vec around) {
        Vec home = spot(around, random);
        Actor owl = stage.armorStand(home, (float) (random.nextDouble() * 360), DuskAnimation.OWL_STAND);
        owl.equipSkull(Slot.HELMET, DuskAnimation.OWL);
        puff(stage, home);
        return new Member(owl, new OwlGlide(home), random.nextInt(MIN_LIFE, MAX_LIFE + 1));
    }

    private static final class Member {

        private final Actor owl;
        private final OwlGlide glide;
        private int life;

        Member(Actor owl, OwlGlide glide, int life) {
            this.owl = owl;
            this.glide = glide;
            this.life = life;
        }
    }
}
