package lanse.fractalworld;

import lanse.fractalworld.FractalCalculator.ColumnClearer;
import lanse.fractalworld.FractalCalculator.FractalGenerator;
import lanse.fractalworld.FractalCalculator.FractalPresets;
import lanse.fractalworld.FractalCalculator.WorldPainter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.IceBlock;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

import java.util.*;

public class WorldEditor {

    //List of valid surface blocks making up the ground. This does not include stuff like trees and
    //structures, since those will be placed on top of this ground level.
    public static final Set<Block> VALID_BLOCKS = Set.of(
            Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.STONE, Blocks.ANDESITE, Blocks.GRANITE, Blocks.DIORITE,
            Blocks.COAL_ORE, Blocks.IRON_ORE, Blocks.GRAVEL, Blocks.SAND, Blocks.RED_SAND, Blocks.PODZOL,
            Blocks.MYCELIUM, Blocks.MUD, Blocks.PACKED_ICE, Blocks.BLUE_ICE, Blocks.SNOW_BLOCK,
            Blocks.POWDER_SNOW, Blocks.DRIPSTONE_BLOCK, Blocks.CLAY, Blocks.DIRT_PATH, Blocks.COARSE_DIRT,
            Blocks.NETHERRACK, Blocks.END_STONE, Blocks.OBSIDIAN
    );

    public static void adjustColumn(ServerWorld world, int x, int z, String dimensionType) {
        // Find the highest valid block in the column
        int highestY = -2500;

        for (int y = 320; y >= -64; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = world.getBlockState(pos);

            if (isValidBlock(state.getBlock())) {
                highestY = y;
                break;
            }

            // Check for water or lava above Y level 50, and clear it if it is liquid.
            if (FractalGenerator.heightGeneratorEnabled) {
                if (y > 50 && (state.isOf(Blocks.WATER) || state.isOf(Blocks.LAVA))) {
                    clearNearbyFluids(world, pos, 25);
                }
            }
        }

        //If it is a 2d fractal and there is no valid top blocks in the entire column, clear it.
        if (highestY == -2500 && !FractalPresets.is3DFractal(FractalPresets.fractalPreset)) {
            if (!WorldPainter.worldPainterEnabled && !WorldPainter.worldPainterFullHeightEnabled) {
                ColumnClearer.clearColumn(world, x, z);
                return;
            }
        }

        //Below this, it checks if it is 2d or 3d, then it calculates the height and color of the blocks.
        int[] column;
        int targetHeight;

        if (FractalPresets.is3DFractal(FractalPresets.fractalPreset)){
            column = FractalGenerator.get3DHeight(x, z, dimensionType);
        } else {
            column = new int[]{FractalGenerator.getHeight(x, z, dimensionType)};
        }

        //2D fractals only get an array length of 1, so if it's bigger than that, it is 3d.
        if (column.length > 1){
            convert3DFractal(world, x, highestY, z, column);
            return;

        } else { targetHeight = column[0]; }

        if (FractalGenerator.heightGeneratorEnabled && dimensionType.equals("END")){
            createEndIsland(world, x, z, targetHeight);
            return;
        }

        if (targetHeight - FractalGenerator.INITIAL_HEIGHT_OFFSET >= FractalGenerator.MAX_ITER / FractalGenerator.SMOOTHING_VALUE) {
            if (FractalGenerator.heightGeneratorEnabled) {
                ColumnClearer.clearColumn(world, x, z);
            }
            if (WorldPainter.worldPainterEnabled || WorldPainter.worldPainterFullHeightEnabled){
                WorldPainter.paintBlack(world, x, z);
            }
            return; // Too high of an iteration.
        }
        if (targetHeight - FractalGenerator.INITIAL_HEIGHT_OFFSET <= FractalGenerator.MIN_ITER) {
            if (FractalGenerator.heightGeneratorEnabled) {
                ColumnClearer.clearColumn(world, x, z);
                return; // Too low of an iteration.
            }
        }

        if (FractalGenerator.heightGeneratorEnabled) {
            // Adjust the entire column based on the height difference
            moveColumn(world, x, z, highestY, targetHeight);
        }

        if (WorldPainter.worldPainterEnabled || WorldPainter.worldPainterFullHeightEnabled){
            WorldPainter.paintWorld(world, x, z, targetHeight);
        }
    }

    public static boolean isValidBlock(Block block) {
        return VALID_BLOCKS.contains(block) || block instanceof IceBlock;
    }
    public static void moveColumn(ServerWorld world, int x, int z, int currentY, int targetY) {

        int heightDifference = FractalGenerator.INVERTED_HEIGHT ? currentY - targetY : targetY - currentY;

        if (heightDifference > 0) {
            // Move the column up to what the fractal says it should be
            for (int y = world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z); y >= -64; y--) {
                BlockPos oldPos = new BlockPos(x, y, z);
                BlockState state = world.getBlockState(oldPos);
                BlockPos newPos = new BlockPos(x, y + heightDifference, z);
                world.setBlockState(newPos, state);
                world.setBlockState(oldPos, Blocks.AIR.getDefaultState()); // Clear old block
            }
        } else if (heightDifference < 0) {
            // Move the column down to what the fractal says it should be
            for (int y = -64; y <= 319; y++) {
                BlockPos oldPos = new BlockPos(x, y, z);
                BlockState state = world.getBlockState(oldPos);
                BlockPos newPos = new BlockPos(x, y + heightDifference, z);

                // Ensure the new position is within valid world bounds (-64 to 320)
                if (newPos.getY() <= 320 && newPos.getY() >= -64) {
                    world.setBlockState(newPos, state); // Move the block down
                }

                // Clear the old block only if we're above the target height (so we don't clear beyond the move)
                if (y <= currentY && y + heightDifference >= -64) {
                    world.setBlockState(oldPos, Blocks.AIR.getDefaultState()); // Clear old block
                }
            }
        }
    }
    //Clear all water and lava blocks within int radius around the given position
    public static void clearNearbyFluids(ServerWorld world, BlockPos origin, int radius) {

        if (ColumnClearer.currentMode == ColumnClearer.ClearMode.OCEAN || ColumnClearer.currentMode == ColumnClearer.ClearMode.LAVA_OCEAN){
            return;
        }

        for (int x = origin.getX() - radius; x <= origin.getX() + radius; x++) {
            for (int y = origin.getY() - radius; y <= origin.getY() + radius; y++) {
                for (int z = origin.getZ() - radius; z <= origin.getZ() + radius; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = world.getBlockState(pos);

                    // Check if the block is water or lava, and replace it with air
                    if ((state.isOf(Blocks.WATER) || state.isOf(Blocks.LAVA) ||
                            (state.contains(Properties.WATERLOGGED) &&
                                    state.get(Properties.WATERLOGGED)))) {

                        world.setBlockState(pos, Blocks.AIR.getDefaultState());
                    }
                }
            }
        }
    }
    private static void convert3DFractal(ServerWorld world, int x, int highestY, int z, int[] column) {
        //This function turns the array of numbers into blocks.

        //If world painter is on, let the WorldPainter class handle it.
        if (WorldPainter.worldPainterEnabled || WorldPainter.worldPainterFullHeightEnabled) {
            WorldPainter.paint3DWorld(world, x, z, column, highestY);
            return;
        }

        //Otherwise, it's going to convert the entire world to be the fractal.
        List<BlockState> belowHighestY = new ArrayList<>();
        List<BlockState> aboveHighestY = new ArrayList<>();

        //For each position, add the block to a new array, to be used later. Air above highestY is kept.
        for (int y = 319; y >= -64; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState currentState = world.getBlockState(pos);

            if (currentState.isAir() && y < highestY) { continue; }

            if (y <= highestY) {
                belowHighestY.add(currentState);
            } else {
                aboveHighestY.add(currentState);
                if (!currentState.isAir()){
                    world.setBlockState(pos, Blocks.AIR.getDefaultState());
                }
            }
        }
        Collections.reverse(aboveHighestY);

        //For everything below highest Y:
        for (int y = highestY; y >= -64 && !belowHighestY.isEmpty(); y--) {
            int iteration = column[y + 64];

            //If it is too high or low of an iteration, set it to air. Otherwise, get the highest block
            //from the belowHighestY array, set it to that, and remove that from the array.
            BlockPos pos = new BlockPos(x, y, z);
            if (iteration >= FractalGenerator.MAX_ITER / FractalGenerator.SMOOTHING_VALUE || iteration <= FractalGenerator.MIN_ITER) {
                world.setBlockState(pos, Blocks.AIR.getDefaultState());
            } else {
                world.setBlockState(pos, belowHighestY.remove(0));
            }
        }

        boolean hasValidBlocks = false;
        for (int y = highestY + 1; y >= -64; y--) {
            if (column[y + 64] > FractalGenerator.MIN_ITER && column[y + 64] < FractalGenerator.MAX_ITER / FractalGenerator.SMOOTHING_VALUE) {
                hasValidBlocks = true;
                break;
            }
        }

        //Starts 1 block above highestY, and places the blocks above highestY above it.
        if (hasValidBlocks) {
            int aboveIndex = 0;
            for (int y = highestY + 1; y <= 319 && aboveIndex < aboveHighestY.size(); y++) {
                BlockPos pos = new BlockPos(x, y, z);
                world.setBlockState(pos, aboveHighestY.get(aboveIndex));
                aboveIndex++;
            }
        }
    }
    private static void createEndIsland(ServerWorld world, int x, int z, int iterations) {
        Random random = new Random();

        //Outer End Island
        if (Math.sqrt(x * x + z * z) > 800) {
            return;

            //Inner End Island
        } else {
            for (int y = world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z); y >= world.getBottomY(); y--) {
                BlockPos pos = new BlockPos(x, y, z);
                Block block = world.getBlockState(pos).getBlock();

                if (block == Blocks.END_STONE) {
                    world.setBlockState(pos, Blocks.AIR.getDefaultState());
                }
            }

            if (iterations - FractalGenerator.INITIAL_HEIGHT_OFFSET >= FractalGenerator.MAX_ITER / FractalGenerator.SMOOTHING_VALUE
            || iterations - FractalGenerator.INITIAL_HEIGHT_OFFSET <= FractalGenerator.MIN_ITER) return;

            BlockPos mainEndstonePos = new BlockPos(x, iterations - 20, z);
            world.setBlockState(mainEndstonePos, Blocks.END_STONE.getDefaultState());

            int depth = random.nextInt(iterations / 3) + 3;
            for (int i = 1; i <= depth; i++) {
                BlockPos belowPos = mainEndstonePos.down(i);
                world.setBlockState(belowPos, Blocks.END_STONE.getDefaultState());

            }
        }
    }
}