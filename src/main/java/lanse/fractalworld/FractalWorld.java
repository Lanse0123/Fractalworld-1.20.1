package lanse.fractalworld;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lanse.fractalworld.Automata.AutomataControl;
import lanse.fractalworld.FractalCalculator.CustomFractalCalculator;
import lanse.fractalworld.FractalCalculator.FractalGenerator;
import lanse.fractalworld.Storage.Database;
import lanse.fractalworld.Storage.RegionStorage;
import lanse.fractalworld.WorldSorter.SorterPresets;
import lanse.fractalworld.WorldSorter.SortingGenerator;
import lanse.fractalworld.WorldSymmetrifier.Symmetrifier;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class FractalWorld implements ModInitializer {
	public static boolean isModEnabled = false;
	public static MinecraftServer originalServer;
	public static Queue<ChunkTask> processingQueue = new LinkedList<>();
	public static int maxColumnsPerTick = 100;
	public static int tickCount = 0;
	public static boolean permaSave = false;
	public static Queue<PermaTask> permaQueue = new LinkedList<>();
	public static Queue<PermaTask2> permaQueue2 = new LinkedList<>();
	public static int refreshRate = 1000;
	public static boolean autoRefreshModeIsOn = false;
	public static int maxThreads = 1;

	public static final Map<String, RegionStorage> regionCache = new LinkedHashMap<>(50, 0.75f, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<String, RegionStorage> eldest) {
			return size() > 50; // Keep only 50 most recently accessed regions in memory
		}
	};
	public static boolean permaSaveNeedsWrite = false;

	@Override
	public void onInitialize() {
		ServerTickEvents.START_SERVER_TICK.register(this::onServerTick);
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> Commands.registerCommands(dispatcher));
		FractalGenerator.setScale(25.0);

		// Load config on startup
		Path configFolder = getConfigFolder();
		if (!Files.exists(configFolder)) {
			try {
				Files.createDirectories(configFolder);
			} catch (IOException ignored) {}
		} else {
			try {
				loadConfigFromJson(configFolder);
			} catch (IOException ignored) {}
		}
	}

	private void onServerTick(MinecraftServer server) {
		tickCount++;
		if (tickCount > 2147483641) tickCount = 0;

		if (tickCount % 300 == 0 && Database.changed) {
			Database dataSaver = Database.getOrCreate(server);
			Path configFolder = getConfigFolder();
			dataSaver.saveToJson(configFolder);
		}

		if (isModEnabled) {
			if (server != originalServer) isModEnabled = false;

			if ((autoRefreshModeIsOn && tickCount % refreshRate == 0) || ChunkProcessor.processedChunkCount > 25000) {
				ChunkProcessor.clearProcessedChunks();
			}

			if (SortingGenerator.WorldSorterIsEnabled) {
				SorterPresets.sortWorld(server);
				return;
			}

			processQueuedChunks();

			if (tickCount % 10 == 0) {
				DimensionHandler.dimensionalChecker(server);
				DimensionHandler.processTeleportQueue(server, tickCount);
			}

			if (processingQueue.size() < maxColumnsPerTick * 20 && (!ChunkProcessor.complete || tickCount % 120 == 0)) {
				ChunkProcessor.tryNewChunks(server, true);
			}

			if (CustomFractalCalculator.formulaIsInvalid) {
				CustomFractalCalculator.formulaIsInvalid = false;
				isModEnabled = false;

				for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
					player.sendMessage(Text.of("Invalid Fractal formula."));
				}
			}

			if (CarpetPlayerWorldLoader.playerWorldLoaderEnabled){
				CarpetPlayerWorldLoader.rotatePlayerIfTheyMovedTooFarOrSomething();
			}
		}

		if (permaSave && tickCount % 100 == 0) {
			regionCache.values().forEach(RegionStorage::saveToFile);
			regionCache.clear(); // Prevent excessive memory usage
			convertToNewPermaSave();
		}
	}

	public void processQueuedChunks() {
		int processedCount = 0;
		while (!processingQueue.isEmpty() && processedCount < maxColumnsPerTick * 1000) {
			ChunkTask task = processingQueue.poll();
			if (task != null) {
				BlockPos blockPos;
				boolean full = true;
				RegistryKey<World> dimensionKey = task.world.getRegistryKey();
				if (dimensionKey.equals(World.OVERWORLD)) {
					blockPos = new BlockPos(task.x, 319, task.z);
					full = processOverworldColumn(task, blockPos);
				} else if (dimensionKey.equals(World.NETHER)) {
					blockPos = new BlockPos(task.x, 255, task.z);
					full = processNetherColumn(task, blockPos);
				} else if (dimensionKey.equals(World.END)) {
					blockPos = new BlockPos(task.x, 255, task.z);
					full = processEndColumn(task, blockPos);
				}
				processedCount += full ? 1000 : 1;
			}
		}
		if (AutomataControl.automataIsEnabled && tickCount % 5 == 0) {
			AutomataControl.completeDrawing();
		}
	}

	private boolean processOverworldColumn(ChunkTask task, BlockPos blockPos) {
		if (ChunkProcessor.overworldIsDisabled) return false;
		if (task.world.getPlayers().stream().noneMatch(player -> player.getWorld() == task.world)) return false;
		if (hasPermaSave(task.world, blockPos)) return false;

		WorldEditor.adjustColumn(task.world, task.x, task.z, "OVERWORLD");

		if (Symmetrifier.verticalMirrorWorldEnabled){
			Symmetrifier.mirrorWorldAbove(task.world, task.x, task.z);
		}
		processPermaSave(task.world, blockPos);
		return true;
	}
	private boolean processNetherColumn(ChunkTask task, BlockPos blockPos) {
		if (ChunkProcessor.netherIsDisabled) return false;
		if (task.world.getPlayers().stream().noneMatch(player -> player.getWorld() == task.world)) return false;
		if (hasPermaSave(task.world, blockPos)) return false;

		if (!Symmetrifier.symmetrifierEnabled && !SortingGenerator.WorldSorterIsEnabled) {
			// Destroy Nether Roof logic, clearing blocks down to Y=90
			for (int y = 128; y >= 90; y--) {
				BlockPos pos = new BlockPos(task.x, y, task.z);
				if (!task.world.getBlockState(pos).isAir()) {
					task.world.setBlockState(pos, Blocks.AIR.getDefaultState());
				}
			}
			WorldEditor.adjustColumn(task.world, task.x, task.z, "NETHER");
			processPermaSave(task.world, blockPos);
		}
		return true;
	}
	private boolean processEndColumn(ChunkTask task, BlockPos blockPos) {
		if (ChunkProcessor.endIsDisabled) return false;
		if (task.world.getPlayers().stream().noneMatch(player -> player.getWorld() == task.world)) return false;
		if (hasPermaSave(task.world, blockPos)) return false;

		WorldEditor.adjustColumn(task.world, task.x, task.z, "END");
		processPermaSave(task.world, blockPos);
		return true;
	}

	public record ChunkTask(ServerWorld world, int x, int z) {
		//IDK why this works, but it autocorrected to record from class
		//I don't even know what a record is or how it works, but it somehow just works
		//It sounds kinda funny though "public record"
		//I HAVE YOU ON MY PUBLIC RECORD!!! WE CAUGHT YOU STEALING THE SECRET FORMULA!!
		//Its only 12:40 AM...

		//Oh hey I learned what records are
		//Future Lanse - I need to add a dimension holder for this
		//Nvm no I dont, it is already storing the world which can get the registry key
	}

	public record PermaTask(ServerWorld world, BlockPos pos) {}
	public record PermaTask2(ServerWorld world, BlockPos pos) {}

	public static void processPermaSave(ServerWorld world, BlockPos blockPos) {
		if (!permaSave || !isModEnabled) return;

		// Only save if this block is the final one in the chunk.
		if ((blockPos.getX() & 15) != 15 || (blockPos.getZ() & 15) != 15) return;

		int chunkX = blockPos.getX() >> 4;
		int chunkZ = blockPos.getZ() >> 4;
		int regionX = chunkX >> 10;
		int regionZ = chunkZ >> 10;
		int localX = chunkX & 1023;
		int localZ = chunkZ & 1023;

		String dimensionId = getDimensionId(world);
		String regionKey = dimensionId + ":" + regionX + "," + regionZ;

		RegionStorage region = regionCache.computeIfAbsent(regionKey, key -> {
			Path regionFile = RegionStorage.getRegionFile(world.getServer().getSavePath(WorldSavePath.ROOT), dimensionId, regionX, regionZ);
			return new RegionStorage(regionFile);
		});
		region.saveChunk(localX, localZ);
	}

	private void convertToNewPermaSave() {
		while (!permaQueue.isEmpty()) {
			PermaTask task = permaQueue.poll();
			if (task == null) continue;

			ServerWorld world = task.world();
			RegistryKey<World> dimensionKey = world.getRegistryKey();
			int y = (dimensionKey.equals(World.OVERWORLD)) ? 319 : 255;
			BlockPos chunkPos = task.pos();
			int chunkX = chunkPos.getX() >> 4;
			int chunkZ = chunkPos.getZ() >> 4;

			for (int x = 0; x < 16; x++) {
				for (int z = 0; z < 16; z++) {
					for (int i = 10; i >= 0; i--) { // Scan downwards from `topY`
						BlockPos currentPos = new BlockPos((chunkX << 4) + x, y, (chunkZ << 4) + z);

						if (world.getBlockState(currentPos).getBlock() == Blocks.STRUCTURE_VOID) {
							// Only remove if it's NOT in the (x mod 15, z mod 15) position
							if ((x & 15) != 15 || (z & 15) != 15) {
								world.setBlockState(currentPos, Blocks.AIR.getDefaultState(), 18);
							}
						}
					}
				}
			}
		}
		while (!permaQueue2.isEmpty()){
			PermaTask2 task = permaQueue2.poll();
			if (task == null) continue;

			ServerWorld world = task.world();
			RegistryKey<World> dimensionKey = world.getRegistryKey();
			int y = (dimensionKey.equals(World.OVERWORLD)) ? 319 : 255;
			BlockPos currentPos = new BlockPos(task.pos.getX(), y, task.pos.getZ());

			world.setBlockState(currentPos, Blocks.STRUCTURE_VOID.getDefaultState(), 18);
		}
	}

	public static boolean hasPermaSave(ServerWorld world, BlockPos blockPos) {
		int chunkX = blockPos.getX() >> 4;
		int chunkZ = blockPos.getZ() >> 4;
		int regionX = chunkX >> 10;
		int regionZ = chunkZ >> 10;
		int localX = chunkX & 1023;
		int localZ = chunkZ & 1023;

		String dimensionId = getDimensionId(world);
		String regionKey = dimensionId + ":" + regionX + "," + regionZ;

		RegionStorage region = regionCache.computeIfAbsent(regionKey, key -> {
			Path regionFile = RegionStorage.getRegionFile(world.getServer().getSavePath(WorldSavePath.ROOT), dimensionId, regionX, regionZ);
			return new RegionStorage(regionFile);
		});

		if (region.isChunkSaved(localX, localZ)) return true;

		if (world.getBlockState(blockPos).getBlock() == Blocks.STRUCTURE_VOID){
			if ((blockPos.getX() & 15) != 15 || (blockPos.getZ() & 15) != 15) return true;
			permaQueue.add(new PermaTask(world, blockPos));
			return true;
		}

		BlockPos lastBlockInChunk = new BlockPos((chunkX << 4) + 15, blockPos.getY(), (chunkZ << 4) + 15);
		if (world.getBlockState(lastBlockInChunk).getBlock() == Blocks.STRUCTURE_VOID) {
			return true;
		}

		// If blockPos = lastBlockInChunk and no structure void, place one and add to permaQueue
		if (permaSave) {
			if ((blockPos.getX() & 15) == 15 && (blockPos.getZ() & 15) == 15) {
				permaQueue.add(new PermaTask(world, blockPos));
				permaQueue2.add(new PermaTask2(world, blockPos));
				return false;
			}
		}
		return false;
	}

	private static String getDimensionId(ServerWorld world) {
		if (world.getRegistryKey().equals(World.OVERWORLD)) {
			return "0";
		} else if (world.getRegistryKey().equals(World.NETHER)) {
			return "-1";
		} else if (world.getRegistryKey().equals(World.END)) {
			return "1";
		}
		// Fallback for other dimensions:
		return "0";
	}

	public static Path getConfigFolder() {
		return FabricLoader.getInstance().getGameDir().resolve("config/fractalworld");
	}

	private void loadConfigFromJson(Path configFolder) throws IOException {
		Path configFile = configFolder.resolve("config.json");
		if (Files.exists(configFile) && Files.size(configFile) > 0) {
			try (Reader reader = Files.newBufferedReader(configFile)) {
				Gson gson = new Gson();
				JsonObject jsonConfig = gson.fromJson(reader, JsonObject.class);
				Database.fromJson(jsonConfig);
			} catch (IOException ignored) {}
		}
	}
}