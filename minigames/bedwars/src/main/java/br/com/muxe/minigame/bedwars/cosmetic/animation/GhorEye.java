package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Pose;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.StandStyle;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.List;

final class GhorEye {

    static final List<String> SKIN = List.of(
        "25993eec16a5133bff5881f708f69d96d2331249d4d7f7cd8a8f4ff4823da271",
        "76099fff70f6cfe3b194a927f9112e5cf99b1e611d9a6caa3d628d0cf12c2d0a",
        "f95ea34d66323b94dfd14b496bcc84287b38f2b4ac7b454cec29d2a6f0e8bae",
        "3fac95dd7493b0e4f4a2eecea2d4028bc3678d400548c394dd091a84d0b19a21",
        "8c75936774df35fe5d855555b0dfcda02e7a3236d60f6b9823933209298b0e13",
        "8a1aa26ed2d473eb84b82a9bf47fd1e25863cafbf49f668681adf768d29c6b0f",
        "c93b2135b651963b637ad92bfb526815e226aa49cce110699fa139164cfca9f6",
        "490cb900decac11c89091d8436c372c12cbcd5d12bc54272a6f03260881e02a2",
        "923aba08fb9f92c277fd79bc9738e96fa1423774bc922c8a1bfbdc54e1814ccc",
        "ab5013cd99754647bbca98a88a18fb3f0a468fd6822ccd34135e1472632236ac",
        "9d0ec8eeadc24426ac24d9ee91edf7dc72ffc2664ce9a4f812fb53266bed4a12",
        "68da7f97f2a1b5442e64da6a00ddaccfdd83938a6eee9b19e2138ed05718556a",
        "209bb3fb10ea8fed93a1e9764c4e7d4a2fa5ded9bf9d14cd53964a80852615ec",
        "da9a5e661d1f234915fcd046f433fea9580d08e3a70a933d7e5ae124df7ae809",
        "d446a3a1e35c84d40edd5d5c6007ad29dd502134d6ec967195c76189743c3aa9",
        "392f17f3eaf026a23ac1924b2b018f1c38e1b8b4aabc17bdf513b71f5eee4e7a",
        "668876e2f230db6315c058bd6467e59119fde2bd6dfd9eaa90f38645feb6efbb",
        "dea177968f3afcfffe5ca76dd2d87194a5cdc60a4b4714f6fbd2d148d6420db4",
        "dbde2282eab8780cf04ce7d83e8c956d351a6ac79f30e59aa96f17038923f78a",
        "4f4c8be886d2e006ac5e99d0913daaf9e87e14a27fe97d610550d10f0a0c12ae",
        "bfa5d704d3027722004e93c53e41db0f244a5acaee244e154ec606f507958843",
        "421c2a2bd1ffe193efc6f03c7f7808ee2e6e536a06f475d618507ea1ae9a037a",
        "4c75d179b7f3575cbb40f7f2437575fe188ce7b10fdddd08e5c001eca2564edc",
        "332fbb52d93e86fa2b678f3a02b77a87e9cfc09095ea135f821298e31728de48",
        "829382c8e02e2e918de760ffb6ff6d84fe45ce57c8d22a70d22e7553bb71dd99",
        "4792e523858285b06891c2fa30f571b6e489216c39b0bffff1bc28ec3357a0a"
    );
    static final StandStyle PIECE = new StandStyle(true, false, false, false, false);
    static final StandStyle SMALL_PIECE = new StandStyle(true, true, false, false, false);
    static final List<Cell> CELLS = cells();
    static final double CELL = 0.594;
    static final double NECK = 1.5;
    static final double FACE = 0.3;
    static final double MOUTH = 0.6;
    static final double CROWN = 1.9;

    private final List<Actor> pieces;

    private GhorEye(List<Actor> pieces) {
        this.pieces = pieces;
    }

    static GhorEye spawn(Stage stage, Vec center, Gaze gaze) {
        List<Actor> pieces = new ArrayList<>();
        for (int index = 0; index < CELLS.size(); index++) {
            Actor piece = stage.armorStand(at(center, gaze, CELLS.get(index)), (float) gaze.yaw(), PIECE);
            piece.equipSkull(Slot.HELMET, SKIN.get(index));
            pieces.add(piece);
        }
        GhorEye eye = new GhorEye(pieces);
        eye.place(center, gaze);
        return eye;
    }

    static Vec at(Vec center, Gaze gaze, Cell cell) {
        return center.add(gaze.ahead().multiply(cell.ahead() * CELL)).add(gaze.up().multiply(cell.up() * CELL)).add(gaze.side().multiply(cell.side() * CELL));
    }

    static Vec neck(Vec center) {
        return center.add(0, NECK, 0);
    }

    static Vec middle(Vec center, Gaze gaze) {
        return neck(center).add(gaze.up().multiply(FACE));
    }

    static Vec mouth(Vec center, Gaze gaze) {
        return middle(center, gaze).add(gaze.ahead().multiply(MOUTH));
    }

    static Vec crown(Vec center, Gaze gaze) {
        return neck(center).add(gaze.ahead().multiply(MOUTH)).add(0, CROWN, 0);
    }

    void place(Vec center, Gaze gaze) {
        Pose pose = Pose.STANDING.withHead(new Vec(gaze.pitch(), 0, 0));
        for (int index = 0; index < pieces.size(); index++) {
            Actor piece = pieces.get(index);
            piece.teleport(at(center, gaze, CELLS.get(index)), (float) gaze.yaw(), (float) gaze.pitch());
            piece.pose(pose);
        }
    }

    void remove() {
        pieces.forEach(Actor::remove);
    }

    private static List<Cell> cells() {
        List<Cell> cells = new ArrayList<>();
        for (int ahead = 1; ahead >= -1; ahead--) {
            for (int up = 1; up >= -1; up--) {
                for (int side = 1; side >= -1; side--) {
                    if (ahead != 0 || up != 0 || side != 0) {
                        cells.add(new Cell(ahead, up, side));
                    }
                }
            }
        }
        return List.copyOf(cells);
    }

    record Cell(int ahead, int up, int side) {
    }
}
