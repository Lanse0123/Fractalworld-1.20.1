package lanse.fractalworld;

import lanse.fractalworld.WorldSorter.SortingGenerator;
import lanse.fractalworld.WorldSymmetrifier.Symmetrifier;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DimensionHandler {
    private static final Map<UUID, RegistryKey<World>> playerDimensions = new HashMap<>();
    private static final Map<UUID, Integer> playersToTeleport = new HashMap<>();

    public static void dimensionalChecker(MinecraftServer server) {
        if (!Symmetrifier.symmetrifierEnabled && !SortingGenerator.WorldSorterIsEnabled){

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                UUID playerId = player.getUuid();
                RegistryKey<World> currentDimension = player.getWorld().getRegistryKey();

                if (!playerDimensions.containsKey(playerId)) {
                    playerDimensions.put(playerId, currentDimension);
                } else if (!playerDimensions.get(playerId).equals(currentDimension)) {
                    playerSwitchedDimension(player, currentDimension);
                    playerDimensions.put(playerId, currentDimension);
                }
            }
        }
    }

    public static void playerSwitchedDimension(ServerPlayerEntity player, RegistryKey<World> currentDimension) {
        BlockPos pos;
        if (currentDimension == World.OVERWORLD) {
            pos = new BlockPos((int) player.getX(), 319, (int) player.getZ());
        } else {
            pos = new BlockPos((int) player.getX(), 255, (int) player.getZ());
        }

        if (currentDimension.equals(World.END) || FractalWorld.hasPermaSave((ServerWorld) player.getWorld(), pos)) return;

        player.teleport(player.getServerWorld(), player.getX(), player.getY() + 10000, player.getZ(), player.getYaw(), player.getPitch());
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 215, 1));
        playersToTeleport.put(player.getUuid(), FractalWorld.tickCount + 200);
        ChunkProcessor.complete = false;
    }

    public static void processTeleportQueue(MinecraftServer server, int tickCount) {
        playersToTeleport.entrySet().removeIf(entry -> {
            UUID playerId = entry.getKey();
            int scheduledTick = entry.getValue();

            if (tickCount >= scheduledTick) {
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerId);
                if (player != null) {
                    completeTeleport(player);
                }
                return true;
            }
            return false;
        });
    }

    private static void completeTeleport(ServerPlayerEntity player) {
        BlockPos playerPos = new BlockPos((int) player.getX(), 319, (int) player.getZ());
        int topY = player.getWorld().getTopY(Heightmap.Type.MOTION_BLOCKING, playerPos.getX(), playerPos.getZ());

        if (topY < 1) {
            topY = 63;
            World world = player.getWorld();
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    world.setBlockState(new BlockPos((int) (player.getX() + x), topY, (int) (player.getZ() + z)), Blocks.OBSIDIAN.getDefaultState());
                }
            }
        }
        player.teleport(player.getServerWorld(), player.getX(), topY + 1, player.getZ(), player.getYaw(), player.getPitch());
    }
    public static void resetDimensionHandler(){
        playerDimensions.clear();
        playersToTeleport.clear();
    }
}