package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;

record Gaze(double yaw, double pitch) {

    static Gaze toward(Vec from, Vec to) {
        return along(to.subtract(from));
    }

    static Gaze along(Vec direction) {
        double flat = Math.hypot(direction.x(), direction.z());
        return new Gaze(Math.toDegrees(Math.atan2(-direction.x(), direction.z())), Math.toDegrees(Math.atan2(-direction.y(), flat)));
    }

    Vec ahead() {
        double yawRadians = Math.toRadians(yaw);
        double pitchRadians = Math.toRadians(pitch);
        return new Vec(-Math.sin(yawRadians) * Math.cos(pitchRadians), -Math.sin(pitchRadians), Math.cos(yawRadians) * Math.cos(pitchRadians));
    }

    Vec up() {
        double yawRadians = Math.toRadians(yaw);
        double pitchRadians = Math.toRadians(pitch);
        return new Vec(-Math.sin(yawRadians) * Math.sin(pitchRadians), Math.cos(pitchRadians), Math.cos(yawRadians) * Math.sin(pitchRadians));
    }

    Vec side() {
        double yawRadians = Math.toRadians(yaw);
        return new Vec(-Math.cos(yawRadians), 0, -Math.sin(yawRadians));
    }

    Gaze follow(Gaze target, double share) {
        return along(ahead().add(target.ahead().subtract(ahead()).multiply(share)));
    }

    Gaze turned(double yawDegrees, double pitchDegrees) {
        return new Gaze(yaw + yawDegrees, pitch + pitchDegrees);
    }
}
