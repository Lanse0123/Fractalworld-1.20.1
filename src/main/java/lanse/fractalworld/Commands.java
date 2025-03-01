package lanse.fractalworld;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import lanse.fractalworld.Automata.*;
import lanse.fractalworld.FractalCalculator.*;
import lanse.fractalworld.Storage.Database;
import lanse.fractalworld.Storage.RegionStorage;
import lanse.fractalworld.WorldSorter.*;
import lanse.fractalworld.WorldSymmetrifier.Symmetrifier;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.nio.file.Path;
import java.util.Random;

public class Commands {

    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("FractalWorld").requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("on").executes(context -> {
                    if (FractalGenerator.MIN_ITER > FractalGenerator.MAX_ITER) {
                        context.getSource().sendError(Text.of("Minimum Iterations cannot be Greater than Maximum Iterations!"));
                        return 1;
                    }
                    if (!FractalGenerator.heightGeneratorEnabled && !WorldPainter.worldPainterEnabled
                            && !WorldPainter.worldPainterFullHeightEnabled && !Symmetrifier.symmetrifierEnabled
                            && !SortingGenerator.WorldSorterIsEnabled && !Symmetrifier.verticalMirrorWorldEnabled
                            && !AutomataControl.automataIsEnabled) {
                        context.getSource().sendError(Text.of("Enable a mode to start!"));
                        return 1;
                    }
                    DimensionHandler.resetDimensionHandler();
                    FractalWorld.originalServer = context.getSource().getServer();
                    FractalWorld.isModEnabled = true;
                    context.getSource().sendFeedback(() -> Text.literal("FractalWorld Enabled!"), true);
                    return 1;
                }))

                .then(CommandManager.literal("off").executes(context -> {
                    FractalWorld.isModEnabled = false;

                    Database dataSaver = Database.getOrCreate(context.getSource().getServer());
                    Path configFolder = FractalWorld.getConfigFolder();
                    dataSaver.saveToJson(configFolder);
                    FractalWorld.regionCache.values().forEach(RegionStorage::saveToFile);
                    context.getSource().sendFeedback(() -> Text.literal("FractalWorld Disabled!"), true);
                    return 1;
                }))

                .then(CommandManager.literal("help").executes(context -> {
                    MutableText message = Text.literal("Need help with FractalWorld? Click the link: ")
                            .append(Text.literal("FractalWorld Wiki").setStyle(Style.EMPTY
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/Lanse0123/Fractalworld-1.20.1/wiki"))
                                    .withColor(Formatting.AQUA).withUnderline(true)));
                    context.getSource().sendFeedback(() -> message, false);
                    return 1;
                }))

                .then(CommandManager.literal("saveAll").executes(context -> {
                    Database dataSaver = Database.getOrCreate(context.getSource().getServer());
                    Path configFolder = FractalWorld.getConfigFolder();
                    dataSaver.saveToJson(configFolder);
                    FractalWorld.regionCache.values().forEach(RegionStorage::saveToFile);
                    context.getSource().sendFeedback(() -> Text.literal("FractalWorld Settings and Region File Saved!"), true);
                    return 1;
                }))

                .then(CommandManager.literal("reset")
                        .then(CommandManager.literal("chunks").executes(context -> {
                            ChunkProcessor.clearProcessedChunks();
                            context.getSource().sendFeedback(() -> Text.literal("FractalWorld processing queue and updated chunks reset!"), true);
                            return 1;
                        }))
                        .then(CommandManager.literal("settings")
                                .then(CommandManager.argument("confirm", StringArgumentType.string())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(new String[]{"confirm"}, builder))
                                        .executes(context -> {
                                            String confirmation = StringArgumentType.getString(context, "confirm");
                                            if (!confirmation.equals("confirm")) {
                                                context.getSource().sendFeedback(() -> Text.literal("Type '/reset settings confirm' to proceed."), false);
                                                return 0;
                                            }
                                            Database.returnToDefaultValues();
                                            context.getSource().sendFeedback(() -> Text.literal("FractalWorld settings have returned to default!"), true);
                                            return 1;
                                        })))
                        .then(CommandManager.literal("permasave")
                                .then(CommandManager.argument("confirm", StringArgumentType.string())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(new String[]{"confirm"}, builder))
                                        .executes(context -> {
                                            String confirmation = StringArgumentType.getString(context, "confirm");
                                            if (!confirmation.equals("confirm")) {
                                                context.getSource().sendFeedback(() -> Text.literal("Type '/reset permasave confirm' to proceed."), false);
                                                return 0;
                                            }
                                            RegionStorage.resetPermaSave(context.getSource().getServer());
                                            ChunkProcessor.clearProcessedChunks();
                                            context.getSource().sendFeedback(() -> Text.literal("Permasave File Reset!"), true);
                                            return 1;
                                        }))))
                .then(CommandManager.literal("Set").requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.literal("colorPalette").then(CommandManager.argument("palette", StringArgumentType.string())
                                .suggests((context, builder) -> CommandSource.suggestMatching(WorldPainter.COLOR_PALLETS, builder)).executes(context -> {
                                    String palette = StringArgumentType.getString(context, "palette").toLowerCase();
                                    if (!palette.equals("custom")) {
                                        WorldPainter.setColorPalette(palette);
                                        context.getSource().sendFeedback(() -> Text.literal("Color palette set to " + palette + "."), true);
                                        Database.changed = true;
                                        return 1;
                                    } else {
                                        context.getSource().sendFeedback(() -> Text.literal("Please provide a list of blocks for the custom palette."), false);
                                        return 0;
                                    }
                                })
                                .then(CommandManager.argument("blocks", StringArgumentType.greedyString())
                                        .suggests((context, builder) -> {
                                            String input = builder.getInput().substring(0, builder.getStart());
                                            String[] parts = input.split("\\s+");
                                            String lastPart = parts[parts.length - 1];

                                            // Clear the builder and set the correct start position
                                            builder = builder.createOffset(input.length() - lastPart.length());

                                            Registries.BLOCK.getIds().stream()
                                                    .map(Identifier::getPath) // Suggest only block names without "minecraft:"
                                                    .filter(name -> name.startsWith(lastPart))
                                                    .forEach(builder::suggest);

                                            return builder.buildFuture();
                                        })
                                        .executes(context -> {
                                            String blockList = StringArgumentType.getString(context, "blocks");
                                            StringBuilder processedList = new StringBuilder();

                                            String[] blocks = blockList.split("\\s+"); // Adjust the delimiter as needed
                                            for (String block : blocks) {
                                                if (!block.contains(":")) {
                                                    block = "minecraft:" + block; // Automatically add "minecraft:" if not present
                                                }
                                                processedList.append(block).append(" ");
                                            }

                                            boolean success = WorldPainter.setCustomPalette(processedList.toString().trim());
                                            if (success) {
                                                context.getSource().sendFeedback(() -> Text.literal("Custom color palette set!"), true);
                                            } else {
                                                context.getSource().sendError(Text.literal("Invalid blocks in custom palette!"));
                                            }
                                            Database.changed = true;
                                            return success ? 1 : 0;
                                        }))))
                        .then(CommandManager.literal("fractalPreset").then(CommandManager.argument("preset", StringArgumentType.string())
                                .suggests((context, builder) -> CommandSource.suggestMatching(FractalPresets.getFractalNames(), builder))
                                .executes(context -> {
                                    String preset = StringArgumentType.getString(context, "preset");
                                    if (FractalPresets.isValidPreset(preset)) {
                                        FractalPresets.setFractalPreset(preset);
                                        Database.changed = true;

                                        if (FractalPresets.isSeededFractal(preset)) {
                                            if (preset.equals("3d_mandelbox_fractal")) {
                                                context.getSource().sendFeedback(() -> Text.literal(
                                                                "Fractal preset set to: " + preset +
                                                                        ". This fractal requires a seed. Please set the seed values using /FractalWorldSet fractalSeed <real> <imaginary>. Mandelbox only uses the real seed parameter, it ignores the imaginary."),
                                                        true);
                                            } else {
                                                context.getSource().sendFeedback(() -> Text.literal(
                                                                "Fractal preset set to: " + preset +
                                                                        ". This fractal requires a seed. Please set the seed values using /FractalWorldSet fractalSeed <real> <imaginary>."),
                                                        true);
                                            }
                                        } else {
                                            context.getSource().sendFeedback(() -> Text.literal("Fractal preset set to: " + preset), true);
                                        }
                                    } else {
                                        context.getSource().sendError(Text.literal("Invalid preset."));
                                        FractalWorld.isModEnabled = false;
                                    }
                                    return 1;
                                })))
                        .then(CommandManager.literal("customFractalEquation")
                                .then(CommandManager.argument("zxFormula", StringArgumentType.string())
                                        .then(CommandManager.argument("zyFormula", StringArgumentType.string()).executes(context -> {
                                                    // Handle the case with 2 arguments (zxFormula and zyFormula)
                                                    Database.changed = true;
                                                    FractalPresets.setFractalPreset("2d_custom_fractal");
                                                    String formula1 = StringArgumentType.getString(context, "zxFormula");
                                                    String formula2 = StringArgumentType.getString(context, "zyFormula");
                                                    String escapeRadius = "4"; // Default escape radius for 2 arguments
                                                    CustomFractalCalculator.setFractalFormula(formula1, formula2, escapeRadius);
                                                    context.getSource().sendFeedback(() -> Text.literal("Custom Fractal Equation has been changed (2 arguments, escape radius = 4)!"), true);
                                                    return 1;
                                                })
                                                .then(CommandManager.argument("escapeRadius", StringArgumentType.string())
                                                        .executes(context -> {
                                                            // Handle the case with 3 arguments (zxFormula, zyFormula, escapeRadius)
                                                            Database.changed = true;
                                                            FractalPresets.setFractalPreset("2d_custom_fractal");
                                                            String formula1 = StringArgumentType.getString(context, "zxFormula");
                                                            String formula2 = StringArgumentType.getString(context, "zyFormula");
                                                            String escapeRadius = StringArgumentType.getString(context, "escapeRadius");
                                                            CustomFractalCalculator.setFractalFormula(formula1, formula2, escapeRadius);
                                                            context.getSource().sendFeedback(() -> Text.literal("Custom Fractal Equation has been changed (3 arguments)!"), true);
                                                            return 1;
                                                        }))))
                                .executes(context -> {
                                    context.getSource().sendError(Text.literal("You must provide at least 2 arguments: zxFormula and zyFormula!"));
                                    return 0;
                                }))
                        .then(CommandManager.literal("fractalSeed")
                                .then(CommandManager.argument("real", DoubleArgumentType.doubleArg(-2.0, 2.0))
                                        .then(CommandManager.argument("imaginary", DoubleArgumentType.doubleArg(-2.0, 2.0)).executes(context -> {
                                            double real = DoubleArgumentType.getDouble(context, "real");
                                            double imaginary = DoubleArgumentType.getDouble(context, "imaginary");
                                            Database.changed = true;

                                            if (FractalPresets.isSeededFractal(FractalPresets.fractalPreset)) {
                                                FractalPresets.setSeedValues(real, imaginary);
                                                context.getSource().sendFeedback(() -> Text.literal("Fractal seed set to: Real = " + real + ", Imaginary = " + imaginary), true);
                                            } else {
                                                context.getSource().sendError(Text.literal("The current fractal preset does not require a seed."));
                                            }
                                            return 1;
                                        }))))
                        .then(CommandManager.literal("fractalOffset")
                                .then(CommandManager.argument("mx", DoubleArgumentType.doubleArg(-10.0, 10.0))
                                        .then(CommandManager.argument("mz", DoubleArgumentType.doubleArg(-10.0, 10.0))
                                                .executes(context -> {
                                                    Database.changed = true;
                                                    FractalGenerator.xOffset = DoubleArgumentType.getDouble(context, "mx");
                                                    FractalGenerator.zOffset = DoubleArgumentType.getDouble(context, "mz");
                                                    context.getSource().sendFeedback(() -> Text.literal("Fractal offsets set to: X Offset = " + FractalGenerator.xOffset + ", Z Offset = " + FractalGenerator.zOffset), true);
                                                    return 1;
                                                }))))
                        .then(CommandManager.literal("advanced_dimension_disabler")
                                .then(CommandManager.argument("feature", StringArgumentType.string())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(new String[]{
                                                "overworld", "nether", "end"
                                        }, builder)).then(CommandManager.argument("enabled", BoolArgumentType.bool()).executes(context -> {
                                            String feature = StringArgumentType.getString(context, "feature");
                                            boolean enabled = BoolArgumentType.getBool(context, "enabled");
                                            Database.changed = true;

                                            switch (feature.toLowerCase()) {
                                                case "overworld" -> {
                                                    ChunkProcessor.overworldIsDisabled = enabled;
                                                    context.getSource().sendFeedback(() -> Text.literal("Overworld is disabled set to: " + enabled), true);
                                                }
                                                case "nether" -> {
                                                    ChunkProcessor.netherIsDisabled = enabled;
                                                    context.getSource().sendFeedback(() -> Text.literal("Nether is disabled set to: " + enabled), true);
                                                }
                                                case "end" -> {
                                                    ChunkProcessor.endIsDisabled = enabled;
                                                    context.getSource().sendFeedback(() -> Text.literal("End is disabled set to: " + enabled), true);
                                                }
                                                default -> {
                                                    context.getSource().sendFeedback(() -> Text.literal("Invalid dimension."), false);
                                                    return 0;
                                                }
                                            }
                                            return 1;
                                        }))))
                        .then(CommandManager.literal("mode")
                                .then(CommandManager.argument("feature", StringArgumentType.string())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(new String[]{
                                                "Fractal_WorldPainter", "Fractal_WorldPainterFullHeight", "Fractal_HeightGenerator",
                                                "Fractal_Inverted_Height", "WorldSorter", "Symmetrifier", "VerticalMirrorWorld",
                                                "ChunkRandomizer", "AutomataGenerator", "Advanced_AutoRefreshMode",
                                                "symmetrifier_circlegen", "symmetrifier_symmetrify"
                                        }, builder)).then(CommandManager.argument("enabled", BoolArgumentType.bool()).executes(context -> {
                                            String feature = StringArgumentType.getString(context, "feature");
                                            boolean enabled = BoolArgumentType.getBool(context, "enabled");
                                            Database.changed = true;

                                            switch (feature.toLowerCase()) {
                                                case "fractal_worldpainter" -> {
                                                    WorldPainter.setWorldPainter(enabled);
                                                    context.getSource().sendFeedback(() -> Text.literal("World Painter set to: " + enabled), true);
                                                }
                                                case "fractal_worldpainterfullheight" -> {
                                                    WorldPainter.setWorldPainterFullHeight(enabled);
                                                    context.getSource().sendFeedback(() -> Text.literal("World Painter Full Height set to: " + enabled), true);
                                                }
                                                case "worldsorter" -> {
                                                    SortingGenerator.WorldSorterIsEnabled = enabled;
                                                    context.getSource().sendFeedback(() -> Text.literal("World Sorter set to: " + enabled + ". This mode is still experimental, and incomplete. Some of the sorters might lag or crash at high settings. This overrides the mod from a fractal loader into a sorting algorithm."), true);
                                                }
                                                case "fractal_heightgenerator" -> {
                                                    FractalGenerator.heightGeneratorEnabled = enabled;
                                                    if (FractalGenerator.MAX_ITER > 250) FractalGenerator.MAX_ITER = 250;
                                                    context.getSource().sendFeedback(() -> Text.literal("Terrain Height Generator set to: " + enabled), true);
                                                }
                                                case "fractal_inverted_height" -> {
                                                    FractalGenerator.INVERTED_HEIGHT = enabled;
                                                    context.getSource().sendFeedback(() -> Text.literal("Terrain Height Inversion set to: " + enabled), true);
                                                }
                                                case "symmetrifier" -> {
                                                    Symmetrifier.symmetrifierEnabled = enabled;
                                                    context.getSource().sendFeedback(() -> Text.literal("World Symmetrifier set to: " + enabled + ". This overrides the mod from a fractal loader into making the world symmetrical."), true);
                                                }
                                                case "verticalmirrorworld" -> {
                                                    Symmetrifier.verticalMirrorWorldEnabled = enabled;
                                                    context.getSource().sendFeedback(() -> Text.literal("Mirror World set to: " + enabled), true);
                                                }
                                                case "automatagenerator" -> {
                                                    AutomataControl.automataIsEnabled = enabled;
                                                    context.getSource().sendFeedback(() -> Text.literal("Automata generator set to: " + enabled + ". This overrides the mod from a fractal loader into an automata loader. Some automata are fractals. This will be compatible with world painter and height generator."), true);
                                                }
                                                case "advanced_autorefreshmode" -> {
                                                    FractalWorld.autoRefreshModeIsOn = enabled;
                                                    context.getSource().sendFeedback(() -> Text.literal("Auto refresh mode set to: " + enabled + ". This will call FractalWorldReset every (autoRefreshRate settings) tick count."), true);
                                                }
                                                case "symmetrifier_circlegen" -> {
                                                    if (enabled) Symmetrifier.clearModes();
                                                    Symmetrifier.circleGen = enabled;
                                                    context.getSource().sendFeedback(() -> Text.literal("Symmetrifier generation set to circleGen: " + enabled), true);
                                                }
                                                case "symmetrifier_symmetrify" -> {
                                                    if (enabled) Symmetrifier.clearModes();
                                                    Symmetrifier.symmetrifier = enabled;
                                                    context.getSource().sendFeedback(() -> Text.literal("Symmetrifier generation set to 4 corners: " + enabled), true);
                                                }
                                                default -> {
                                                    context.getSource().sendFeedback(() -> Text.literal("Invalid mode."), false);
                                                    return 0;
                                                }
                                            }
                                            return 1;
                                        }))))
                        .then(CommandManager.literal("columnClearer").then(CommandManager.argument("mode", StringArgumentType.string())
                                .suggests((context, builder) -> CommandSource.suggestMatching(new String[]{
                                        "xray", "void", "ocean", "lava_ocean", "monolith", "none", "randomize", "parkour_grid"
                                }, builder)).executes(context -> {
                                    String mode = StringArgumentType.getString(context, "mode").toLowerCase();
                                    ServerCommandSource source = context.getSource();
                                    ColumnClearer.resetColumnClearer();
                                    Database.changed = true;

                                    switch (mode) {
                                        case "xray" -> {
                                            ColumnClearer.currentMode = ColumnClearer.ClearMode.XRAY;
                                            source.sendFeedback(() -> Text.literal("Column Clearer set to: Xray"), true);
                                        }
                                        case "void" -> {
                                            ColumnClearer.currentMode = ColumnClearer.ClearMode.VOID;
                                            source.sendFeedback(() -> Text.literal("Column Clearer set to: Void"), true);
                                        }
                                        case "ocean" -> {
                                            ColumnClearer.currentMode = ColumnClearer.ClearMode.OCEAN;
                                            source.sendFeedback(() -> Text.literal("Column Clearer set to: Ocean"), true);
                                        }
                                        case "lava_ocean" -> {
                                            ColumnClearer.currentMode = ColumnClearer.ClearMode.LAVA_OCEAN;
                                            source.sendFeedback(() -> Text.literal("Column Clearer set to: Lava Ocean"), true);
                                        }
                                        case "monolith" -> {
                                            ColumnClearer.currentMode = ColumnClearer.ClearMode.MONOLITH;
                                            source.sendFeedback(() -> Text.literal("Column Clearer set to: Monolith"), true);
                                        }
                                        case "none" -> {
                                            ColumnClearer.currentMode = ColumnClearer.ClearMode.NONE;
                                            source.sendFeedback(() -> Text.literal("Column Clearer set to: None. This will not destroy columns outside of the fractal range."), true);
                                        }
                                        case "randomize" -> {
                                            ColumnClearer.currentMode = ColumnClearer.ClearMode.RANDOMIZE;
                                            source.sendFeedback(() -> Text.literal("Column Clearer set to: Randomize"), true);
                                        }
                                        case "parkour_grid" -> {
                                            ColumnClearer.currentMode = ColumnClearer.ClearMode.PARKOUR_GRID;
                                            source.sendFeedback(() -> Text.literal("Column Clearer set to: Parkour Grid"), true);
                                        }
                                        default -> {
                                            source.sendFeedback(() -> Text.literal("Invalid column clearer mode."), false);
                                            return 0;
                                        }
                                    }
                                    return 1;
                                })))
                        .then(CommandManager.literal("setting")
                                .then(CommandManager.argument("setting", StringArgumentType.string())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(new String[]{
                                                "fractal_max_iterations", "fractal_min_iterations", "fractal_scale",
                                                "main_column_loading_speed", "main_render_distance", "fractal_initial_height_offset",
                                                "fractal_power3d", "symmetrifier_corners", "automata_rule", "advanced_auto_refresh_rate",
                                                "advanced_max_threads"
                                        }, builder)).then(CommandManager.argument("value", IntegerArgumentType.integer()).executes(context -> {
                                            String setting = StringArgumentType.getString(context, "setting").toLowerCase();
                                            int value = IntegerArgumentType.getInteger(context, "value");
                                            Database.changed = true;

                                            switch (setting) {
                                                case "fractal_max_iterations" -> {
                                                    if (value >= 10 && value <= 10000) {
                                                        int newValue;
                                                        if (value > 250 && FractalGenerator.heightGeneratorEnabled){
                                                            newValue = 250;
                                                            context.getSource().sendFeedback(() -> Text.of("Max iterations is capped at 250 when height generation is on!"), true);
                                                        } else {
                                                            newValue = value;
                                                        }
                                                        FractalGenerator.MAX_ITER = newValue;
                                                        context.getSource().sendFeedback(() -> Text.of("Max Iterations set to: " + newValue), true);
                                                    } else {
                                                        context.getSource().sendError(Text.of("Please specify a value between 10 and 10000."));
                                                    }
                                                }
                                                case "fractal_min_iterations" -> {
                                                    if (value >= 0 && value <= 20) {
                                                        FractalGenerator.MIN_ITER = value;
                                                        context.getSource().sendFeedback(() -> Text.of("Min Iterations set to: " + value), true);
                                                    } else {
                                                        context.getSource().sendError(Text.of("Please specify a value between 1 and 20."));
                                                    }
                                                }
                                                case "fractal_scale" -> {
                                                    if (value >= 1 && value <= Integer.MAX_VALUE - 1) {
                                                        FractalGenerator.setScale(value);
                                                        context.getSource().sendFeedback(() -> Text.of("Scale set to: " + value), true);
                                                    } else {
                                                        context.getSource().sendError(Text.of("Please specify a value between 1 and 2,147,483,646."));
                                                    }
                                                }
                                                case "fractal_initial_height_offset" -> {
                                                    if (value >= -64 && value <= 250) {
                                                        FractalGenerator.INITIAL_HEIGHT_OFFSET = value;
                                                        context.getSource().sendFeedback(() -> Text.of("Initial Height Offset set to: " + value), true);
                                                    } else {
                                                        context.getSource().sendError(Text.of("Please specify a value between -64 and 250."));
                                                    }
                                                }
                                                case "main_column_loading_speed" -> {
                                                    if (value >= 1 && value <= 2500) {
                                                        FractalWorld.maxColumnsPerTick = value;
                                                        context.getSource().sendFeedback(() -> Text.of("Column Loading Speed set to: " + value), true);
                                                        if (value > 1000){
                                                            context.getSource().sendFeedback(() -> Text.of("BE CAREFUL WITH VALUES ABOVE 1,000 FOR THIS!!! THE GAME MIGHT CRASH ON SOME SETTINGS OR DEVICES!!!"), true);
                                                        }
                                                    } else {
                                                        context.getSource().sendError(Text.of("Please specify a value between 1 and 2,500."));
                                                    }
                                                }
                                                case "main_render_distance" -> {
                                                    if (value >= 2 && value <= 10000000) {
                                                        ChunkProcessor.MAX_RENDER_DIST = value;
                                                        context.getSource().sendFeedback(() -> Text.of("Render Transform Distance set to: " + value), true);
                                                    } else {
                                                        context.getSource().sendError(Text.of("Please specify a value between 2 and 10,000,000."));
                                                    }
                                                }
                                                case "fractal_power3d" -> {
                                                    if (value >= 1 && value <= 100) {
                                                        FractalPresets.POWER3D = value;
                                                        context.getSource().sendFeedback(() -> Text.of("3D Power multiplier set to: " + value), true);
                                                    } else {
                                                        context.getSource().sendError(Text.of("Please specify a value between 1 and 100."));
                                                    }
                                                }
                                                case "symmetrifier_corners" -> {
                                                    if (value >= 2) {
                                                        Symmetrifier.numberOfCorners = value;
                                                        context.getSource().sendFeedback(() -> Text.of("Symmetrical Corners set to: " + value), true);
                                                    } else {
                                                        context.getSource().sendError(Text.of("Please specify a value above 1."));
                                                    }
                                                }
                                                case "automata_rule" -> {
                                                    if (value >= 1 && value <= 256) {
                                                        AutomataPresets.rule = value;
                                                        context.getSource().sendFeedback(() -> Text.of("Wolfram Rule set to: " + value), true);
                                                    } else {
                                                        context.getSource().sendError(Text.of("Please specify a value between 1 and 256."));
                                                    }
                                                }
                                                case "advanced_auto_refresh_rate" -> {
                                                    if (value >= 1 && value <= 1000000000) {
                                                        FractalWorld.refreshRate = value;
                                                        context.getSource().sendFeedback(() -> Text.of("Auto Refresh Rate set to: " + value), true);
                                                    } else {
                                                        context.getSource().sendError(Text.of("Please specify a value between 1 and 1,000,000,000."));
                                                    }
                                                }
                                                case "advanced_max_threads" -> {
                                                    if (value >= 1 && value <= 32) {
                                                        FractalWorld.maxThreads = value;
                                                        context.getSource().sendFeedback(() -> Text.of("Max Threads for Multithreading set to: " + value), true);
                                                    } else {
                                                        context.getSource().sendError(Text.of("Please specify a value between 1 and 32."));
                                                    }
                                                }
                                                default -> context.getSource().sendError(Text.of("Unknown setting."));
                                            }
                                            return 1;
                                        }))))
                        .then(CommandManager.literal("permaSave")
                                .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                        .executes(context -> {
                                            boolean enabled = BoolArgumentType.getBool(context, "enabled");
                                            FractalWorld.permaSave = enabled;
                                            Database.changed = true;
                                            context.getSource().sendFeedback(() -> Text.literal("PermaSave set to: " + enabled), true);
                                            return 1;
                                        })))
                        .then(CommandManager.literal("automataPreset")
                                .then(CommandManager.argument("preset", StringArgumentType.string())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(AutomataControl.AUTOMATA_LIST, builder)).executes(context -> {
                                            String preset = StringArgumentType.getString(context, "preset");
                                            ServerCommandSource source = context.getSource();

                                            if (!AutomataControl.AUTOMATA_LIST.contains(preset)) {
                                                source.sendError(Text.literal("Invalid automata preset."));
                                                return 0;
                                            }

                                            AutomataControl.automataPreset = preset;
                                            Database.changed = true;

                                            if (preset.equals("2D_wolfram")) {
                                                source.sendFeedback(() -> Text.literal("Automata preset set to: Wolfram Elementary Automata. Please select a rule for it using: FractalWorldSet setting automata_rule."), true);
                                            } else {
                                                source.sendFeedback(() -> Text.literal("Automata preset set to: " + preset), true);
                                            }
                                            return 1;
                                        })))
                        .then(CommandManager.literal("sorterPreset")
                                .then(CommandManager.argument("preset", StringArgumentType.string())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(SorterPresets.SORTING_PRESETS, builder)).executes(context -> {
                                            String preset = StringArgumentType.getString(context, "preset");
                                            if (SorterPresets.isValidPreset(preset)) {
                                                SorterPresets.setSorterPreset(preset);
                                                context.getSource().sendFeedback(() -> Text.literal("Sorter preset set to: " + preset), true);
                                            } else {
                                                context.getSource().sendError(Text.literal("Invalid preset."));
                                            }
                                            FractalWorld.isModEnabled = false;
                                            Database.changed = true;
                                            return 1;
                                        }))))


                .then(CommandManager.literal("Find")
                        .requires(source -> source.hasPermissionLevel(2)).then(CommandManager.literal("MinecraftCoords")
                                .then(CommandManager.argument("mx", DoubleArgumentType.doubleArg())
                                        .then(CommandManager.argument("mz", DoubleArgumentType.doubleArg())
                                                .executes(context -> {
                                                    double mx = DoubleArgumentType.getDouble(context, "mx");
                                                    double mz = DoubleArgumentType.getDouble(context, "mz");
                                                    double[] minecraftCoords = FractalGenerator.findMinecraftCoordinates(mx, mz);
                                                    context.getSource().sendFeedback(() -> Text.literal(
                                                            "Minecraft coordinates for Complex Plane (" + mx + ", " + mz + "): X = " + (int) minecraftCoords[0] + ", Z = " + (int) minecraftCoords[1]), true);
                                                    return 1;
                                                }))))
                        .then(CommandManager.literal("ComplexCoords")
                                .then(CommandManager.argument("x", DoubleArgumentType.doubleArg())
                                        .then(CommandManager.argument("z", DoubleArgumentType.doubleArg())
                                                .executes(context -> {
                                                    double x = DoubleArgumentType.getDouble(context, "x");
                                                    double z = DoubleArgumentType.getDouble(context, "z");
                                                    double[] complexCoords = FractalGenerator.findComplexCoordinates(x, z);
                                                    context.getSource().sendFeedback(() -> Text.literal(
                                                            "Complex Plane coordinates for Minecraft (" + x + ", " + z + "): Real = " + complexCoords[0] + ", Imaginary = " + complexCoords[1]), true);
                                                    return 1;
                                                }))))
                        .then(CommandManager.literal("tpToScale")
                                .then(CommandManager.argument("scale", IntegerArgumentType.integer()).executes(context -> {
                                    ServerPlayerEntity player = context.getSource().getPlayer();
                                    if (player == null){
                                        return 1;
                                    }
                                    int scale = IntegerArgumentType.getInteger(context,"scale");
                                    FractalGenerator.tpPlayerToScale(player, scale);
                                    return 1;
                                })))
                        .then(CommandManager.literal("AdvancedEnableAndResetPlayerWorldLoader")
                                .requires(source -> source.hasPermissionLevel(2)).then(CommandManager.argument("player", EntityArgumentType.player())
                                        .then(CommandManager.argument("enable", BoolArgumentType.bool()).executes(context -> {
                                            CarpetPlayerWorldLoader.trackedPlayer = EntityArgumentType.getPlayer(context, "player");
                                            CarpetPlayerWorldLoader.playerWorldLoaderEnabled = BoolArgumentType.getBool(context, "enable");
                                            CarpetPlayerWorldLoader.trackedPlayerMovementDistance = 20;
                                            context.getSource().sendFeedback(() -> Text.literal("FractalWorld Player World Loader " + (CarpetPlayerWorldLoader.playerWorldLoaderEnabled ? "enabled" : "disabled") + " for " + CarpetPlayerWorldLoader.trackedPlayer.getEntityName() + "! Use a carpet bot in creative flying forward and let it run for a while. It will load everything near you."), true);
                                            return 1;
                                        })))))


                .then(CommandManager.literal("Get")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.literal("Settings").executes(context -> {
                            ServerCommandSource source = context.getSource();
                            FractalGenerator.getSettings(source);
                            return 1;
                        }))
                        .then(CommandManager.literal("Mode").executes(context -> {
                            ServerCommandSource source = context.getSource();
                            FractalGenerator.getMode(source);
                            return 1;
                        }))
                        .then(CommandManager.literal("Automata").executes(context -> {
                            ServerCommandSource source = context.getSource();
                            AutomataControl.getSettings(source);
                            return 1;
                        }))
                        .then(CommandManager.literal("Symmetrifier").executes(context -> {
                            ServerCommandSource source = context.getSource();
                            Symmetrifier.getSettings(source);
                            return 1;
                        })))


                .then(CommandManager.literal("CreateRandom")
                        .requires(source -> source.hasPermissionLevel(2)).then(CommandManager.literal("fractal_settings").executes(context -> {
                            ServerCommandSource source = context.getSource();
                            Random random = new Random();
                            FractalWorld.isModEnabled = false;
                            Database.changed = true;

                            FractalGenerator.MAX_ITER = random.nextInt(150) + 25;
                            FractalGenerator.MIN_ITER = random.nextInt(7) + 3;
                            FractalGenerator.setScale(random.nextInt(1000) + 10);
                            FractalGenerator.INVERTED_HEIGHT = random.nextBoolean();
                            FractalPresets.POWER3D = random.nextInt(50) + 1;
                            FractalPresets.setSeedValues(random.nextDouble() * 4.0 - 2.0, random.nextDouble() * 4.0 - 2.0);

                            if (WorldPainter.worldPainterEnabled || WorldPainter.worldPainterFullHeightEnabled) {
                                WorldPainter.setColorPalette(WorldPainter.COLOR_PALLETS.get(random.nextInt(WorldPainter.COLOR_PALLETS.size())));
                            }

                            FractalGenerator.getSettings(source);
                            return 1;
                        }))
                        .then(CommandManager.literal("fractalPreset").executes(context -> {
                            Random random = new Random();
                            FractalWorld.isModEnabled = false;
                            Database.changed = true;

                            if (random.nextBoolean()) {
                                FractalPresets.setFractalPreset(FractalPresets.FRACTALS_2D.get(random.nextInt(FractalPresets.FRACTALS_2D.size())));
                            } else {
                                FractalPresets.setFractalPreset(FractalPresets.FRACTALS_3D.get(random.nextInt(FractalPresets.FRACTALS_3D.size())));
                            }

                            context.getSource().sendFeedback(() -> Text.of("Fractal preset set to " + FractalPresets.fractalPreset + "."), true);
                            return 1;
                        }))));
    }
}