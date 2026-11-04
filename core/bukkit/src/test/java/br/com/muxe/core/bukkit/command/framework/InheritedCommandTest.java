package br.com.muxe.core.bukkit.command.framework;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.command.framework.annotation.Arg;
import br.com.muxe.core.bukkit.command.framework.annotation.Command;
import br.com.muxe.core.bukkit.command.framework.annotation.Permission;
import br.com.muxe.core.bukkit.command.framework.annotation.Subcommand;
import br.com.muxe.core.bukkit.command.framework.annotation.Suggest;
import br.com.muxe.core.bukkit.command.framework.completion.SuggestionProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InheritedCommandTest {

    private static final String PERMISSION = "test.inherited";

    private final List<String> calls = new ArrayList<>();
    private final List<String> typedByTheCompleter = new ArrayList<>();
    private TestCommands commands;

    abstract static class Base {

        final List<String> calls;

        Base(List<String> calls) {
            this.calls = calls;
        }

        @Subcommand("base")
        public void base(CommandSender sender, @Arg("path") @Suggest(provider = "paths") String path) {
            calls.add("base " + path);
        }

        @Subcommand("shared")
        public void shared(CommandSender sender) {
            calls.add("base shared");
        }
    }

    @Command(name = "child")
    @Permission(PERMISSION)
    public static final class Child extends Base {

        Child(List<String> calls) {
            super(calls);
        }

        @Override
        @Subcommand("shared")
        public void shared(CommandSender sender) {
            calls.add("child shared");
        }

        @Subcommand("extra")
        public void extra(Player player) {
            calls.add("child extra");
        }
    }

    @BeforeEach
    void register() {
        commands = TestCommands.create();
        commands.commands().suggestions("paths", new SuggestionProvider() {
            @Override
            public Collection<String> suggest(CommandSender sender) {
                return List.of("everything");
            }

            @Override
            public Collection<String> suggest(CommandSender sender, String typed) {
                typedByTheCompleter.add(typed);
                return List.of(typed + "-one", typed + "-two");
            }
        });
        commands.register(new Child(calls));
    }

    @Test
    void subcommandsDeclaredInASuperclassAreRegisteredWithTheSubclassCommand() {
        FakeSender staff = FakeSender.named("Staff", PERMISSION);

        commands.run(staff.player(), "child base alvo");

        assertThat(calls).containsExactly("base alvo");
    }

    @Test
    void aSubclassOverridingAMethodReplacesTheInheritedOne() {
        FakeSender staff = FakeSender.named("Staff", PERMISSION);

        commands.run(staff.player(), "child shared");

        assertThat(calls).containsExactly("child shared");
    }

    @Test
    void theCommandPermissionOfTheSubclassGuardsInheritedSubcommands() {
        FakeSender player = FakeSender.named("Steve");

        commands.run(player.player(), "child base alvo");

        assertThat(calls).isEmpty();
        assertThat(player.messages()).containsExactly("no-permission");
    }

    @Test
    void completionListsInheritedAndOwnSubcommands() {
        FakeSender staff = FakeSender.named("Staff", PERMISSION);

        assertThat(commands.complete(staff.player(), "child ")).containsExactly("base", "extra", "shared");
    }

    @Test
    void suggestionProvidersReceiveWhatWasTypedForTheArgument() {
        FakeSender staff = FakeSender.named("Staff", PERMISSION);

        List<String> suggestions = commands.complete(staff.player(), "child base ab");

        assertThat(typedByTheCompleter).containsExactly("ab");
        assertThat(suggestions).containsExactly("ab-one", "ab-two");
    }
}
