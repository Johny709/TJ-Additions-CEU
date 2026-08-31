package tja.mixin.gregtech;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tja.capability.IRecipeInfo;
import tja.capability.TJACapabilities;
import tja.util.TJAItemUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mixin(value = AbstractRecipeLogic.class, remap = false)
public abstract class MixinAbstractRecipeLogic implements IRecipeInfo {

    @Shadow
    @Nonnull
    protected List<ItemStack> itemOutputs;

    @Shadow
    @Nonnull
    protected List<FluidStack> fluidOutputs;

    @Shadow
    protected Recipe previousRecipe;

    @Shadow
    public abstract int getParallelRecipesPerformed();

    @Override
    public boolean hasProblem() {
        return false;
    }

    @Override
    public long getEnergyPerTick() {
        return 0;
    }

    @Nonnull
    @Override
    public List<ItemStack> getItemInputs() {
        if (this.previousRecipe == null)
            return Collections.emptyList();
        final List<ItemStack> itemStacks = new ArrayList<>();
        for (GTRecipeInput item : this.previousRecipe.getInputs()) {
            item = item.copyWithAmount(item.getAmount() * this.getParallelRecipesPerformed());
            itemStacks.add(TJAItemUtils.getItemStackOreDict(item.getInputStacks(), false));
        }
        return itemStacks;
    }

    @Nonnull
    @Override
    public List<ItemStack> getItemOutputs() {
        return this.itemOutputs;
    }

    @Nonnull
    @Override
    public List<FluidStack> getFluidInputs() {
        if (this.previousRecipe == null)
            return Collections.emptyList();
        final List<FluidStack> fluidStacks = new ArrayList<>();
        for (GTRecipeInput fluid : this.previousRecipe.getFluidInputs()) {
            final FluidStack fluidStack = fluid.getInputFluidStack().copy();
            fluidStack.amount = fluidStack.amount * this.getParallelRecipesPerformed();
            fluidStacks.add(fluidStack);
        }
        return fluidStacks;
    }

    @Nonnull
    @Override
    public List<FluidStack> getFluidOutputs() {
        return this.fluidOutputs;
    }

    /**
     * @author Johny
     * @reason other mixin options are slow.
     */
    @Overwrite
    public <T> T getCapability(Capability<T> capability) {
        final AbstractRecipeLogic recipeLogic = (AbstractRecipeLogic)(Object)this;
        if (capability == GregtechTileCapabilities.CAPABILITY_WORKABLE) {
            return GregtechTileCapabilities.CAPABILITY_WORKABLE.cast(recipeLogic);
        } else if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(recipeLogic);
        } else if (capability == GregtechTileCapabilities.CAPABILITY_RECIPE_LOGIC) {
            return GregtechTileCapabilities.CAPABILITY_RECIPE_LOGIC.cast(recipeLogic);
        } else if (capability == TJACapabilities.CAPABILITY_RECIPE_INFO) {
            return TJACapabilities.CAPABILITY_RECIPE_INFO.cast((IRecipeInfo) recipeLogic);
        } else return null;
    }
}
