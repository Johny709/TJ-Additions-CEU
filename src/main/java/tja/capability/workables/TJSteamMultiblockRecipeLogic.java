package tja.capability.workables;

import gregtech.api.GTValues;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.logic.OCResult;
import gregtech.api.recipes.properties.RecipePropertyStorage;
import gregtech.api.unification.material.Materials;
import net.minecraftforge.fluids.FluidStack;
import tja.machines.controllers.TJRecipeMapSteamMultiblockController;

import javax.annotation.Nonnull;

public class TJSteamMultiblockRecipeLogic extends MultiblockRecipeLogic {

    private final boolean runAtZeroEnergy;
    private FluidStack steamConsumption;

    public TJSteamMultiblockRecipeLogic(RecipeMapMultiblockController tileEntity) {
        this(tileEntity, false);
    }

    public TJSteamMultiblockRecipeLogic(RecipeMapMultiblockController tileEntity, boolean runAtZeroEnergy) {
        super(tileEntity);
        this.runAtZeroEnergy = runAtZeroEnergy;
    }

    @Override
    protected boolean drawEnergy(long recipeEUt, boolean simulate) {
        return this.runAtZeroEnergy || this.steamConsumption.isFluidStackIdentical(((TJRecipeMapSteamMultiblockController) this.metaTileEntity).getSteamTank().drain(this.steamConsumption, !simulate));
    }

    @Override
    protected boolean hasEnoughPower(long eut, int duration) {
        return this.runAtZeroEnergy || this.steamConsumption.isFluidStackIdentical(((TJRecipeMapSteamMultiblockController) this.metaTileEntity).getSteamTank().drain(this.steamConsumption, false));
    }

    @Override
    protected void modifyOverclockPost(@Nonnull OCResult ocResult, @Nonnull RecipePropertyStorage storage) {
        super.modifyOverclockPost(ocResult, storage);
        this.steamConsumption = Materials.Steam.getFluid((int) ocResult.eut() * 2);
    }

    @Override
    protected long getMaxParallelVoltage() {
        return GTValues.V[GTValues.LV] * this.getParallelLimit();
    }

    @Override
    public long getMaxVoltage() {
        return GTValues.V[GTValues.LV];
    }
}
