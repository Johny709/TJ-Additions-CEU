package tja;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import tja.blocks.TJAAE2Blocks;
import tja.blocks.TJAMetaBlocks;
import tja.items.TJAAE2Items;
import tja.items.TJAItems;
import tja.recipes.TJARecipes;


@Mod.EventBusSubscriber(modid = TJA.MOD_ID)
public class CommonProxy {

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        final IForgeRegistry<Block> registry = event.getRegistry();
        if (TJAValues.isModLoaded(TJAValues.AE2_MOD_ID))
            TJAAE2Blocks.init(registry);
        if (TJAValues.isModLoaded(TJAValues.GREGTECH_MOD_ID))
            registry.register(TJAMetaBlocks.BATTERY_CELL);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        final IForgeRegistry<Item> registry = event.getRegistry();
        TJAItems.init(registry);
        if (TJAValues.isModLoaded(TJAValues.GREGTECH_MOD_ID))
            TJAMetaBlocks.registerItemBlocks(registry);
        if (TJAValues.isModLoaded(TJAValues.AE2_MOD_ID)) {
            TJAAE2Items.init(registry);
            TJAAE2Blocks.registerItemBlock(registry);
        }
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        TJARecipes.init(event.getRegistry());
    }
}
