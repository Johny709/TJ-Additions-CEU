package tja.integration.theoneprobe.providers;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IWorkable;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.Capability;
import tja.integration.theoneprobe.impl.ElementTJAText;
import tja.util.Color;

import javax.annotation.Nonnull;

public class CoverWorkableInfoProvider extends CoverCapabilityInfo<IWorkable> {

    @Nonnull
    @Override
    protected Capability<IWorkable> getCapability() {
        return GregtechTileCapabilities.CAPABILITY_WORKABLE;
    }

    @Override
    protected void addProbeInfo(IWorkable workable, IProbeInfo probeInfo, EntityPlayer player, TileEntity tileEntity, IProbeHitData data) {
        final double maxProgress = (double) workable.getMaxProgress() / 20;
        final double progress = Math.min(maxProgress, (double) workable.getProgress() / 20);

        final IProbeInfo progressInfo = probeInfo.vertical(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
        progressInfo.element(new ElementProgressBar("{*gregtech.top.progress*}", String.valueOf(progress), String.valueOf(maxProgress),
                "s", "s", Color.GREEN.toString(), ",##0.00"));
        if (!workable.isWorkingEnabled())
            progressInfo.element(new ElementTJAText("{*gregtech.multiblock.work_paused*}"));
        else if (workable.isActive()) {
            progressInfo.element(new ElementTJAText("{*gregtech.multiblock.running*}"));
        }
    }

    @Override
    public String getID() {
        return "tja:cover_progress_provider";
    }
}
