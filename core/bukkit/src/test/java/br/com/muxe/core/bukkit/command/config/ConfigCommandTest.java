package br.com.muxe.core.bukkit.command.config;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.command.framework.FakeSender;
import br.com.muxe.core.bukkit.command.framework.TestCommands;
import br.com.muxe.core.bukkit.command.framework.argument.ArgumentTypes;
import br.com.muxe.core.bukkit.config.edit.ConfigEditor;
import br.com.muxe.core.bukkit.config.edit.ConfigReloads;
import br.com.muxe.core.bukkit.config.edit.MemoryConfigStore;
import br.com.muxe.core.bukkit.shop.ShopFixtures;
import br.com.muxe.core.bukkit.text.MessageCatalog;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConfigCommandTest {

    private static final MessageCatalog MESSAGES = MessageCatalog.from(ShopFixtures.CONFIG.getConfigurationSection("messages"));
    private static final String DEFAULTS = String.join("\n",
        "preview:",
        "  world: previa",
        "  timeout: 120s",
        "  count: 3",
        "  spawn:",
        "    x: 6.5",
        "    y: 11.0",
        "    z: 21.5",
        "    yaw: 180.0",
        "    pitch: 10.0",
        "  names:",
        "    - Ana",
        "    - Bia",
        "spawn:",
        "  world: lobby",
        "  x: 0.5",
        "database:",
        "  password: env:SECRET",
        "other:",
        "  value: 1",
        "");

    private MemoryConfigStore store;
    private TestCommands commands;
    private final List<String> applied = new ArrayList<>();

    @BeforeEach
    void register() {
        store = new MemoryConfigStore(DEFAULTS);
        ConfigReloads reloads = new ConfigReloads().register("preview", "preview", config -> applied.add(config.getString("preview.timeout")));
        ConfigEditor editor = new ConfigEditor(store, reloads);
        commands = new TestCommands(ArgumentTypes.basic(), MESSAGES);
        ConfigPathSuggestions.register(commands.commands(), editor);
        commands.register(new CoreConfigCommand(editor, new ConfigReplies(MESSAGES)));
    }

    @Test
    void theCommandNeedsTheConfigPermission() {
        FakeSender player = FakeSender.named("Steve");

        commands.run(player.player(), "coreconfig ver preview");

        assertThat(player.messages()).containsExactly(MESSAGES.get("no-permission"));
    }

    @Test
    void viewingAValueShowsItsTypeAndASectionListsItsKeys() {
        FakeSender staff = FakeSender.named("Staff", ConfigCommand.PERMISSION);

        commands.run(staff.player(), "coreconfig ver preview.timeout");
        commands.run(staff.player(), "coreconfig ver preview.spawn");
        commands.run(staff.player(), "coreconfig ver preview.names");

        assertThat(staff.messages()).containsExactly(
            MESSAGES.get("config.view-scalar", "path", "preview.timeout", "value", "120s", "type", "texto"),
            MESSAGES.get("config.view-keys-header", "path", "preview.spawn", "count", "5"),
            MESSAGES.get("config.view-keys-line", "key", "pitch"),
            MESSAGES.get("config.view-keys-line", "key", "x"),
            MESSAGES.get("config.view-keys-line", "key", "y"),
            MESSAGES.get("config.view-keys-line", "key", "yaw"),
            MESSAGES.get("config.view-keys-line", "key", "z"),
            MESSAGES.get("config.view-list-header", "path", "preview.names", "count", "2"),
            MESSAGES.get("config.view-list-line", "item", "Ana"),
            MESSAGES.get("config.view-list-line", "item", "Bia"));
    }

    @Test
    void viewingWithoutAPathListsTheTopLevelKeys() {
        FakeSender staff = FakeSender.named("Staff", ConfigCommand.PERMISSION);

        commands.run(staff.console(), "coreconfig ver");

        assertThat(staff.messages().getFirst()).isEqualTo(MESSAGES.get("config.view-keys-header", "path", ConfigReplies.ROOT, "count", "4"));
    }

    @Test
    void secretsAreNeverShown() {
        FakeSender staff = FakeSender.named("Staff", ConfigCommand.PERMISSION);

        commands.run(staff.player(), "coreconfig ver database.password");

        assertThat(staff.messages()).containsExactly(MESSAGES.get("config.protected", "path", "database.password"));
        assertThat(staff.messages().toString()).doesNotContain("SECRET");
    }

    @Test
    void settingAValueSavesItAndTellsWhatWasAppliedLive() {
        FakeSender staff = FakeSender.named("Staff", ConfigCommand.PERMISSION);

        commands.run(staff.player(), "coreconfig definir preview.timeout 45s");

        assertThat(staff.messages()).containsExactly(
            MESSAGES.get("config.set", "path", "preview.timeout", "value", "45s"),
            MESSAGES.get("config.live", "components", "preview"));
        assertThat(store.onDisk().getString("preview.timeout")).isEqualTo("45s");
        assertThat(applied).containsExactly("45s");
    }

    @Test
    void aValueNoComponentCoversIsSavedAndTheStaffIsToldItWaitsForARestart() {
        FakeSender staff = FakeSender.named("Staff", ConfigCommand.PERMISSION);

        commands.run(staff.console(), "coreconfig definir other.value 5");

        assertThat(staff.messages()).containsExactly(
            MESSAGES.get("config.set", "path", "other.value", "value", "5"),
            MESSAGES.get("config.restart"));
        assertThat(store.onDisk().getInt("other.value")).isEqualTo(5);
    }

    @Test
    void textValuesMayHaveSpaces() {
        FakeSender staff = FakeSender.named("Staff", ConfigCommand.PERMISSION);

        commands.run(staff.player(), "coreconfig definir preview.world previa nova");

        assertThat(store.onDisk().getString("preview.world")).isEqualTo("previa nova");
    }

    @Test
    void refusalsExplainWhy() {
        FakeSender staff = FakeSender.named("Staff", ConfigCommand.PERMISSION);

        commands.run(staff.player(), "coreconfig definir preview.nothing 1");
        commands.run(staff.player(), "coreconfig definir preview 1");
        commands.run(staff.player(), "coreconfig definir preview.count muitos");
        commands.run(staff.player(), "coreconfig definir preview.timeout.deeper 1 --novo");
        commands.run(staff.player(), "coreconfig definir database.password x");
        commands.run(staff.player(), "coreconfig definir preview..x 1");
        commands.run(staff.player(), "coreconfig definir preview.world --novo");

        assertThat(staff.messages()).containsExactly(
            MESSAGES.get("config.unknown-path", "path", "preview.nothing"),
            MESSAGES.get("config.is-section", "path", "preview"),
            MESSAGES.get("config.type-mismatch", "path", "preview.count", "type", "inteiro"),
            MESSAGES.get("config.blocked", "path", "preview.timeout.deeper", "blocker", "preview.timeout"),
            MESSAGES.get("config.protected", "path", "database.password"),
            MESSAGES.get("config.invalid-path", "path", "preview..x"),
            MESSAGES.get("config.missing-value"));
        assertThat(store.saves()).isZero();
    }

    @Test
    void theNewFlagCreatesAPathTheDefaultsDoNotHave() {
        FakeSender staff = FakeSender.named("Staff", ConfigCommand.PERMISSION);

        commands.run(staff.player(), "coreconfig definir preview.extra 9 --novo");

        assertThat(store.onDisk().getInt("preview.extra")).isEqualTo(9);
    }

    @Test
    void listsCanBeSetAddedToAndRemovedFrom() {
        FakeSender staff = FakeSender.named("Staff", ConfigCommand.PERMISSION);

        commands.run(staff.player(), "coreconfig adicionar preview.names Caio");
        commands.run(staff.player(), "coreconfig remover preview.names Ana");
        commands.run(staff.player(), "coreconfig remover preview.names Zeca");
        commands.run(staff.player(), "coreconfig adicionar preview.timeout x");

        assertThat(store.onDisk().getStringList("preview.names")).containsExactly("Bia", "Caio");
        assertThat(staff.messages()).contains(
            MESSAGES.get("config.list-added", "path", "preview.names", "value", "Caio"),
            MESSAGES.get("config.list-removed", "path", "preview.names", "value", "Ana"),
            MESSAGES.get("config.missing-item", "path", "preview.names", "value", "Zeca"),
            MESSAGES.get("config.not-list", "path", "preview.timeout"));
    }

    @Test
    void theLocationOfThePlayerIsStoredUnderThePath() {
        Located staff = new Located("Staff", "previa_nova", 1.234, 5.0, 9.876, 90f, 5f);

        commands.run(staff.player(), "coreconfig local preview.spawn");

        assertThat(store.onDisk().getDouble("preview.spawn.x")).isEqualTo(1.23);
        assertThat(store.onDisk().getDouble("preview.spawn.z")).isEqualTo(9.88);
        assertThat(store.onDisk().getDouble("preview.spawn.yaw")).isEqualTo(90.0);
        assertThat(store.onDisk().contains("preview.spawn.world")).isFalse();
        assertThat(staff.messages()).containsExactly(
            MESSAGES.get("config.location-set", "path", "preview.spawn", "location", "previa_nova 1.23 5.0 9.88 (yaw 90.0, pitch 5.0)"),
            MESSAGES.get("config.live", "components", "preview"));
    }

    @Test
    void theCenterAndWorldFlagsShapeTheStoredLocation() {
        Located staff = new Located("Staff", "previa_nova", 1.9, 5.0, -9.1, 0f, 0f);

        commands.run(staff.player(), "coreconfig local preview.spawn --centro --mundo");

        assertThat(store.onDisk().getDouble("preview.spawn.x")).isEqualTo(1.5);
        assertThat(store.onDisk().getDouble("preview.spawn.z")).isEqualTo(-9.5);
        assertThat(store.onDisk().getString("preview.spawn.world")).isEqualTo("previa_nova");
    }

    @Test
    void aSectionWithAWorldKeepsItsWorldUpToDate() {
        Located staff = new Located("Staff", "outro", 1, 2, 3, 0f, 0f);

        commands.run(staff.player(), "coreconfig local spawn");

        assertThat(store.onDisk().getString("spawn.world")).isEqualTo("outro");
    }

    @Test
    void aLocationOnAnUnknownPathNeedsTheNewFlagAndUnknownOptionsAreRefused() {
        Located staff = new Located("Staff", "lobby", 1, 2, 3, 0f, 0f);

        commands.run(staff.player(), "coreconfig local brand.new");
        commands.run(staff.player(), "coreconfig local brand.new --talvez");
        commands.run(staff.player(), "coreconfig local brand.new --novo");

        assertThat(staff.messages()).contains(
            MESSAGES.get("config.unknown-path", "path", "brand.new"),
            MESSAGES.get("config.unknown-option", "option", "--talvez", "options", "--centro --mundo --novo"));
        assertThat(store.onDisk().getString("brand.new.world")).isEqualTo("lobby");
    }

    @Test
    void theLocationSubcommandIsForPlayersOnly() {
        FakeSender console = FakeSender.named("CONSOLE", ConfigCommand.PERMISSION);

        commands.run(console.console(), "coreconfig local spawn");

        assertThat(console.messages()).containsExactly(MESSAGES.get("players-only"));
    }

    @Test
    void reloadingRereadsTheFileAndListsWhatWaitsForARestart() {
        FakeSender staff = FakeSender.named("Staff", ConfigCommand.PERMISSION);
        commands.run(staff.player(), "coreconfig definir other.value 5");
        staff.clear();

        commands.run(staff.player(), "coreconfig recarregar");

        assertThat(staff.messages()).containsExactly(
            MESSAGES.get("config.reload-done"),
            MESSAGES.get("config.reload-applied", "components", "preview"),
            MESSAGES.get("config.reload-pending", "count", "1"),
            MESSAGES.get("config.reload-pending-line", "path", "other.value"));
        assertThat(store.reloads()).isEqualTo(1);
    }

    @Test
    void pathsAreCompletedLevelByLevelAndSecretsAreHidden() {
        FakeSender staff = FakeSender.named("Staff", ConfigCommand.PERMISSION);

        assertThat(commands.complete(staff.player(), "coreconfig ")).containsExactly("adicionar", "definir", "local", "recarregar", "remover", "ver");
        assertThat(commands.complete(staff.player(), "coreconfig ver pre")).containsExactly("preview.");
        assertThat(commands.complete(staff.player(), "coreconfig definir preview.t")).containsExactly("preview.timeout");
        assertThat(commands.complete(staff.player(), "coreconfig local preview.sp")).containsExactly("preview.spawn.");
        assertThat(commands.complete(staff.player(), "coreconfig ver database.")).isEmpty();
    }

    private static final class Located {

        private final String name;
        private final Location location;
        private final List<String> messages = new ArrayList<>();

        Located(String name, String world, double x, double y, double z, float yaw, float pitch) {
            this.name = name;
            World bukkitWorld = (World) Proxy.newProxyInstance(World.class.getClassLoader(), new Class<?>[] {World.class}, (proxy, method, args) -> switch (method.getName()) {
                case "getName" -> world;
                case "hashCode" -> 1;
                case "equals" -> proxy == args[0];
                default -> null;
            });
            this.location = new Location(bukkitWorld, x, y, z, yaw, pitch);
        }

        Player player() {
            return (Player) Proxy.newProxyInstance(Player.class.getClassLoader(), new Class<?>[] {Player.class}, (proxy, method, args) -> switch (method.getName()) {
                case "getName" -> name;
                case "getLocation" -> location;
                case "hasPermission" -> true;
                case "sendMessage" -> {
                    messages.add((String) args[0]);
                    yield null;
                }
                case "hashCode" -> 2;
                case "equals" -> proxy == args[0];
                default -> method.getReturnType() == boolean.class ? false : null;
            });
        }

        List<String> messages() {
            return messages;
        }
    }
}
