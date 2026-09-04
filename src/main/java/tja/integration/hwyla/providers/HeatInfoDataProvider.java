package tja.integration.hwyla.providers;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import mcp.mobius.waila.api.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import tja.capability.IHeatInfo;
import tja.capability.TJACapabilities;

import javax.annotation.Nonnull;
import java.util.List;

public class HeatInfoDataProvider implements IWailaDataProvider {

    public static final HeatInfoDataProvider INSTANCE = new HeatInfoDataProvider();

    public void register(IWailaRegistrar registrar) {
        registrar.registerNBTProvider(this, TileEntity.class);
        registrar.registerBodyProvider(this, TileEntity.class);
        registrar.addConfig("TJA", "tja.heatinfo");
    }

    @Nonnull
    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos) {
        if (!(te instanceof MetaTileEntityHolder))
            return tag;
        final MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) te).getMetaTileEntity();
        if (metaTileEntity == null)
            return tag;
        final IHeatInfo heatInfo = metaTileEntity.getCapability(TJACapabilities.CAPABILITY_HEAT, null);
        if (heatInfo == null)
            return tag;
        final NBTTagCompound compound = new NBTTagCompound();
        compound.setLong("heat", heatInfo.heat());
        compound.setLong("maxHeat", heatInfo.maxHeat());
        tag.setTag("tja.heatinfo", compound);
        return tag;
    }

    @Nonnull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        if (!config.getConfig("tja.heatinfo"))
            return tooltip;
        if (!(accessor.getTileEntity() instanceof MetaTileEntityHolder))
            return tooltip;
        final MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) accessor.getTileEntity()).getMetaTileEntity();
        if (metaTileEntity == null)
            return tooltip;
        final IHeatInfo heatInfo = metaTileEntity.getCapability(TJACapabilities.CAPABILITY_HEAT, null);
        if (heatInfo == null)
            return tooltip;
        final NBTTagCompound compound = accessor.getNBTData().getCompoundTag("tja.heatinfo");
        final long maxHeat = compound.getLong("maxHeat");
        final long heat = Math.min(maxHeat, compound.getLong("heat"));
        tooltip.add(SpecialChars.getRenderString("tja.progressinfo", I18n.format("tja.top.progress.heat"),
                String.valueOf(heat), String.valueOf(maxHeat), "°C", "°C", "RED", ",###"));
        return tooltip;
    }
}
