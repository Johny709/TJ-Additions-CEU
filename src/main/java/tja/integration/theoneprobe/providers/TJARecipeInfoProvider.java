package tja.integration.theoneprobe.providers;

import gregtech.api.GTValues;
import gregtech.integration.theoneprobe.provider.CapabilityInfoProvider;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import tja.capability.IRecipeInfo;
import tja.capability.TJACapabilities;
import tja.integration.theoneprobe.impl.ElementFluidList;
import tja.integration.theoneprobe.impl.ElementItemList;
import tja.integration.theoneprobe.impl.ElementTJAText;
import tja.util.TJAUtility;

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
        if (recipeInfo.getEnergyPerTick() > 0)
            probeInfo.vertical(probeInfo.defaultLayoutStyle()).element(new ElementTJAText(String.format("{*tja.machine.universal.eut[*%s;%s*]*}",
                    recipeInfo.getEnergyPerTick(),
                    GTValues.VOCNF[TJAUtility.getTierFromVoltage(recipeInfo.getEnergyPerTick())])));
        if (recipeInfo.hasProblem()) {
            probeInfo.vertical(probeInfo.defaultLayoutStyle()).element(new ElementTJAText("{*tja.machine.universal.has_problems*}"));
        } else if (recipeInfo.isActive()) {
            probeInfo.vertical(probeInfo.defaultLayoutStyle()).element(new ElementTJAText("{*tja.machine.universal.running*}"));
        }
        if (!itemInputs.isEmpty() || !fluidInputs.isEmpty()) {
            probeInfo.vertical(probeInfo.defaultLayoutStyle()).element(new ElementTJAText("{*tja.top.inputs*}"));
            final IProbeInfo inputInfo = probeInfo.horizontal(probeInfo.defaultLayoutStyle());
            inputInfo.element(new ElementItemList(itemInputs));
            inputInfo.element(new ElementFluidList(fluidInputs));
        }
        if (!itemOutputs.isEmpty() | !fluidOutputs.isEmpty()) {
            probeInfo.vertical(probeInfo.defaultLayoutStyle()).element(new ElementTJAText("{*tja.top.outputs*}"));
            final IProbeInfo outputInfo = probeInfo.horizontal(probeInfo.defaultLayoutStyle());
            outputInfo.element(new ElementItemList(itemOutputs));
            outputInfo.element(new ElementFluidList(fluidOutputs));
        }
    }

    @Override
    public String getID() {
        return "tja:recipe_info";
    }
}
