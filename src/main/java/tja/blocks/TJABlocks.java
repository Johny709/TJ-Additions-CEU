package tja.blocks;

import appeng.block.AEBaseItemBlock;
import appeng.core.features.BlockDefinition;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import tja.TJA;
import tja.integration.ae2.blocks.*;
import tja.integration.ae2.tile.*;

import java.util.function.Function;

public class TJABlocks {

    public static final Object2ObjectMap<ResourceLocation, BlockDefinition> TJ_BLOCK_DEFINITION_REGISTRY = new Object2ObjectOpenHashMap<>();

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

        GameRegistry.registerTileEntity(TileSuperInterface.class, new ResourceLocation(TJA.MOD_ID, "me.super_interface"));
        GameRegistry.registerTileEntity(TileSuperFluidInterface.class, new ResourceLocation(TJA.MOD_ID, "me.super_fluid_interface"));
        GameRegistry.registerTileEntity(TileSuperDualInterface.class, new ResourceLocation(TJA.MOD_ID, "me.super_dual_interface"));
        GameRegistry.registerTileEntity(TilePatternInterface.class, new ResourceLocation(TJA.MOD_ID, "me.pattern_interface"));
        GameRegistry.registerTileEntity(TileStockingInterface.class, new ResourceLocation(TJA.MOD_ID, "me.stocking_interface"));
        GameRegistry.registerTileEntity(TileStockingFluidInterface.class, new ResourceLocation(TJA.MOD_ID, "me.stocking_fluid_interface"));
        GameRegistry.registerTileEntity(TileStockingDualInterface.class, new ResourceLocation(TJA.MOD_ID, "me.stocking_dual_interface"));
        GameRegistry.registerTileEntity(TileSuperUltimateInterface.class, new ResourceLocation(TJA.MOD_ID, "me.super_ultimate_interface"));
        GameRegistry.registerTileEntity(TileTJCraftingStorageTile.class, new ResourceLocation(TJA.MOD_ID, "me.crafting_storage"));
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
