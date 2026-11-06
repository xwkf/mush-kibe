package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Ground;
import br.com.muxe.core.bukkit.cosmetic.animation.SignedSkin;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.random.RandomGenerator;

final class Gnome {

    static final SignedSkin SKIN = new SignedSkin(
        "ewogICJ0aW1lc3RhbXAiIDogMTc5MDg3MTkyOTQ3MCwKICAicHJvZmlsZUlkIiA6ICI4Nzg0OTAzMGYzNzE0OGM4YjM3YzdlYjBjNThhYTEzMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJsb25lX2dub21lIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2U1MTQyYTVhYTg0NDFiNjBkOWQwZjBlYWFkYjhlNzVjZDc0NjYzZWFlYWRkOWU5Nzc2MWE2MDY5NTc5N2JkZWUiCiAgICB9CiAgfQp9",
        "U2bXUID3pAZk3Ck4r47LPZ5V2mGijU8N/5ogr2A36LtzfpZOv9erQFyZh20F3NBTD1isR/Dd5jMC96B3Ms4eC1DfZmkYKmCOK7Lq79O0EbepsdbNh2n4iohKX6llvMx6+trd2mKwmOw0iPSlp6HbdZLcAln3DVakDiZcpK9i/SUrl1I6GMYJl8thOVO8WANHpaAtIcA4kN4khzsiia3Asmj69py6J0MF23rR+j7UeZKpkcy0QUe+PTywl61x6ZN8VEezuWBUaGjgt6ZMzN8NIEu/xw3dPZ8KYH4TPYr47tpp77VHYgQNDex4tgMTrrstn+43/AD8hzhuCu+ehJv0g7Te9zYBWpRcpAYa3bLEIqikuE5SibsNClEJF33Ecj32SuF3vPm7ubWx0JexOFuHl8pyRdaJt2he1e7zloDJMKhsZYzhrC/FleWh586Nz7is/UsFeUiaeyxU8eUNOBVHO0jw+rLvbbhgEpDTk/8KR/RHTgEazah3feMbT2RSCiMVFRlMB5AOpToEhdxH3Qf3oLt/Brm41GsCOH468z7y048Tm1TFhDB+pk9uw1S9bTwr0e4jQyfv94RNlA5kvcWt6ska1tsgy4Lys5qgcxZVFw5zAyQ15AIGgWhtu7PObsS7SbGKXgmsP9eODQj+oZGdr6hhfY8/J4FjrZ07n8h+K8o=");
    static final double SPEED = 0.225;
    static final float TURN = 360;

    private final Actor body;
    private Vec position;
    private float yaw;

    private Gnome(Actor body, Vec position, float yaw) {
        this.body = body;
        this.position = position;
        this.yaw = yaw;
    }

    static Gnome spawn(Stage stage, Vec position, float yaw) {
        return new Gnome(stage.player(position, yaw, SKIN), position, yaw);
    }

    void walk(Ground.Solid solid, RandomGenerator random) {
        Vec next = position.add(Flight.direction(yaw, 0).multiply(SPEED));
        if (blocked(solid, next)) {
            yaw = random.nextFloat() * TURN - TURN / 2;
        } else {
            position = next;
        }
        body.teleport(position, yaw, 0);
    }

    static boolean blocked(Ground.Solid solid, Vec at) {
        int x = (int) Math.floor(at.x());
        int y = (int) Math.floor(at.y());
        int z = (int) Math.floor(at.z());
        return solid.at(x, y, z) || !solid.at(x, y - 1, z);
    }

    Vec position() {
        return position;
    }

    float yaw() {
        return yaw;
    }

    void remove() {
        body.remove();
    }
}
