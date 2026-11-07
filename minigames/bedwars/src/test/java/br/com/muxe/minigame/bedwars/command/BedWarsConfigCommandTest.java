package br.com.muxe.minigame.bedwars.command;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.command.config.ConfigCommand;
import br.com.muxe.core.bukkit.command.config.ConfigPathSuggestions;
import br.com.muxe.core.bukkit.command.config.ConfigReplies;
import br.com.muxe.core.bukkit.command.framework.Commands;
import br.com.muxe.core.bukkit.command.framework.annotation.Suggest;
import br.com.muxe.core.bukkit.command.framework.argument.ArgumentTypes;
import br.com.muxe.core.bukkit.command.framework.completion.SuggestionRegistry;
import br.com.muxe.core.bukkit.command.framework.registry.CommandRegistrar;
import br.com.muxe.core.bukkit.command.framework.registry.RegisteredCommand;
import br.com.muxe.core.bukkit.config.edit.ConfigEditor;
import br.com.muxe.core.bukkit.config.edit.ConfigReloads;
import br.com.muxe.core.bukkit.config.edit.ConfigStore;
import br.com.muxe.core.bukkit.text.MessageCatalog;
import br.com.muxe.core.common.config.live.LiveValue;
import br.com.muxe.minigame.api.cosmetic.preview.ScenePreviewSettings;
import br.com.muxe.minigame.bedwars.cosmetic.preview.PreviewSettings;
import br.com.muxe.minigame.bedwars.cosmetic.preview.config.BedBlocks;
import br.com.muxe.minigame.bedwars.cosmetic.preview.config.PreviewAction;
import br.com.muxe.minigame.bedwars.cosmetic.preview.config.PreviewKind;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BedWarsConfigCommandTest {

    private static final MessageCatalog MESSAGES = MessageCatalog.of(Map.ofEntries(
        Map.entry("no-permission", "no-permission"),
        Map.entry("players-only", "players-only"),
        Map.entry("config.live", "live {components}"),
        Map.entry("config.restart", "restart"),
        Map.entry("config.live-failed", "failed {component}: {reason}"),
        Map.entry("preview-config-unknown-kind", "unknown kind {value} ({kinds})"),
        Map.entry("preview-config-unknown-action", "unknown action {value} ({actions})"),
        Map.entry("preview-config-not-supported", "{kind} does not take that"),
        Map.entry("preview-config-bad-number", "bad number {value}"),
        Map.entry("preview-config-bad-duration", "bad duration {value}"),
        Map.entry("preview-config-not-a-bed", "look at a bed within {distance}"),
        Map.entry("preview-config-spawn", "spawn {kind}: {detail}"),
        Map.entry("preview-config-cama", "bed {kind}: {detail}"),
        Map.entry("preview-config-distancia", "distance {kind}: {detail}"),
        Map.entry("preview-config-tempo", "timeout {kind}: {detail}")
    ));

    private final Map<String, RegisteredCommand> registered = new HashMap<>();
    private final List<String> received = new ArrayList<>();
    private final List<String> refreshed = new ArrayList<>();
    private Optional<BedBlocks> lookedAt = Optional.empty();
    private FileConfiguration config;
    private int saves;
    private LiveValue<PreviewSettings> bed;
    private LiveValue<ScenePreviewSettings> finalKill;
    private LiveValue<ScenePreviewSettings> aura;
    private LiveValue<ScenePreviewSettings> victory;

    @BeforeEach
    void register() {
        config = defaultConfig();
        bed = new LiveValue<>(PreviewSettings.from(config.getConfigurationSection("preview")));
        victory = new LiveValue<>(ScenePreviewSettings.from(config.getConfigurationSection("victory-preview")));
        finalKill = new LiveValue<>(ScenePreviewSettings.from(config.getConfigurationSection("final-kill-preview")));
        aura = new LiveValue<>(ScenePreviewSettings.from(config.getConfigurationSection("aura-preview")));
        ConfigReloads reloads = new ConfigReloads()
            .register("preview", "preview", root -> {
                bed.reload(() -> PreviewSettings.from(root.getConfigurationSection("preview")));
                refreshed.add("preview");
            })
            .register("victory-preview", "victory-preview", root -> victory.reload(() -> ScenePreviewSettings.from(root.getConfigurationSection("victory-preview"))))
            .register("final-kill-preview", "final-kill-preview", root -> finalKill.reload(() -> ScenePreviewSettings.from(root.getConfigurationSection("final-kill-preview"))))
            .register("aura-preview", "aura-preview", root -> aura.reload(() -> ScenePreviewSettings.from(root.getConfigurationSection("aura-preview"))));
        ConfigEditor editor = new ConfigEditor(new ConfigStore() {
            @Override
            public FileConfiguration config() {
                return config;
            }

            @Override
            public void save() {
                saves++;
            }

            @Override
            public void reload() {
            }
        }, reloads);
        CommandRegistrar registrar = new CommandRegistrar() {
            @Override
            public void register(RegisteredCommand command) {
                registered.put(command.definition().name(), command);
            }

            @Override
            public void unregisterAll() {
                registered.clear();
            }
        };
        Commands commands = new Commands(ArgumentTypes.basic(), new SuggestionRegistry(), MESSAGES, Logger.getLogger("test"), registrar);
        ConfigPathSuggestions.register(commands, editor);
        commands.register(new BedWarsConfigCommand(editor, new ConfigReplies(MESSAGES), player -> lookedAt));
    }

    @Test
    void theSpawnOfAPreviewMovesTheWorldAndTheEntryPointLiveAndOldSnapshotsKeepTheirSettings() {
        ScenePreviewSettings running = aura.get();

        run(player("previa_nova", 1.234, 20, 3.5, 90f, 5f), "previa", "aura", "spawn");

        assertThat(config.getString("aura-preview.world")).isEqualTo("previa_nova");
        assertThat(config.getDouble("aura-preview.spawn.x")).isEqualTo(1.23);
        assertThat(config.getDouble("aura-preview.spawn.yaw")).isEqualTo(90.0);
        assertThat(config.contains("aura-preview.spawn.world")).isFalse();
        assertThat(aura.get().world()).isEqualTo("previa_nova");
        assertThat(aura.get().x()).isEqualTo(1.23);
        assertThat(aura.get().yaw()).isEqualTo(90f);
        assertThat(aura.get().pitch()).isEqualTo(5f);
        assertThat(running.world()).isEqualTo("previa_aura");
        assertThat(running.x()).isEqualTo(10.5);
        assertThat(received).containsExactly("spawn aura: previa_nova 1.23 20.0 3.5 (yaw 90.0, pitch 5.0)", "live aura-preview");
        assertThat(saves).isEqualTo(1);
    }

    @Test
    void theCenterFlagPutsTheEntryPointOnTheMiddleOfTheBlock() {
        run(player("previa_nova", 1.9, 20, -3.1, 0f, 0f), "previa", "vitoria", "spawn", "--centro");

        assertThat(victory.get().x()).isEqualTo(1.5);
        assertThat(victory.get().z()).isEqualTo(-3.5);
    }

    @Test
    void eachPreviewKindEditsOnlyItsOwnSection() {
        FileConfiguration before = defaultConfig();

        run(player("outro", 1, 2, 3, 0f, 0f), "previa", "kill-final", "spawn");

        assertThat(config.getString("final-kill-preview.world")).isEqualTo("outro");
        assertThat(config.getString("preview.world")).isEqualTo(before.getString("preview.world"));
        assertThat(config.getString("victory-preview.world")).isEqualTo(before.getString("victory-preview.world"));
        assertThat(config.getString("aura-preview.world")).isEqualTo(before.getString("aura-preview.world"));
    }

    @Test
    void theBedLookedAtBecomesTheBedOfThePreview() {
        lookedAt = Optional.of(BedBlocks.of(20, 12, 30, 0));

        run(player("previa_quebra_de_cama", 0, 0, 0, 0f, 0f), "previa", "cama", "cama");

        assertThat(config.getInt("preview.bed.foot.x")).isEqualTo(20);
        assertThat(config.getInt("preview.bed.foot.y")).isEqualTo(12);
        assertThat(config.getInt("preview.bed.foot.z")).isEqualTo(30);
        assertThat(config.getInt("preview.bed.head.x")).isEqualTo(20);
        assertThat(config.getInt("preview.bed.head.z")).isEqualTo(31);
        assertThat(bed.get().isBed(20, 12, 31)).isTrue();
        assertThat(bed.get().isBed(6, 11, 17)).isFalse();
        assertThat(received).containsExactly("bed cama: 20 12 30 -> 20 12 31", "live preview");
        assertThat(refreshed).containsExactly("preview");
    }

    @Test
    void lookingAwayFromABedChangesNothing() {
        run(player("x", 0, 0, 0, 0f, 0f), "previa", "cama", "cama");

        assertThat(received).containsExactly("look at a bed within 6");
        assertThat(saves).isZero();
        assertThat(bed.get().isBed(6, 11, 17)).isTrue();
    }

    @Test
    void onlyTheBedPreviewHasABed() {
        lookedAt = Optional.of(BedBlocks.of(20, 12, 30, 0));

        run(player("x", 0, 0, 0, 0f, 0f), "previa", "aura", "cama");

        assertThat(received).containsExactly("aura does not take that");
        assertThat(saves).isZero();
    }

    @Test
    void theTargetDistanceOfTheFinalKillIsChangedLive() {
        ScenePreviewSettings before = finalKill.get();

        run(player("x", 0, 0, 0, 0f, 0f), "previa", "kill-final", "distancia", "6,5");

        assertThat(before.target().orElseThrow().distance()).isEqualTo(4.0);
        assertThat(finalKill.get().target().orElseThrow().distance()).isEqualTo(6.5);
        assertThat(received).containsExactly("distance kill-final: 6.5", "live final-kill-preview");
    }

    @Test
    void badDistancesAndKindsWithoutATargetAreRefused() {
        Player staff = player("x", 0, 0, 0, 0f, 0f);

        run(staff, "previa", "kill-final", "distancia", "longe");
        run(staff, "previa", "aura", "distancia", "4");

        assertThat(received).containsExactly("bad number longe", "aura does not take that");
        assertThat(finalKill.get().target().orElseThrow().distance()).isEqualTo(4.0);
    }

    @Test
    void theTimeoutOfEachPreviewIsChangedLive() {
        Player staff = player("x", 0, 0, 0, 0f, 0f);

        run(staff, "previa", "cama", "tempo", "45s");
        run(staff, "previa", "vitoria", "tempo", "2m");
        run(staff, "previa", "aura", "tempo", "30s");
        run(staff, "previa", "aura", "tempo", "agora");

        assertThat(bed.get().timeout()).isEqualTo(Duration.ofSeconds(45));
        assertThat(victory.get().timeout()).isEqualTo(Duration.ofMinutes(2));
        assertThat(aura.get().timeout()).isEqualTo(Duration.ofSeconds(30));
        assertThat(received).endsWith("bad duration agora");
    }

    @Test
    void unknownKindsAndActionsListTheValidOnes() {
        Player staff = player("x", 0, 0, 0, 0f, 0f);

        run(staff, "previa", "lobby", "spawn");
        run(staff, "previa", "aura", "girar");

        assertThat(received).containsExactly(
            "unknown kind lobby (cama, vitoria, kill-final, aura, rastro)",
            "unknown action girar (spawn, cama, distancia, tempo)");
    }

    @Test
    void aServerWithoutLivePreviewsSavesAndAsksForARestart() {
        ConfigReloads none = new ConfigReloads();
        FileConfiguration bare = defaultConfig();
        ConfigEditor editor = new ConfigEditor(new ConfigStore() {
            @Override
            public FileConfiguration config() {
                return bare;
            }

            @Override
            public void save() {
                saves++;
            }

            @Override
            public void reload() {
            }
        }, none);
        BedWarsConfigCommand command = new BedWarsConfigCommand(editor, new ConfigReplies(MESSAGES), player -> Optional.empty());
        Commands commands = new Commands(ArgumentTypes.basic(), new SuggestionRegistry(), MESSAGES, Logger.getLogger("test"), new CommandRegistrar() {
            @Override
            public void register(RegisteredCommand registeredCommand) {
                registered.put("match", registeredCommand);
            }

            @Override
            public void unregisterAll() {
            }
        });
        ConfigPathSuggestions.register(commands, editor);
        commands.register(command);

        registered.get("match").run(player("x", 0, 0, 0, 0f, 0f), "bwconfig", new String[] {"previa", "aura", "tempo", "10s"});

        assertThat(bare.getString("aura-preview.timeout")).isEqualTo("10s");
        assertThat(received).containsExactly("timeout aura: 10s", "restart");
    }

    @Test
    void aPreviewThatCannotReopenIsReportedAndTheFileStillKeepsTheChange() {
        ConfigReloads failing = new ConfigReloads().register("aura-preview", "aura-preview", root -> {
            throw new IllegalStateException("there is no world folder named previa_nova");
        });
        ConfigEditor editor = new ConfigEditor(new ConfigStore() {
            @Override
            public FileConfiguration config() {
                return config;
            }

            @Override
            public void save() {
                saves++;
            }

            @Override
            public void reload() {
            }
        }, failing);
        Commands commands = new Commands(ArgumentTypes.basic(), new SuggestionRegistry(), MESSAGES, Logger.getLogger("test"), new CommandRegistrar() {
            @Override
            public void register(RegisteredCommand registeredCommand) {
                registered.put("failing", registeredCommand);
            }

            @Override
            public void unregisterAll() {
            }
        });
        ConfigPathSuggestions.register(commands, editor);
        commands.register(new BedWarsConfigCommand(editor, new ConfigReplies(MESSAGES), player -> Optional.empty()));

        registered.get("failing").run(player("previa_nova", 0, 0, 0, 0f, 0f), "bwconfig", new String[] {"previa", "aura", "spawn"});

        assertThat(config.getString("aura-preview.world")).isEqualTo("previa_nova");
        assertThat(received).containsExactly(
            "spawn aura: previa_nova 0.0 0.0 0.0 (yaw 0.0, pitch 0.0)",
            "failed aura-preview: there is no world folder named previa_nova",
            "restart");
    }

    @Test
    void thePreviewIsForPlayersAndTheSuggestedWordsMatchTheEnums() throws NoSuchMethodException {
        var method = BedWarsConfigCommand.class.getMethod("preview", Player.class, String.class, String.class, Optional.class);
        String[] kinds = method.getParameters()[1].getAnnotation(Suggest.class).value();
        String[] actions = method.getParameters()[2].getAnnotation(Suggest.class).value();

        assertThat(kinds).containsExactlyElementsOf(PreviewKind.words());
        assertThat(actions).containsExactlyElementsOf(PreviewAction.words());
        assertThat(BedWarsConfigCommand.class.getSuperclass()).isEqualTo(ConfigCommand.class);
    }

    @Test
    void thePreviewShortcutsAreCompletedAndTheGenericSubcommandsAreInherited() {
        Player staff = player("x", 0, 0, 0, 0f, 0f);
        RegisteredCommand command = registered.get("bwconfig");

        assertThat(command.complete(staff, new String[] {""})).containsExactly("adicionar", "definir", "local", "previa", "recarregar", "remover", "ver");
        assertThat(command.complete(staff, new String[] {"previa", ""})).containsExactly("aura", "cama", "kill-final", "rastro", "vitoria");
        assertThat(command.complete(staff, new String[] {"previa", "cama", ""})).containsExactly("cama", "distancia", "spawn", "tempo");
        assertThat(command.complete(staff, new String[] {"ver", "final-kill-preview.ta"})).containsExactly("final-kill-preview.target.");
    }

    private void run(Player player, String... arguments) {
        registered.get("bwconfig").run(player, "bwconfig", arguments);
    }

    private Player player(String world, double x, double y, double z, float yaw, float pitch) {
        World bukkitWorld = (World) Proxy.newProxyInstance(World.class.getClassLoader(), new Class<?>[] {World.class}, (proxy, method, args) -> switch (method.getName()) {
            case "getName" -> world;
            case "hashCode" -> 1;
            case "equals" -> proxy == args[0];
            default -> null;
        });
        Location location = new Location(bukkitWorld, x, y, z, yaw, pitch);
        return (Player) Proxy.newProxyInstance(Player.class.getClassLoader(), new Class<?>[] {Player.class}, (proxy, method, args) -> switch (method.getName()) {
            case "sendMessage" -> {
                received.add((String) args[0]);
                yield null;
            }
            case "getLocation" -> location;
            case "hasPermission" -> true;
            case "getName" -> "Staff";
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> proxy == args[0];
            default -> method.getReturnType() == boolean.class ? false : null;
        });
    }

    private static YamlConfiguration defaultConfig() {
        try (Reader reader = new InputStreamReader(Objects.requireNonNull(BedWarsConfigCommandTest.class.getResourceAsStream("/config.yml")), StandardCharsets.UTF_8)) {
            return YamlConfiguration.loadConfiguration(reader);
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}
