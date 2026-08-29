package tja.blocks;

import appeng.api.config.Upgrades;
import appeng.block.AEBaseItemBlock;
import appeng.core.features.BlockDefinition;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import tja.TJA;
import tja.integration.ae2.blocks.*;
import tja.items.TJAAE2Items;
import tja.rendering.IBlockModel;
import tja.rendering.IItemMeshing;

import java.util.function.Function;

import static tja.items.TJAAE2Items.UPGRADES;

public final class TJAAE2Blocks {

    public static final Object2ObjectMap<ResourceLocation, BlockDefinition> TJ_BLOCK_DEFINITION_REGISTRY = new Object2ObjectOpenHashMap<>();

    public static BlockDefinition DUAL_INTERFACE_V2;
    public static BlockDefinition SUPER_INTERFACE;
    public static BlockDefinition SUPER_FLUID_INTERFACE;
    public static BlockDefinition SUPER_DUAL_INTERFACE;
    public static BlockDefinition PATTERN_INTERFACE;
    public static BlockDefinition STOCKING_INTERFACE;
    public static BlockDefinition STOCKING_FLUID_INTERFACE;
    public static BlockDefinition STOCKING_DUAL_INTERFACE;
    public static BlockDefinition SUPER_ULTIMATE_INTERFACE;

    public static BlockDefinition CRAFTING_STORAGE_65536K;
    public static BlockDefinition CRAFTING_STORAGE_262144K;
    public static BlockDefinition CRAFTING_STORAGE_1048M;
    public static BlockDefinition CRAFTING_STORAGE_SINGULARITY;

    public static void init(IForgeRegistry<Block> registry) {
        DUAL_INTERFACE_V2 = registerBlock(registry, "me.dual_interface_v2", new BlockDualInterfaceV2(), AEBaseItemBlock::new);
        SUPER_INTERFACE = registerBlock(registry, "me.super_interface", new BlockSuperInterface(), AEBaseItemBlock::new);
        SUPER_FLUID_INTERFACE = registerBlock(registry, "me.super_fluid_interface", new BlockSuperFluidInterface(), AEBaseItemBlock::new);
        SUPER_DUAL_INTERFACE = registerBlock(registry, "me.super_dual_interface", new BlockSuperDualInterface(), AEBaseItemBlock::new);
        PATTERN_INTERFACE = registerBlock(registry, "me.pattern_interface", new BlockPatternInterface(), AEBaseItemBlock::new);
        STOCKING_INTERFACE = registerBlock(registry, "me.stocking_interface", new BlockStockingInterface(), AEBaseItemBlock::new);
        STOCKING_FLUID_INTERFACE = registerBlock(registry, "me.stocking_fluid_interface", new BlockStockingFluidInterface(), AEBaseItemBlock::new);
        STOCKING_DUAL_INTERFACE = registerBlock(registry, "me.stocking_dual_interface", new BlockStockingDualInterface(), AEBaseItemBlock::new);
        SUPER_ULTIMATE_INTERFACE = registerBlock(registry, "me.super_ultimate_interface", new BlockSuperUltimateInterface(), AEBaseItemBlock::new);

        CRAFTING_STORAGE_65536K = registerBlock(registry, "me.crafting_storage.65536k", new BlockTJCraftingUnit(BlockTJCraftingUnit.TJCraftingUnitType.STORAGE_65536k));
        CRAFTING_STORAGE_262144K = registerBlock(registry, "me.crafting_storage.262144k", new BlockTJCraftingUnit(BlockTJCraftingUnit.TJCraftingUnitType.STORAGE_262144k));
        CRAFTING_STORAGE_1048M = registerBlock(registry, "me.crafting_storage.1048m", new BlockTJCraftingUnit(BlockTJCraftingUnit.TJCraftingUnitType.STORAGE_1048M));
        CRAFTING_STORAGE_SINGULARITY = registerBlock(registry, "me.crafting_storage.singularity", new BlockTJCraftingUnit(BlockTJCraftingUnit.TJCraftingUnitType.STORAGE_SINGULARITY));
    }

    @SideOnly(Side.CLIENT)
    public static void registerModels() {
        TJAAE2Blocks.TJ_BLOCK_DEFINITION_REGISTRY.forEach((location, blockDefinition) -> {
            final Block block = blockDefinition.maybeBlock().orElse(null);
            if (block instanceof IBlockModel) {
                ModelLoader.setCustomStateMapper(block, ((IBlockModel) block).getStateMapper(location));
            } else
                ModelLoader.setCustomModelResourceLocation(blockDefinition.maybeItem().orElseThrow(() -> new NullPointerException("Item not found")), 0, new ModelResourceLocation(location, "inventory"));
        });
    }

    @SideOnly(Side.CLIENT)
    public static void registerItemModels() {
        TJAAE2Blocks.TJ_BLOCK_DEFINITION_REGISTRY.forEach((location, blockDefinition) -> {
            final Block block = blockDefinition.maybeBlock().orElse(null);
            if (block instanceof IItemMeshing)
                Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(blockDefinition.maybeItem().orElseThrow(() -> new NullPointerException("Item not found")), 0, new ModelResourceLocation(location, "inventory"));
        });
    }

    public static void registerItemBlock(IForgeRegistry<Item> registry) {
        TJ_BLOCK_DEFINITION_REGISTRY.forEach(((location, blockDefinition) -> registry.register(blockDefinition.maybeItem().orElseThrow(() -> new NullPointerException("Item not found")))));
    }

    public static void registerUpgrades() {
        UPGRADES.put(TJAAE2Blocks.SUPER_INTERFACE.maybeItem().orElse(null), 1);
        UPGRADES.put(TJAAE2Blocks.SUPER_FLUID_INTERFACE.maybeItem().orElse(null), 1);
        UPGRADES.put(TJAAE2Blocks.SUPER_DUAL_INTERFACE.maybeItem().orElse(null), 1);
        UPGRADES.put(TJAAE2Blocks.PATTERN_INTERFACE.maybeItem().orElse(null), 1);
        UPGRADES.put(TJAAE2Blocks.STOCKING_INTERFACE.maybeItem().orElse(null), 1);
        UPGRADES.put(TJAAE2Blocks.STOCKING_FLUID_INTERFACE.maybeItem().orElse(null), 1);
        UPGRADES.put(TJAAE2Blocks.STOCKING_DUAL_INTERFACE.maybeItem().orElse(null), 1);
        UPGRADES.put(TJAAE2Blocks.SUPER_ULTIMATE_INTERFACE.maybeItem().orElse(null), 1);

        // Item Storage Cells
        Upgrades.FUZZY.registerItem(TJAAE2Items.ITEM_CELL_65536K, 1);
        Upgrades.INVERTER.registerItem(TJAAE2Items.ITEM_CELL_65536K, 1);
        Upgrades.STICKY.registerItem(TJAAE2Items.ITEM_CELL_65536K, 1);

        Upgrades.FUZZY.registerItem(TJAAE2Items.ITEM_CELL_262144K, 1);
        Upgrades.INVERTER.registerItem(TJAAE2Items.ITEM_CELL_262144K, 1);
        Upgrades.STICKY.registerItem(TJAAE2Items.ITEM_CELL_262144K, 1);

        Upgrades.FUZZY.registerItem(TJAAE2Items.ITEM_CELL_1048M, 1);
        Upgrades.INVERTER.registerItem(TJAAE2Items.ITEM_CELL_1048M, 1);
        Upgrades.STICKY.registerItem(TJAAE2Items.ITEM_CELL_1048M, 1);

        Upgrades.FUZZY.registerItem(TJAAE2Items.ITEM_CELL_DIGITAL_SINGULARITY, 1);
        Upgrades.INVERTER.registerItem(TJAAE2Items.ITEM_CELL_DIGITAL_SINGULARITY, 1);
        Upgrades.STICKY.registerItem(TJAAE2Items.ITEM_CELL_DIGITAL_SINGULARITY, 1);

        Upgrades.FUZZY.registerItem(TJAAE2Items.ITEM_BLOCK_CONTAINER_64K, 1);
        Upgrades.INVERTER.registerItem(TJAAE2Items.ITEM_BLOCK_CONTAINER_64K, 1);
        Upgrades.STICKY.registerItem(TJAAE2Items.ITEM_BLOCK_CONTAINER_64K, 1);

        Upgrades.FUZZY.registerItem(TJAAE2Items.ITEM_BLOCK_CONTAINER_65536K, 1);
        Upgrades.INVERTER.registerItem(TJAAE2Items.ITEM_BLOCK_CONTAINER_65536K, 1);
        Upgrades.STICKY.registerItem(TJAAE2Items.ITEM_BLOCK_CONTAINER_65536K, 1);

        Upgrades.FUZZY.registerItem(TJAAE2Items.ITEM_BLOCK_CONTAINER_SINGULARITY, 1);
        Upgrades.INVERTER.registerItem(TJAAE2Items.ITEM_BLOCK_CONTAINER_SINGULARITY, 1);
        Upgrades.STICKY.registerItem(TJAAE2Items.ITEM_BLOCK_CONTAINER_SINGULARITY, 1);

        // Fluid Storage Cells
        Upgrades.INVERTER.registerItem(TJAAE2Items.FLUID_CELL_65536K, 1);
        Upgrades.STICKY.registerItem(TJAAE2Items.FLUID_CELL_65536K, 1);

        Upgrades.INVERTER.registerItem(TJAAE2Items.FLUID_CELL_262144K, 1);
        Upgrades.STICKY.registerItem(TJAAE2Items.FLUID_CELL_262144K, 1);

        Upgrades.INVERTER.registerItem(TJAAE2Items.FLUID_CELL_1048M, 1);
        Upgrades.STICKY.registerItem(TJAAE2Items.FLUID_CELL_1048M, 1);

        Upgrades.INVERTER.registerItem(TJAAE2Items.FLUID_CELL_DIGITAL_SINGULARITY, 1);
        Upgrades.STICKY.registerItem(TJAAE2Items.FLUID_CELL_DIGITAL_SINGULARITY, 1);

        // Super Interfaces
        Upgrades.CAPACITY.registerItem(TJAAE2Blocks.SUPER_INTERFACE, 4);
        Upgrades.CAPACITY.registerItem(TJAAE2Items.PART_SUPER_INTERFACE, 4);
        Upgrades.PATTERN_EXPANSION.registerItem(TJAAE2Blocks.SUPER_INTERFACE, 7);
        Upgrades.PATTERN_EXPANSION.registerItem(TJAAE2Items.PART_SUPER_INTERFACE, 7);
        Upgrades.CRAFTING.registerItem(TJAAE2Blocks.SUPER_INTERFACE, 1);
        Upgrades.CRAFTING.registerItem(TJAAE2Items.PART_SUPER_INTERFACE, 1);

        Upgrades.CAPACITY.registerItem(TJAAE2Blocks.SUPER_FLUID_INTERFACE, 4);
        Upgrades.CAPACITY.registerItem(TJAAE2Items.PART_SUPER_FLUID_INTERFACE, 4);

        Upgrades.CAPACITY.registerItem(TJAAE2Blocks.SUPER_DUAL_INTERFACE, 4);
        Upgrades.CAPACITY.registerItem(TJAAE2Items.PART_SUPER_DUAL_INTERFACE, 4);
        Upgrades.PATTERN_EXPANSION.registerItem(TJAAE2Blocks.SUPER_DUAL_INTERFACE, 7);
        Upgrades.PATTERN_EXPANSION.registerItem(TJAAE2Items.PART_SUPER_DUAL_INTERFACE, 7);
        Upgrades.CRAFTING.registerItem(TJAAE2Blocks.SUPER_DUAL_INTERFACE, 1);
        Upgrades.CRAFTING.registerItem(TJAAE2Items.PART_SUPER_DUAL_INTERFACE, 1);

        Upgrades.CAPACITY.registerItem(TJAAE2Blocks.PATTERN_INTERFACE, 4);
        Upgrades.CAPACITY.registerItem(TJAAE2Items.PART_PATTERN_INTERFACE, 4);
        Upgrades.PATTERN_EXPANSION.registerItem(TJAAE2Blocks.PATTERN_INTERFACE, 31);
        Upgrades.PATTERN_EXPANSION.registerItem(TJAAE2Items.PART_PATTERN_INTERFACE, 31);

        Upgrades.CAPACITY.registerItem(TJAAE2Blocks.STOCKING_INTERFACE, 4);
        Upgrades.CAPACITY.registerItem(TJAAE2Items.PART_STOCKING_INTERFACE, 4);
        Upgrades.CRAFTING.registerItem(TJAAE2Blocks.STOCKING_INTERFACE, 1);
        Upgrades.CRAFTING.registerItem(TJAAE2Items.PART_STOCKING_INTERFACE, 1);

        Upgrades.CAPACITY.registerItem(TJAAE2Blocks.STOCKING_DUAL_INTERFACE, 4);
        Upgrades.CAPACITY.registerItem(TJAAE2Items.PART_STOCKING_DUAL_INTERFACE, 4);
        Upgrades.CRAFTING.registerItem(TJAAE2Blocks.STOCKING_DUAL_INTERFACE, 1);
        Upgrades.CRAFTING.registerItem(TJAAE2Items.PART_STOCKING_DUAL_INTERFACE, 1);

        Upgrades.CAPACITY.registerItem(TJAAE2Blocks.SUPER_ULTIMATE_INTERFACE, 4);
        Upgrades.CAPACITY.registerItem(TJAAE2Items.PART_SUPER_ULTIMATE_INTERFACE, 4);
        Upgrades.PATTERN_EXPANSION.registerItem(TJAAE2Blocks.SUPER_ULTIMATE_INTERFACE, 124);
        Upgrades.PATTERN_EXPANSION.registerItem(TJAAE2Items.PART_SUPER_ULTIMATE_INTERFACE, 124);
        Upgrades.CRAFTING.registerItem(TJAAE2Blocks.SUPER_ULTIMATE_INTERFACE, 1);
        Upgrades.CRAFTING.registerItem(TJAAE2Items.PART_SUPER_ULTIMATE_INTERFACE, 1);
    }

    private static BlockDefinition registerBlock(IForgeRegistry<Block> registry, String resource, Block block) {
        return registerBlock(registry, resource, block, null);
    }

    private static BlockDefinition registerBlock(IForgeRegistry<Block> registry, String resource, Block block, Function<Block, ItemBlock> itemBlockFunction) {
        final ResourceLocation resourceLocation = new ResourceLocation(TJA.MOD_ID, resource);
        final ItemBlock itemBlock;
        if (itemBlockFunction != null) {
            itemBlock = itemBlockFunction.apply(block);
        } else itemBlock = new ItemBlock(block);
        itemBlock.setRegistryName(resourceLocation);
        itemBlock.setTranslationKey(resource);
        final BlockDefinition blockDefinition = new BlockDefinition(resource, block, itemBlock);
        block.setRegistryName(resourceLocation);
        block.setTranslationKey(resource);
        registry.register(block);
        TJ_BLOCK_DEFINITION_REGISTRY.put(resourceLocation, blockDefinition);
        return blockDefinition;
    }
}
