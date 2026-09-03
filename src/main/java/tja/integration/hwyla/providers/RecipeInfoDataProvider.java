package tja.integration.hwyla.providers;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import mcp.mobius.waila.api.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import tja.TJAValues;
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
                final NBTTagList itemInputs = new NBTTagList();
                final NBTTagList itemOutputs = new NBTTagList();
                final NBTTagList fluidInputs = new NBTTagList();
                final NBTTagList fluidOutputs = new NBTTagList();
                for (ItemStack input : recipeInfo.getItemInputs())
                    itemInputs.appendTag(input.serializeNBT());
                for (ItemStack output : recipeInfo.getItemOutputs())
                    itemOutputs.appendTag(output.serializeNBT());
                for (FluidStack input : recipeInfo.getFluidInputs())
                    fluidInputs.appendTag(input.writeToNBT(new NBTTagCompound()));
                for (FluidStack output : recipeInfo.getFluidOutputs())
                    fluidOutputs.appendTag(output.writeToNBT(new NBTTagCompound()));
                compound.setTag("itemInputs", itemInputs);
                compound.setTag("itemOutputs", itemOutputs);
                compound.setTag("fluidInputs", fluidInputs);
                compound.setTag("fluidOutputs", fluidOutputs);
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
                        tooltip.add(I18n.format("tja.machine.universal.eut", TJAValues.thousandFormat.format(energyPerTick),
                                GTValues.VOCNF[TJAUtility.getTierByVoltage(energyPerTick)]));
                    if (compound.getBoolean("problem")) {
                        tooltip.add(I18n.format("tja.machine.universal.has_problems"));
                    } else if (compound.getBoolean("active")) {
                        tooltip.add(I18n.format("tja.machine.universal.running"));
                    }
                    final NBTTagList itemInputs = compound.getTagList("itemInputs", 10);
                    final NBTTagList itemOutputs = compound.getTagList("itemOutputs", 10);
                    final NBTTagList fluidInputs = compound.getTagList("fluidInputs", 10);
                    final NBTTagList fluidOutputs = compound.getTagList("fluidOutputs", 10);
                    if (!itemInputs.isEmpty() || !fluidInputs.isEmpty()) {
                        tooltip.add(I18n.format("tja.top.inputs"));
                        tooltip.add(SpecialChars.getRenderString("tja.recipeinfo", "input"));
                    }
                    if (!itemOutputs.isEmpty() || !fluidOutputs.isEmpty()) {
                        tooltip.add(I18n.format("tja.top.outputs"));
                        tooltip.add(SpecialChars.getRenderString("tja.recipeinfo", "output"));
                    }
                }
            }
        }
        return tooltip;
    }
}
