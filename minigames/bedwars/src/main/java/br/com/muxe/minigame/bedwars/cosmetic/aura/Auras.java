package br.com.muxe.minigame.bedwars.cosmetic.aura;

import java.util.List;
import java.util.Optional;

public final class Auras {

    public static final Aura MASTER_LEAGUE = new Aura("liga-mestre", AuraParticle.FLAME);
    public static final Aura DIAMOND_LEAGUE = new Aura("liga-diamante", AuraParticle.SPELL_WITCH);

    private static final List<Aura> ALL = List.of(MASTER_LEAGUE, DIAMOND_LEAGUE);

    private Auras() {
    }

    public static Optional<Aura> find(String id) {
        return ALL.stream().filter(aura -> aura.id().equals(id)).findFirst();
    }

    public static List<Aura> all() {
        return ALL;
    }
}
