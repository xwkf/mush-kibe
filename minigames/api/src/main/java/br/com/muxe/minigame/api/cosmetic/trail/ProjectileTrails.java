package br.com.muxe.minigame.api.cosmetic.trail;

import br.com.muxe.minigame.api.cosmetic.trail.effect.ColorCycleTrail;
import br.com.muxe.minigame.api.cosmetic.trail.effect.FortuneSpiralTrail;
import br.com.muxe.minigame.api.cosmetic.trail.effect.ItemDropTrail;
import br.com.muxe.minigame.api.cosmetic.trail.effect.MusicalNotesTrail;
import br.com.muxe.minigame.api.cosmetic.trail.effect.ParticleTrail;
import br.com.muxe.minigame.api.cosmetic.trail.effect.RandomParticleTrail;
import java.util.List;
import java.util.Optional;

public final class ProjectileTrails {

    private static final List<ProjectileTrail> ALL = List.of(
        FortuneSpiralTrail.ESPIRAL_DA_FORTUNA,
        ParticleTrail.CRITICO,
        ParticleTrail.CHAMAS,
        ColorCycleTrail.ULTRA,
        ColorCycleTrail.NATALINO,
        ItemDropTrail.OSSOS,
        ParticleTrail.ESPECTRO_VERDE,
        ParticleTrail.CORACOES,
        ParticleTrail.MAGIA,
        ItemDropTrail.SORTE_AZUL,
        ParticleTrail.FAISCA_COSMICA,
        ParticleTrail.LAVA,
        ParticleTrail.CHROMA,
        ParticleTrail.FUMACA,
        ParticleTrail.VAPOR,
        ParticleTrail.AGUA,
        ParticleTrail.ALDEAO_FURIOSO,
        ParticleTrail.FOGOS_DE_ARTIFICIO,
        new MusicalNotesTrail(),
        ParticleTrail.SANGRENTO,
        ParticleTrail.CRITICO_ENCANTADO,
        RandomParticleTrail.MEGA_LOUCURA,
        ColorCycleTrail.ARCO_IRIS);

    private ProjectileTrails() {
    }

    public static Optional<ProjectileTrail> find(String id) {
        return ALL.stream().filter(trail -> trail.id().equals(id)).findFirst();
    }

    public static List<ProjectileTrail> all() {
        return ALL;
    }
}
