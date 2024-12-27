package lanse.fractalworld.ChunkRandomizer;

import lanse.fractalworld.FractalWorld;
import lanse.fractalworld.WorldSorter.SortingGenerator;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class ChunkRandomizer {

    public static boolean isChunkRandomizerEnabled = false;

    public static void RandomizeChunk(ServerWorld world, int x, int z) {
        int height = 255;

        if (world.getRegistryKey().equals(World.OVERWORLD)) {
            height = 318;
        }

        BlockPos topPos = new BlockPos(x, height, z);
        if (world.getBlockState(topPos).getBlock() == Blocks.STRUCTURE_VOID) return;

        int borderSize = (int) world.getWorldBorder().getSize();
        Random random = world.getRandom();

        ChunkPos chunk1Pos = new ChunkPos(x >> 4, z >> 4);

        ChunkPos chunk2Pos = new ChunkPos(
                chunk1Pos.x + random.nextInt(2 * borderSize / 128 + 1) - borderSize / 128,
                chunk1Pos.z + random.nextInt(2 * borderSize / 128 + 1) - borderSize / 128);

        swapChunks(world, chunk1Pos, chunk2Pos);
        placeStructureVoidsAtChunkTop(world, chunk1Pos);
    }

    private static void placeStructureVoidsAtChunkTop(ServerWorld world, ChunkPos chunkPos) {
        if (FractalWorld.permaSave){

            int height = 255;

            if (world.getRegistryKey().equals(World.OVERWORLD)) {
                height = 318;
            }

            int chunkStartX = chunkPos.getStartX();
            int chunkStartZ = chunkPos.getStartZ();

            for (int dx = 0; dx < 16; dx++) {
                for (int dz = 0; dz < 16; dz++) {
                    BlockPos topPos = new BlockPos(chunkStartX + dx, height, chunkStartZ + dz);
                    world.setBlockState(topPos, Blocks.STRUCTURE_VOID.getDefaultState());
                }
            }
        }
    }
    private static void swapChunks(ServerWorld world, ChunkPos chunk1Pos, ChunkPos chunk2Pos) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                BlockPos pos1 = new BlockPos((chunk1Pos.x << 4) + x, -64, (chunk1Pos.z << 4) + z);
                BlockPos pos2 = new BlockPos((chunk2Pos.x << 4) + x, -64, (chunk2Pos.z << 4) + z);
                SortingGenerator.overWriteColumns(world, pos1.getX(), pos1.getZ(), pos2.getX(), pos2.getZ(), false);
            }
        }
    }
}