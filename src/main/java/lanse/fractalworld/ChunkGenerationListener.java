package lanse.fractalworld;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChunkGenerationListener {

    public static final Set<ChunkPos> processedChunksOverworld = new HashSet<>();
    public static final Set<ChunkPos> processedChunksNether = new HashSet<>();
    public static final Set<ChunkPos> processedChunksEnd = new HashSet<>();
    public static int MAX_RENDER_DIST = 8;

    public static void tryNewChunks(MinecraftServer server) {
        ServerWorld world;

        //For each player, find a new chunk position within the max render distance
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {

            world = player.getServerWorld();
            ChunkPos playerChunkPos = player.getChunkPos();
            List<Chunk> nearbyChunks = new ArrayList<>();
            int radius = 1;

            for (int i = 1; i < MAX_RENDER_DIST; i++) {

                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dz = -radius; dz <= radius; dz++) {

                        ChunkPos chunkPos = new ChunkPos(playerChunkPos.x + dx, playerChunkPos.z + dz);
                        Chunk chunk = world.getChunkManager().getChunk(chunkPos.x, chunkPos.z, ChunkStatus.FULL, false);

                        if (chunk != null){
                            if (world.getRegistryKey().equals(World.OVERWORLD)){
                                if (!processedChunksOverworld.contains(chunkPos)){
                                    nearbyChunks.add(chunk);
                                }
                            }
                            else if (world.getRegistryKey().equals(World.NETHER)){
                                if (!processedChunksNether.contains(chunkPos)){
                                    nearbyChunks.add(chunk);
                                }
                            }
                            else if (world.getRegistryKey().equals(World.END)){
                                if (!processedChunksEnd.contains(chunkPos)){
                                    nearbyChunks.add(chunk);
                                }
                            }
                        }
                    }
                }
                if (nearbyChunks.size() < 10) {
                    radius++;
                } else {
                    for (Chunk chunk : nearbyChunks) {
                        processChunk(world, chunk);
                    }
                    break; // Exit the loop after processing chunks for this player
                }
            }
        }
    }

    private static void processChunk(ServerWorld world, Chunk chunk) {
        ChunkPos chunkPos = chunk.getPos();

        // Skip if chunk is already processed
        if (world.getRegistryKey().equals(World.OVERWORLD)) {
            if (processedChunksOverworld.contains(chunkPos)) return;
            processedChunksOverworld.add(chunkPos);

        } else if (world.getRegistryKey().equals(World.NETHER)) {
            if (processedChunksNether.contains(chunkPos)) return;
            processedChunksNether.add(chunkPos);

        } else if (world.getRegistryKey().equals(World.END)) {
            if (processedChunksEnd.contains(chunkPos)) return;
            processedChunksEnd.add(chunkPos);
        }

        // Queue each column for processing
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunkPos.getStartX() + x;
                int worldZ = chunkPos.getStartZ() + z;
                FractalWorld.processingQueue.add(new FractalWorld.ChunkTask(world, worldX, worldZ));
            }
        }
    }
    public static void clearProcessedChunks() {
        processedChunksOverworld.clear();
        processedChunksNether.clear();
        processedChunksEnd.clear();
        FractalWorld.processingQueue.clear();
    }
}