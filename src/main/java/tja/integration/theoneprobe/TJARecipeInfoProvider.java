package tja.integration.theoneprobe;

import gregtech.integration.theoneprobe.provider.CapabilityInfoProvider;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.TextStyleClass;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import tja.capability.IRecipeInfo;
import tja.capability.TJACapabilities;
import tja.integration.theoneprobe.impl.ElementFluidStack;

import javax.annotation.Nonnull;
import java.util.List;

public class TJARecipeInfoProvider extends CapabilityInfoProvider<IRecipeInfo> {

    @Override
    protected @Nonnull Capability<IRecipeInfo> getCapability() {
        return TJACapabilities.CAPABILITY_RECIPE_INFO;
    }

    @Override
    protected void addProbeInfo(IRecipeInfo recipeInfo, IProbeInfo probeInfo, EntityPlayer entityPlayer, TileEntity tileEntity, IProbeHitData probeHitData) {
        final List<ItemStack> itemInputs = recipeInfo.getItemInputs();
        final List<ItemStack> itemOutputs = recipeInfo.getItemOutputs();
        final List<FluidStack> fluidInputs = recipeInfo.getFluidInputs();
        final List<FluidStack> fluidOutputs = recipeInfo.getFluidOutputs();
        if (recipeInfo.hasProblem()) {
            probeInfo.vertical(probeInfo.defaultLayoutStyle()).text(TextStyleClass.INFO + "{*tja.machine.universal.has_problems*}");
        } else if (recipeInfo.isActive()) {
            probeInfo.vertical(probeInfo.defaultLayoutStyle()).text(TextStyleClass.INFO + "{*tja.machine.universal.running*}");
        }
        if (!itemInputs.isEmpty() || !fluidInputs.isEmpty()) {
            probeInfo.vertical(probeInfo.defaultLayoutStyle()).text(TextStyleClass.INFO + "{*tja.top.inputs*}");
            final IProbeInfo inputInfo = probeInfo.horizontal(probeInfo.defaultLayoutStyle());
            for (FluidStack fluidStack : fluidInputs)
                inputInfo.element(new ElementFluidStack(fluidStack));
            for (ItemStack itemStack : itemInputs)
                inputInfo.item(itemStack);
        }
        if (!itemOutputs.isEmpty() | !fluidOutputs.isEmpty()) {
            probeInfo.vertical(probeInfo.defaultLayoutStyle()).text(TextStyleClass.INFO + "{*tja.top.outputs*}");
            final IProbeInfo outputInfo = probeInfo.horizontal(probeInfo.defaultLayoutStyle());
            for (FluidStack fluidStack : fluidOutputs)
                outputInfo.element(new ElementFluidStack(fluidStack));
            for (ItemStack itemStack : itemOutputs)
                outputInfo.item(itemStack);
        }
    }

    @Override
    public String getID() {
        return "tja:recipe_info";
    }
}
