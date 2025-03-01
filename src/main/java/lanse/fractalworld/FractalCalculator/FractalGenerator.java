package lanse.fractalworld.FractalCalculator;

import lanse.fractalworld.ChunkProcessor;
import lanse.fractalworld.FractalWorld;
import lanse.fractalworld.WorldSorter.SortingGenerator;
import lanse.fractalworld.WorldSymmetrifier.Symmetrifier;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Objects;

public class FractalGenerator {
    public static int MAX_ITER = 50;
    public static int MIN_ITER = 5;
    public static int INITIAL_HEIGHT_OFFSET = 63;
    public static double scale = 0.5;
    public static int playerScale = 1;
    public static boolean INVERTED_HEIGHT = false;
    private static final double DEFAULT_SCALE = 0.5;
    public static boolean heightGeneratorEnabled = true;
    public static double xOffset = 0.0;
    public static double zOffset = 0.0;

    public static void setScale(double newScale) {
        //scale is what the math uses, playerScale is what it shows the player it is at.
        scale = DEFAULT_SCALE / newScale;
        playerScale = (int) newScale;
    }

    public static int getHeight(double x, double z, String dimensionType) {
        double mx;
        double mz;
        double tempPlayerScale = playerScale;
        String tempPreset = FractalPresets.fractalPreset;

        //This is also correctly telling the right dimension. (DEBUG TESTED)
        if (Objects.equals(dimensionType, "OVERWORLD")) {
            mx = x * scale - xOffset;
            mz = z * scale - zOffset;
        } else if (Objects.equals(dimensionType, "NETHER")) {
            setScale((double) playerScale / 8.0);
            mx = x * (scale) - xOffset;
            mz = z * (scale) - zOffset;
        } else { //END DIMENSION
            tempPreset = "2d_mandelbrot_fractal";
            //Outer end islands (mess around with these later) (This does nothing rn)
            if (Math.abs(x) > 500 || Math.abs(z) > 500){
                setScale(100000000);
                mx = x * scale - (0.006394660980056699 + (Math.sqrt(x * z) / 10000000));
                mz = z * scale + 0.6552201285415977;
            } else {
                setScale(75);
                //Inner end island (REMOVED THE X AND Z OFFSET)
                mx = x * scale - 0;
                mz = z * scale - 0;
            }
        }

        //All the hellish math is done in the fractalPreset class
        int iter = FractalPresets.createFractal(mx, mz);
        FractalPresets.fractalPreset = tempPreset;
        setScale(tempPlayerScale);

        //TODO - I think I want to just end the function here. ^
        // This way since now the fractal math is being stored to some sort of array, I can just search that
        // instead of constantly calling this.

        if (iter == -40404) return iter;

        return INITIAL_HEIGHT_OFFSET + (iter);
    }
    public static int[] get3DHeight(double x, double z, String dimensionType) {
        double mx;
        double mz;
        double tempPlayerScale = playerScale;
        String tempPreset = FractalPresets.fractalPreset;

        if (Objects.equals(dimensionType, "OVERWORLD")) {
            mx = x * scale - xOffset;
            mz = z * scale - zOffset;
        } else if (Objects.equals(dimensionType, "NETHER")) {
            setScale((double) playerScale / 8);
            mx = x * (scale) - xOffset;
            mz = z * (scale) - zOffset;
        } else { // END DIMENSION
            tempPreset = "2d_mandelbrot_fractal";
            // Adjust parameters based on distance for outer end islands
            if (Math.abs(x) > 500 || Math.abs(z) > 500) {
                setScale(100000000);
                mx = x * scale - (0.2957601666065326 + (Math.sqrt(x * z) / 10000000));
                mz = z * scale + 0.5794332427578901;
            } else {
                setScale(75);
                // Inner end island
                mx = x * scale - xOffset;
                mz = z * scale - zOffset;
            }
        }
        int[] iterArr = FractalPresets.create3DFractal(mx, mz);
        FractalPresets.fractalPreset = tempPreset;
        setScale(tempPlayerScale);

        return iterArr;
    }

    public static double[] findMinecraftCoordinates(double mx, double mz) {
        double x = (mx + xOffset) / scale;
        double z = (mz + zOffset) / scale;

        return new double[]{x, z};
    }
    public static double[] findComplexCoordinates(double x, double z) {
        double real = (x * scale) - xOffset;
        double imag = (z * scale) - zOffset;

        return new double[]{real, imag};
    }
    public static void tpPlayerToScale(ServerPlayerEntity player, int newScale) {
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        double[] complexCoords = findComplexCoordinates(x, z);

        double tempScale = scale;
        scale = DEFAULT_SCALE / newScale; //Temporary Scale conversion

        double[] newCoords = findMinecraftCoordinates(complexCoords[0], complexCoords[1]);
        double newX = newCoords[0];
        double newZ = newCoords[1];
        scale = tempScale;

        player.teleport(player.getServerWorld(), newX, y, newZ, player.getYaw(), player.getPitch());
    }

    public static void getSettings(ServerCommandSource source){
        int maxIterations = FractalGenerator.MAX_ITER;
        int minIterations = FractalGenerator.MIN_ITER;
        int scale = FractalGenerator.playerScale;
        int initialHeightOffset = FractalGenerator.INITIAL_HEIGHT_OFFSET;
        int chunkLoadingSpeed = FractalWorld.maxColumnsPerTick;
        int renderDistance = ChunkProcessor.MAX_RENDER_DIST;
        boolean permaSave = FractalWorld.permaSave;
        String fractalPreset = FractalPresets.fractalPreset;
        double seedReal = FractalPresets.seedReal;
        double seedImaginary = FractalPresets.seedImaginary;
        double mx = xOffset;
        double mz = zOffset;
        int power = FractalPresets.POWER3D;
        String colorPallet = WorldPainter.colorPallet;

        String settingsMessage = String.format(
                """     
                        Fractal World Settings:
                        
                        - Max Iterations: %d
                        - Min Iterations: %d
                        - Scale: %d
                        - Initial Height Offset: %d
                        - Chunk Loading Speed: %d
                        - Render Distance: %d
                        - Permanent Save Enabled: %b
                        - Fractal Preset: %s
                        - Fractal Seed: %f %f
                        - Fractal Offset: %f %f
                        - Fractal Power 3D: %d
                        - Color Pallet: %s""",
                maxIterations, minIterations, scale, initialHeightOffset,
                chunkLoadingSpeed, renderDistance, permaSave, fractalPreset, seedReal,
                seedImaginary, mx, mz, power, colorPallet
        );
        source.sendFeedback(() -> Text.literal(settingsMessage), false);
    }
    public static void getMode(ServerCommandSource source){
        boolean worldPainterEnabled = WorldPainter.worldPainterEnabled;
        boolean worldPainterFullHeightEnabled = WorldPainter.worldPainterFullHeightEnabled;
        boolean heightGeneratorEnabled = FractalGenerator.heightGeneratorEnabled;
        boolean inverted = FractalGenerator.INVERTED_HEIGHT;
        boolean sorter = SortingGenerator.WorldSorterIsEnabled;
        boolean symmetrifier = Symmetrifier.symmetrifierEnabled;

        String settingsMessage = String.format(
                """     
                        Fractal World Mode:
                        
                        - World Painter Enabled: %b
                        - World Painter Full Height Enabled: %b
                        - Terrain Height Generator Enabled: %b
                        - Terrain Height Inverted: %b
                        - World Sorter Enabled: %b
                        - World Symmetrifier Enabled: %b""",
                worldPainterEnabled, worldPainterFullHeightEnabled, heightGeneratorEnabled, inverted,
                sorter, symmetrifier
        );
        source.sendFeedback(() -> Text.literal(settingsMessage), false);
    }
    public static int mapToRange(double value) {
        return (int) (MIN_ITER + (value + 1) * (MAX_ITER - MIN_ITER) / 2);
    }
}