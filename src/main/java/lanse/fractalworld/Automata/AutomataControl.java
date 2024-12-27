package lanse.fractalworld.Automata;

import net.minecraft.block.Block;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

import java.util.*;

public class AutomataControl {

    public static Queue<AutomataTask> automataQueue = new LinkedList<>();
    public static Queue<WireTask> wireQueue = new LinkedList<>();
    public static boolean automataIsEnabled = false;
    public static Block lastBlock = Blocks.GRASS_BLOCK;
    public static String automataPreset = "wolfram";

    //TODO - split the list into 2 categories: 2D and 3D
    public static final List<String> AUTOMATA_LIST = Arrays.asList(
            "2D_wolfram", "2D_life", "2D_coral", "2D_invasiveCoral", "2D_maze", "2D_highlife", "2D_amoeba", "2D_cave",
            "2D_wireWorld", "2D_sponge"
    );

    public static void getSettings(ServerCommandSource source) {
        boolean isEnabled = automataIsEnabled;
        String preset = automataPreset;
        int rule = AutomataPresets.rule;

        String settingsMessage = String.format(
                """     
                        Automata Settings:
                        
                        - Is Automata mode enabled: %b
                        - Automata preset: %s
                        - Rule: %d""",
                isEnabled, preset, rule
        );
        source.sendFeedback(() -> Text.literal(settingsMessage), false);
    }

    public static void draw(ServerWorld world, int x, int z) {

        switch (automataPreset) {
            case "2D_wolfram" -> AutomataPresets.wolframElementaryAutomataDRAW2D(world, x, z);
            case "2D_life" -> AutomataPresets.conwaysLifeDRAW2D(world, x, z);
            case "2D_highlife" -> AutomataPresets.highlifeDRAW2D(world, x, z);
            case "2D_coral" -> AutomataPresets.coralDRAW2D(world, x, z);
            case "2D_invasiveCoral" -> AutomataPresets.invasiveCoralDRAW2D(world, x, z);
            case "2D_maze" -> AutomataPresets.mazeDRAW2D(world, x, z);
            case "2D_amoeba" -> AutomataPresets.amoebaDRAW2D(world, x, z);
            case "2D_cave" -> AutomataPresets.caveCreatorDRAW2D(world, x, z);
            case "2D_sponge" -> AutomataPresets.spongeDRAW2D(world, x, z);

            //Special / complex ones
            case "2D_wireWorld" -> AutomataPresets.wireWorldDRAW2D(world, x, z);
        }
    }
    public static void completeDrawing() {
        boolean previousGenerationIsAlive;

        if (Objects.equals(automataPreset, "2D_wireWorld")) {

            while (!wireQueue.isEmpty()) {
                WireTask task = wireQueue.poll();
                if (task == null) return;
                BlockPos blockPos = task.world.getTopPosition(Heightmap.Type.WORLD_SURFACE, new BlockPos(task.x, 0, task.z)).down();
                task.world.setBlockState(blockPos, task.block.getDefaultState());
            }

        } else {
            while (!automataQueue.isEmpty()) {
                AutomataTask task = automataQueue.poll();

                if (task == null) return;

                BlockPos blockPos = task.world.getTopPosition(Heightmap.Type.WORLD_SURFACE, new BlockPos(task.x, 0, task.z)).down();

                if (task.isAlive) {
                    task.world.setBlockState(blockPos, Blocks.BLACK_CONCRETE.getDefaultState());

                } else {
                    previousGenerationIsAlive = isAlive(task.world, task.x, task.z);

                    if (previousGenerationIsAlive) {
                        task.world.setBlockState(blockPos, lastBlock.getDefaultState());

                    } else {
                        lastBlock = task.world.getBlockState(blockPos).getBlock();
                    }
                }
            }
        }
    }

    //Overloaded function for normal automata and WireWorld.
    public static void addToQueue(AutomataTask task) {
        automataQueue.add(task);
        if (automataQueue.size() > 1500) completeDrawing();
    }

    public static void addToQueue(WireTask task) {
        wireQueue.add(task);
        if (wireQueue.size() > 1500) completeDrawing();
    }

    public record AutomataTask(ServerWorld world, int x, int z, boolean isAlive) {}

    public record WireTask(ServerWorld world, int x, int z, Block block) {}
    public static boolean isAlive(ServerWorld world, int x, int z) {
        BlockPos topPos = world.getTopPosition(Heightmap.Type.WORLD_SURFACE, new BlockPos(x, 0, z)).down();
        return world.getBlockState(topPos).isOf(Blocks.BLACK_CONCRETE);
    }

    public static int countLiveNeighbors(ServerWorld world, int x, int z) {
        int count = 0;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue; // Skip the center cell
                if (AutomataControl.isAlive(world, x + dx, z + dz)) {
                    count++;
                }
            }
        }
        return count;
    }

    //This is for WireWorld
    public static int countElectronHeadNeighbors(ServerWorld world, int x, int z) {
        int count = 0;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue; // Skip the center cell
                BlockPos neighborPos = world.getTopPosition(Heightmap.Type.WORLD_SURFACE, new BlockPos(x + dx, 0, z + dz)).down();
                if (world.getBlockState(neighborPos).isOf(Blocks.BLUE_CONCRETE)) {
                    count++;
                }
            }
        }
        return count;
    }

    public static double noiseFunction(int x, int z) {
        // Simple pseudo-random function for consistent results
        long seed = x * 49632L + z * 325176L;
        seed = (seed << 13) ^ seed;
        return (1.0 + (seed * (seed * seed * 15731L + 789221L) + 1376312589L & 0x7fffffff) / 1073741824.0) / 2.0;
    }

    public static boolean isMengerSponge(int x, int z) {
        x = Math.abs(x);
        z = Math.abs(z);

        while (x > 0 || z > 0) {
            if (x % 3 == 1 && z % 3 == 1) {
                return false;
            }
            x /= 3;
            z /= 3;
        }
        return true;
    }
    public static boolean isDiagonalMengerSponge(int x, int z) {
        x = Math.abs(x);
        z = Math.abs(z);

        while (x > 0 || z > 0) {
            if ((x + z) % 3 == 1) {
                return false;
            }
            x /= 3;
            z /= 3;
        }
        return true;
    }
}