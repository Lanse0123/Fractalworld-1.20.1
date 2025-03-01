package lanse.fractalworld.Storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import lanse.fractalworld.Automata.AutomataControl;
import lanse.fractalworld.Automata.AutomataPresets;
import lanse.fractalworld.ChunkProcessor;
import lanse.fractalworld.FractalCalculator.*;
import lanse.fractalworld.FractalWorld;
import lanse.fractalworld.WorldSorter.SorterPresets;
import lanse.fractalworld.WorldSorter.SortingGenerator;
import lanse.fractalworld.WorldSymmetrifier.Symmetrifier;
import net.minecraft.server.MinecraftServer;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class Database {
    public static boolean changed = false;

    public static Database getOrCreate(MinecraftServer server) {

        Path configFolder = server.getRunDirectory().toPath().resolve("config/fractalworld");
        Path configFile = configFolder.resolve("config.json");

        // Check if the JSON config file exists
        if (!Files.exists(configFile)) {
            try {
                Files.createDirectories(configFolder);
            } catch (IOException ignored) {}
        }
        return new Database();
    }

    //Save Data to JSON
    public void saveToJson(Path configFolder) {
        Path configFile = configFolder.resolve("config.json");

        try {
            //TODO - I AM SURE THIS COULD BE BETTER

            //To overwrite data, delete the current file.
            if (Files.exists(configFile)) {
                Files.delete(configFile);
            }

            Files.createFile(configFile);

            try (Writer writer = Files.newBufferedWriter(configFile)) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                JsonObject jsonConfig = toJson();
                gson.toJson(jsonConfig, writer);
            }
        } catch (IOException ignored) {}
    }

    //Save Data to JSON
    public JsonObject toJson() {
        JsonObject json = new JsonObject();

        json.addProperty("maxRender", ChunkProcessor.MAX_RENDER_DIST);
        json.addProperty("overworldIsDisabled", ChunkProcessor.overworldIsDisabled);
        json.addProperty("netherIsDisabled", ChunkProcessor.netherIsDisabled);
        json.addProperty("endIsDisabled", ChunkProcessor.endIsDisabled);

        json.addProperty("maxColumnsPerTick", FractalWorld.maxColumnsPerTick);
        json.addProperty("permaSave", FractalWorld.permaSave);
        json.addProperty("refreshRate", FractalWorld.refreshRate);
        json.addProperty("autoRefreshModeIsOn", FractalWorld.autoRefreshModeIsOn);
        json.addProperty("maxThreads", FractalWorld.maxThreads);

        json.addProperty("maxIter", FractalGenerator.MAX_ITER);
        json.addProperty("minIter", FractalGenerator.MIN_ITER);
        json.addProperty("initialHeightOffset", FractalGenerator.INITIAL_HEIGHT_OFFSET);
        json.addProperty("scale", FractalGenerator.scale);
        json.addProperty("playerScale", FractalGenerator.playerScale);
        json.addProperty("invertedHeight", FractalGenerator.INVERTED_HEIGHT);
        json.addProperty("heightGeneratorEnabled", FractalGenerator.heightGeneratorEnabled);
        json.addProperty("xOffset", FractalGenerator.xOffset);
        json.addProperty("zOffset", FractalGenerator.zOffset);

        json.addProperty("fractalPreset", FractalPresets.fractalPreset);
        json.addProperty("seedReal", FractalPresets.seedReal);
        json.addProperty("seedImaginary", FractalPresets.seedImaginary);
        json.addProperty("power3D", FractalPresets.POWER3D);

        String formula0 = CustomFractalCalculator.customFractalFormula[0];
        String formula1 = CustomFractalCalculator.customFractalFormula[1];
        String formula2 = CustomFractalCalculator.customFractalFormula[2];
        json.addProperty("formula0", formula0);
        json.addProperty("formula1", formula1);
        json.addProperty("formula2", formula2);

        json.addProperty("colorPalette", WorldPainter.colorPallet);
        json.addProperty("worldPainterEnabled", WorldPainter.worldPainterEnabled);
        json.addProperty("worldPainterFullHeightEnabled", WorldPainter.worldPainterFullHeightEnabled);
        json.addProperty("customPalette", WorldPainter.getPaletteByID());

        json.addProperty("automataRule", AutomataPresets.rule);
        json.addProperty("automataIsEnabled", AutomataControl.automataIsEnabled);
        json.addProperty("automataPreset", AutomataControl.automataPreset);

        json.addProperty("sorterPreset", SorterPresets.sorterPreset);
        json.addProperty("worldSorterIsEnabled", SortingGenerator.WorldSorterIsEnabled);

        json.addProperty("symmetrifierEnabled", Symmetrifier.symmetrifierEnabled);
        json.addProperty("verticalMirrorWorldEnabled", Symmetrifier.verticalMirrorWorldEnabled);
        json.addProperty("circleGen", Symmetrifier.circleGen);
        json.addProperty("symmetrifier", Symmetrifier.symmetrifier);
        json.addProperty("numberOfCorners", Symmetrifier.numberOfCorners);

        json.addProperty("clearMode", ColumnClearer.currentMode.name());

        changed = false;
        return json;
    }

    //Load data from JSON
    public static void fromJson(JsonObject json) {
        ChunkProcessor.MAX_RENDER_DIST = getInt(json, "maxRender", 8);
        ChunkProcessor.overworldIsDisabled = getBoolean(json, "overworldIsDisabled", false);
        ChunkProcessor.overworldIsDisabled = getBoolean(json, "netherIsDisabled", false);
        ChunkProcessor.overworldIsDisabled = getBoolean(json, "endIsDisabled", false);

        FractalWorld.maxColumnsPerTick = getInt(json, "maxColumnsPerTick", 100);
        FractalWorld.permaSave = getBoolean(json, "permaSave", false);
        FractalWorld.refreshRate = getInt(json, "refreshRate", 10000);
        FractalWorld.autoRefreshModeIsOn = getBoolean(json, "autoRefreshModeIsOn", false);
        FractalWorld.maxThreads = getInt(json, "maxThreads", 1);

        FractalGenerator.MAX_ITER = getInt(json, "maxIter", 50);
        FractalGenerator.MIN_ITER = getInt(json, "minIter", 5);
        FractalGenerator.INITIAL_HEIGHT_OFFSET = getInt(json, "initialHeightOffset", 63);
        FractalGenerator.scale = getDouble(json, "scale", 0.5);
        FractalGenerator.playerScale = getInt(json, "playerScale", 1);
        FractalGenerator.INVERTED_HEIGHT = getBoolean(json, "invertedHeight", false);
        FractalGenerator.heightGeneratorEnabled = getBoolean(json, "heightGeneratorEnabled", true);
        FractalGenerator.xOffset = getDouble(json, "xOffset", 0.0);
        FractalGenerator.zOffset = getDouble(json, "zOffset", 0.0);

        FractalPresets.fractalPreset = getString(json, "fractalPreset", "2d_mandelbrot_fractal");
        FractalPresets.seedReal = getDouble(json, "seedReal", -0.7);
        FractalPresets.seedImaginary = getDouble(json, "seedImaginary", 0.27015);
        FractalPresets.POWER3D = getInt(json, "power3D", 8);

        CustomFractalCalculator.customFractalFormula = new String[]{
                getString(json, "formula0", "zx * zx - zy * zy + x"),
                getString(json, "formula1", "2 * zx * zy + y"),
                getString(json, "formula2", "4")
        };

        WorldPainter.colorPallet = getString(json, "colorPalette", "");
        WorldPainter.worldPainterEnabled = getBoolean(json, "worldPainterEnabled", false);
        WorldPainter.worldPainterFullHeightEnabled = getBoolean(json, "worldPainterFullHeightEnabled", false);
        WorldPainter.setPaletteByID(getString(json, "customPalette", "147,148,233"));

        AutomataPresets.rule = getInt(json, "automataRule", 0);
        AutomataControl.automataIsEnabled = getBoolean(json, "automataIsEnabled", false);
        AutomataControl.automataPreset = getString(json, "automataPreset", "");

        SorterPresets.sorterPreset = getString(json, "sorterPreset", "");
        SortingGenerator.WorldSorterIsEnabled = getBoolean(json, "worldSorterIsEnabled", false);

        Symmetrifier.symmetrifierEnabled = getBoolean(json, "symmetrifierEnabled", false);
        Symmetrifier.verticalMirrorWorldEnabled = getBoolean(json, "verticalMirrorWorldEnabled", false);
        Symmetrifier.circleGen = getBoolean(json, "circleGen", false);
        Symmetrifier.symmetrifier = getBoolean(json, "symmetrifier", false);
        Symmetrifier.numberOfCorners = getInt(json, "numberOfCorners", 0);

        String clearModeString = getString(json, "clearMode", "VOID");
        try { ColumnClearer.currentMode = ColumnClearer.ClearMode.valueOf(clearModeString);
        } catch (IllegalArgumentException e) { ColumnClearer.currentMode = ColumnClearer.ClearMode.VOID; }

        //More future variables here if I ever update this
    }

    public static void returnToDefaultValues() {
        FractalWorld.isModEnabled = false;

        ChunkProcessor.MAX_RENDER_DIST = 8;
        ChunkProcessor.overworldIsDisabled = false;
        ChunkProcessor.netherIsDisabled = false;
        ChunkProcessor.endIsDisabled = false;

        FractalWorld.maxColumnsPerTick = 100;
        FractalWorld.permaSave = false;
        FractalWorld.refreshRate = 10000;
        FractalWorld.autoRefreshModeIsOn = false;
        FractalWorld.maxThreads = 1;

        FractalGenerator.MAX_ITER = 50;
        FractalGenerator.MIN_ITER = 5;
        FractalGenerator.INITIAL_HEIGHT_OFFSET = 63;
        FractalGenerator.scale = 0.5;
        FractalGenerator.playerScale = 1;
        FractalGenerator.INVERTED_HEIGHT = false;
        FractalGenerator.heightGeneratorEnabled = true;
        FractalGenerator.xOffset = 0;
        FractalGenerator.zOffset = 0;

        FractalPresets.fractalPreset = "2d_mandelbrot_fractal";
        FractalPresets.seedReal = -0.7;
        FractalPresets.seedImaginary = 0.27015;
        FractalPresets.POWER3D = 8;

        WorldPainter.colorPallet = "full_rainbow";
        WorldPainter.worldPainterEnabled = false;
        WorldPainter.worldPainterFullHeightEnabled = false;

        AutomataPresets.rule = 34;
        AutomataControl.automataIsEnabled = false;
        AutomataControl.automataPreset = "wolfram";

        SorterPresets.sorterPreset = "player_chunk_insertion_sort";
        SortingGenerator.WorldSorterIsEnabled = false;

        Symmetrifier.symmetrifierEnabled = false;
        Symmetrifier.verticalMirrorWorldEnabled = false;
        Symmetrifier.circleGen = false;
        Symmetrifier.symmetrifier = true;
        Symmetrifier.numberOfCorners = 4;
    }

    private static int getInt(JsonObject json, String key, int defaultValue) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsInt() : defaultValue;
    }

    private static boolean getBoolean(JsonObject json, String key, boolean defaultValue) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsBoolean() : defaultValue;
    }

    private static double getDouble(JsonObject json, String key, double defaultValue) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsDouble() : defaultValue;
    }

    private static String getString(JsonObject json, String key, String defaultValue) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : defaultValue;
    }
}