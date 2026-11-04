package tja.multiblockpart;

import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import tja.capability.handler.IRecipeMapProvider;

public final class TJAMultiblockAbility {
    public static final MultiblockAbility<IRecipeMapProvider> CRAFTER_HATCH = MultiblockAbility.ability("crafter_hatches", IRecipeMapProvider.class);
}
