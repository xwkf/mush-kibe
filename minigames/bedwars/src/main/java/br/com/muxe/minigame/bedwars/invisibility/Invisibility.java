package br.com.muxe.minigame.bedwars.invisibility;

import br.com.muxe.minigame.engine.team.TeamColor;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class Invisibility {

    public static final double STEP_SQUARED = 0.64;

    private Invisibility() {
    }

    public static boolean hidesArmor(UUID viewer, Optional<TeamColor> viewerTeam, UUID target, TeamColor targetTeam) {
        return !viewer.equals(target) && viewerTeam.filter(targetTeam::equals).isEmpty();
    }

    public static boolean leavesFootstep(double movedSquared, boolean onGround) {
        return onGround && movedSquared >= STEP_SQUARED;
    }

    public static PotionEffect potionEffect(PotionEffectType type, int ticks, int amplifier) {
        return new PotionEffect(type, ticks, amplifier, false, !PotionEffectType.INVISIBILITY.equals(type));
    }
}
