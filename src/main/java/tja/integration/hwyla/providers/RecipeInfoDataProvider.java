package tja.integration.hwyla.providers;

import gregtech.api.GTValues;
import mcp.mobius.waila.api.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import tja.TJAValues;
import tja.capability.IRecipeInfo;
import tja.capability.TJACapabilities;
import tja.util.TJAUtility;

import javax.annotation.Nonnull;
import java.util.List;

public class RecipeInfoDataProvider extends CapabilityInfoDataProvider<IRecipeInfo> {

    public static final RecipeInfoDataProvider INSTANCE = new RecipeInfoDataProvider();

    public void register(IWailaRegistrar registrar) {
        super.register(registrar);
        registrar.registerBodyProvider(this, TileEntity.class);
        registrar.registerNBTProvider(this, TileEntity.class);
    }

    @Override
    public Capability<IRecipeInfo> getCapability() {
        return TJACapabilities.CAPABILITY_RECIPE_INFO;
    }

    @Override
    public String getConfigName() {
        return "tja.recipeinfo";
    }

    @Nonnull
    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos, IRecipeInfo recipeInfo) {
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
        tag.setTag(this.getConfigName(), compound);
        return tag;
    }

    @Nonnull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config, IRecipeInfo recipeInfo) {
        final NBTTagCompound compound = accessor.getNBTData().getCompoundTag(this.getConfigName());
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
        return tooltip;
    }
}
