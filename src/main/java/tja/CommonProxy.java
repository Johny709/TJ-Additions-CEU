package tja;

import gregtech.api.GregTechAPI;
import gregtech.api.block.VariantItemBlock;
import gregtech.api.metatileentity.registry.MTEManager;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import tja.blocks.TJABlocks;
import tja.blocks.TJAMetaBlocks;
import tja.items.TJAItems;
import tja.recipes.TJARecipes;

import java.util.function.Function;

@Mod.EventBusSubscriber(modid = TJA.MOD_ID)
public class CommonProxy {

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        final IForgeRegistry<Block> registry = event.getRegistry();
        TJABlocks.init(registry);
        if (TJAValues.isModLoaded(TJAValues.GREGTECH_MOD_ID))
            registry.register(TJAMetaBlocks.BATTERY_CELL);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        final IForgeRegistry<Item> registry = event.getRegistry();
        TJAItems.init(registry);
        TJABlocks.TJ_BLOCK_DEFINITION_REGISTRY.forEach(((location, blockDefinition) -> registry.register(blockDefinition.maybeItem().orElseThrow(() -> new NullPointerException("Item not found")))));
        if (TJAValues.isModLoaded(TJAValues.GREGTECH_MOD_ID))
            registry.register(createItemBlock(TJAMetaBlocks.BATTERY_CELL, VariantItemBlock::new));
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        TJARecipes.init();
    }

    @SubscribeEvent
    public static void registerMTERegistry(MTEManager.MTERegistryEvent event) {
        GregTechAPI.mteManager.createRegistry(TJA.MOD_ID);
    }

    private static <T extends Block> ItemBlock createItemBlock(T block, Function<T, ItemBlock> producer) {
        ItemBlock itemBlock = producer.apply(block);
        ResourceLocation registryName = block.getRegistryName();
        if (registryName == null) {
            throw new IllegalArgumentException("Block " + block.getTranslationKey() + " has no registry name.");
        }
        itemBlock.setRegistryName(registryName);
        return itemBlock;
    }
}
