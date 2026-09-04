package tja.integration.theoneprobe.providers;

import gregtech.integration.theoneprobe.provider.CapabilityInfoProvider;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.Capability;
import tja.TJAValues;
import tja.capability.IHeatInfo;
import tja.capability.TJACapabilities;
import tja.integration.theoneprobe.impl.ElementTJAText;

import javax.annotation.Nonnull;


public class HeatInfoProvider extends CapabilityInfoProvider<IHeatInfo> {

    @Nonnull
    @Override
    protected Capability<IHeatInfo> getCapability() {
        return TJACapabilities.CAPABILITY_HEAT;
    }

    @Override
    protected void addProbeInfo(IHeatInfo heatInfo, IProbeInfo probeInfo, EntityPlayer entityPlayer, TileEntity tileEntity, IProbeHitData iProbeHitData) {
        final long heat = heatInfo.heat();
        final long maxHeat = heatInfo.maxHeat();
        final int progressScaled = maxHeat == 0 ? 0 : (int) Math.floor(heat / (maxHeat * 1.0) * 100);
        final String displayHeat = String.format("%s/%s °C | ", TJAValues.thousandFormat.format(heat), TJAValues.thousandFormat.format(maxHeat));

        IProbeInfo pageInfo = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
        pageInfo.element(new ElementTJAText("{*tja.top.progress.heat*}"));
        pageInfo.progress(progressScaled, 100, probeInfo.defaultProgressStyle()
                .width((int) (displayHeat.length() * 6.2))
                .prefix(displayHeat)
                .suffix("%")
                .alternateFilledColor(0xFFF10000)
                .filledColor(0xFFF10000));
    }

    @Override
    public String getID() {
        return "tja:heat_provider";
    }
}
