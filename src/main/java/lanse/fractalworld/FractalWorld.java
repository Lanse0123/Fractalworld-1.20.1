package lanse.fractalworld;

import lanse.fractalworld.WorldSorter.SorterPresets;
import lanse.fractalworld.WorldSorter.SortingGenerator;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

public class FractalWorld implements ModInitializer {
	public static boolean isModEnabled = false;
	public static Queue<ChunkTask> processingQueue = new LinkedList<>();
	public static int maxColumnsPerTick = 50;
	public static int tickCount = 0;
	public static boolean permaSave = false;
	public static boolean debug = true;

	@Override
	public void onInitialize() {
		//Initialize required systems like server tick, commands, and disconnect listeners.
		ServerTickEvents.START_SERVER_TICK.register(this::onServerTick);
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> Commands.registerCommands(dispatcher));
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> isModEnabled = false);
	}

	private void onServerTick(MinecraftServer server) {
		if (isModEnabled) {
			tickCount++;
			if (tickCount > 2147483641){
				tickCount = 0;
				isModEnabled = false;
				for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()){
					player.sendMessage(Text.of("Tick count passed integer limit. Mod automatically stopped, And dimensional Queue fixed. Please turn the Mod back on Manually with the command: FractalWorldOn."));
				}
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

			if (processingQueue.size() < maxColumnsPerTick * 20) {
				ChunkGenerationListener.tryNewChunks(server);
			}
		}
	}

	// Process queued chunks in batches, since the game was crashing when it was unlimited speed
	public void processQueuedChunks() {
		int processedCount = 0;

		while (!processingQueue.isEmpty() && processedCount < maxColumnsPerTick) {
			ChunkTask task = processingQueue.poll();
			if (task != null) {
				BlockPos blockPos = new BlockPos(task.x, 319, task.z);

				//This is correctly telling which dimension it is. (DEBUG TESTED)
				RegistryKey<World> dimensionKey = task.world.getRegistryKey();
				if (dimensionKey.equals(World.OVERWORLD)) {
					processOverworldColumn(task, blockPos);
				} else if (dimensionKey.equals(World.NETHER)) {
					processNetherColumn(task, blockPos);
				} else if (dimensionKey.equals(World.END)) {
					processEndColumn(task, blockPos);
				}
				processedCount++;
			}
		}
	}
	private void processOverworldColumn(ChunkTask task, BlockPos blockPos) {

		if (task.world.getPlayers().stream().noneMatch(player -> player.getWorld() == task.world)) {
			return; // Skip processing if no players are present in that dimension
		}

		if (task.world.getBlockState(blockPos).getBlock() != Blocks.STRUCTURE_VOID) {
			WorldEditor.adjustColumn(task.world, task.x, task.z, "OVERWORLD");
			markProcessed(task.world, blockPos);
		}
	}
	private void processNetherColumn(ChunkTask task, BlockPos blockPos) {

		if (task.world.getPlayers().stream().noneMatch(player -> player.getWorld() == task.world)) {
			return; // Skip processing if no players are present in that dimension
		}
		blockPos = new BlockPos(blockPos.getX(), 127, blockPos.getZ());

		if (task.world.getBlockState(blockPos).getBlock() != Blocks.STRUCTURE_VOID) {

			// Destroy Nether Roof logic, clearing blocks up to Y=90
			for (int y = 128; y >= 90; y--) {
				BlockPos pos = new BlockPos(task.x, y, task.z);
				if (!task.world.getBlockState(pos).isAir()) {
					task.world.setBlockState(pos, Blocks.AIR.getDefaultState());
				}
			}
			WorldEditor.adjustColumn(task.world, task.x, task.z, "NETHER");
		}
		markProcessed(task.world, blockPos);
	}
	private void processEndColumn(ChunkTask task, BlockPos blockPos) {

		if (task.world.getPlayers().stream().noneMatch(player -> player.getWorld() == task.world)) {
			return; // Skip processing if no players are present in that dimension
		}
        WorldEditor.adjustColumn(task.world, task.x, task.z, "END");
		markProcessed(task.world, blockPos);
	}

	private void markProcessed(ServerWorld world, BlockPos blockPos) {

		if (permaSave) {
			if (world.getRegistryKey().equals(World.NETHER)) {
				blockPos = new BlockPos(blockPos.getX(), 127, blockPos.getZ());
			} else {
				blockPos = new BlockPos(blockPos.getX(), 319, blockPos.getZ());
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
}