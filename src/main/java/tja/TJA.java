package tja;

import appeng.api.config.Upgrades;
import gregtech.api.capability.SimpleCapabilityManager;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import tja.blocks.TJABlocks;
import tja.capability.IHeatInfo;
import tja.capability.IItemFluidHandlerInfo;
import tja.capability.IRecipeInfo;
import tja.items.TJAItems;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tja.machines.TJAMetaTileEntities;
import tja.rendering.IItemMeshing;

import static tja.items.TJAItems.UPGRADES;


@Mod(modid = TJA.MOD_ID, name = TJA.MOD_NAME, version = TJA.VERSION)
public class TJA {
    public static final String MOD_ID = "tja";
    public static final String MOD_NAME = "TJ Additions CEU";
    public static final String VERSION = "1.0";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    /**
     * <a href="https://cleanroommc.com/wiki/forge-mod-development/event#overview">
     *     Take a look at how many FMLStateEvents you can listen to via the @Mod.EventHandler annotation here
     * </a>
     */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("Hello From {}!", MOD_NAME);
        if (TJAValues.isModLoaded(TJAValues.GREGTECH_MOD_ID))
            TJAMetaTileEntities.init();
        SimpleCapabilityManager.registerCapabilityWithNoDefault(IHeatInfo.class);
        SimpleCapabilityManager.registerCapabilityWithNoDefault(IRecipeInfo.class);
        SimpleCapabilityManager.registerCapabilityWithNoDefault(IItemFluidHandlerInfo.class);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        UPGRADES.put(TJABlocks.SUPER_INTERFACE.maybeItem().orElse(null), 1);
        UPGRADES.put(TJABlocks.SUPER_FLUID_INTERFACE.maybeItem().orElse(null), 1);
        UPGRADES.put(TJABlocks.SUPER_DUAL_INTERFACE.maybeItem().orElse(null), 1);
        UPGRADES.put(TJABlocks.PATTERN_INTERFACE.maybeItem().orElse(null), 1);
        UPGRADES.put(TJABlocks.STOCKING_INTERFACE.maybeItem().orElse(null), 1);
        UPGRADES.put(TJABlocks.STOCKING_FLUID_INTERFACE.maybeItem().orElse(null), 1);
        UPGRADES.put(TJABlocks.STOCKING_DUAL_INTERFACE.maybeItem().orElse(null), 1);
        UPGRADES.put(TJABlocks.SUPER_ULTIMATE_INTERFACE.maybeItem().orElse(null), 1);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        if (TJAValues.isModLoaded(TJAValues.AE2_MOD_ID)) {
            // Item Storage Cells
            Upgrades.FUZZY.registerItem(TJAItems.ITEM_CELL_65536K, 1);
            Upgrades.INVERTER.registerItem(TJAItems.ITEM_CELL_65536K, 1);
            Upgrades.STICKY.registerItem(TJAItems.ITEM_CELL_65536K, 1);

            Upgrades.FUZZY.registerItem(TJAItems.ITEM_CELL_262144K, 1);
            Upgrades.INVERTER.registerItem(TJAItems.ITEM_CELL_262144K, 1);
            Upgrades.STICKY.registerItem(TJAItems.ITEM_CELL_262144K, 1);

            Upgrades.FUZZY.registerItem(TJAItems.ITEM_CELL_1048M, 1);
            Upgrades.INVERTER.registerItem(TJAItems.ITEM_CELL_1048M, 1);
            Upgrades.STICKY.registerItem(TJAItems.ITEM_CELL_1048M, 1);

            Upgrades.FUZZY.registerItem(TJAItems.ITEM_CELL_DIGITAL_SINGULARITY, 1);
            Upgrades.INVERTER.registerItem(TJAItems.ITEM_CELL_DIGITAL_SINGULARITY, 1);
            Upgrades.STICKY.registerItem(TJAItems.ITEM_CELL_DIGITAL_SINGULARITY, 1);

            Upgrades.FUZZY.registerItem(TJAItems.ITEM_BLOCK_CONTAINER_64K, 1);
            Upgrades.INVERTER.registerItem(TJAItems.ITEM_BLOCK_CONTAINER_64K, 1);
            Upgrades.STICKY.registerItem(TJAItems.ITEM_BLOCK_CONTAINER_64K, 1);

            Upgrades.FUZZY.registerItem(TJAItems.ITEM_BLOCK_CONTAINER_65536K, 1);
            Upgrades.INVERTER.registerItem(TJAItems.ITEM_BLOCK_CONTAINER_65536K, 1);
            Upgrades.STICKY.registerItem(TJAItems.ITEM_BLOCK_CONTAINER_65536K, 1);

            Upgrades.FUZZY.registerItem(TJAItems.ITEM_BLOCK_CONTAINER_SINGULARITY, 1);
            Upgrades.INVERTER.registerItem(TJAItems.ITEM_BLOCK_CONTAINER_SINGULARITY, 1);
            Upgrades.STICKY.registerItem(TJAItems.ITEM_BLOCK_CONTAINER_SINGULARITY, 1);

            // Fluid Storage Cells
            Upgrades.INVERTER.registerItem(TJAItems.FLUID_CELL_65536K, 1);
            Upgrades.STICKY.registerItem(TJAItems.FLUID_CELL_65536K, 1);

            Upgrades.INVERTER.registerItem(TJAItems.FLUID_CELL_262144K, 1);
            Upgrades.STICKY.registerItem(TJAItems.FLUID_CELL_262144K, 1);

            Upgrades.INVERTER.registerItem(TJAItems.FLUID_CELL_1048M, 1);
            Upgrades.STICKY.registerItem(TJAItems.FLUID_CELL_1048M, 1);

            Upgrades.INVERTER.registerItem(TJAItems.FLUID_CELL_DIGITAL_SINGULARITY, 1);
            Upgrades.STICKY.registerItem(TJAItems.FLUID_CELL_DIGITAL_SINGULARITY, 1);

            // Super Interfaces
            Upgrades.CAPACITY.registerItem(TJABlocks.SUPER_INTERFACE, 4);
            Upgrades.CAPACITY.registerItem(TJAItems.PART_SUPER_INTERFACE, 4);
            Upgrades.PATTERN_EXPANSION.registerItem(TJABlocks.SUPER_INTERFACE, 7);
            Upgrades.PATTERN_EXPANSION.registerItem(TJAItems.PART_SUPER_INTERFACE, 7);
            Upgrades.CRAFTING.registerItem(TJABlocks.SUPER_INTERFACE, 1);
            Upgrades.CRAFTING.registerItem(TJAItems.PART_SUPER_INTERFACE, 1);

            Upgrades.CAPACITY.registerItem(TJABlocks.SUPER_FLUID_INTERFACE, 4);
            Upgrades.CAPACITY.registerItem(TJAItems.PART_SUPER_FLUID_INTERFACE, 4);

            Upgrades.CAPACITY.registerItem(TJABlocks.SUPER_DUAL_INTERFACE, 4);
            Upgrades.CAPACITY.registerItem(TJAItems.PART_SUPER_DUAL_INTERFACE, 4);
            Upgrades.PATTERN_EXPANSION.registerItem(TJABlocks.SUPER_DUAL_INTERFACE, 7);
            Upgrades.PATTERN_EXPANSION.registerItem(TJAItems.PART_SUPER_DUAL_INTERFACE, 7);
            Upgrades.CRAFTING.registerItem(TJABlocks.SUPER_DUAL_INTERFACE, 1);
            Upgrades.CRAFTING.registerItem(TJAItems.PART_SUPER_DUAL_INTERFACE, 1);

            Upgrades.CAPACITY.registerItem(TJABlocks.PATTERN_INTERFACE, 4);
            Upgrades.CAPACITY.registerItem(TJAItems.PART_PATTERN_INTERFACE, 4);
            Upgrades.PATTERN_EXPANSION.registerItem(TJABlocks.PATTERN_INTERFACE, 31);
            Upgrades.PATTERN_EXPANSION.registerItem(TJAItems.PART_PATTERN_INTERFACE, 31);

            Upgrades.CAPACITY.registerItem(TJABlocks.STOCKING_INTERFACE, 4);
            Upgrades.CAPACITY.registerItem(TJAItems.PART_STOCKING_INTERFACE, 4);
            Upgrades.CRAFTING.registerItem(TJABlocks.STOCKING_INTERFACE, 1);
            Upgrades.CRAFTING.registerItem(TJAItems.PART_STOCKING_INTERFACE, 1);

            Upgrades.CAPACITY.registerItem(TJABlocks.STOCKING_DUAL_INTERFACE, 4);
            Upgrades.CAPACITY.registerItem(TJAItems.PART_STOCKING_DUAL_INTERFACE, 4);
            Upgrades.CRAFTING.registerItem(TJABlocks.STOCKING_DUAL_INTERFACE, 1);
            Upgrades.CRAFTING.registerItem(TJAItems.PART_STOCKING_DUAL_INTERFACE, 1);

            Upgrades.CAPACITY.registerItem(TJABlocks.SUPER_ULTIMATE_INTERFACE, 4);
            Upgrades.CAPACITY.registerItem(TJAItems.PART_SUPER_ULTIMATE_INTERFACE, 4);
            Upgrades.PATTERN_EXPANSION.registerItem(TJABlocks.SUPER_ULTIMATE_INTERFACE, 124);
            Upgrades.PATTERN_EXPANSION.registerItem(TJAItems.PART_SUPER_ULTIMATE_INTERFACE, 124);
            Upgrades.CRAFTING.registerItem(TJABlocks.SUPER_ULTIMATE_INTERFACE, 1);
            Upgrades.CRAFTING.registerItem(TJAItems.PART_SUPER_ULTIMATE_INTERFACE, 1);
        }
        if (event.getSide() == Side.CLIENT) {
            TJABlocks.TJ_BLOCK_DEFINITION_REGISTRY.forEach((location, blockDefinition) -> {
                final Block block = blockDefinition.maybeBlock().orElse(null);
                if (block instanceof IItemMeshing)
                    Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(blockDefinition.maybeItem().orElseThrow(() -> new NullPointerException("Item not found")), 0, new ModelResourceLocation(location, "inventory"));
            });
        }
    }
}
