package tja.capability.workables;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.items.IItemHandlerModifiable;
import tja.capability.AbstractWorkableHandler;
import tja.capability.handler.IMachineHandler;
import tja.util.TJAItemUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;


public class ArchitectWorkbenchWorkableHandler extends AbstractWorkableHandler<IMachineHandler> {

    private final List<ItemStack> itemInputs = new ArrayList<>();
    private final List<ItemStack> itemOutputs = new ArrayList<>();

    @Nonnull
    private ItemStack catalyst = ItemStack.EMPTY;
    private int parallelsPerformed;
    private int outputIndex;
    private boolean foundRecipe;

    public ArchitectWorkbenchWorkableHandler(MetaTileEntity metaTileEntity) {
        super(metaTileEntity);
    }

    @Override
    public void invalidate() {
        this.lastInputIndex = 0;
    }

    @Override
    protected boolean startRecipe() {
        IItemHandlerModifiable itemInputs;
        if (this.isDistinct) {
            itemInputs = this.handler.getInputBus(this.lastInputIndex);
            this.foundRecipe = this.findCatalyst(itemInputs) && this.findAndConsumeInputs(itemInputs, true);
            if (!this.foundRecipe) for (int i = 0; i < this.busCount; i++) {
                if (i == this.lastInputIndex) continue;
                itemInputs = this.handler.getInputBus(i);
                this.foundRecipe = this.findCatalyst(itemInputs) && this.findAndConsumeInputs(itemInputs, true);
                if (this.foundRecipe) {
                    this.lastInputIndex = i;
                    break;
                }
            }
        } else {
            itemInputs = this.handler.getImportItemInventory();
            this.foundRecipe = this.findCatalyst(itemInputs) && this.findAndConsumeInputs(itemInputs, true);
        }
        if (this.foundRecipe && this.handler.getInputEnergyContainer().getEnergyStored() >= GTValues.VA[this.handler.getTier()]) {
            this.findAndConsumeInputs(itemInputs, false);
            this.maxProgress = this.calculateOverclock(7, 400, 2);
            if (!this.handler.hasFreeParallels())
                this.energyPerTick *= this.parallelsPerformed;
            return true;
        } else return false;
    }

    @Override
    protected boolean completeRecipe() {
        final boolean voidingMode = this.handler.getVoidingModeInt() == 1 || this.handler.getVoidingModeInt() == 3;
        for (int i = this.outputIndex; i < this.itemOutputs.size(); i++) {
            final ItemStack stack = this.itemOutputs.get(i);
            if (voidingMode || TJAItemUtils.insertIntoItemHandler(this.handler.getExportItemInventory(), stack, true).isEmpty()) {
                TJAItemUtils.insertIntoItemHandler(this.handler.getExportItemInventory(), stack, false);
                this.outputIndex++;
            } else return false;
        }
        this.foundRecipe = false;
        this.outputIndex = 0;
        this.itemInputs.clear();
        this.itemOutputs.clear();
        this.catalyst = ItemStack.EMPTY;
        return true;
    }

    private boolean findCatalyst(IItemHandlerModifiable itemInputs) {
        for (int i = 0; i < itemInputs.getSlots(); i++) {
            final ItemStack stack = itemInputs.getStackInSlot(i);
            if (this.isArchitectureStack(stack.getTagCompound())) {
                this.catalyst = stack;
                return true;
            }
        }
        return false;
    }

    private boolean findAndConsumeInputs(IItemHandlerModifiable itemInputs, boolean simulate) {
        this.parallelsPerformed = 0;
        int availableParallels = this.handler.getParallel();
        final int maxParallels = availableParallels;
        for (int i = 0; i < itemInputs.getSlots() && availableParallels > 0; i++) {
            final ItemStack stack = itemInputs.getStackInSlot(i);
            if (stack.isEmpty() || this.isArchitectureStack(stack.getTagCompound())) continue;
            final ItemStack input = itemInputs.extractItem(i, availableParallels, simulate);
            final int inputCount = input.getCount();
            availableParallels -= inputCount;
            if (!simulate) {
                final ItemStack output = TJAItemUtils.getItemStackFromName("architecturecraft:shape", inputCount);
                final NBTTagCompound compound = this.catalyst.getTagCompound().copy();
                compound.setString("BaseName", Item.REGISTRY.getNameForObject(input.getItem()).toString());
                compound.setInteger("BaseData", input.getMetadata());
                output.setTagCompound(compound);
                this.itemInputs.add(input);
                this.itemOutputs.add(output);
                this.parallelsPerformed += inputCount;
            }
        }
        return availableParallels != maxParallels;
    }

    private boolean isArchitectureStack(NBTTagCompound tagCompound) {
        return tagCompound != null && tagCompound.hasKey("Shape") && tagCompound.hasKey("BaseName") && tagCompound.hasKey("BaseData");
    }

    @Nonnull
    @Override
    public NBTTagCompound serializeNBT() {
        final NBTTagCompound compound = super.serializeNBT();
        final NBTTagList itemInputList = new NBTTagList(), itemOutputList = new NBTTagList();
        if (!this.catalyst.isEmpty())
            compound.setTag("catalyst", this.catalyst.serializeNBT());
        for (ItemStack stack : this.itemInputs)
            itemInputList.appendTag(stack.serializeNBT());
        for (ItemStack stack : this.itemOutputs)
            itemOutputList.appendTag(stack.serializeNBT());
        compound.setTag("itemInputs", itemInputList);
        compound.setTag("itemOutputs", itemOutputList);
        compound.setInteger("outputIndex", this.outputIndex);
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        super.deserializeNBT(compound);
        final NBTTagList itemInputList = compound.getTagList("itemInputs", 10), itemOutputsList = compound.getTagList("itemOutputs", 10);
        if (compound.hasKey("catalyst"))
            this.catalyst = new ItemStack(compound.getCompoundTag("catalyst"));
        this.outputIndex = compound.getInteger("outputIndex");
        for (int i = 0; i < itemInputList.tagCount(); i++)
            this.itemInputs.add(new ItemStack(itemInputList.getCompoundTagAt(i)));
        for (int i = 0; i < itemOutputsList.tagCount(); i++)
            this.itemOutputs.add(new ItemStack(itemOutputsList.getCompoundTagAt(i)));
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
