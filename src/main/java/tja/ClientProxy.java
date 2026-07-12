package tja;

import appeng.client.render.model.AutoRotatingModel;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tja.blocks.TJABlocks;
import tja.items.TJAItems;
import tja.rendering.BakedModelLoader;
import tja.rendering.IBlockModel;

import java.util.HashSet;
import java.util.Set;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = TJA.MOD_ID, value = Side.CLIENT)
public class ClientProxy {

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ModelLoaderRegistry.registerLoader(new BakedModelLoader());
        TJABlocks.TJ_BLOCK_DEFINITION_REGISTRY.forEach((location, blockDefinition) -> {
            final Block block = blockDefinition.maybeBlock().orElse(null);
            if (block instanceof IBlockModel) {
                ModelLoader.setCustomStateMapper(block, ((IBlockModel) block).getStateMapper(location));
            } else ModelLoader.setCustomModelResourceLocation(blockDefinition.maybeItem().orElseThrow(() -> new NullPointerException("Item not found")), 0, new ModelResourceLocation(location, "inventory"));
        });
        TJAItems.TJ_ITEM_REGISTRY.forEach((location, item) -> ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(location, "inventory")));
        TJAItems.TJ_ITEM_DEFINITION_REGISTRY.forEach((location, itemDefinition) -> ModelLoader.setCustomModelResourceLocation(itemDefinition.maybeItem().orElseThrow(() -> new NullPointerException("Item not found")), 0, new ModelResourceLocation(location, "inventory")));
    }

    @SubscribeEvent
    public static void onModelsBake(ModelBakeEvent event) {
        final IRegistry<ModelResourceLocation, IBakedModel> modelRegistry = event.getModelRegistry();
        final Set<ModelResourceLocation> locationSet = new HashSet<>(modelRegistry.getKeys());
        final IModel missingModel = ModelLoaderRegistry.getMissingModel();
        for (ModelResourceLocation modelResourceLocation : locationSet) {
            if (!modelResourceLocation.getNamespace().equals(TJA.MOD_ID)) continue;
            final IBakedModel model = modelRegistry.getObject(modelResourceLocation);
            if (model == missingModel) continue; // Don't customize the missing model. This causes Forge to swallow exceptions
            switch (modelResourceLocation.getPath()) {
                case "me.super_interface":
                case "me.super_dual_interface":
                case "me.pattern_interface":
                case "me.stocking_interface":
                case "me.stocking_dual_interface":
                case "me.super_ultimate_interface":
                    modelRegistry.putObject(modelResourceLocation, new AutoRotatingModel(model));
            }
        }
    }
}
