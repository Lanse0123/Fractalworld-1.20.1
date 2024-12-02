package lanse.fractalworld;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import lanse.fractalworld.FractalCalculator.ColumnClearer;
import lanse.fractalworld.FractalCalculator.FractalGenerator;
import lanse.fractalworld.FractalCalculator.FractalPresets;
import lanse.fractalworld.FractalCalculator.WorldPainter;
import lanse.fractalworld.WorldSorter.SorterPresets;
import lanse.fractalworld.WorldSorter.SortingGenerator;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Random;

public class Commands {

    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("FractalWorldOn")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    if (FractalGenerator.MIN_ITER > FractalGenerator.MAX_ITER){
                        context.getSource().sendError(Text.of("Minimum Iterations can not be Greater than Maximum Iterations!"));
                        return 1;
                    }
                    if (!FractalGenerator.heightGeneratorEnabled && !WorldPainter.worldPainterEnabled && !WorldPainter.worldPainterFullHeightEnabled){
                        context.getSource().sendError(Text.of("Enable WorldPainter, HeightGenerator or both to start!"));
                        return 1;
                    }
                    DimensionHandler.resetDimensionHandler();
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

        dispatcher.register(CommandManager.literal("FractalWorldReset")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
            ChunkGenerationListener.clearProcessedChunks();
            context.getSource().sendFeedback(() -> Text.literal("FractalWorld processing queue and updated chunks reset!"), true);
            return 1;
        }));

        dispatcher.register(CommandManager.literal("FractalWorldSet")
                .requires(source -> source.hasPermissionLevel(2)).then(CommandManager.literal("colorPalette")
                        .then(CommandManager.argument("palette", StringArgumentType.string())
                                .suggests((context, builder) -> CommandSource.suggestMatching(WorldPainter.COLOR_PALLETS, builder)).executes(context -> {
                                    String palette = StringArgumentType.getString(context, "palette");
                                    palette = palette.toLowerCase();
                                    String finalPalette = palette;
                                    context.getSource().sendFeedback(() -> Text.literal("Color palette set to " + finalPalette + "."), true);
                                    WorldPainter.setColorPalette(palette);
                                    return 1;
                                })))
                .then(CommandManager.literal("fractalPreset")
                        .then(CommandManager.argument("preset", StringArgumentType.string())
                                .suggests((context, builder) -> CommandSource.suggestMatching(FractalPresets.getFractalNames(), builder))
                                .executes(context -> {
                                    {
                                        String preset = StringArgumentType.getString(context, "preset");
                                        if (FractalPresets.isValidPreset(preset)) {
                                            FractalPresets.setFractalPreset(preset);

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
                                .then(CommandManager.argument("imaginary", DoubleArgumentType.doubleArg(-2.0, 2.0))
                                        .executes(context -> {
                                            double real = DoubleArgumentType.getDouble(context, "real");
                                            double imaginary = DoubleArgumentType.getDouble(context, "imaginary");

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
                                            FractalGenerator.xOffset = DoubleArgumentType.getDouble(context, "mx");
                                            FractalGenerator.zOffset = DoubleArgumentType.getDouble(context, "mz");
                                            context.getSource().sendFeedback(
                                                    () -> Text.literal("Fractal offsets set to: X Offset = " + FractalGenerator.xOffset + ", Z Offset = " + FractalGenerator.zOffset),
                                                    true
                                            );
                                            return 1;
                                        }))))
                .then(CommandManager.literal("mode")
                        .then(CommandManager.argument("feature", StringArgumentType.string())
                                .suggests((context, builder) -> CommandSource.suggestMatching(new String[]{
                                        "WorldPainter", "WorldPainterFullHeight", "HeightGenerator", "Inverted_Height", "WorldSorter"
                                }, builder))
                                .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                        .executes(context -> {
                                            String feature = StringArgumentType.getString(context, "feature");
                                            boolean enabled = BoolArgumentType.getBool(context, "enabled");

                                            switch (feature.toLowerCase()) {
                                                case "worldpainter" -> {
                                                    WorldPainter.setWorldPainter(enabled);
                                                    context.getSource().sendFeedback(() -> Text.literal("World Painter set to: " + enabled), true);
                                                }
                                                case "worldpainterfullheight" -> {
                                                    WorldPainter.setWorldPainterFullHeight(enabled);
                                                    context.getSource().sendFeedback(() -> Text.literal("World Painter Full Height set to: " + enabled), true);
                                                }
                                                case "worldsorter" -> {
                                                    SortingGenerator.WorldSorterIsEnabled = enabled;
                                                    context.getSource().sendFeedback(() -> Text.literal("World Sorter set to: " + enabled + ". This mode is still experimental, and incomplete. Some of the sorters might lag or crash at high settings. This converts the mod from a fractal loader into a sorting algorithm."), true);
                                                }
                                                case "heightgenerator" -> {
                                                    FractalGenerator.heightGeneratorEnabled = enabled;
                                                    if (FractalGenerator.MAX_ITER > 250) FractalGenerator.MAX_ITER = 250;
                                                    context.getSource().sendFeedback(() -> Text.literal("Terrain Height Generator set to: " + enabled), true);
                                                }
                                                case "inverted_height" -> {
                                                    FractalGenerator.INVERTED_HEIGHT = enabled;
                                                    context.getSource().sendFeedback(() -> Text.literal("Terrain Height Inversion set to: " + enabled), true);
                                                }
                                                default -> {
                                                    context.getSource().sendFeedback(() -> Text.literal("Invalid feature."), false);
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
                        .then(CommandManager.argument("setting", StringArgumentType.string())
                                .suggests((context, builder) -> CommandSource.suggestMatching(new String[]{
                                        "max_iterations", "min_iterations", "scale", "chunk_loading_speed",
                                        "render_distance", "initial_height_offset", "smoothing_value", "power3d", "fractal_offset"
                                }, builder)).then(CommandManager.argument("value", IntegerArgumentType.integer()).executes(context -> {
                                    String setting = StringArgumentType.getString(context, "setting").toLowerCase();
                                    int value = IntegerArgumentType.getInteger(context, "value");

                                    switch (setting) {
                                        case "max_iterations" -> {
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
                                        case "min_iterations" -> {
                                            if (value >= 0 && value <= 20) {
                                                FractalGenerator.MIN_ITER = value;
                                                context.getSource().sendFeedback(() -> Text.of("Min Iterations set to: " + value), true);
                                            } else {
                                                context.getSource().sendError(Text.of("Please specify a value between 1 and 20."));
                                            }
                                        }
                                        case "scale" -> {
                                            if (value >= 1 && value <= Integer.MAX_VALUE - 1) {
                                                FractalGenerator.setScale(value);
                                                context.getSource().sendFeedback(() -> Text.of("Scale set to: " + value), true);
                                            } else {
                                                context.getSource().sendError(Text.of("Please specify a value between 1 and 2,147,483,646."));
                                            }
                                        }
                                        case "initial_height_offset" -> {
                                            if (value >= -64 && value <= 250) {
                                                FractalGenerator.INITIAL_HEIGHT_OFFSET = value;
                                                context.getSource().sendFeedback(() -> Text.of("Initial Height Offset set to: " + value), true);
                                            } else {
                                                context.getSource().sendError(Text.of("Please specify a value between -64 and 250."));
                                            }
                                        }
                                        case "smoothing_value" -> {
                                            if (value >= 1 && value <= 10) {
                                                FractalGenerator.SMOOTHING_VALUE = value;
                                                context.getSource().sendFeedback(() -> Text.of("Smoothing Value set to: " + value), true);
                                            } else {
                                                context.getSource().sendError(Text.of("Please specify a value between 1 and 10."));
                                            }
                                        }
                                        case "chunk_loading_speed" -> {
                                            if (value >= 1 && value <= 1000) {
                                                FractalWorld.maxColumnsPerTick = value;
                                                context.getSource().sendFeedback(() -> Text.of("Chunk Loading Speed set to: " + value), true);
                                            } else {
                                                context.getSource().sendError(Text.of("Please specify a value between 1 and 1,000."));
                                            }
                                        }
                                        case "render_distance" -> {
                                            if (value >= 2 && value <= 10000000) {
                                                ChunkGenerationListener.MAX_RENDER_DIST = value;
                                                context.getSource().sendFeedback(() -> Text.of("Render Transform Distance set to: " + value), true);
                                            } else {
                                                context.getSource().sendError(Text.of("Please specify a value between 2 and 10,000,000."));
                                            }
                                        }
                                        case "power3d" -> {
                                            if (value >= 1 && value <= 100) {
                                                FractalPresets.POWER3D = value;
                                                context.getSource().sendFeedback(() -> Text.of("3D Power multiplier set to: " + value), true);
                                            } else {
                                                context.getSource().sendError(Text.of("Please specify a value between 1 and 100."));
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
                                    context.getSource().sendFeedback(() -> Text.literal("PermaSave set to: " + enabled), true);
                                    return 1;
                                })))
                .then(CommandManager.literal("sorterPreset")
                        .then(CommandManager.argument("preset", StringArgumentType.string())
                                .suggests((context, builder) -> CommandSource.suggestMatching(SorterPresets.SORTING_PRESETS, builder))
                                .executes(context -> {
                                    String preset = StringArgumentType.getString(context, "preset");
                                    if (SorterPresets.isValidPreset(preset)) {
                                        SorterPresets.setSorterPreset(preset);
                                        context.getSource().sendFeedback(() -> Text.literal("Sorter preset set to: " + preset), true);
                                    } else {
                                        context.getSource().sendError(Text.literal("Invalid preset."));
                                    }
                                    FractalWorld.isModEnabled = false;
                                    return 1;
                                }))));


        dispatcher.register(CommandManager.literal("FractalWorldFind")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("MinecraftCoords")
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
                })));


        dispatcher.register(CommandManager.literal("FractalWorldCreateRandom")
                .requires(source -> source.hasPermissionLevel(2)).then(CommandManager.literal("settings").executes(context -> {
                    ServerCommandSource source = context.getSource();
                    Random random = new Random();
                    FractalWorld.isModEnabled = false;

                    FractalGenerator.MAX_ITER = random.nextInt(150) + 25;
                    FractalGenerator.MIN_ITER = random.nextInt(7) + 3;
                    FractalGenerator.setScale(random.nextInt(1000) + 10);
                    FractalGenerator.SMOOTHING_VALUE = random.nextInt(10) + 1;
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