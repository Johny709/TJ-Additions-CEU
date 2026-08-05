package tja.integration.theoneprobe;

import gregtech.integration.theoneprobe.provider.CapabilityInfoProvider;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.Capability;
import tja.capability.IRecipeInfo;
import tja.capability.TJACapabilities;

import javax.annotation.Nonnull;

public class TJARecipeInfoProvider extends CapabilityInfoProvider<IRecipeInfo> {

    @Override
    protected @Nonnull Capability<IRecipeInfo> getCapability() {
        return TJACapabilities.CAPABILITY_RECIPE_INFO;
    }

    @Override
    protected void addProbeInfo(IRecipeInfo iRecipeInfo, IProbeInfo iProbeInfo, EntityPlayer entityPlayer, TileEntity tileEntity, IProbeHitData iProbeHitData) {

    }

    @Override
    public String getID() {
        return "tja:recipe_info";
    }
}
