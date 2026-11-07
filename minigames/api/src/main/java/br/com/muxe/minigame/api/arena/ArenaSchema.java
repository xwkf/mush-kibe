package br.com.muxe.minigame.api.arena;

import br.com.muxe.minigame.engine.mode.GameMode;
import br.com.muxe.minigame.engine.setup.LinkedBlock;
import br.com.muxe.minigame.engine.setup.SetupSchema;
import br.com.muxe.minigame.engine.setup.SetupStep;
import br.com.muxe.minigame.engine.setup.StepKind;
import br.com.muxe.minigame.engine.setup.TeamRule;
import br.com.muxe.minigame.engine.team.TeamColor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bukkit.configuration.ConfigurationSection;

public final class ArenaSchema implements SetupSchema {

    public static final SetupStep WAITING_SPAWN = SetupStep.single("espera", "Spawn da espera", StepKind.LOCATION, false, "&e&lESPERA");
    public static final SetupStep WAITING_AREA = new SetupStep("area-espera", "Área da espera", StepKind.REGION, false, 2, 2,
        List.of("&e&lÁREA DA ESPERA", "&7Pos {index}"));
    public static final SetupStep SPECTATOR = SetupStep.single("espectador", "Spawn dos espectadores", StepKind.LOCATION, false, "&7&lESPECTADORES");
    public static final SetupStep VOID = SetupStep.single("void", "Altura do void", StepKind.HEIGHT, false);
    public static final SetupStep SPAWN = SetupStep.single("spawn", "Spawn", StepKind.LOCATION, true, "&a&lSPAWN", "{team}");

    private final Map<String, GameMode> modes;
    private final Map<String, List<TeamColor>> fixedTeams;
    private final List<SetupStep> steps;
    private final Map<String, List<SetupStep>> extraSteps;
    private final Optional<SetupStep> anchor;
    private final List<LinkedBlock> linked;

    private ArenaSchema(Builder builder) {
        this.modes = builder.modes.stream().collect(Collectors.toMap(GameMode::id, Function.identity(), (first, second) -> first, LinkedHashMap::new));
        this.fixedTeams = Map.copyOf(builder.fixedTeams);
        this.steps = List.copyOf(builder.steps);
        this.extraSteps = Map.copyOf(builder.extraSteps);
        this.anchor = builder.anchor;
        this.linked = List.copyOf(builder.linked);
        fixedTeams.forEach((mode, teams) -> {
            GameMode gameMode = modes.get(mode);
            if (gameMode != null && gameMode.teams() != teams.size()) {
                throw new IllegalArgumentException("Mode " + mode + " has " + gameMode.teams() + " teams but lists " + teams.size() + " fixed teams");
            }
        });
    }

    public static Builder builder(List<GameMode> modes) {
        return new Builder(modes);
    }

    public GameMode mode(String id) {
        GameMode mode = modes.get(id);
        if (mode == null) {
            throw new IllegalArgumentException("Unknown mode " + id);
        }
        return mode;
    }

    public String modeName(String id) {
        GameMode mode = modes.get(id);
        return mode == null ? id : mode.displayName();
    }

    @Override
    public List<String> modes() {
        return List.copyOf(modes.keySet());
    }

    @Override
    public TeamRule teams(String mode) {
        List<TeamColor> fixed = fixedTeams.get(mode);
        return fixed == null ? TeamRule.chosen(mode(mode).teams()) : new TeamRule(fixed, fixed.size());
    }

    @Override
    public List<SetupStep> steps(String mode) {
        List<SetupStep> extra = extraSteps.getOrDefault(mode, List.of());
        if (extra.isEmpty()) {
            return steps;
        }
        List<SetupStep> all = new ArrayList<>(steps);
        all.addAll(extra);
        return List.copyOf(all);
    }

    @Override
    public Optional<SetupStep> teamAnchor(String mode) {
        return anchor;
    }

    @Override
    public List<LinkedBlock> linkedBlocks(String mode) {
        return linked;
    }

    public static final class Builder {

        private final List<GameMode> modes;
        private final Map<String, List<TeamColor>> fixedTeams = new LinkedHashMap<>();
        private final List<SetupStep> steps = new ArrayList<>();
        private final Map<String, List<SetupStep>> extraSteps = new LinkedHashMap<>();
        private final List<LinkedBlock> linked = new ArrayList<>();
        private Optional<SetupStep> anchor = Optional.empty();

        private Builder(List<GameMode> modes) {
            this.modes = List.copyOf(modes);
        }

        public Builder waiting() {
            return step(WAITING_SPAWN, WAITING_AREA);
        }

        public Builder spectator() {
            return step(SPECTATOR);
        }

        public Builder voidHeight() {
            return step(VOID);
        }

        public Builder teamSpawn() {
            step(SPAWN);
            return anchor(SPAWN);
        }

        public Builder step(SetupStep... added) {
            steps.addAll(List.of(added));
            return this;
        }

        public Builder stepsFor(Set<String> forModes, SetupStep... added) {
            for (String mode : forModes) {
                extraSteps.computeIfAbsent(mode, ignored -> new ArrayList<>()).addAll(List.of(added));
            }
            return this;
        }

        public Builder anchor(SetupStep step) {
            this.anchor = Optional.of(step);
            return this;
        }

        public Builder linked(LinkedBlock block) {
            linked.add(block);
            return this;
        }

        public Builder fixedTeams(String mode, List<TeamColor> teams) {
            if (!teams.isEmpty()) {
                fixedTeams.put(mode, List.copyOf(teams));
            }
            return this;
        }

        public Builder fixedTeams(ConfigurationSection modesSection) {
            for (GameMode mode : modes) {
                ConfigurationSection section = modesSection.getConfigurationSection(mode.id());
                if (section == null) {
                    continue;
                }
                List<TeamColor> teams = new ArrayList<>();
                for (String id : section.getStringList("fixed-teams")) {
                    teams.add(TeamColor.find(id).orElseThrow(() -> new IllegalArgumentException("Unknown team " + id + " in mode " + mode.id())));
                }
                fixedTeams(mode.id(), teams);
            }
            return this;
        }

        public ArenaSchema build() {
            return new ArenaSchema(this);
        }
    }
}
