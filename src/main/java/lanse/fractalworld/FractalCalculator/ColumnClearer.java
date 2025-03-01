package lanse.fractalworld.FractalCalculator;

import lanse.fractalworld.WorldEditor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

import java.util.Random;
import java.util.Set;

public class ColumnClearer {
    public static final Set<Block> XRAY_BLOCKS = Set.of(
            Blocks.STONE, Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE, Blocks.TUFF, Blocks.DEEPSLATE,
            Blocks.CALCITE, Blocks.DRIPSTONE_BLOCK, Blocks.NETHERRACK, Blocks.GRAVEL, Blocks.SAND,
            Blocks.RED_SAND, Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.ROOTED_DIRT, Blocks.GRASS_BLOCK,
            Blocks.PODZOL, Blocks.MYCELIUM, Blocks.CLAY, Blocks.SNOW, Blocks.SNOW_BLOCK, Blocks.BASALT,
            Blocks.BLACKSTONE, Blocks.SOUL_SAND, Blocks.SOUL_SOIL, Blocks.END_STONE, Blocks.BEDROCK,

            Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES, Blocks.BIRCH_LEAVES, Blocks.JUNGLE_LEAVES,
            Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.MANGROVE_LEAVES, Blocks.AZALEA_LEAVES,
            Blocks.FLOWERING_AZALEA_LEAVES, Blocks.VINE, Blocks.CAVE_VINES, Blocks.GLOW_LICHEN, Blocks.MOSS_BLOCK,
            Blocks.MOSS_CARPET, Blocks.TWISTING_VINES, Blocks.WEEPING_VINES, Blocks.NETHER_SPROUTS,
            Blocks.CRIMSON_FUNGUS, Blocks.WARPED_FUNGUS, Blocks.CRIMSON_ROOTS, Blocks.WARPED_ROOTS,
            Blocks.SEAGRASS, Blocks.TALL_SEAGRASS,

            Blocks.WATER, Blocks.LAVA, Blocks.ICE, Blocks.PACKED_ICE, Blocks.BLUE_ICE,

            Blocks.OAK_LOG, Blocks.SPRUCE_LOG, Blocks.BIRCH_LOG, Blocks.JUNGLE_LOG, Blocks.ACACIA_LOG,
            Blocks.DARK_OAK_LOG, Blocks.MANGROVE_LOG, Blocks.CRIMSON_STEM, Blocks.WARPED_STEM,

            Blocks.SANDSTONE, Blocks.RED_SANDSTONE, Blocks.TERRACOTTA, Blocks.WHITE_TERRACOTTA, Blocks.ORANGE_TERRACOTTA,
            Blocks.MAGENTA_TERRACOTTA, Blocks.LIGHT_BLUE_TERRACOTTA, Blocks.YELLOW_TERRACOTTA, Blocks.LIME_TERRACOTTA,
            Blocks.PINK_TERRACOTTA, Blocks.GRAY_TERRACOTTA, Blocks.LIGHT_GRAY_TERRACOTTA, Blocks.CYAN_TERRACOTTA,
            Blocks.PURPLE_TERRACOTTA, Blocks.BLUE_TERRACOTTA, Blocks.BROWN_TERRACOTTA, Blocks.GREEN_TERRACOTTA,
            Blocks.RED_TERRACOTTA, Blocks.BLACK_TERRACOTTA
    );
    public enum ClearMode {
        VOID, XRAY, OCEAN, LAVA_OCEAN, MONOLITH, NONE, RANDOMIZE, PARKOUR_GRID
    }
    public static ClearMode currentMode = ClearMode.VOID;
    public static void resetColumnClearer() {
        currentMode = null;
    }

    public static void clearColumn(ServerWorld world, int x, int z) {
        int topY = 319;

        switch (currentMode) {
            case VOID -> clearVoid(world, x, z, topY);
            case XRAY -> clearXray(world, x, z, topY);
            case PARKOUR_GRID -> parkour_grid(world, x, z, topY);
            case OCEAN -> clearOcean(world, x, z, Blocks.WATER.getDefaultState());
            case LAVA_OCEAN -> clearOcean(world, x, z, Blocks.LAVA.getDefaultState());
            case MONOLITH -> monolitholisize(world, x, z);
            case RANDOMIZE -> randomize(world, x, z);
            case NONE -> nothing();
        }
    }

    private static void clearVoid(ServerWorld world, int x, int z, int topY) {
        for (int y = topY; y >= -64; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            if (world.getBlockState(pos).getBlock() == Blocks.WATER || world.getBlockState(pos).getBlock() == Blocks.LAVA){
                WorldEditor.clearNearbyFluids(world, pos, 25);
            }
            if (!isProtectedBlock(world.getBlockState(pos))) {
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), 18);
            }
        }
    }
    private static void clearXray(ServerWorld world, int x, int z, int topY) {
        for (int y = topY; y >= -64; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            if (world.getBlockState(pos).getBlock() == Blocks.WATER || world.getBlockState(pos).getBlock() == Blocks.LAVA){
                WorldEditor.clearNearbyFluids(world, pos, 25);
            }
            if (XRAY_BLOCKS.contains(world.getBlockState(pos).getBlock())) {
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), 18);
            }
        }
    }
    private static void parkour_grid(ServerWorld world, int x, int z, int topY) {
        int highestY = -40404;
        int y;

        if (!(Math.abs(x) % 2 == 1) || !(Math.abs(z) % 2 == 1)){
            ColumnClearer.clearVoid(world, x, z, topY);
            return;
        }

        for (y = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z); y >= -64; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = world.getBlockState(pos);

            if (WorldEditor.isValidBlock(state.getBlock())) {
                highestY = y;
                break;
            }

            // Check for water or lava above Y level 50, and clear it if it is liquid.
            if (FractalGenerator.heightGeneratorEnabled) {
                if (y > 50 && (state.isOf(Blocks.WATER) || state.isOf(Blocks.LAVA))) {
                    WorldEditor.clearNearbyFluids(world, pos, 25);
                }
            }
        }

        for (y = topY; y > highestY; y--){
            BlockPos pos = new BlockPos(x, y, z);
            if (world.getBlockState(pos).getBlock() == Blocks.WATER || world.getBlockState(pos).getBlock() == Blocks.LAVA){
                WorldEditor.clearNearbyFluids(world, pos, 25);
            }
            if (!isProtectedBlock(world.getBlockState(pos))) {
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), 18);
            }
        }

        for (y = highestY - 1; y >= -64; y--){
            BlockPos pos = new BlockPos(x, y, z);
            if (world.getBlockState(pos).getBlock() == Blocks.WATER || world.getBlockState(pos).getBlock() == Blocks.LAVA){
                WorldEditor.clearNearbyFluids(world, pos, 25);
            }
            if (!isProtectedBlock(world.getBlockState(pos))) {
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), 18);
            }
        }
    }
    private static void clearOcean(ServerWorld world, int x, int z, BlockState liquidBlock) {
        int oceanSurfaceY = FractalGenerator.INITIAL_HEIGHT_OFFSET + FractalGenerator.MIN_ITER;

        // Clear air blocks above ocean height
        for (int y = 319; y >= oceanSurfaceY; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            if (!isProtectedBlock(world.getBlockState(pos))) {
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), 18);
            }
        }
        for (int y = oceanSurfaceY - 1; y > -63; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            if (!isProtectedBlock(world.getBlockState(pos))) {
                world.setBlockState(pos, liquidBlock);
            }
        }
        BlockPos pos = new BlockPos(x, -63, z);
        world.setBlockState(pos, Blocks.BEDROCK.getDefaultState(), 18);
    }
    private static void monolitholisize(ServerWorld world, int x, int z) {
        int currentHeight = world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z);
        int topHeight = Math.min((FractalGenerator.MAX_ITER + FractalGenerator.INITIAL_HEIGHT_OFFSET + 1), 310);
        WorldEditor.moveColumn(world, x, z, currentHeight, topHeight);
    }

    private static void nothing(){}

    private static void randomize(ServerWorld world, int x, int z) {
        Random random = new Random();
        int currentHeight = world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z);
        WorldEditor.moveColumn(world, x, z, currentHeight, random.nextInt(400) - 80);
    }
    private static boolean isProtectedBlock(BlockState state) {
        return state.getBlock() == Blocks.END_PORTAL_FRAME || state.getBlock() == Blocks.END_PORTAL;
    }
}