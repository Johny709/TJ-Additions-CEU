package tja.integration.theoneprobe.providers;

import gregtech.integration.theoneprobe.provider.CapabilityInfoProvider;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.Capability;
import tja.capability.IHeatInfo;
import tja.capability.TJACapabilities;
import tja.integration.theoneprobe.impl.ElementProgressBar;
import tja.util.Color;

import javax.annotation.Nonnull;


public class HeatInfoProvider extends CapabilityInfoProvider<IHeatInfo> {

    @Nonnull
    @Override
    protected Capability<IHeatInfo> getCapability() {
        return TJACapabilities.CAPABILITY_HEAT;
    }

    @Override
    protected void addProbeInfo(IHeatInfo heatInfo, IProbeInfo probeInfo, EntityPlayer entityPlayer, TileEntity tileEntity, IProbeHitData iProbeHitData) {
        final String heat = String.valueOf(heatInfo.heat());
        final String maxHeat = String.valueOf(heatInfo.maxHeat());

        final IProbeInfo pageInfo = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
        pageInfo.element(new ElementProgressBar("{*tja.top.progress.heat*}", heat, maxHeat, "°C", "°C", Color.RED.toString(), ",###"));
    }

    @Override
    public String getID() {
        return "tja:heat_provider";
    }
}
