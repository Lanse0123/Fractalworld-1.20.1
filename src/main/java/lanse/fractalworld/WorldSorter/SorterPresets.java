package lanse.fractalworld.WorldSorter;

import lanse.fractalworld.ChunkGenerationListener;
import lanse.fractalworld.FractalCalculator.ColumnClearer;
import lanse.fractalworld.FractalWorld;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SorterPresets {

    public static String sorterPreset = "player_chunk_insertion_sort";
    public static boolean intelligentDesignMessageHappened = false;
    public static int columnsCompleted = 0;
    public static int columnsAttempted = 0;
    public static int RENDER_32 = 512;

    //TODO - Add a block pointer. This can work just like chunktask, but instead it will save the last
    //TODO - Block that the sorter was on. This can prevent it from softlocking.

    public static final List<String> SORTING_PRESETS = Arrays.asList(
            "very_first_sorter_lanse_made", "player_chunk_insertion_sort", "optimized_bozo_sort",
            "intelligent_design_sort", "bubble_sort", "selection_sort", "pivot_sort",
            "heap_sort", "merge_sort", "chunk_median_sort", "radial_sort", "nyan_wall_sort",
            "bubba_sort", "stalin_sort", "odd_column_destroyer_sort", "even_column_destroyer_sort",
            "gnome_sort", "bitonic_sort", "pigeonhole_sort", "shell_sort", "bucket_sort", "miracle_sort"
    );

    public static boolean isValidPreset(String preset) {
        return SORTING_PRESETS.contains(preset.toLowerCase());
    }
    public static void setSorterPreset(String preset) { sorterPreset = preset; }
    public static boolean columnCheck(){
        return FractalWorld.maxColumnsPerTick > columnsCompleted && FractalWorld.maxColumnsPerTick * 10 > columnsAttempted;
    }
    public static int maxRender(){
        return Math.min(ChunkGenerationListener.MAX_RENDER_DIST * 16, RENDER_32);
    }
    public static void resetChunkAttempts(){
        columnsAttempted = 0;
        columnsCompleted = 0;
    }
    public static void sortWorld(MinecraftServer server) {
        ServerWorld world;
        RegistryKey<World> dimensionKey;

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            world = (ServerWorld) player.getWorld();
            dimensionKey = world.getRegistryKey();

            if (!dimensionKey.equals(World.OVERWORLD)){
                continue;
            }

            switch (sorterPreset) {
                case "very_first_sorter_lanse_made" -> veryFirstSorter(player, world);
                case "player_chunk_insertion_sort" -> playerChunkInsertionSort(player, world);
                case "optimized_bozo_sort" -> optimizedBozoSort(player, world);
                case "bozo_sort" -> bozoSort(player, world);
                case "intelligent_design_sort" -> gaySort(player);
                case "miracle_sort" -> miracleSort();
                case "selection_sort" -> selectionSort(player, world);
                case "bubble_sort" -> bubbleSort(player, world);
                case "bubba_sort" -> bubbaSort(player, world);
                case "pivot_sort" -> pivotSort(player, world);
                case "heap_sort" -> heapSort(player, world);
                case "merge_sort" -> mergeSort(player, world);
                case "gnome_sort" -> gnomeSort(player, world);
                case "chunk_median_sort" -> chunkMedianSort(player, world);
                case "radial_sort" -> radialSort(player, world);
                case "nyan_wall_sort" -> nyanWallSort(player, world);
                case "stalin_sort" -> stalinSort(player, world);
                case "odd_column_destroyer_sort" -> oddColumnDestroyerSort(player, world);
                case "even_column_destroyer_sort" -> evenColumnDestroyerSort(player, world);
                case "bitonic_sort" -> bitonicSort(player, world);
                case "pigeonhole_sort" -> pigeonholeSort(player, world);
                case "shell_sort" -> shellSort(player, world);
                case "bucket_sort" -> bucketSort(player, world);
            }
        }
    }

    // FUN FACT! IF YOUR MATH IS UNREADABLE, NOBODY WILL BOTHER CHANGING IT! IT'S A SECURITY MEASURE! //

    private static void veryFirstSorter(ServerPlayerEntity player, ServerWorld world) {
        for (int i = 0; i < FractalWorld.maxColumnsPerTick; i++) {
            int maxRenderDist = maxRender();
            BlockPos playerPos = player.getBlockPos();
            ChunkPos playerChunk = world.getChunk(playerPos).getPos();
            BlockPos pos1 = new BlockPos((playerChunk.x << 4) + world.getRandom().nextInt(16),
                    playerPos.getY(), (playerChunk.z << 4) + world.getRandom().nextInt(16));
            BlockPos pos2 = playerPos.add(world.getRandom().nextInt(2 * maxRenderDist + 1) - maxRenderDist,
                    0, world.getRandom().nextInt(2 * maxRenderDist + 1) - maxRenderDist);
            SortingGenerator.swapColumns(world, pos1.getX(), pos1.getZ(), pos2.getX(), pos2.getZ(), true);
        }
    }
    private static void playerChunkInsertionSort(ServerPlayerEntity player, ServerWorld world) {
        resetChunkAttempts();
        int maxRenderDist = maxRender();
        BlockPos pos1;
        BlockPos pos2;

        do {
            BlockPos playerPos = player.getBlockPos();
            ChunkPos playerChunk = world.getChunk(playerPos).getPos();
            pos1 = new BlockPos((playerChunk.x << 4) + world.getRandom().nextInt(16),
                    playerPos.getY(), (playerChunk.z << 4) + world.getRandom().nextInt(16));
            pos2 = playerPos.add(world.getRandom().nextInt(2 * maxRenderDist + 1) - maxRenderDist,
                    0, world.getRandom().nextInt(2 * maxRenderDist + 1) - maxRenderDist);
            SortingGenerator.swapColumns(world, pos1.getX(), pos1.getZ(), pos2.getX(), pos2.getZ(), true);

        } while (columnCheck());
    }
    private static void bozoSort(ServerPlayerEntity player, ServerWorld world) {
        //BozoSort is surprisingly decent in 3d.
        resetChunkAttempts();
        int maxRenderDist = maxRender();

        BlockPos playerPos = player.getBlockPos();
        BlockPos pos1;
        BlockPos pos2;

        do {
            pos1 = playerPos.add(world.getRandom().nextInt(2 * maxRenderDist + 1) - maxRenderDist,
                    0, world.getRandom().nextInt(2 * maxRenderDist + 1) - maxRenderDist);
            pos2 = playerPos.add(world.getRandom().nextInt(2 * maxRenderDist + 1) - maxRenderDist,
                    0, world.getRandom().nextInt(2 * maxRenderDist + 1) - maxRenderDist);
            SortingGenerator.swapColumns(world, pos1.getX(), pos1.getZ(), pos2.getX(), pos2.getZ(), false);

        } while (columnCheck());
    }
    private static void optimizedBozoSort(ServerPlayerEntity player, ServerWorld world) {
        //BozoSort is surprisingly decent in 3d.
        resetChunkAttempts();
        int maxRenderDist = maxRender();

        BlockPos playerPos = player.getBlockPos();
        BlockPos pos1;
        BlockPos pos2;

        do {
            pos1 = playerPos.add(world.getRandom().nextInt(2 * maxRenderDist + 1) - maxRenderDist,
                    0, world.getRandom().nextInt(2 * maxRenderDist + 1) - maxRenderDist);
            pos2 = playerPos.add(world.getRandom().nextInt(2 * maxRenderDist + 1) - maxRenderDist,
                    0, world.getRandom().nextInt(2 * maxRenderDist + 1) - maxRenderDist);
            SortingGenerator.swapColumns(world, pos1.getX(), pos1.getZ(), pos2.getX(), pos2.getZ(), true);

        } while (columnCheck());
    }
    private static void gaySort(ServerPlayerEntity player) {
        if (!intelligentDesignMessageHappened) {
            String message = "The probability of the original input list being in the exact order it's in is 1/(n!). There is such a small likelihood of this that it's clearly absurd to say that this happened by chance, so it must have been consciously put in that order by an intelligent Sorter. Therefore, it's safe to assume that it's already optimally Sorted in some way that transcends our naïve mortal understanding of ascending order. Any attempt to change that order to conform to our own preconceptions would actually make it less sorted.";
            player.sendMessage(Text.of(message));
            intelligentDesignMessageHappened = true;
        }
    }
    private static void miracleSort(){
        //I mean at least gaySort made an argument, this one is just waiting for a miracle bit flip
    }
    private static void bubbleSort(ServerPlayerEntity player, ServerWorld world) {
        resetChunkAttempts();
        int maxRenderDist = maxRender();
        BlockPos playerPos = player.getBlockPos();
        BlockPos pos1 = playerPos.add(
                world.getRandom().nextInt(2 * maxRenderDist + 1) - maxRenderDist, 0,
                world.getRandom().nextInt(2 * maxRenderDist + 1) - maxRenderDist);
        BlockPos pos2;

        do {
            int deltaX = world.getRandom().nextBoolean() ? 1 : -1;
            int deltaZ = world.getRandom().nextBoolean() ? 1 : -1;
            pos2 = pos1.add(deltaX, 0, deltaZ);
            SortingGenerator.swapColumns(world, pos1.getX(), pos1.getZ(), pos2.getX(), pos2.getZ(), true);
            pos1 = pos2;

        } while (columnCheck());
    }
    private static void bubbaSort(ServerPlayerEntity player, ServerWorld world) {
        resetChunkAttempts();
        int maxRenderDist = maxRender();
        BlockPos playerPos = player.getBlockPos();
        BlockPos pos1 = playerPos.add(
                world.getRandom().nextInt(2 * maxRenderDist + 1) - maxRenderDist, 0,
                world.getRandom().nextInt(2 * maxRenderDist + 1) - maxRenderDist);
        BlockPos pos2;

        do {
            int deltaX = world.getRandom().nextBoolean() ? 1 : -1;
            int deltaZ = world.getRandom().nextBoolean() ? 1 : -1;
            pos2 = pos1.add(deltaX, 0, deltaZ);
            SortingGenerator.swapColumns(world, pos1.getX(), pos1.getZ(), pos2.getX(), pos2.getZ(), false);
            pos1 = pos2;

        } while (columnCheck());
    }
    private static void selectionSort(ServerPlayerEntity player, ServerWorld world) {
        resetChunkAttempts();
        int maxRenderDist = maxRender();
        BlockPos playerPos = player.getBlockPos();
        BlockPos pos1;
        BlockPos pos2;

        do {
            // Select a random starting position within the player's render distance
            pos1 = playerPos.add(
                    world.getRandom().nextInt(2 * maxRenderDist + 1) - maxRenderDist, 0,
                    world.getRandom().nextInt(2 * maxRenderDist + 1) - maxRenderDist);

            BlockPos highestPos = pos1;
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos candidatePos = pos1.add(x, 0, z);
                    if (SortingGenerator.getHighestValidY(world, candidatePos.getX(), candidatePos.getZ()) >
                            SortingGenerator.getHighestValidY(world, highestPos.getX(), highestPos.getZ())) {
                        highestPos = candidatePos;
                    }
                }
            }
            pos2 = playerPos.add(
                    world.getRandom().nextInt(2 * maxRenderDist + 1) - maxRenderDist, 0,
                    world.getRandom().nextInt(2 * maxRenderDist + 1) - maxRenderDist);
            SortingGenerator.swapColumns(world, highestPos.getX(), highestPos.getZ(), pos2.getX(), pos2.getZ(), true);

        } while (columnCheck());
    }
    private static void pivotSort(ServerPlayerEntity player, ServerWorld world) {
        resetChunkAttempts();
        int maxRenderDist = maxRender();
        BlockPos playerPos = player.getBlockPos();
        BlockPos pos1;
        BlockPos pos2;

        do {
            pos1 = playerPos.add(
                    world.getRandom().nextInt(2 * maxRenderDist + 1) - maxRenderDist, 0,
                    world.getRandom().nextInt(2 * maxRenderDist + 1) - maxRenderDist);
            BlockPos targetPos = pos1;
            int minOrMaxHeight = SortingGenerator.getHighestValidY(world, pos1.getX(), pos1.getZ());

            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos candidatePos = pos1.add(x, 0, z);
                    int candidateHeight = SortingGenerator.getHighestValidY(world, candidatePos.getX(), candidatePos.getZ());

                    if (world.getRandom().nextBoolean() && candidateHeight < minOrMaxHeight) {
                        minOrMaxHeight = candidateHeight;
                        targetPos = candidatePos;
                    } else if (!world.getRandom().nextBoolean() && candidateHeight > minOrMaxHeight) {
                        minOrMaxHeight = candidateHeight;
                        targetPos = candidatePos;
                    }
                }
            }
            pos2 = playerPos.add(
                    world.getRandom().nextInt(2 * maxRenderDist + 1) - maxRenderDist, 0,
                    world.getRandom().nextInt(2 * maxRenderDist + 1) - maxRenderDist);
            SortingGenerator.swapColumns(world, targetPos.getX(), targetPos.getZ(), pos2.getX(), pos2.getZ(), true);

        } while (columnCheck());
    }
    private static void heapSort(ServerPlayerEntity player, ServerWorld world) {
        resetChunkAttempts();
        int maxRenderDist = maxRender();
        BlockPos playerPos = player.getBlockPos();
        List<BlockPos> heap = new ArrayList<>();

        for (int i = 0; i < FractalWorld.maxColumnsPerTick; i++) {
            BlockPos randomPos = playerPos.add(
                    world.getRandom().nextInt(2 * maxRenderDist + 1) - maxRenderDist, 0,
                    world.getRandom().nextInt(2 * maxRenderDist + 1) - maxRenderDist);
            heap.add(randomPos);
            heapifyUp(heap, heap.size() - 1, world);
        }
        while (!heap.isEmpty() && columnCheck()) {
            BlockPos maxPos = heap.get(0);
            heap.set(0, heap.get(heap.size() - 1));
            heap.remove(heap.size() - 1);
            heapifyDown(world, heap, 0);
            BlockPos swapPos = playerPos.add(
                    world.getRandom().nextInt(2 * maxRenderDist + 1) - maxRenderDist, 0,
                    world.getRandom().nextInt(2 * maxRenderDist + 1) - maxRenderDist);
            SortingGenerator.swapColumns(world, maxPos.getX(), maxPos.getZ(), swapPos.getX(), swapPos.getZ(), true);
        }
    }
    private static void heapifyUp(List<BlockPos> heap, int index, ServerWorld world) {
        while (index > 0) {
            int parentIndex = (index - 1) / 2;
            if (SortingGenerator.getHighestValidY(world, heap.get(index).getX(), heap.get(index).getZ()) >
                    SortingGenerator.getHighestValidY(world, heap.get(parentIndex).getX(), heap.get(parentIndex).getZ())) {
                Collections.swap(heap, index, parentIndex);
                index = parentIndex;
            } else {
                break;
            }
        }
    }
    private static void heapifyDown(ServerWorld world, List<BlockPos> heap, int index) {
        int size = heap.size();
        while (index < size) {
            int leftChild = 2 * index + 1;
            int rightChild = 2 * index + 2;
            int largest = index;

            if (leftChild < size &&
                    SortingGenerator.getHighestValidY(world, heap.get(leftChild).getX(), heap.get(leftChild).getZ()) >
                            SortingGenerator.getHighestValidY(world, heap.get(largest).getX(), heap.get(largest).getZ())) {
                largest = leftChild;
            }
            if (rightChild < size &&
                    SortingGenerator.getHighestValidY(world, heap.get(rightChild).getX(), heap.get(rightChild).getZ()) >
                            SortingGenerator.getHighestValidY(world, heap.get(largest).getX(), heap.get(largest).getZ())) {
                largest = rightChild;
            }

            if (largest != index) {
                Collections.swap(heap, index, largest);
                index = largest;
            } else {
                break;
            }
        }
    }
    private static void mergeSort(ServerPlayerEntity player, ServerWorld world) {
        resetChunkAttempts();
        int maxRenderDist = maxRender();
        BlockPos playerPos = player.getBlockPos();
        List<BlockPos> columnPositions = new ArrayList<>();
        for (int x = -maxRenderDist; x <= maxRenderDist; x++) {
            for (int z = -maxRenderDist; z <= maxRenderDist; z++) {
                columnPositions.add(playerPos.add(x, 0, z));
            }
        }
        List<BlockPos> sortedColumns = mergeSortColumns(columnPositions, world);
        for (int i = 0; i < sortedColumns.size() && columnCheck(); i++) {
            BlockPos sortedPos = sortedColumns.get(i);
            BlockPos originalPos = columnPositions.get(i);

            if (!sortedPos.equals(originalPos)) {
                SortingGenerator.swapColumns(world, originalPos.getX(), originalPos.getZ(), sortedPos.getX(), sortedPos.getZ(), true);
            }
        }
    }
    private static List<BlockPos> mergeSortColumns(List<BlockPos> columns, ServerWorld world) {
        if (columns.size() <= 1) {
            return columns;
        }
        int mid = columns.size() / 2;
        List<BlockPos> left = mergeSortColumns(columns.subList(0, mid), world);
        List<BlockPos> right = mergeSortColumns(columns.subList(mid, columns.size()), world);
        return merge(left, right, world);
    }
    private static List<BlockPos> merge(List<BlockPos> left, List<BlockPos> right, ServerWorld world) {
        List<BlockPos> merged = new ArrayList<>();
        int i = 0, j = 0;

        while (i < left.size() && j < right.size()) {
            if (SortingGenerator.getHighestValidY(world, left.get(i).getX(), left.get(i).getZ()) >
                    SortingGenerator.getHighestValidY(world, right.get(j).getX(), right.get(j).getZ())) {
                merged.add(left.get(i));
                i++;
            } else {
                merged.add(right.get(j));
                j++;
            }
        }
        while (i < left.size()) {
            merged.add(left.get(i));
            i++;
        }
        while (j < right.size()) {
            merged.add(right.get(j));
            j++;
        }
        return merged;
    }
    private static void gnomeSort(ServerPlayerEntity player, ServerWorld world) {
        resetChunkAttempts();
        int maxRenderDist = maxRender();
        BlockPos playerPos = player.getBlockPos();
        List<BlockPos> columnPositions = new ArrayList<>();

        for (int x = -maxRenderDist; x <= maxRenderDist; x++) {
            for (int z = -maxRenderDist; z <= maxRenderDist; z++) {
                columnPositions.add(playerPos.add(x, 0, z));
            }
        }
        int index = 0;
        while (index < columnPositions.size() && columnCheck()) {
            if (index == 0 || SortingGenerator.getHighestValidY(world, columnPositions.get(index - 1).getX(), columnPositions.get(index - 1).getZ()) <=
                    SortingGenerator.getHighestValidY(world, columnPositions.get(index).getX(), columnPositions.get(index).getZ())) {
                index++;
            } else {
                BlockPos pos1 = columnPositions.get(index);
                BlockPos pos2 = columnPositions.get(index - 1);
                SortingGenerator.swapColumns(world, pos1.getX(), pos1.getZ(), pos2.getX(), pos2.getZ(), true);
                columnPositions.set(index, pos2);
                columnPositions.set(index - 1, pos1);
                index--;
            }
        }
    }
    private static void chunkMedianSort(ServerPlayerEntity player, ServerWorld world) {
        resetChunkAttempts();
        int maxRenderDist = maxRender();
        BlockPos playerPos = player.getBlockPos();
        ChunkPos playerChunkPos = world.getChunk(playerPos).getPos();
        Random random = world.getRandom();

        do {
            ChunkPos chunk1Pos = new ChunkPos(
                    playerChunkPos.x + random.nextInt(2 * maxRenderDist / 16 + 1) - maxRenderDist / 16,
                    playerChunkPos.z + random.nextInt(2 * maxRenderDist / 16 + 1) - maxRenderDist / 16);
            ChunkPos chunk2Pos = new ChunkPos(
                    playerChunkPos.x + random.nextInt(2 * maxRenderDist / 16 + 1) - maxRenderDist / 16,
                    playerChunkPos.z + random.nextInt(2 * maxRenderDist / 16 + 1) - maxRenderDist / 16);
            double avgHeight1 = calculateChunkAverageHeight(world, chunk1Pos);
            double avgHeight2 = calculateChunkAverageHeight(world, chunk2Pos);

            if (avgHeight1 > avgHeight2) {
                swapChunks(world, chunk1Pos, chunk2Pos);
            }
        } while (columnCheck());
    }
    private static double calculateChunkAverageHeight(ServerWorld world, ChunkPos chunkPos) {
        double totalHeight = 0;
        int count = 0;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                BlockPos topBlockPos = world.getTopPosition(
                        Heightmap.Type.WORLD_SURFACE, new BlockPos((chunkPos.x << 4) + x, 0, (chunkPos.z << 4) + z)
                );
                totalHeight += topBlockPos.getY();
                count++;
            }
        }
        return count > 0 ? totalHeight / count : 0;
    }
    private static void swapChunks(ServerWorld world, ChunkPos chunk1Pos, ChunkPos chunk2Pos) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                BlockPos pos1 = new BlockPos((chunk1Pos.x << 4) + x, -64, (chunk1Pos.z << 4) + z);
                BlockPos pos2 = new BlockPos((chunk2Pos.x << 4) + x, -64, (chunk2Pos.z << 4) + z);
                SortingGenerator.swapColumns(world, pos1.getX(), pos1.getZ(), pos2.getX(), pos2.getZ(), true);
            }
        }
    }
    private static void radialSort(ServerPlayerEntity player, ServerWorld world) {
        resetChunkAttempts();
        int maxRenderDist = maxRender();
        BlockPos playerPos = player.getBlockPos();

        do {
            BlockPos pos1 = playerPos.add(
                    world.getRandom().nextInt(2 * maxRenderDist + 1) - maxRenderDist, 0,
                    world.getRandom().nextInt(2 * maxRenderDist + 1) - maxRenderDist);
            BlockPos nearestLowerDistancePos = pos1;
            double nearestDistance = playerPos.getSquaredDistance(pos1);

            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos candidatePos = pos1.add(x, 0, z);
                    double candidateDistance = playerPos.getSquaredDistance(candidatePos);

                    if (candidateDistance < nearestDistance &&
                            SortingGenerator.getHighestValidY(world, candidatePos.getX(), candidatePos.getZ()) >
                                    SortingGenerator.getHighestValidY(world, pos1.getX(), pos1.getZ())) {
                        nearestLowerDistancePos = candidatePos;
                        nearestDistance = candidateDistance;
                    }
                }
            }
            if (nearestLowerDistancePos != pos1) {
                SortingGenerator.swapColumns(world, pos1.getX(), pos1.getZ(), nearestLowerDistancePos.getX(), nearestLowerDistancePos.getZ(), true);
            }
        } while (columnCheck());
    }
    private static void nyanWallSort(ServerPlayerEntity player, ServerWorld world) {
        resetChunkAttempts();
        int maxRenderDist = maxRender();
        BlockPos playerPos = player.getBlockPos();
        int radius = 1;
        int direction = 0;
        BlockPos pos1 = playerPos;

        do {
            switch (direction) {
                case 0 -> pos1 = pos1.east();  // Move right
                case 1 -> pos1 = pos1.south(); // Move down
                case 2 -> pos1 = pos1.west();  // Move left
                case 3 -> pos1 = pos1.north(); // Move up
            }
            if (pos1.getManhattanDistance(playerPos) >= radius) {
                direction = (direction + 1) % 4;
                if (direction == 0) {
                    radius++;
                }
            }
            BlockPos pos2 = playerPos.add(
                    world.getRandom().nextInt(2 * maxRenderDist + 1) - maxRenderDist, 0,
                    world.getRandom().nextInt(2 * maxRenderDist + 1) - maxRenderDist);
            if (SortingGenerator.getHighestValidY(world, pos1.getX(), pos1.getZ()) <
                    SortingGenerator.getHighestValidY(world, pos2.getX(), pos2.getZ())) {
                SortingGenerator.swapColumns(world, pos1.getX(), pos1.getZ(), pos2.getX(), pos2.getZ(), true);
            }
        } while (columnCheck());
    }
    private static void stalinSort(ServerPlayerEntity player, ServerWorld world) {
        resetChunkAttempts();
        int maxRenderDist = maxRender();

        BlockPos playerPos = player.getBlockPos();
        BlockPos pos1;
        BlockPos pos2;

        do {
            pos1 = playerPos.add(world.getRandom().nextInt(2 * maxRenderDist + 1) - maxRenderDist,
                    0, world.getRandom().nextInt(2 * maxRenderDist + 1) - maxRenderDist);

            int[][] offsets = {
                    {-1, -1}, {0, -1}, {1, -1},
                    {-1, 0}, /*pos1*/ {1, 0},
                    {-1, 1}, {0, 1}, {1, 1}
            };

            int[] offset = offsets[world.getRandom().nextInt(8)];
            pos2 = pos1.add(offset[0], 0, offset[1]);

            SortingGenerator.stalinSwap(world, pos1.getX(), pos1.getZ(), pos2.getX(), pos2.getZ());

        } while (columnCheck());
    }
    private static void oddColumnDestroyerSort(ServerPlayerEntity player, ServerWorld world) {
        resetChunkAttempts();
        int maxRenderDist = maxRender();

        BlockPos playerPos = player.getBlockPos();
        BlockPos pos;

        do {
            pos = playerPos.add(world.getRandom().nextInt(2 * maxRenderDist + 1) - maxRenderDist,
                    0, world.getRandom().nextInt(2 * maxRenderDist + 1) - maxRenderDist);
            if (SortingGenerator.getHighestValidY(world, pos.getX(), pos.getZ()) % 2 == 1){
                ColumnClearer.clearColumn(world, pos.getX(), pos.getZ());
                columnsCompleted++;
            }
            columnsAttempted++;

        } while (columnCheck());
    }
    private static void evenColumnDestroyerSort(ServerPlayerEntity player, ServerWorld world) {
        resetChunkAttempts();
        int maxRenderDist = maxRender();

        BlockPos playerPos = player.getBlockPos();
        BlockPos pos;

        do {
            pos = playerPos.add(world.getRandom().nextInt(2 * maxRenderDist + 1) - maxRenderDist,
                    0, world.getRandom().nextInt(2 * maxRenderDist + 1) - maxRenderDist);
            if (SortingGenerator.getHighestValidY(world, pos.getX(), pos.getZ()) % 2 == 0){
                ColumnClearer.clearColumn(world, pos.getX(), pos.getZ());
                columnsCompleted++;
            }
            columnsAttempted++;

        } while (columnCheck());
    }
    private static void bitonicSort(ServerPlayerEntity player, ServerWorld world) {
        resetChunkAttempts();
        int maxRenderDist = maxRender();
        BlockPos playerPos = player.getBlockPos();
        List<BlockPos> columnPositions = new ArrayList<>();

        for (int x = -maxRenderDist; x <= maxRenderDist; x++) {
            for (int z = -maxRenderDist; z <= maxRenderDist; z++) {
                columnPositions.add(playerPos.add(x, 0, z));
            }
        }

        bitonicSortRecursive(columnPositions, 0, columnPositions.size(), true, world);

        for (int i = 0; i < columnPositions.size() && columnCheck(); i++) {
            BlockPos sortedPos = columnPositions.get(i);
            BlockPos originalPos = playerPos.add(i % (2 * maxRenderDist + 1) - maxRenderDist, 0, i / (2 * maxRenderDist + 1) - maxRenderDist);

            if (!sortedPos.equals(originalPos)) {
                SortingGenerator.swapColumns(world, originalPos.getX(), originalPos.getZ(), sortedPos.getX(), sortedPos.getZ(), true);
            }
        }
    }
    private static void bitonicSortRecursive(List<BlockPos> columns, int low, int count, boolean ascending, ServerWorld world) {
        if (count > 1) {
            int mid = count / 2;
            bitonicSortRecursive(columns, low, mid, true, world);
            bitonicSortRecursive(columns, low + mid, mid, false, world);
            bitonicMerge(columns, low, count, ascending, world);
        }
    }
    private static void bitonicMerge(List<BlockPos> columns, int low, int count, boolean ascending, ServerWorld world) {
        if (count > 1) {
            int mid = count / 2;
            for (int i = low; i < low + mid; i++) {
                if ((SortingGenerator.getHighestValidY(world, columns.get(i).getX(), columns.get(i).getZ()) >
                        SortingGenerator.getHighestValidY(world, columns.get(i + mid).getX(), columns.get(i + mid).getZ())) == ascending) {
                    BlockPos temp = columns.get(i);
                    columns.set(i, columns.get(i + mid));
                    columns.set(i + mid, temp);
                }
            }
            bitonicMerge(columns, low, mid, ascending, world);
            bitonicMerge(columns, low + mid, mid, ascending, world);
        }
    }
    private static void pigeonholeSort(ServerPlayerEntity player, ServerWorld world) {
        resetChunkAttempts();
        int maxRenderDist = maxRender();
        BlockPos playerPos = player.getBlockPos();
        List<BlockPos> columnPositions = new ArrayList<>();

        for (int x = -maxRenderDist; x <= maxRenderDist; x++) {
            for (int z = -maxRenderDist; z <= maxRenderDist; z++) {
                columnPositions.add(playerPos.add(x, 0, z));
            }
        }
        int minHeight = Integer.MAX_VALUE;
        int maxHeight = Integer.MIN_VALUE;
        for (BlockPos pos : columnPositions) {
            int height = SortingGenerator.getHighestValidY(world, pos.getX(), pos.getZ());
            if (height < minHeight) minHeight = height;
            if (height > maxHeight) maxHeight = height;
        }
        int range = maxHeight - minHeight + 1;
        List<List<BlockPos>> pigeonholes = new ArrayList<>(range);
        for (int i = 0; i < range; i++) {
            pigeonholes.add(new ArrayList<>());
        }
        for (BlockPos pos : columnPositions) {
            int height = SortingGenerator.getHighestValidY(world, pos.getX(), pos.getZ());
            pigeonholes.get(height - minHeight).add(pos);
        }
        List<BlockPos> sortedColumns = new ArrayList<>();
        for (List<BlockPos> hole : pigeonholes) {
            sortedColumns.addAll(hole);
        }
        for (int i = 0; i < sortedColumns.size() && columnCheck(); i++) {
            BlockPos sortedPos = sortedColumns.get(i);
            BlockPos originalPos = columnPositions.get(i);

            if (!sortedPos.equals(originalPos)) {
                SortingGenerator.swapColumns(world, originalPos.getX(), originalPos.getZ(), sortedPos.getX(), sortedPos.getZ(), true);
            }
        }
    }
    private static void shellSort(ServerPlayerEntity player, ServerWorld world) {
        resetChunkAttempts();
        int maxRenderDist = maxRender();
        BlockPos playerPos = player.getBlockPos();
        List<BlockPos> columnPositions = new ArrayList<>();

        for (int x = -maxRenderDist; x <= maxRenderDist; x++) {
            for (int z = -maxRenderDist; z <= maxRenderDist; z++) {
                columnPositions.add(playerPos.add(x, 0, z));
            }
        }
        int gap = columnPositions.size() / 2;
        while (gap > 0) {
            for (int i = gap; i < columnPositions.size(); i++) {
                BlockPos tempPos = columnPositions.get(i);
                int j = i;

                while (j >= gap && SortingGenerator.getHighestValidY(world, columnPositions.get(j - gap).getX(), columnPositions.get(j - gap).getZ()) >
                        SortingGenerator.getHighestValidY(world, tempPos.getX(), tempPos.getZ())) {

                    BlockPos pos1 = columnPositions.get(j - gap);
                    BlockPos pos2 = columnPositions.get(j);

                    SortingGenerator.swapColumns(world, pos1.getX(), pos1.getZ(), pos2.getX(), pos2.getZ(), true);
                    columnsAttempted++;
                    columnPositions.set(j, columnPositions.get(j - gap));
                    j -= gap;
                }
                columnPositions.set(j, tempPos);
            }
            gap /= 2;
        }
        BlockPos pos1 = playerPos.add(
                world.getRandom().nextInt(2 * maxRenderDist + 1) - maxRenderDist, 0,
                world.getRandom().nextInt(2 * maxRenderDist + 1) - maxRenderDist);
        BlockPos pos2;

        do {
            int deltaX = world.getRandom().nextBoolean() ? 1 : -1;
            int deltaZ = world.getRandom().nextBoolean() ? 1 : -1;
            pos2 = pos1.add(deltaX, 0, deltaZ);
            SortingGenerator.swapColumns(world, pos1.getX(), pos1.getZ(), pos2.getX(), pos2.getZ(), true);
            pos1 = pos2;

        } while (columnCheck());
    }
    private static void bucketSort(ServerPlayerEntity player, ServerWorld world) {
        resetChunkAttempts();
        int maxRenderDist = maxRender();
        BlockPos playerPos = player.getBlockPos();
        List<BlockPos> columnPositions = new ArrayList<>();

        for (int x = -maxRenderDist; x <= maxRenderDist; x++) {
            for (int z = -maxRenderDist; z <= maxRenderDist; z++) {
                columnPositions.add(playerPos.add(x, 0, z));
            }
        }

        int minHeight = Integer.MAX_VALUE;
        int maxHeight = Integer.MIN_VALUE;

        for (BlockPos pos : columnPositions) {
            int height = SortingGenerator.getHighestValidY(world, pos.getX(), pos.getZ());
            minHeight = Math.min(minHeight, height);
            maxHeight = Math.max(maxHeight, height);
        }

        int bucketCount = (int) Math.sqrt(columnPositions.size());
        int range = (maxHeight - minHeight) / bucketCount + 1;
        List<List<BlockPos>> buckets = new ArrayList<>();
        for (int i = 0; i < bucketCount; i++) {
            buckets.add(new ArrayList<>());
        }
        for (BlockPos pos : columnPositions) {
            int height = SortingGenerator.getHighestValidY(world, pos.getX(), pos.getZ());
            int bucketIndex = (height - minHeight) / range;
            buckets.get(bucketIndex).add(pos);
        }
        for (List<BlockPos> bucket : buckets) {
            for (int i = 1; i < bucket.size(); i++) {
                BlockPos key = bucket.get(i);
                int j = i - 1;

                while (j >= 0 && SortingGenerator.getHighestValidY(world, bucket.get(j).getX(), bucket.get(j).getZ()) >
                        SortingGenerator.getHighestValidY(world, key.getX(), key.getZ())) {
                    bucket.set(j + 1, bucket.get(j));
                    j--;
                }
                bucket.set(j + 1, key);
            }
        }
        int index = 0;
        for (List<BlockPos> bucket : buckets) {
            for (BlockPos pos : bucket) {
                columnPositions.set(index++, pos);
            }
        }
        BlockPos pos1 = playerPos.add(
                world.getRandom().nextInt(2 * maxRenderDist + 1) - maxRenderDist, 0,
                world.getRandom().nextInt(2 * maxRenderDist + 1) - maxRenderDist);
        BlockPos pos2;

        do {
            int deltaX = world.getRandom().nextBoolean() ? 1 : -1;
            int deltaZ = world.getRandom().nextBoolean() ? 1 : -1;
            pos2 = pos1.add(deltaX, 0, deltaZ);
            SortingGenerator.swapColumns(world, pos1.getX(), pos1.getZ(), pos2.getX(), pos2.getZ(), true);
            pos1 = pos2;

        } while (columnCheck());
    }
}
