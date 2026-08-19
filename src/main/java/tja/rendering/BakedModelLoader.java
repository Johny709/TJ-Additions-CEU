package tja.rendering;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import tja.TJA;
import tja.TJAValues;
import tja.integration.ae2.blocks.TJACraftingUnitType;
import tja.integration.ae2.render.TJACraftingCubeModel;


import javax.annotation.Nonnull;

public class BakedModelLoader implements ICustomModelLoader {

    private static final TJACraftingCubeModel CRAFTING_STORAGE_65536k_MODEL;
    private static final TJACraftingCubeModel CRAFTING_STORAGE_262144K_MODEL;
    private static final TJACraftingCubeModel CRAFTING_STORAGE_1048M_MODEL;
    private static final TJACraftingCubeModel CRAFTING_STORAGE_SINGULARITY_MODEL;

    static {
        if (TJAValues.isModLoaded(TJAValues.AE2_MOD_ID)) {
            CRAFTING_STORAGE_65536k_MODEL = new TJACraftingCubeModel(TJACraftingUnitType.STORAGE_65M);
            CRAFTING_STORAGE_262144K_MODEL = new TJACraftingCubeModel(TJACraftingUnitType.STORAGE_262M);
            CRAFTING_STORAGE_1048M_MODEL = new TJACraftingCubeModel(TJACraftingUnitType.STORAGE_1048M);
            CRAFTING_STORAGE_SINGULARITY_MODEL = new TJACraftingCubeModel(TJACraftingUnitType.STORAGE_SINGULARITY);
        } else {
            CRAFTING_STORAGE_65536k_MODEL = null;
            CRAFTING_STORAGE_262144K_MODEL = null;
            CRAFTING_STORAGE_1048M_MODEL = null;
            CRAFTING_STORAGE_SINGULARITY_MODEL = null;
        }
    }

    private final Object2ObjectMap<ResourceLocation, IModel> models = new Object2ObjectOpenHashMap<>();

    public BakedModelLoader() {
        if (TJAValues.isModLoaded(TJAValues.AE2_MOD_ID)) {
            this.models.put(new ModelResourceLocation(new ResourceLocation(TJA.MOD_ID, "me.crafting_storage.65536k"), "normal"), CRAFTING_STORAGE_65536k_MODEL);
            this.models.put(new ModelResourceLocation(new ResourceLocation(TJA.MOD_ID, "me.crafting_storage.262144k"), "normal"), CRAFTING_STORAGE_262144K_MODEL);
            this.models.put(new ModelResourceLocation(new ResourceLocation(TJA.MOD_ID, "me.crafting_storage.1048m"), "normal"), CRAFTING_STORAGE_1048M_MODEL);
            this.models.put(new ModelResourceLocation(new ResourceLocation(TJA.MOD_ID, "me.crafting_storage.singularity"), "normal"), CRAFTING_STORAGE_SINGULARITY_MODEL);
        }
    }

    @Override
    public void onResourceManagerReload(@Nonnull IResourceManager resourceManager) {

    }

    @Override
    public boolean accepts(@Nonnull ResourceLocation modelLocation) {
        return this.models.containsKey(modelLocation);
    }

    @Nonnull
    @Override
    public IModel loadModel(@Nonnull ResourceLocation modelLocation) {
        return this.models.get(modelLocation);
    }
}
