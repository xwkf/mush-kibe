package br.com.muxe.minigame.bedwars.command;

import br.com.muxe.core.bukkit.command.config.ConfigCommand;
import br.com.muxe.core.bukkit.command.config.ConfigReplies;
import br.com.muxe.core.bukkit.command.framework.annotation.Arg;
import br.com.muxe.core.bukkit.command.framework.annotation.Command;
import br.com.muxe.core.bukkit.command.framework.annotation.Greedy;
import br.com.muxe.core.bukkit.command.framework.annotation.Permission;
import br.com.muxe.core.bukkit.command.framework.annotation.Subcommand;
import br.com.muxe.core.bukkit.command.framework.annotation.Suggest;
import br.com.muxe.core.bukkit.config.edit.ConfigEditor;
import br.com.muxe.core.bukkit.config.edit.ConfigFlags;
import br.com.muxe.core.bukkit.config.edit.ConfigOutcome;
import br.com.muxe.minigame.bedwars.cosmetic.preview.config.BedBlocks;
import br.com.muxe.minigame.bedwars.cosmetic.preview.config.BedLook;
import br.com.muxe.minigame.bedwars.cosmetic.preview.config.PreviewAction;
import br.com.muxe.minigame.bedwars.cosmetic.preview.config.PreviewEdit;
import br.com.muxe.minigame.bedwars.cosmetic.preview.config.PreviewEdits;
import br.com.muxe.minigame.bedwars.cosmetic.preview.config.PreviewKind;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.bukkit.entity.Player;

@Command(name = "bwconfig", description = "Views and changes the BedWars configuration.")
@Permission(ConfigCommand.PERMISSION)
public final class BedWarsConfigCommand extends ConfigCommand {

    private static final Set<String> PREVIEW_FLAGS = Set.of(ConfigFlags.CENTER);

    private final Function<Player, Optional<BedBlocks>> bedLook;

    public BedWarsConfigCommand(ConfigEditor editor, ConfigReplies replies) {
        this(editor, replies, BedLook::at);
    }

    public BedWarsConfigCommand(ConfigEditor editor, ConfigReplies replies, Function<Player, Optional<BedBlocks>> bedLook) {
        super(editor, replies);
        this.bedLook = bedLook;
    }

    @Subcommand("previa")
    public void preview(
        Player player,
        @Arg("tipo") @Suggest({"cama", "vitoria", "kill-final", "aura", "rastro"}) String kindWord,
        @Arg("ação") @Suggest({"spawn", "cama", "distancia", "tempo"}) String actionWord,
        @Arg("valor") @Greedy Optional<String> value
    ) {
        Optional<PreviewKind> kind = PreviewKind.parse(kindWord);
        if (kind.isEmpty()) {
            replies().send(player, "preview-config-unknown-kind", "value", kindWord, "kinds", String.join(", ", PreviewKind.words()));
            return;
        }
        Optional<PreviewAction> action = PreviewAction.parse(actionWord);
        if (action.isEmpty()) {
            replies().send(player, "preview-config-unknown-action", "value", actionWord, "actions", String.join(", ", PreviewAction.words()));
            return;
        }
        ConfigFlags flags = ConfigFlags.parse(value, PREVIEW_FLAGS);
        PreviewEdit edit = switch (action.get()) {
            case SPAWN -> PreviewEdits.spawn(kind.get(), here(player.getLocation(), flags.has(ConfigFlags.CENTER)));
            case BED -> bedLook.apply(player)
                .<PreviewEdit>map(bed -> PreviewEdits.bed(kind.get(), bed))
                .orElseGet(() -> new PreviewEdit.Refused(PreviewEdits.NOT_A_BED, ""));
            case DISTANCE -> PreviewEdits.distance(kind.get(), flags.rest());
            case TIMEOUT -> PreviewEdits.timeout(kind.get(), flags.rest());
        };
        switch (edit) {
            case PreviewEdit.Refused refused -> replies().send(player, refused.message(),
                "kind", kind.get().word(), "action", action.get().word(), "value", refused.value(), "distance", Integer.toString(BedLook.REACH));
            case PreviewEdit.Write write -> apply(player, kind.get(), action.get(), write);
        }
    }

    private void apply(Player player, PreviewKind kind, PreviewAction action, PreviewEdit.Write write) {
        ConfigOutcome outcome = editor().setAll(write.values(), true);
        if (replies().refused(player, outcome)) {
            return;
        }
        replies().send(player, "preview-config-" + action.word(), "kind", kind.word(), "detail", write.detail());
        replies().applied(player, outcome);
    }
}
