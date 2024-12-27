package lanse.fractalworld.WorldSorter;

import lanse.fractalworld.WorldEditor;
import lanse.fractalworld.FractalCalculator.ColumnClearer;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SortingGenerator {

    public static boolean WorldSorterIsEnabled = false;

    public static int getHighestValidY(ServerWorld world, int x, int z){
        BlockPos topPos = world.getTopPosition(Heightmap.Type.WORLD_SURFACE, new BlockPos(x, 0, z)).down();
        BlockPos pos = topPos;

        for (int i = topPos.getY(); i > -64; i--){
            if (WorldEditor.isValidBlock(world.getBlockState(pos).getBlock())){
                return pos.getY();
            } else {
                pos = pos.down();
            }
        }
        return -40404;
    }
    public static void swapColumns(ServerWorld world, int x1, int z1, int x2, int z2, boolean safeSwap) {

        SorterPresets.columnsAttempted++;
        int column1HighestY = getHighestValidY(world, x1, z1);
        int column2HighestY = getHighestValidY(world, x2, z2);

        if (safeSwap) {
            if (column1HighestY == column2HighestY) {
                return;
            }

            boolean isColumn1Higher = column1HighestY > column2HighestY;
            double distanceColumn1 = Math.sqrt(x1 * x1 + z1 * z1);
            double distanceColumn2 = Math.sqrt(x2 * x2 + z2 * z2);

            // Check if the higher column is closer to (0,0)
            if ((isColumn1Higher && distanceColumn1 >= distanceColumn2) ||
                    (!isColumn1Higher && distanceColumn2 >= distanceColumn1)) {
                return;
            }
        }

        BlockPos pos1 = new BlockPos(x1, -64, z1);
        BlockPos pos2 = new BlockPos(x2, -64, z2);
        List<BlockState> column1 = new ArrayList<>();
        List<BlockState> column2 = new ArrayList<>();

        // Save the columns and clear fluids if needed
        for (int i = -64; i < 319; i++) {
            BlockState state1 = world.getBlockState(pos1);
            column1.add(state1);

            // Clear nearby fluids conditionally
            if (!Objects.equals(SorterPresets.sorterPreset, "very_first_sorter_lanse_made") &&
                    (state1.getBlock() == Blocks.WATER || state1.getBlock() == Blocks.LAVA) && pos1.getY() > 50) {
                WorldEditor.clearNearbyFluids(world, pos1, 25);
            }

            BlockState state2 = world.getBlockState(pos2);
            column2.add(state2);
            pos1 = pos1.up();
            pos2 = pos2.up();
        }

        //Set blocks to air so they don't drop
        pos1 = new BlockPos(x1, 319, z1);
        pos2 = new BlockPos(x2, 319, z2);

        while (pos1.getY() > -64){
            world.setBlockState(pos1, Blocks.AIR.getDefaultState());
            pos1 = pos1.down();
        }
        while (pos2.getY() > -64){
            world.setBlockState(pos2, Blocks.AIR.getDefaultState());
            pos2 = pos2.down();
        }

        //Swap the columns
        pos1 = new BlockPos(x1, -64, z1);
        pos2 = new BlockPos(x2, -64, z2);

        for (int i = -64; i < 319; i++) {
            world.setBlockState(pos1, column2.get(i + 64));
            world.setBlockState(pos2, column1.get(i + 64));
            pos1 = pos1.up();
            pos2 = pos2.up();
        }
        SorterPresets.columnsCompleted++;
    }
    public static void overWriteColumns(ServerWorld world, int x1, int z1, int x2, int z2, boolean safeSwap) {

        SorterPresets.columnsAttempted++;
        int column1HighestY = getHighestValidY(world, x1, z1);
        int column2HighestY = getHighestValidY(world, x2, z2);

        if (safeSwap) {
            if (column1HighestY == column2HighestY) {
                return;
            }

            boolean isColumn1Higher = column1HighestY > column2HighestY;
            double distanceColumn1 = Math.sqrt(x1 * x1 + z1 * z1);
            double distanceColumn2 = Math.sqrt(x2 * x2 + z2 * z2);

            // Check if the higher column is closer to (0,0)
            if ((isColumn1Higher && distanceColumn1 >= distanceColumn2) ||
                    (!isColumn1Higher && distanceColumn2 >= distanceColumn1)) {
                return;
            }
        }

        BlockPos pos1;
        BlockPos pos2 = new BlockPos(x2, -64, z2);
        List<BlockState> column2 = new ArrayList<>();

        // Save column2 blocks
        for (int i = -64; i < 319; i++) {
            BlockState state2 = world.getBlockState(pos2);
            column2.add(state2);
            pos2 = pos2.up();
        }

        // Clear column1 blocks to air to prevent drops
        pos1 = new BlockPos(x1, 319, z1);
        while (pos1.getY() > -64) {
            world.setBlockState(pos1, Blocks.AIR.getDefaultState());
            pos1 = pos1.down();
        }

        // Copy column2 blocks to column1's position
        pos1 = new BlockPos(x1, -64, z1);
        for (int i = -64; i < 319; i++) {
            world.setBlockState(pos1, column2.get(i + 64));
            pos1 = pos1.up();
        }

        SorterPresets.columnsCompleted++;
    }

    public static void stalinSwap(ServerWorld world, int x1, int z1, int x2, int z2) {

        SorterPresets.columnsAttempted++;
        int column1HighestY = getHighestValidY(world, x1, z1);
        int column2HighestY = getHighestValidY(world, x2, z2);

        if (column1HighestY == column2HighestY) {
            return;
        }

        boolean isColumn1Higher = column1HighestY > column2HighestY;
        double distanceColumn1 = Math.sqrt(x1 * x1 + z1 * z1);
        double distanceColumn2 = Math.sqrt(x2 * x2 + z2 * z2);

        if ((isColumn1Higher && distanceColumn1 >= distanceColumn2) ||
                (!isColumn1Higher && distanceColumn2 >= distanceColumn1)) {
            return;
        }
        int airCount = 0;
        if (isAirAtHighest(world, x1 + 1, z1)) airCount++; // East
        if (isAirAtHighest(world, x1 - 1, z1)) airCount++; // West
        if (isAirAtHighest(world, x1, z1 + 1)) airCount++; // South
        if (isAirAtHighest(world, x1, z1 - 1)) airCount++; // North
        if (isAirAtHighest(world, x1 + 1, z1 + 1)) airCount++; // Southeast
        if (isAirAtHighest(world, x1 + 1, z1 - 1)) airCount++; // Northeast
        if (isAirAtHighest(world, x1 - 1, z1 + 1)) airCount++; // Southwest
        if (isAirAtHighest(world, x1 - 1, z1 - 1)) airCount++; // Northwest

        if (airCount >= 3) {
            return;
        }

        ColumnClearer.clearColumn(world, x2, z2);
        SorterPresets.columnsCompleted++;
    }
    private static boolean isAirAtHighest(ServerWorld world, int x, int z) {
        int highestY = getHighestValidY(world, x, z);
        BlockPos highestPos = new BlockPos(x, highestY, z);
        return world.getBlockState(highestPos).isAir();
    }
}
