package tja.capability.workables;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.worldgen.bedrockFluids.BedrockFluidVeinHandler;
import gregtech.common.items.MetaItems;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import tja.capability.AbstractWorkableHandler;
import tja.capability.handler.IMachineHandler;
import tja.util.TJAItemUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class FluidSamplerWorkableHandler extends AbstractWorkableHandler<IMachineHandler> {

    private final List<ItemStack> itemInputs = new ArrayList<>();
    private final List<ItemStack> itemOutputs = new ArrayList<>();

    public FluidSamplerWorkableHandler(MetaTileEntity metaTileEntity) {
        super(metaTileEntity);
    }

    @Override
    protected boolean startRecipe() {
        if (this.handler.getInputEnergyContainer().getEnergyStored() < GTValues.VAOC[this.handler.getTier()])
            return false;
        for (int i = 0; i < this.handler.getImportItemInventory().getSlots(); i++) {
            final ItemStack itemStack = this.handler.getImportItemInventory().getStackInSlot(i);
            if (itemStack.isEmpty() || !MetaItems.TOOL_DATA_STICK.isItemEqual(itemStack)) continue;
            if (this.handler.getImportItemInventory().extractItem(i, 1, true).isEmpty()) continue;
            if (!TJAItemUtils.insertIntoItemHandler(this.handler.getExportItemInventory(), itemStack, true).isEmpty())
                return false;
            this.itemInputs.add(this.handler.getImportItemInventory().extractItem(i, 1, false).copy());
            final World world = this.metaTileEntity.getWorld();
            final Chunk chunk = world.getChunk(this.metaTileEntity.getPos());
            final Fluid fluid = BedrockFluidVeinHandler.getFluidInChunk(world, chunk.x, chunk.z);
            final NBTTagCompound compound = TJAItemUtils.getCompoundFromStack(itemStack);
            final NBTTagCompound fluidCompound = new NBTTagCompound();
            if (fluid != null) {
                fluidCompound.setString("name", "gregtech.material." + fluid.getName());
                fluidCompound.setInteger("yield", BedrockFluidVeinHandler.getFluidYield(world, chunk.x, chunk.z));
                fluidCompound.setInteger("depletedYield", BedrockFluidVeinHandler.getDepletedFluidYield(world, chunk.x, chunk.z));
            }
            compound.setTag("fluidInChunk", fluidCompound);
            this.itemOutputs.add(itemStack.copy());
            this.setMaxProgress(this.calculateOverclock(GTValues.VAOC[0], 1200, 2));
            return true;
        }
        return false;
    }

    @Override
    protected boolean completeRecipe() {
        for (ItemStack itemStack : this.itemOutputs)
            TJAItemUtils.insertIntoItemHandler(this.handler.getExportItemInventory(), itemStack, false);
        this.itemInputs.clear();
        this.itemOutputs.clear();
        return true;
    }

    @Nonnull
    @Override
    public NBTTagCompound serializeNBT() {
        final NBTTagCompound compound = super.serializeNBT();
        final NBTTagList itemInputs = new NBTTagList();
        final NBTTagList itemOutputs = new NBTTagList();
        for (ItemStack itemStack : this.itemInputs)
            itemInputs.appendTag(itemStack.serializeNBT());
        for (ItemStack itemStack : this.itemOutputs)
            itemOutputs.appendTag(itemStack.serializeNBT());
        compound.setTag("itemInputs", itemInputs);
        compound.setTag("itemOutputs", itemOutputs);
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        super.deserializeNBT(compound);
        final NBTTagList itemInputs = compound.getTagList("itemInputs", Constants.NBT.TAG_COMPOUND);
        final NBTTagList itemOutputs = compound.getTagList("itemOutputs", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < itemInputs.tagCount(); i++)
            this.itemInputs.add(new ItemStack(itemInputs.getCompoundTagAt(i)));
        for (int i = 0; i < itemOutputs.tagCount(); i++)
            this.itemOutputs.add(new ItemStack(itemOutputs.getCompoundTagAt(i)));
    }

    @Nonnull
    @Override
    public List<ItemStack> getItemInputs() {
        return this.itemInputs;
    }

    @Nonnull
    @Override
    public List<ItemStack> getItemOutputs() {
        return this.itemOutputs;
    }
}
