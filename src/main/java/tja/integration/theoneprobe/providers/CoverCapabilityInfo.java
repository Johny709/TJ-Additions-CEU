package tja.integration.theoneprobe.providers;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.integration.theoneprobe.provider.CapabilityInfoProvider;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public abstract class CoverCapabilityInfo<T> extends CapabilityInfoProvider<T> {

    @Override
    public void addProbeInfo(@Nonnull ProbeMode mode, @Nonnull IProbeInfo probeInfo, @Nonnull EntityPlayer player, @Nonnull World world, IBlockState blockState, @Nonnull IProbeHitData data) {
        if (!blockState.getBlock().hasTileEntity(blockState)) return;
        final EnumFacing sideHit = data.getSideHit();
        final TileEntity tileEntity = world.getTileEntity(data.getPos());
        if (!(tileEntity instanceof MetaTileEntityHolder)) return;
        final MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) tileEntity).getMetaTileEntity();
        if (metaTileEntity == null || metaTileEntity.getCoverAtSide(sideHit) == null) return;
        final T resultCapability = metaTileEntity.getCoverCapability(this.getCapability(), sideHit);
        if (resultCapability != null && allowDisplaying(resultCapability)) {
            this.addProbeInfo(resultCapability, probeInfo, player, tileEntity, data);
        }
    }
}
