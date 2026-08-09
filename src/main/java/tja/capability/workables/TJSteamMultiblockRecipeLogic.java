package tja.capability.workables;

import gregtech.api.GTValues;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.logic.OCResult;
import gregtech.api.recipes.properties.RecipePropertyStorage;
import gregtech.api.unification.material.Materials;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import tja.capability.IRecipeInfo;
import tja.capability.TJACapabilities;
import tja.machines.controllers.TJRecipeMapSteamMultiblockController;

import javax.annotation.Nonnull;
import java.util.List;

public class TJSteamMultiblockRecipeLogic extends MultiblockRecipeLogic implements IRecipeInfo {

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

    @Override
    public <T> T getCapability(Capability<T> capability) {
        if (capability == TJACapabilities.CAPABILITY_RECIPE_INFO)
            return TJACapabilities.CAPABILITY_RECIPE_INFO.cast(this);
        return super.getCapability(capability);
    }

    @Override
    public boolean hasProblem() {
        return false;
    }

    @Override
    public long getEnergyPerTick() {
        return 0; // has no energy
    }

    @Nonnull
    @Override
    public List<FluidStack> getFluidOutputs() {
        return this.fluidOutputs;
    }

    @Nonnull
    @Override
    public List<ItemStack> getItemOutputs() {
        return this.itemOutputs;
    }
}
