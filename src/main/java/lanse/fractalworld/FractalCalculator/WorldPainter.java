package lanse.fractalworld.FractalCalculator;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WorldPainter {

    public static final Block[] concretePalette = {
            Blocks.RED_CONCRETE,
            Blocks.ORANGE_CONCRETE,
            Blocks.YELLOW_CONCRETE,
            Blocks.LIME_CONCRETE,
            Blocks.GREEN_CONCRETE,
            Blocks.CYAN_CONCRETE,
            Blocks.BLUE_CONCRETE,
            Blocks.PURPLE_CONCRETE,
            Blocks.MAGENTA_CONCRETE,
            Blocks.PINK_CONCRETE
    };
    public static final Block[] terracottaPalette = {
            Blocks.RED_TERRACOTTA,
            Blocks.ORANGE_TERRACOTTA,
            Blocks.YELLOW_TERRACOTTA,
            Blocks.LIME_TERRACOTTA,
            Blocks.GREEN_TERRACOTTA,
            Blocks.CYAN_TERRACOTTA,
            Blocks.BLUE_TERRACOTTA,
            Blocks.PURPLE_TERRACOTTA,
            Blocks.MAGENTA_TERRACOTTA,
            Blocks.PINK_TERRACOTTA
    };
    public static final Block[] woolPalette = {
            Blocks.RED_WOOL,
            Blocks.ORANGE_WOOL,
            Blocks.YELLOW_WOOL,
            Blocks.LIME_WOOL,
            Blocks.GREEN_WOOL,
            Blocks.CYAN_WOOL,
            Blocks.BLUE_WOOL,
            Blocks.PURPLE_WOOL,
            Blocks.MAGENTA_WOOL,
            Blocks.PINK_WOOL
    };
    public static final Block[] smallPalette = {
            Blocks.RED_WOOL,
            Blocks.ORANGE_WOOL,
            Blocks.YELLOW_WOOL,
            Blocks.GREEN_WOOL,
            Blocks.BLUE_WOOL,
            Blocks.PURPLE_WOOL,
            Blocks.PINK_WOOL
    };
    public static final Block[] glassPalette = {
            Blocks.RED_STAINED_GLASS,
            Blocks.ORANGE_STAINED_GLASS,
            Blocks.YELLOW_STAINED_GLASS,
            Blocks.LIME_STAINED_GLASS,
            Blocks.GREEN_STAINED_GLASS,
            Blocks.CYAN_STAINED_GLASS,
            Blocks.BLUE_STAINED_GLASS,
            Blocks.PURPLE_STAINED_GLASS,
            Blocks.MAGENTA_STAINED_GLASS,
            Blocks.PINK_STAINED_GLASS
    };
    public static final Block[] combinedPalette = {
            Blocks.RED_CONCRETE, Blocks.RED_TERRACOTTA, Blocks.RED_WOOL,
            Blocks.ORANGE_CONCRETE, Blocks.ORANGE_TERRACOTTA, Blocks.ORANGE_WOOL,
            Blocks.YELLOW_CONCRETE, Blocks.YELLOW_TERRACOTTA, Blocks.YELLOW_WOOL,
            Blocks.LIME_CONCRETE, Blocks.LIME_TERRACOTTA, Blocks.LIME_WOOL,
            Blocks.GREEN_CONCRETE, Blocks.GREEN_TERRACOTTA, Blocks.GREEN_WOOL,
            Blocks.CYAN_CONCRETE, Blocks.CYAN_TERRACOTTA, Blocks.CYAN_WOOL,
            Blocks.BLUE_CONCRETE, Blocks.BLUE_TERRACOTTA, Blocks.BLUE_WOOL,
            Blocks.PURPLE_CONCRETE, Blocks.PURPLE_TERRACOTTA, Blocks.PURPLE_WOOL,
            Blocks.MAGENTA_CONCRETE, Blocks.MAGENTA_TERRACOTTA, Blocks.MAGENTA_WOOL,
            Blocks.PINK_CONCRETE, Blocks.PINK_TERRACOTTA, Blocks.PINK_WOOL
    };
    public static final Block[] woodPalette = {
            Blocks.DARK_OAK_PLANKS, Blocks.SPRUCE_WOOD, Blocks.DARK_OAK_WOOD, Blocks.STRIPPED_DARK_OAK_WOOD, Blocks.MANGROVE_WOOD,
            Blocks.JUNGLE_WOOD, Blocks.STRIPPED_SPRUCE_WOOD, Blocks.SPRUCE_PLANKS, Blocks.OAK_WOOD, Blocks.OAK_PLANKS,
            Blocks.STRIPPED_OAK_WOOD, Blocks.STRIPPED_BIRCH_WOOD, Blocks.BIRCH_PLANKS, Blocks.JUNGLE_PLANKS, Blocks.STRIPPED_JUNGLE_WOOD,
            Blocks.STRIPPED_ACACIA_WOOD, Blocks.STRIPPED_MANGROVE_WOOD, Blocks.STRIPPED_CRIMSON_HYPHAE, Blocks.CRIMSON_PLANKS,
            Blocks.MANGROVE_PLANKS, Blocks.STRIPPED_CHERRY_WOOD, Blocks.CHERRY_PLANKS,

            Blocks.STRIPPED_CHERRY_WOOD, Blocks.MANGROVE_PLANKS, Blocks.CRIMSON_PLANKS, Blocks.STRIPPED_CRIMSON_HYPHAE,
            Blocks.STRIPPED_MANGROVE_WOOD, Blocks.STRIPPED_ACACIA_WOOD, Blocks.STRIPPED_JUNGLE_WOOD, Blocks.JUNGLE_PLANKS,
            Blocks.BIRCH_PLANKS, Blocks.STRIPPED_BIRCH_WOOD, Blocks.STRIPPED_OAK_WOOD, Blocks.OAK_PLANKS, Blocks.OAK_WOOD,
            Blocks.SPRUCE_PLANKS, Blocks.STRIPPED_SPRUCE_WOOD, Blocks.JUNGLE_WOOD, Blocks.MANGROVE_WOOD, Blocks.STRIPPED_DARK_OAK_WOOD,
            Blocks.DARK_OAK_WOOD, Blocks.SPRUCE_WOOD, Blocks.DARK_OAK_PLANKS
    };

    public static Block[] customPalette = {
            Blocks.RED_WOOL, Blocks.ORANGE_WOOL, Blocks.YELLOW_WOOL, Blocks.GREEN_WOOL, Blocks.BLUE_WOOL, Blocks.PURPLE_WOOL, Blocks.PINK_WOOL
    };
    public static final List<String> COLOR_PALLETS = Arrays.asList(
            "concrete", "terracotta", "wool", "full_rainbow", "small_rainbow", "glass", "wood", "custom"
    );
    public static String colorPallet = "full_rainbow";
    private static Block[] currentColorPalette = combinedPalette;
    public static boolean worldPainterEnabled = false;
    public static boolean worldPainterFullHeightEnabled = false;

    public static void setColorPalette(String palette) {

        switch (palette){
            case "concrete" -> {
                currentColorPalette = concretePalette;
                WorldPainter.colorPallet = palette;
            }
            case "terracotta" -> {
                currentColorPalette = terracottaPalette;
                WorldPainter.colorPallet = palette;
            }
            case "wool" -> {
                currentColorPalette = woolPalette;
                WorldPainter.colorPallet = palette;
            }
            case "full_rainbow" -> {
                currentColorPalette = combinedPalette;
                WorldPainter.colorPallet = palette;
            }
            case "small_rainbow" -> {
                currentColorPalette = smallPalette;
                WorldPainter.colorPallet = palette;
            }
            case "glass" -> {
                currentColorPalette = glassPalette;
                WorldPainter.colorPallet = palette;
            }
            case "wood" -> {
                currentColorPalette = woodPalette;
                WorldPainter.colorPallet = palette;
            }
            case "custom" -> {
                currentColorPalette = customPalette;
                WorldPainter.colorPallet = palette;
            }
        }
    }

    public static void paintWorld(ServerWorld world, int x, int z, int iterations) {
        //Paint the top block the correct color. If full height is on, paint the rest of the blocks.

        //Converter between color pallet lists and iterations.
        int colorIndex = iterations % currentColorPalette.length;
        Block block = currentColorPalette[colorIndex];
        BlockPos topPos = world.getTopPosition(Heightmap.Type.WORLD_SURFACE, new BlockPos(x, 0, z)).down();
        world.setBlockState(topPos, block.getDefaultState());

        if (worldPainterFullHeightEnabled) {
            for (int y = topPos.getY(); y > world.getBottomY(); y--) {
                BlockPos posBelow = new BlockPos(x, y, z);
                world.setBlockState(posBelow, block.getDefaultState(), 18);
            }
        }
    }
    public static void paint3DWorld(ServerWorld world, int x, int z, int[] column, int highestY) {
        Block block;

        for (int y = highestY + 3; y >= -64; y--) {
            int iterations = column[y + 64];

            if (iterations >= FractalGenerator.MAX_ITER || iterations <= FractalGenerator.MIN_ITER){
                block = Blocks.AIR;
            } else {
                int colorIndex = iterations % currentColorPalette.length;
                block = currentColorPalette[colorIndex];
            }

            BlockPos pos = new BlockPos(x, y, z);
            world.setBlockState(pos, block.getDefaultState(), 18);
        }
    }
    public static void paintBlack(ServerWorld world, int x, int z) {
        BlockPos topPos = world.getTopPosition(Heightmap.Type.WORLD_SURFACE, new BlockPos(x, 0, z)).down();
        world.setBlockState(topPos, Blocks.BLACK_CONCRETE.getDefaultState(), 18);
    }
    public static void setWorldPainter(boolean enabled) {
        worldPainterEnabled = enabled;
    }
    public static void setWorldPainterFullHeight(boolean enabled) {
        worldPainterFullHeightEnabled = enabled;
    }

    public static boolean setCustomPalette(String blockList) {
        String[] blockNames = blockList.split("\\s+");
        List<Block> blocks = new ArrayList<>();

        for (String blockName : blockNames) {
            Identifier id = new Identifier(blockName);
            if (Registries.BLOCK.containsId(id)) {
                blocks.add(Registries.BLOCK.get(id));
            } else {
                return false; // Invalid block found
            }
        }

        if (!blocks.isEmpty()) {
            customPalette = blocks.toArray(new Block[0]);
            currentColorPalette = customPalette;
            colorPallet = "custom";
            return true;
        }
        return false;
    }

    public static String getPaletteByID() {
        StringBuilder idString = new StringBuilder();

        for (int i = 0; i < customPalette.length; i++) {
            int id = Registries.BLOCK.getRawId(customPalette[i]);
            idString.append(id);
            if (i < customPalette.length - 1) {
                idString.append(","); // Separate IDs with commas
            }
        }
        return idString.toString();
    }

    public static void setPaletteByID(String idList) {
        String[] idStrings = idList.split(",");
        List<Block> blocks = new ArrayList<>();

        for (String idStr : idStrings) {
            try {
                int id = Integer.parseInt(idStr.trim());
                Block block = Registries.BLOCK.get(id);
                blocks.add(block);
            } catch (NumberFormatException e) {
                return; // Invalid number format
            }
        }
        if (!blocks.isEmpty()) {
            customPalette = blocks.toArray(new Block[0]);
        }
    }
}