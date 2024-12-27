package lanse.fractalworld;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import lanse.fractalworld.Automata.AutomataControl;
import lanse.fractalworld.Automata.AutomataPresets;
import lanse.fractalworld.ChunkRandomizer.ChunkRandomizer;
import lanse.fractalworld.FractalCalculator.ColumnClearer;
import lanse.fractalworld.FractalCalculator.FractalGenerator;
import lanse.fractalworld.FractalCalculator.FractalPresets;
import lanse.fractalworld.FractalCalculator.WorldPainter;
import lanse.fractalworld.WorldSorter.SorterPresets;
import lanse.fractalworld.WorldSorter.SortingGenerator;
import lanse.fractalworld.WorldSymmetrifier.Symmetrifier;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Random;

public class Commands {

    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("FractalWorldOn")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    if (FractalGenerator.MIN_ITER > FractalGenerator.MAX_ITER){
                        context.getSource().sendError(Text.of("Minimum Iterations can not be Greater than Maximum Iterations!"));
                        return 1;
                    }
                    if (!FractalGenerator.heightGeneratorEnabled && !WorldPainter.worldPainterEnabled
                            && !WorldPainter.worldPainterFullHeightEnabled && !Symmetrifier.symmetrifierEnabled
                            && !SortingGenerator.WorldSorterIsEnabled && !Symmetrifier.verticalMirrorWorldEnabled
                            && !ChunkRandomizer.isChunkRandomizerEnabled && !AutomataControl.automataIsEnabled){
                        context.getSource().sendError(Text.of("Enable a mode to start!"));
                        return 1;
                    }
                    DimensionHandler.resetDimensionHandler();
                    FractalWorld.originalServer = context.getSource().getServer();
                    FractalWorld.isModEnabled = true;
                    context.getSource().sendFeedback(() -> Text.literal("FractalWorld Enabled!"), true);
                    return 1;
                }));

        dispatcher.register(CommandManager.literal("FractalWorldOff")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    FractalWorld.isModEnabled = false;
                    context.getSource().sendFeedback(() -> Text.literal("FractalWorld Disabled!"), true);
                    return 1;
                }));

        dispatcher.register(CommandManager.literal("FractalWorldHelp")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    MutableText message = Text.literal("Need help with FractalWorld? Click the link: ")
                            .append(Text.literal("FractalWorld Wiki").setStyle(Style.EMPTY
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/Lanse0123/Fractalworld-1.20.1/wiki"))
                                    .withColor(Formatting.AQUA).withUnderline(true)));
                    context.getSource().sendFeedback(() -> message, false);
                    return 1;
                }));

        dispatcher.register(CommandManager.literal("FractalWorldResetChunks")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    ChunkGenerationListener.clearProcessedChunks();
                    context.getSource().sendFeedback(() -> Text.literal("FractalWorld processing queue and updated chunks reset!"), true);
                    return 1;
                }));

        dispatcher.register(CommandManager.literal("FractalWorldReturnToDefaultSettings").requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("confirm")).then(CommandManager.argument("confirm", StringArgumentType.string())
                        .suggests((context, builder) -> CommandSource.suggestMatching(new String[]{"confirm"}, builder)).executes(context -> {
                    DataSaver.returnToDefaultValues(); //FFS THIS COMMAND WAS SUCH A PAIN
                    context.getSource().sendFeedback(() -> Text.literal("FractalWorld settings have returned to default!"), true);
                    return 1;
                })));

        dispatcher.register(CommandManager.literal("FractalWorldSet")
                .requires(source -> source.hasPermissionLevel(2)).then(CommandManager.literal("colorPalette")
                        .then(CommandManager.argument("palette", StringArgumentType.string())
                                .suggests((context, builder) -> CommandSource.suggestMatching(WorldPainter.COLOR_PALLETS, builder)).executes(context -> {
                                    String palette = StringArgumentType.getString(context, "palette");
                                    palette = palette.toLowerCase();
                                    String finalPalette = palette;
                                    DataSaver.changed = true;
                                    context.getSource().sendFeedback(() -> Text.literal("Color palette set to " + finalPalette + "."), true);
                                    WorldPainter.setColorPalette(palette);
                                    return 1;
                                })))
                .then(CommandManager.literal("fractalPreset")
                        .then(CommandManager.argument("preset", StringArgumentType.string())
                                .suggests((context, builder) -> CommandSource.suggestMatching(FractalPresets.getFractalNames(), builder)).executes(context -> {{
                                    String preset = StringArgumentType.getString(context, "preset");
                                    if (FractalPresets.isValidPreset(preset)) {
                                        FractalPresets.setFractalPreset(preset);
                                        DataSaver.changed = true;

                                        if (FractalPresets.isSeededFractal(preset)) {

                                            if (preset.equals("3d_mandelbox_fractal")) {
                                                context.getSource().sendFeedback(() -> Text.literal("Fractal preset set to: " + preset + ". This fractal requires a seed. Please set the seed values using /FractalWorldSet fractalSeed <real> <imaginary>. Mandelbox only uses the real seed parameter, it ignores the imaginary."), true);
                                            } else {
                                                context.getSource().sendFeedback(() -> Text.literal("Fractal preset set to: " + preset + ". This fractal requires a seed. Please set the seed values using /FractalWorldSet fractalSeed <real> <imaginary>."), true);
                                            }

                                        } else {
                                            context.getSource().sendFeedback(() -> Text.literal("Fractal preset set to: " + preset), true);
                                        }

                                    } else {
                                        context.getSource().sendError(Text.literal("Invalid preset."));
                                        FractalWorld.isModEnabled = false;
                                    }
                                    return 1;
                                }
                                })))
                .then(CommandManager.literal("fractalSeed")
                        .then(CommandManager.argument("real", DoubleArgumentType.doubleArg(-2.0, 2.0))
                                .then(CommandManager.argument("imaginary", DoubleArgumentType.doubleArg(-2.0, 2.0)).executes(context -> {
                                    double real = DoubleArgumentType.getDouble(context, "real");
                                    double imaginary = DoubleArgumentType.getDouble(context, "imaginary");
                                    DataSaver.changed = true;

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
                                            DataSaver.changed = true;
                                            FractalGenerator.xOffset = DoubleArgumentType.getDouble(context, "mx");
                                            FractalGenerator.zOffset = DoubleArgumentType.getDouble(context, "mz");
                                            context.getSource().sendFeedback(() -> Text.literal("Fractal offsets set to: X Offset = " + FractalGenerator.xOffset + ", Z Offset = " + FractalGenerator.zOffset), true);
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
                                    DataSaver.changed = true;

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
                                        case "chunkrandomizer" -> {
                                            ChunkRandomizer.isChunkRandomizerEnabled = enabled;
                                            context.getSource().sendFeedback(() -> Text.literal("Chunk Randomizer set to: " + enabled + ". This overrides the mod from a fractal loader into a chunk randomizer."), true);
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
                                "xray", "void", "ocean", "lava_ocean", "monolith", "none", "randomize"
                        }, builder)).executes(context -> {
                            String mode = StringArgumentType.getString(context, "mode").toLowerCase();
                            ServerCommandSource source = context.getSource();
                            ColumnClearer.resetColumnClearer();
                            DataSaver.changed = true;

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
                                    source.sendFeedback(() -> Text.literal("Column Clearer set to: Randomize."), true);
                                }
                                default -> {
                                    source.sendFeedback(() -> Text.literal("Invalid column clearer mode."), false);
                                    return 0;
                                }
                            }
                            return 1;
                        })))
                .then(CommandManager.literal("setting")
                        //TODO - add automata and refresh rate stuff to the wiki.
                        .then(CommandManager.argument("setting", StringArgumentType.string())
                                .suggests((context, builder) -> CommandSource.suggestMatching(new String[]{
                                        "fractal_max_iterations", "fractal_min_iterations", "fractal_scale",
                                        "main_column_loading_speed", "main_render_distance", "fractal_initial_height_offset",
                                        "fractal_power3d", "symmetrifier_corners", "automata_rule", "advanced_auto_refresh_rate"
                                }, builder)).then(CommandManager.argument("value", IntegerArgumentType.integer()).executes(context -> {
                                    String setting = StringArgumentType.getString(context, "setting").toLowerCase();
                                    int value = IntegerArgumentType.getInteger(context, "value");
                                    DataSaver.changed = true;

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
                                                ChunkGenerationListener.MAX_RENDER_DIST = value;
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
                                        default -> context.getSource().sendError(Text.of("Unknown setting."));
                                    }
                                    return 1;
                                }))))
                .then(CommandManager.literal("permaSave")
                        .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> {
                                    boolean enabled = BoolArgumentType.getBool(context, "enabled");
                                    FractalWorld.permaSave = enabled;
                                    DataSaver.changed = true;
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
                                    DataSaver.changed = true;

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
                                    DataSaver.changed = true;
                                    return 1;
                                }))));


        dispatcher.register(CommandManager.literal("FractalWorldFind")
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
                        }))));


        dispatcher.register(CommandManager.literal("FractalWorldGet")
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
                })));


        dispatcher.register(CommandManager.literal("FractalWorldCreateRandom")
                .requires(source -> source.hasPermissionLevel(2)).then(CommandManager.literal("fractal_settings").executes(context -> {
                    ServerCommandSource source = context.getSource();
                    Random random = new Random();
                    FractalWorld.isModEnabled = false;
                    DataSaver.changed = true;

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
                    DataSaver.changed = true;

                    if (random.nextBoolean()) {
                        FractalPresets.setFractalPreset(FractalPresets.FRACTALS_2D.get(random.nextInt(FractalPresets.FRACTALS_2D.size())));
                    } else {
                        FractalPresets.setFractalPreset(FractalPresets.FRACTALS_3D.get(random.nextInt(FractalPresets.FRACTALS_3D.size())));
                    }

                    context.getSource().sendFeedback(() -> Text.of("Fractal preset set to " + FractalPresets.fractalPreset + "."), true);
                    return 1;
                })));
    }
}
