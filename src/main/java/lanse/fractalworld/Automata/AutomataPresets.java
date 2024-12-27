package lanse.fractalworld.Automata;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

public class AutomataPresets {

    //I KNOW IT LOOKS BAD BUT IT'S NOT WHAT IT LOOKS LIKE I PROMISE 💀💀💀
    public static int rule = 34;
    public static boolean nextState;

    public static void wolframElementaryAutomataDRAW2D(ServerWorld world, int x, int z) {
        BlockPos currentPos = world.getTopPosition(Heightmap.Type.WORLD_SURFACE, new BlockPos(x, 0, z)).down();
        if (AutomataControl.isAlive(world, currentPos.getX(), currentPos.getZ())) return;

        boolean[] ruleBits = getRuleBits(rule);
        int pattern = getPattern(world, x, z);
        boolean nextState = ruleBits[pattern];

        AutomataControl.automataQueue.add(new AutomataControl.AutomataTask(world, x, z, nextState));
    }
    private static boolean[] getRuleBits(int rule) {
        boolean[] ruleBits = new boolean[512];
        for (int i = 0; i < 512; i++) {
            ruleBits[i] = (rule & (1 << i)) != 0;
        }
        return ruleBits;
    }
    private static int getPattern(ServerWorld world, int x, int z) {
        int pattern = 0;
        int index = 8;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                boolean isAlive = AutomataControl.isAlive(world, x + dx, z + dz);
                if (isAlive) {
                    pattern |= (1 << index);
                }
                index--;
            }
        }
        return pattern;
    }

    //TODO - make this calculate the blocks and then update them at the end of the tick. This way blocks arent changing
    // as much mid cycle. Results can be stored in a map or something.

    public static void conwaysLifeDRAW2D(ServerWorld world, int x, int z) {

        int liveNeighbors = AutomataControl.countLiveNeighbors(world, x, z);
        boolean isCurrentlyAlive = AutomataControl.isAlive(world, x, z);

        if (isCurrentlyAlive) {
            nextState = (liveNeighbors == 2 || liveNeighbors == 3); // Survival: 2-3 neighbors
        } else {
            nextState = (liveNeighbors == 3); // Birth: exactly 3 neighbors
        }

        AutomataControl.addToQueue(new AutomataControl.AutomataTask(world, x, z, nextState));
    }

    public static void highlifeDRAW2D(ServerWorld world, int x, int z) {
        int liveNeighbors = AutomataControl.countLiveNeighbors(world, x, z);
        boolean isCurrentlyAlive = AutomataControl.isAlive(world, x, z);

        if (isCurrentlyAlive) {
            nextState = (liveNeighbors == 2 || liveNeighbors == 3);
        } else {
            nextState = (liveNeighbors == 3 || liveNeighbors == 6); // Highlife addition
        }

        AutomataControl.addToQueue(new AutomataControl.AutomataTask(world, x, z, nextState));
    }

    public static void coralDRAW2D(ServerWorld world, int x, int z) {
        int liveNeighbors = AutomataControl.countLiveNeighbors(world, x, z);
        boolean isCurrentlyAlive = AutomataControl.isAlive(world, x, z);

        if (isCurrentlyAlive) {
            nextState = (liveNeighbors >= 4); // Survival: 4-8 neighbors
        } else {
            nextState = (liveNeighbors == 3); // Birth: exactly 3 neighbors
        }

        AutomataControl.addToQueue(new AutomataControl.AutomataTask(world, x, z, nextState));
    }

    public static void invasiveCoralDRAW2D(ServerWorld world, int x, int z) {
        int liveNeighbors = AutomataControl.countLiveNeighbors(world, x, z);
        boolean isCurrentlyAlive = AutomataControl.isAlive(world, x, z);

        if (isCurrentlyAlive) {
            nextState = (liveNeighbors >= 4); // Survival: 4-8 neighbors
        } else {
            nextState = (liveNeighbors == 3); // Birth: exactly 3 neighbors
        }

        BlockPos blockpos = world.getTopPosition(Heightmap.Type.WORLD_SURFACE, new BlockPos(x, 0, z)).down();

        if (nextState){
            world.setBlockState(blockpos, Blocks.BLACK_CONCRETE.getDefaultState());
        } else {
            world.setBlockState(blockpos, AutomataControl.lastBlock.getDefaultState());
        }

    }

    public static void mazeDRAW2D(ServerWorld world, int x, int z) {
        int liveNeighbors = AutomataControl.countLiveNeighbors(world, x, z);
        boolean isCurrentlyAlive = AutomataControl.isAlive(world, x, z);

        if (isCurrentlyAlive) {
            nextState = (liveNeighbors >= 1 && liveNeighbors <= 5); // Survival: 1-5 neighbors
        } else {
            nextState = (liveNeighbors == 3); // Birth: exactly 3 neighbors
        }

        AutomataControl.addToQueue(new AutomataControl.AutomataTask(world, x, z, nextState));
    }

    public static void amoebaDRAW2D(ServerWorld world, int x, int z) {
        int liveNeighbors = AutomataControl.countLiveNeighbors(world, x, z);
        boolean isCurrentlyAlive = AutomataControl.isAlive(world, x, z);

        if (isCurrentlyAlive) {
            nextState = (liveNeighbors == 1 || liveNeighbors == 3 || liveNeighbors == 5 || liveNeighbors == 8);
        } else {
            nextState = (liveNeighbors == 3 || liveNeighbors == 5 || liveNeighbors == 7);
        }

        AutomataControl.addToQueue(new AutomataControl.AutomataTask(world, x, z, nextState));
    }
    public static void caveCreatorDRAW2D(ServerWorld world, int x, int z) {
        int liveNeighbors = AutomataControl.countLiveNeighbors(world, x, z);
        boolean isCurrentlyAlive = AutomataControl.isAlive(world, x, z);

        if (isCurrentlyAlive) {
            nextState = liveNeighbors >= 4; // Solid remains if 4+ neighbors
        } else {
            nextState = liveNeighbors >= 5; // Empty becomes solid if 5+ neighbors
        }

        AutomataControl.addToQueue(new AutomataControl.AutomataTask(world, x, z, nextState));
    }
    public static void spongeDRAW2D(ServerWorld world, int x, int z) {
        int liveNeighbors = AutomataControl.countLiveNeighbors(world, x, z);
        boolean useDiagonalVariant = AutomataControl.noiseFunction(x, z) > 0.5;
        boolean isInSponge = useDiagonalVariant ? AutomataControl.isDiagonalMengerSponge(x, z) : AutomataControl.isMengerSponge(x, z);

        if (isInSponge) {
            nextState = liveNeighbors > 0;
        } else {
            nextState = liveNeighbors >= 3 && liveNeighbors <= 5;
        }

        AutomataControl.addToQueue(new AutomataControl.AutomataTask(world, x, z, nextState));
    }

    //Special 2D automata below.
    public static void wireWorldDRAW2D(ServerWorld world, int x, int z) {
        BlockPos currentPos = world.getTopPosition(Heightmap.Type.WORLD_SURFACE, new BlockPos(x, 0, z)).down();
        BlockState currentState = world.getBlockState(currentPos);
        BlockState nextState = currentState;

        if (currentState.isOf(Blocks.BLUE_CONCRETE)) {
            // Electron Head -> Electron Tail
            nextState = Blocks.RED_CONCRETE.getDefaultState();

        } else if (currentState.isOf(Blocks.RED_CONCRETE)) {
            // Electron Tail -> Conductor
            nextState = Blocks.BLACK_CONCRETE.getDefaultState();

        } else if (currentState.isOf(Blocks.BLACK_CONCRETE)) {
            // Conductor -> Electron Head if 1 or 2 Electron Head neighbors
            int electronHeadCount = AutomataControl.countElectronHeadNeighbors(world, x, z);
            if (electronHeadCount == 1 || electronHeadCount == 2) {
                nextState = Blocks.BLUE_CONCRETE.getDefaultState();
            }
        }

        if (!currentState.equals(nextState)) {
            AutomataControl.addToQueue(new AutomataControl.WireTask(world, x, z, nextState.getBlock()));
        }
    }
}