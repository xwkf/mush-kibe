package br.com.muxe.minigame.api.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.minigame.api.cosmetic.aura.Auras;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class SceneAnimations {

    private final Map<String, SceneAnimation> byId = new LinkedHashMap<>();

    public SceneAnimations(List<SceneAnimation> animations) {
        animations.forEach(animation -> byId.put(animation.id(), animation));
    }

    public static SceneAnimations victories() {
        return new SceneAnimations(List.of(new PaintVictory(), new FlyingHyenaVictory(), new FlyingPigVictory(), new BalloonsVictory(), new PumpkinHarvestVictory(),
            new HauntedVictory(), new PaparazziVictory(), new ZombieApocalypseVictory(), new GrapplerVictory(), new NecromancerVictory(), new DesertVictory(),
            new WitchVictory(), new DragonVictory(), new InfernoVictory(), new EndVictory(), new TsunamiVictory(), new WitherVictory(), new DuskVictory(),
            new FrozenVictory(), new FestivalVictory(), new MagicalGirlVictory(), new EnchantedVictory()));
    }

    public static SceneAnimations finalKills() {
        return new SceneAnimations(List.of(new GiftedFinalKill(), new GiftShowerFinalKill(), new BirdsFinalKill(), new BrainFreezeFinalKill(),
            new DrownFinalKill(), new DuskFinalKill(), new FrightfulFinalKill(), new RainbowFinalKill(), new CookiesAndMilkFinalKill(),
            new BountyHunterFinalKill(), new PumpkinHarvestFinalKill(), new BeehiveFinalKill(), new DecapitatedFinalKill(), new TakeOffFinalKill(),
            new ElectrifiedFinalKill(), new WrappedFinalKill(), new EnchantedFinalKill(), new DesertFinalKill(), new CursedSkeletonFinalKill(), new SkeletalFinalKill(),
            new PumpkinExplosionFinalKill(), new MelonExplosionFinalKill(), new TntExplosionFinalKill(), new ExplodingSquidFinalKill(), new GhostsFinalKill(),
            new DiscoPartyFinalKill(), new FireworksFinalKill(), new GalacticFinalKill(), new MagicalGirlFinalKill(), new IgniteFinalKill(),
            new FlowerGardenFinalKill(), new CoinsFinalKill(), new MagicTrickFinalKill(), new SnowPenguinsFinalKill(), new EndPortalFinalKill(),
            new LightningFinalKill(), ShatterFinalKill.glass(), ShatterFinalKill.redstone(), new SixSevenFinalKill(), new PaintFinalKill(), new MidasTouchFinalKill(),
            new ChineseDragonsFinalKill(), new VanillaFinalKill()));
    }

    public static SceneAnimations auras() {
        return new SceneAnimations(Auras.all().stream().<SceneAnimation>map(AuraAnimation::new).toList());
    }

    public Optional<SceneAnimation> find(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    public List<SceneAnimation> all() {
        return List.copyOf(byId.values());
    }
}
