package br.com.muxe.minigame.bedwars.hotbar;

import br.com.muxe.minigame.bedwars.hotbar.domain.HotbarLayout;
import java.util.UUID;

public interface HotbarStore {

    HotbarLayout of(UUID playerId);

    void change(UUID playerId, HotbarLayout layout);
}
