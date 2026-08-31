package tja.integration.hwyla;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import tja.capability.IRecipeInfo;
import tja.capability.TJACapabilities;
import tja.machines.MetaTileEntityUtils;
import tja.util.TJAUtility;

import javax.annotation.Nonnull;
import java.util.List;

public class RecipeInfoDataProvider implements IWailaDataProvider {

    public static final RecipeInfoDataProvider INSTANCE = new RecipeInfoDataProvider();

    public void register(IWailaRegistrar registrar) {
        registrar.registerBodyProvider(this, TileEntity.class);
        registrar.registerNBTProvider(this, TileEntity.class);
        registrar.addConfig("TJA", "tja.recipeinfo");
    }

    @Nonnull
    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos) {
        final MetaTileEntity metaTileEntity = MetaTileEntityUtils.getMetaTileEntityAt(world, pos);
        if (metaTileEntity != null) {
            final IRecipeInfo recipeInfo = metaTileEntity.getCapability(TJACapabilities.CAPABILITY_RECIPE_INFO, null);
            if (recipeInfo != null) {
                final NBTTagCompound compound = new NBTTagCompound();
                compound.setLong("energyPerTick", recipeInfo.getEnergyPerTick());
                compound.setBoolean("active", recipeInfo.isActive());
                compound.setBoolean("problem", recipeInfo.hasProblem());
                tag.setTag("tja.recipeinfo", compound);
            }
        }
        return tag;
    }

    @Nonnull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        if (config.getConfig("tja.recipeinfo")) {
            final MetaTileEntity metaTileEntity = MetaTileEntityUtils.getMetaTileEntityAt(accessor.getWorld(), accessor.getPosition());
            if (metaTileEntity != null) {
                final IRecipeInfo recipeInfo = metaTileEntity.getCapability(TJACapabilities.CAPABILITY_RECIPE_INFO, accessor.getSide());
                if (recipeInfo != null) {
                    final NBTTagCompound compound = accessor.getNBTData().getCompoundTag("tja.recipeinfo");
                    final long energyPerTick = compound.getLong("energyPerTick");
                    if (energyPerTick > 0)
                        tooltip.add(I18n.format("tja.machine.universal.eut", energyPerTick,
                                GTValues.VOCNF[TJAUtility.getTierFromVoltage(energyPerTick)]));
                    if (compound.getBoolean("problem")) {
                        tooltip.add(I18n.format("tja.machine.universal.has_problems"));
                    } else if (compound.getBoolean("active")) {
                        tooltip.add(I18n.format("tja.machine.universal.running"));
                    }
                }
            }
        }
        return tooltip;
    }
}
