package tja;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import tja.blocks.TJABlocks;
import tja.items.TJAItems;

@Mod.EventBusSubscriber(modid = TJA.MOD_ID)
public class CommonProxy {

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        final IForgeRegistry<Block> registry = event.getRegistry();
        TJABlocks.init(registry);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        final IForgeRegistry<Item> registry = event.getRegistry();
        TJAItems.init(registry);
        TJABlocks.TJ_BLOCK_DEFINITION_REGISTRY.forEach(((location, blockDefinition) -> registry.register(blockDefinition.maybeItem().orElseThrow(() -> new NullPointerException("Item not found")))));
    }
}
