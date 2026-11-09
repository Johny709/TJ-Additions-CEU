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
import net.minecraftforge.items.IItemHandlerModifiable;
import tja.capability.AbstractWorkableHandler;
import tja.capability.handler.IMachineHandler;
import tja.util.TJAItemUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class FluidSamplerWorkableHandler extends AbstractWorkableHandler<IMachineHandler> {

    private final List<ItemStack> itemInputs = new ArrayList<>();
    private final List<ItemStack> itemOutputs = new ArrayList<>();
    private boolean foundRecipe;

    public FluidSamplerWorkableHandler(MetaTileEntity metaTileEntity) {
        super(metaTileEntity);
    }

    @Override
    protected boolean startRecipe() {
        ItemStack itemStack;
        IItemHandlerModifiable itemInputs;
        if (this.isDistinct) {
            itemInputs = this.handler.getInputBus(this.lastInputIndex);
            itemStack = this.findAndConsumeInputs(itemInputs, true);
            if (itemStack.isEmpty()) for (int i = 0; i < this.busCount; i++) {
                if (i == this.lastInputIndex) continue;
                itemInputs = this.handler.getInputBus(i);
                itemStack = this.findAndConsumeInputs(itemInputs, true);
                if (!itemStack.isEmpty()) {
                    this.lastInputIndex = i;
                    break;
                }
            }
        } else {
            itemInputs = this.handler.getImportItemInventory();
            itemStack = this.findAndConsumeInputs(itemInputs, true);
        }
        if (!TJAItemUtils.insertIntoItemHandler(this.handler.getExportItemInventory(), itemStack, true).isEmpty())
            return false;
        this.foundRecipe = !itemStack.isEmpty();
        if (this.foundRecipe && this.handler.getInputEnergyContainer().getEnergyStored() >= GTValues.VAOC[this.handler.getTier()]) {
            final ItemStack dataStick = this.findAndConsumeInputs(itemInputs, false);
            final World world = this.metaTileEntity.getWorld();
            final Chunk chunk = world.getChunk(this.metaTileEntity.getPos());
            final Fluid fluid = BedrockFluidVeinHandler.getFluidInChunk(world, chunk.x, chunk.z);
            final NBTTagCompound compound = TJAItemUtils.getCompoundFromStack(dataStick);
            final NBTTagCompound fluidCompound = new NBTTagCompound();
            if (fluid != null) {
                fluidCompound.setString("name", "gregtech.material." + fluid.getName());
                fluidCompound.setInteger("yield", BedrockFluidVeinHandler.getFluidYield(world, chunk.x, chunk.z));
                fluidCompound.setInteger("depletedYield", BedrockFluidVeinHandler.getDepletedFluidYield(world, chunk.x, chunk.z));
            }
            compound.setTag("fluidInChunk", fluidCompound);
            this.itemOutputs.add(dataStick);
            this.maxProgress = this.calculateOverclock(7, 1200, 2);
            return true;
        } else return false;
    }

    @Override
    protected boolean completeRecipe() {
        for (ItemStack itemStack : this.itemOutputs)
            TJAItemUtils.insertIntoItemHandler(this.handler.getExportItemInventory(), itemStack, false);
        this.itemInputs.clear();
        this.itemOutputs.clear();
        return true;
    }

    private ItemStack findAndConsumeInputs(IItemHandlerModifiable itemInputs, boolean simulate) {
        for (int i = 0; i < itemInputs.getSlots(); i++) {
            if (!MetaItems.TOOL_DATA_STICK.isItemEqual(itemInputs.getStackInSlot(i))) continue;
            final ItemStack stack = itemInputs.extractItem(i, 1, simulate).copy();
            if (stack.isEmpty()) continue;
            if (!simulate)
                this.itemInputs.add(stack);
            return stack;
        }
        return ItemStack.EMPTY;
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

    @Override
    protected boolean otherConditions() {
        return this.foundRecipe;
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
