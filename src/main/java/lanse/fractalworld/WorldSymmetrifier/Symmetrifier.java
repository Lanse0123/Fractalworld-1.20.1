package lanse.fractalworld.WorldSymmetrifier;

import lanse.fractalworld.FractalWorld;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

import java.util.ArrayList;
import java.util.List;

public class Symmetrifier {

    public static boolean symmetrifierEnabled = false;
    public static boolean verticalMirrorWorldEnabled = false;
    public static boolean circleGen = false;
    public static boolean symmetrifier = true;
    public static int numberOfCorners = 4;

    public static void clearModes(){
        circleGen = false;
        symmetrifier = false;
    }

    public static void Symmetrify(ServerWorld world, int x, int z){

        if (circleGen) CircleGen(world, x, z);
        else if (symmetrifier) Symmetrify4Corners(world, x, z);
    }

    public static void Symmetrify4Corners(ServerWorld world, int x, int z) {

        if (x > 0 && z > 0){
            return;
        }

        for (int y = 319; y >= -63; y--) {
            BlockPos taskPos = new BlockPos(x, y, z);
            BlockPos dominantPos = new BlockPos(Math.abs(x), y, Math.abs(z));
            world.setBlockState(taskPos, world.getBlockState(dominantPos), 18);
        }
    }

    public static void CircleGen(ServerWorld world, int x, int z) {

        /** I should've just kept this at only 4 corners 💀 **/

        //TODO - All code in the comment separators are circular, instead of symmetrical.
        // They will become a new setting. Make a fixed version.

        /////////////////////////////////////////////////////////////////////////////////////////////
        double sectorAngle = 2 * Math.PI / numberOfCorners;
        double distance = Math.sqrt(x * x + z * z);
        int dominantSector = 0;
        double dominantAngle = dominantSector * sectorAngle;
        int dominantX = (int) Math.round(distance * Math.cos(dominantAngle));
        int dominantZ = (int) Math.round(distance * Math.sin(dominantAngle));

        for (int y = 319; y >= -63; y--) {
            BlockPos taskPos = new BlockPos(x, y, z);
            BlockPos dominantPos = new BlockPos(dominantX, y, dominantZ);
            world.setBlockState(taskPos, world.getBlockState(dominantPos), 18);
        }
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////

    public static void mirrorWorldAbove(ServerWorld world, int x, int z) {
        //TODO - Add compatibility with nether and end.

        BlockPos topBlockPos = world.getTopPosition(Heightmap.Type.WORLD_SURFACE, new BlockPos(x, 0, z));
        int highestY = topBlockPos.getY();

        List<BlockState> blocksBelow = new ArrayList<>();
        for (int y = -64; y <= highestY; y++) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState blockState = world.getBlockState(pos);

            if (!FractalWorld.hasPermaSave(world, topBlockPos)) {
                blocksBelow.add(blockState);
            }
        }
        int currentY = 317;

        for (BlockState state : blocksBelow) {
            if (currentY <= highestY) break;

            // Check if the block is a falling block and replace it with a solid equivalent
            BlockState solidState = state;
            if (state.isOf(Blocks.SAND)) {
                solidState = Blocks.SANDSTONE.getDefaultState();
            } else if (state.isOf(Blocks.RED_SAND)) {
                solidState = Blocks.RED_SANDSTONE.getDefaultState();
            } else if (state.isOf(Blocks.GRAVEL)) {
                solidState = Blocks.STONE.getDefaultState();
            }

            BlockPos mirroredPos = new BlockPos(x, currentY, z);
            world.setBlockState(mirroredPos, solidState, 18);
            currentY--;
        }
    }

    public static void getSettings(ServerCommandSource source){

        String settingsMessage = String.format(
                """     
                        Symmetrical World Settings: (incomplete)
                        
                        - Symmetrifier Enabled: %b
                        - Mirror World Enabled: %b
                        - Symmetrifier 4 corners Enabled: %b
                        - Circle Generation Enabled: %b
                        - Number of Corners: %d""",
                symmetrifierEnabled, verticalMirrorWorldEnabled, symmetrifier, circleGen, numberOfCorners
        );
        source.sendFeedback(() -> Text.literal(settingsMessage), false);
    }
}