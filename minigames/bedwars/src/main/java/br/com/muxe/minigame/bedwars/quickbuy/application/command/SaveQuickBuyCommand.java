package br.com.muxe.minigame.bedwars.quickbuy.application.command;

import br.com.muxe.core.common.cqrs.command.Command;
import br.com.muxe.core.common.result.Result;
import br.com.muxe.minigame.bedwars.quickbuy.domain.QuickBuy;
import java.util.Objects;
import java.util.UUID;

public record SaveQuickBuyCommand(UUID playerId, QuickBuy quickBuy) implements Command<Result<Void>> {

    public SaveQuickBuyCommand {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(quickBuy, "quickBuy");
    }
}
