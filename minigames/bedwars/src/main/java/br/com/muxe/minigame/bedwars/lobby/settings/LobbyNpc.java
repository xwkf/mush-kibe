package br.com.muxe.minigame.bedwars.lobby.settings;

import br.com.muxe.core.bukkit.npc.NpcSkin;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.bukkit.configuration.ConfigurationSection;

public record LobbyNpc(String id, String action, Spot spot, Optional<NpcSkin> skin, List<String> players, List<String> lines) {

    public LobbyNpc {
        players = List.copyOf(players);
        lines = List.copyOf(lines);
    }

    public static List<LobbyNpc> from(ConfigurationSection npcs) {
        List<LobbyNpc> all = new ArrayList<>();
        for (String id : npcs.getKeys(false)) {
            ConfigurationSection npc = Objects.requireNonNull(npcs.getConfigurationSection(id), "Missing NPC " + id);
            ConfigurationSection skin = npc.getConfigurationSection("skin");
            Optional<NpcSkin> texture = Optional.ofNullable(skin)
                .filter(section -> section.isString("value") && section.isString("signature"))
                .map(section -> new NpcSkin(section.getString("value"), section.getString("signature")));
            all.add(new LobbyNpc(id, npc.getString("action", "nenhuma"), Spot.from(Objects.requireNonNull(npc.getConfigurationSection("location"), "Missing location of NPC " + id)),
                texture, npc.getStringList("players"), npc.getStringList("lines")));
        }
        return all;
    }
}
