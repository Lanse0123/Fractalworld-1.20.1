package lanse.fractalworld.Storage;

import lanse.fractalworld.FractalWorld;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

import java.io.*;
import java.nio.file.*;
import java.util.BitSet;
import java.util.stream.Stream;

public class RegionStorage {
    private static final int REGION_SIZE = 1024; // 1024x1024 chunks per region
    private static final int TOTAL_CHUNKS = REGION_SIZE * REGION_SIZE;
    private static final int REGION_BYTES = TOTAL_CHUNKS / 8; // 131,072 bytes
    private final Path regionFile;
    private final BitSet bitSet;
    private boolean modified = false;

    public RegionStorage(Path regionPath) {
        this.regionFile = regionPath;
        this.bitSet = new BitSet(TOTAL_CHUNKS);
        loadRegion();
    }

    private void loadRegion() {
        if (Files.exists(regionFile)) {
            try (InputStream in = Files.newInputStream(regionFile)) {
                byte[] data = in.readAllBytes();
                if (data.length == REGION_BYTES) {
                    bitSet.or(BitSet.valueOf(data)); // Properly load without misalignment
                }
            } catch (IOException ignored) {}
        }
    }

    public boolean isChunkSaved(int chunkX, int chunkZ) {
        return bitSet.get(getIndex(chunkX, chunkZ));
    }

    public void saveChunk(int chunkX, int chunkZ) {
        int index = getIndex(chunkX, chunkZ);
        if (!bitSet.get(index)) {
            bitSet.set(index);
            modified = true;
        }
    }

    public void saveToFile() {
        if (!modified) return;

        try (OutputStream out = Files.newOutputStream(regionFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            byte[] data = bitSet.toByteArray();
            byte[] paddedData = new byte[REGION_BYTES]; // Ensure fixed size
            System.arraycopy(data, 0, paddedData, 0, Math.min(data.length, REGION_BYTES));
            out.write(paddedData);
        } catch (IOException ignored) {}
        modified = false;
    }

    private int getIndex(int chunkX, int chunkZ) {
        return chunkX + chunkZ * REGION_SIZE;
    }

    public static Path getRegionFile(Path worldFolder, String dimensionId, int regionX, int regionZ) {
        Path regionDir = worldFolder.resolve("FractalRegion").resolve(dimensionId);
        try {
            Files.createDirectories(regionDir);
        } catch (IOException ignored) {}
        return regionDir.resolve(regionX + "_" + regionZ + ".bin");
    }

    public static void resetPermaSave(MinecraftServer server) {
        // Get the world save folder
        Path worldFolder = server.getSavePath(WorldSavePath.ROOT);
        String[] dimensionFolders = {"1", "0", "-1"}; // The End, Overworld, Nether

        for (String dimFolder : dimensionFolders) {
            Path regionFolder = worldFolder.resolve("FractalRegion").resolve(dimFolder);

            // Delete all region files but keep the folder
            if (Files.exists(regionFolder)) {
                try (Stream<Path> files = Files.list(regionFolder)) {
                    files.forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException ignored) {}
                    });
                } catch (IOException ignored) {}
            }
        }
        // Clear in-memory cache
        FractalWorld.regionCache.clear();
        FractalWorld.permaSaveNeedsWrite = true;
    }
}