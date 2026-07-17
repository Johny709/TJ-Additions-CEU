package tja.recipes;

import gregtech.api.recipes.RecipeMaps;
import gregtech.common.metatileentities.MetaTileEntities;
import tja.machines.TJAMetaTileEntities;

public class TJARecipes {

    public static void init() {
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(MetaTileEntities.COKE_OVEN, 64)
                .input(MetaTileEntities.COKE_OVEN, 64)
                .input(MetaTileEntities.COKE_OVEN, 64)
                .input(MetaTileEntities.COKE_OVEN, 64)
                .output(TJAMetaTileEntities.MEGA_COKE_OVEN)
                .EUt(30).duration(1200)
                .buildAndRegister();
    }
}
