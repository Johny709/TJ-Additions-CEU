package tja;

import appeng.client.render.model.AutoRotatingModel;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tja.blocks.TJAAE2Blocks;
import tja.blocks.TJAMetaBlocks;
import tja.items.TJAAE2Items;
import tja.items.TJAItems;
import tja.rendering.BakedModelLoader;

import java.util.HashSet;
import java.util.Set;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = TJA.MOD_ID, value = Side.CLIENT)
public class ClientProxy {

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ModelLoaderRegistry.registerLoader(new BakedModelLoader());
        TJAItems.TJ_ITEM_REGISTRY.forEach((location, item) -> ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(location, "inventory")));
        if (TJAValues.isModLoaded(TJAValues.GREGTECH_MOD_ID))
            TJAMetaBlocks.registerItemModels();
        if (TJAValues.isModLoaded(TJAValues.AE2_MOD_ID)) {
            TJAAE2Items.registerModels();
            TJAAE2Blocks.registerModels();
        }
    }

    @SubscribeEvent
    @Optional.Method(modid = TJAValues.AE2_MOD_ID)
    public static void onModelsBake(ModelBakeEvent event) {
        final IRegistry<ModelResourceLocation, IBakedModel> modelRegistry = event.getModelRegistry();
        final Set<ModelResourceLocation> locationSet = new HashSet<>(modelRegistry.getKeys());
        final IModel missingModel = ModelLoaderRegistry.getMissingModel();
        for (ModelResourceLocation modelResourceLocation : locationSet) {
            if (!modelResourceLocation.getNamespace().equals(TJA.MOD_ID)) continue;
            final IBakedModel model = modelRegistry.getObject(modelResourceLocation);
            if (model == missingModel)
                continue; // Don't customize the missing model. This causes Forge to swallow exceptions
            switch (modelResourceLocation.getPath()) {
                case "me.dual_interface_v2":
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
