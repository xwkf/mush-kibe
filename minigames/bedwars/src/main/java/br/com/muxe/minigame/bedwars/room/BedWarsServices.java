package br.com.muxe.minigame.bedwars.room;

import br.com.muxe.core.bukkit.api.MuxeCore;
import br.com.muxe.core.bukkit.cosmetic.animation.BedFrame;
import br.com.muxe.core.bukkit.display.DisplayService;
import br.com.muxe.core.bukkit.mission.config.MissionConfig;
import br.com.muxe.core.bukkit.npc.NpcService;
import br.com.muxe.core.bukkit.text.MessageCatalog;
import br.com.muxe.minigame.bedwars.cosmetic.message.KillMessages;
import br.com.muxe.minigame.bedwars.cosmetic.message.MessageSets;
import br.com.muxe.minigame.bedwars.lobby.format.LevelBadges;
import br.com.muxe.minigame.bedwars.mission.MissionRules;
import br.com.muxe.minigame.bedwars.preferences.PlayerPreferences;
import br.com.muxe.minigame.bedwars.settings.BedWarsSettings;
import br.com.muxe.minigame.engine.level.LevelUps;
import java.util.List;
import java.util.Map;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public record BedWarsServices(
    JavaPlugin plugin,
    MuxeCore core,
    BedWarsSettings settings,
    MessageCatalog messages,
    NpcService npcs,
    Map<String, List<String>> npcLines,
    DisplayService displays,
    MissionConfig missions,
    MissionRules missionRules,
    PlayerPreferences preferences,
    MessageSets messageSets,
    KillMessages killMessages,
    BedBreakHandler bedBreaks,
    LevelUps levelUps,
    LevelBadges levelBadges
) {

    @FunctionalInterface
    public interface BedBreakHandler {

        void broken(Player breaker, World world, BedFrame bed, List<Player> viewers);
    }
}
