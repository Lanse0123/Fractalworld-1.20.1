package lanse.fractalworld;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public class CarpetPlayerWorldLoader {

    public static int trackedPlayerMovementDistance = 20;
    public static ServerPlayerEntity trackedPlayer = null;
    public static boolean playerWorldLoaderEnabled = false;
    public static BlockPos pivotPoint = new BlockPos(0, 0, 0);

    public static void rotatePlayerIfTheyMovedTooFarOrSomething() {
        if (trackedPlayer == null) return;

        BlockPos playerPos = trackedPlayer.getBlockPos();
        double distance = playerPos.getSquaredDistance(pivotPoint);

        if (distance > trackedPlayerMovementDistance * trackedPlayerMovementDistance) {
            // Rotate the player 90 degrees (yaw rotation)
            float newYaw = trackedPlayer.getYaw() + 90.0f;
            if (newYaw >= 360.0f) newYaw -= 360.0f; // Keep yaw within [0, 360)

            trackedPlayer.setYaw(newYaw);
            trackedPlayer.setHeadYaw(newYaw);
            pivotPoint = playerPos;
            trackedPlayerMovementDistance += 15;
        }
    }
}