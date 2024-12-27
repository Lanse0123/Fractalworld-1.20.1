package lanse.fractalworld;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lanse.fractalworld.Automata.AutomataControl;
import lanse.fractalworld.ChunkRandomizer.ChunkRandomizer;
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
import net.minecraft.server.world.ServerWorld;
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
	public static boolean debug = true;
	public static int refreshRate = 1000;
	public static boolean autoRefreshModeIsOn = false;

	@Override
	public void onInitialize() {
		ServerTickEvents.START_SERVER_TICK.register(this::onServerTick);
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> Commands.registerCommands(dispatcher));

		// Check or create configuration folder during server initialization
		Path configFolder = getConfigFolder();
		if (!Files.exists(configFolder)) {
			try {
				Files.createDirectories(configFolder);
			} catch (IOException ignored) {}

		} else {
			loadConfigFromJson(configFolder);
		}
	}

	private void onServerTick(MinecraftServer server) {
		tickCount++;
		if (tickCount > 2147483641) tickCount = 0;

		if (tickCount % 300 == 0 && DataSaver.changed) {
			DataSaver dataSaver = DataSaver.getOrCreate(server);
			Path configFolder = getConfigFolder();
			dataSaver.saveToJson(configFolder);
		}

		if (isModEnabled) {

			if (server != originalServer) isModEnabled = false;

			if ((autoRefreshModeIsOn && tickCount % refreshRate == 0) || ChunkGenerationListener.processedChunkCount > 20000){
				ChunkGenerationListener.clearProcessedChunks();
			}

			if (tickCount % 20 == 0) {
				debug = true;
			}

			if (SortingGenerator.WorldSorterIsEnabled) {
				SorterPresets.sortWorld(server);
				return;
			}

			DimensionHandler.dimensionalChecker(server);
			processQueuedChunks();
			DimensionHandler.processTeleportQueue(server, tickCount);

			if (processingQueue.size() < maxColumnsPerTick * 20
					&& (!ChunkGenerationListener.complete || tickCount % 120 == 0)) {

				//If the chunk randomizer is on, then it will not repeat.
				ChunkGenerationListener.tryNewChunks(server, !ChunkRandomizer.isChunkRandomizerEnabled);
			}
		}
	}

	// Process queued chunks in batches, since the game was crashing when it was unlimited speed
	public void processQueuedChunks() {
		int processedCount = 0;

		while (!processingQueue.isEmpty() && processedCount < maxColumnsPerTick) {
			ChunkTask task = processingQueue.poll();
			if (task != null) {
				BlockPos blockPos;

				if (ChunkRandomizer.isChunkRandomizerEnabled) {
					ChunkRandomizer.RandomizeChunk(task.world, task.x, task.z);
					return;
				}

				RegistryKey<World> dimensionKey = task.world.getRegistryKey();
				if (dimensionKey.equals(World.OVERWORLD)) {
					blockPos = new BlockPos(task.x, 319, task.z);
					processOverworldColumn(task, blockPos);
				} else if (dimensionKey.equals(World.NETHER)) {
					blockPos = new BlockPos(task.x, 255, task.z);
					processNetherColumn(task, blockPos);
				} else if (dimensionKey.equals(World.END)) {
					blockPos = new BlockPos(task.x, 255, task.z);
					processEndColumn(task, blockPos);
				}
				processedCount++;
			}
		}
		if (AutomataControl.automataIsEnabled && tickCount % 5 == 0){
			AutomataControl.completeDrawing();
		}
	}
	private void processOverworldColumn(ChunkTask task, BlockPos blockPos) {

		if (task.world.getPlayers().stream().noneMatch(player -> player.getWorld() == task.world)) {
			return; // Skip processing if no players are present in that dimension
		}

		if (task.world.getBlockState(blockPos).getBlock() != Blocks.STRUCTURE_VOID) {
			WorldEditor.adjustColumn(task.world, task.x, task.z, "OVERWORLD");

			if (Symmetrifier.verticalMirrorWorldEnabled){
				Symmetrifier.mirrorWorldAbove(task.world, task.x, task.z);
			}

			processPermaSave(task.world, blockPos);
		}
	}
	private void processNetherColumn(ChunkTask task, BlockPos blockPos) {

		if (task.world.getPlayers().stream().noneMatch(player -> player.getWorld() == task.world)) {
			return; // Skip processing if no players are present in that dimension
		}
		blockPos = new BlockPos(blockPos.getX(), 255, blockPos.getZ());

		if (task.world.getBlockState(blockPos).getBlock() != Blocks.STRUCTURE_VOID &&
				!Symmetrifier.symmetrifierEnabled && !SortingGenerator.WorldSorterIsEnabled) {

			// Destroy Nether Roof logic, clearing blocks down to Y=90
			for (int y = 128; y >= 90; y--) {
				BlockPos pos = new BlockPos(task.x, y, task.z);
				if (!task.world.getBlockState(pos).isAir()) {
					task.world.setBlockState(pos, Blocks.AIR.getDefaultState());
				}
			}
		}
		WorldEditor.adjustColumn(task.world, task.x, task.z, "NETHER");
		processPermaSave(task.world, blockPos);
	}
	private void processEndColumn(ChunkTask task, BlockPos blockPos) {

		if (task.world.getPlayers().stream().noneMatch(player -> player.getWorld() == task.world)) {
			return; // Skip processing if no players are present in that dimension
		}
		if (task.world.getBlockState(blockPos).getBlock() != Blocks.STRUCTURE_VOID) {
			WorldEditor.adjustColumn(task.world, task.x, task.z, "END");
			processPermaSave(task.world, blockPos);
		}
	}

	public static void processPermaSave(ServerWorld world, BlockPos blockPos) {

		if (permaSave) {
			if (world.getRegistryKey().equals(World.OVERWORLD)) {
				blockPos = new BlockPos(blockPos.getX(), 319, blockPos.getZ());
			} else {
				blockPos = new BlockPos(blockPos.getX(), 255, blockPos.getZ());
			}
			world.setBlockState(blockPos, Blocks.STRUCTURE_VOID.getDefaultState());
		}
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

	private Path getConfigFolder() {
		return FabricLoader.getInstance().getGameDir().resolve("config/fractalworld");
	}

	private void loadConfigFromJson(Path configFolder) {
		Path configFile = configFolder.resolve("config.json");

		if (Files.exists(configFile)) {
			try (Reader reader = Files.newBufferedReader(configFile)) {
				Gson gson = new Gson();
				JsonObject jsonConfig = gson.fromJson(reader, JsonObject.class);
				DataSaver.fromJson(jsonConfig);
			} catch (IOException ignored) {
			}
		}
	}
}

