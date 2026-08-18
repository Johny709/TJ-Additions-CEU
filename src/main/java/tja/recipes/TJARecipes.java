package tja.recipes;

import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import tja.TJA;
import tja.TJAValues;


public final class TJARecipes {

    public static void init(IForgeRegistry<IRecipe> recipes) {
        if (TJAValues.isModLoaded(TJAValues.AE2_MOD_ID))
            AE2Recipes.init(recipes);
        if (TJAValues.isModLoaded(TJAValues.GREGTECH_MOD_ID))
            GTRecipes.init(recipes);
    }

    static ResourceLocation resource(String path) {
        return new ResourceLocation(TJA.MOD_ID, path);
    }
}
