package br.com.muxe.core.bukkit.cosmetic.stage;

import br.com.muxe.core.bukkit.npc.NpcSkin;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.random.RandomGenerator;

final class FakePlayers {

    private static final int NAME_LENGTH = 8;
    private static final String HEX = "0123456789abcdef";

    private FakePlayers() {
    }

    static Optional<UUID> skinOwner(List<UUID> online, Set<UUID> watching, RandomGenerator random) {
        List<UUID> others = online.stream().filter(player -> !watching.contains(player)).toList();
        return others.isEmpty() ? Optional.empty() : Optional.of(others.get(random.nextInt(others.size())));
    }

    static Optional<String> signature(String texture, List<NpcSkin> online) {
        return online.stream().filter(skin -> skin.value().equals(texture)).map(NpcSkin::signature).findFirst();
    }

    static String name(RandomGenerator random) {
        StringBuilder name = new StringBuilder(NAME_LENGTH);
        for (int index = 0; index < NAME_LENGTH; index++) {
            name.append(HEX.charAt(random.nextInt(HEX.length())));
        }
        return name.toString();
    }
}
