package tja.machines.singleblocks;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import gregtech.api.capability.*;
import gregtech.api.cover.CoverRayTracer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.ItemStackHandler;
import tja.capability.IJEIRecipeUID;
import tja.capability.handler.IMachineHandler;

import javax.annotation.Nonnull;

/**
 * Serves as a basis to implement your own recipe or workable handler for single blocks.
 */
public abstract class TJATieredWorkableMetaTileEntity extends TJATieredMetaTileEntity implements IActiveOutputSide,
        IMachineHandler,
        IJEIRecipeUID,
        IControllable {

    protected final ItemStackHandler chargerInventory;
    private EnumFacing outputFacing = this.getInitialFacing();
    private boolean allowInputFromOutputSide;
    private boolean isItemAutoOutput;
    private boolean isFluidAutoOutput;

    public TJATieredWorkableMetaTileEntity(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        this.chargerInventory = new ItemStackHandler(1) {
            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };
    }

    @Override
    public void update() {
        super.update();
        if (!this.getWorld().isRemote) {
            final IElectricItem chargerContainer = this.chargerInventory.getStackInSlot(0)
                    .getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
            if (chargerContainer != null && this.energyContainer.getEnergyStored() >= this.getMaxVoltage()) {
                this.energyContainer.removeEnergy(chargerContainer.charge(this.getMaxVoltage(), 1, true, false));
            }
            if (this.getOffsetTimer() % 5 == 0) {
                if (this.isItemAutoOutput)
                    this.pushItemsIntoNearbyHandlers(this.outputFacing);
                if (this.isFluidAutoOutput)
                    this.pushFluidsIntoNearbyHandlers(this.outputFacing);
            }
        }
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        final EnumFacing hitFacing = CoverRayTracer.determineGridSideHit(hitResult);
        if (facing == this.getOutputFacing() || (hitFacing == this.getOutputFacing() && playerIn.isSneaking())) {
            if (!this.getWorld().isRemote) {
                if (this.isAllowInputFromOutputSideItems()) {
                    this.setAllowInputFromOutputSide(false);
                    playerIn.sendMessage(new TextComponentTranslation("gregtech.machine.basic.input_from_output_side.disallow"));
                } else {
                    this.setAllowInputFromOutputSide(true);
                    playerIn.sendMessage(new TextComponentTranslation("gregtech.machine.basic.input_from_output_side.allow"));
                }
            }
            return true;
        }
        return super.onScrewdriverClick(playerIn, hand, facing, hitResult);
    }

    @Override
    public boolean onWrenchClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (!playerIn.isSneaking()) {
            final EnumFacing currentOutputSide = this.getOutputFacing();
            if (currentOutputSide == facing || this.getFrontFacing() == facing) {
                return false;
            }
            if (!this.getWorld().isRemote) {
                this.setOutputFacing(facing);
            }
            return true;
        }
        return super.onWrenchClick(playerIn, hand, facing, hitResult);
    }

    @Override
    public void receiveCustomData(int dataId, @Nonnull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        switch (dataId) {
            case 100: this.outputFacing = EnumFacing.VALUES[buf.readByte()]; break;
            case 101: this.isItemAutoOutput = buf.readBoolean(); break;
            case 102: this.isFluidAutoOutput = buf.readBoolean(); break;
        }
        this.scheduleRenderUpdate();
    }

    @Override
    public void writeInitialSyncData(@Nonnull PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(this.isItemAutoOutput);
        buf.writeBoolean(this.isFluidAutoOutput);
        buf.writeInt(this.outputFacing.getIndex());
        buf.writeBoolean(this.allowInputFromOutputSide);
    }

    @Override
    public void receiveInitialSyncData(@Nonnull PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.isItemAutoOutput = buf.readBoolean();
        this.isFluidAutoOutput = buf.readBoolean();
        this.outputFacing = EnumFacing.VALUES[buf.readInt()];
        this.allowInputFromOutputSide = buf.readBoolean();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("isItemAutoOutput", this.isItemAutoOutput);
        data.setBoolean("isFluidAutoOutput", this.isFluidAutoOutput);
        data.setBoolean("allowInputFromOutputSide", this.allowInputFromOutputSide);
        data.setInteger("outputFacing", this.outputFacing.getIndex());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.isItemAutoOutput = data.getBoolean("isItemAutoOutput");
        this.isFluidAutoOutput = data.getBoolean("isFluidAutoOutput");
        this.allowInputFromOutputSide = data.getBoolean("allowInputFromOutputSide");
        this.outputFacing = EnumFacing.VALUES[data.getInteger("outputFacing")];
    }

    protected void setOutputFacing(EnumFacing outputFacing) {
        this.outputFacing = outputFacing;
        if (!this.getWorld().isRemote) {
            this.getHolder().notifyBlockUpdate();
            this.writeCustomData(100, buf -> buf.writeByte(outputFacing.getIndex()));
            this.markDirty();
        }
    }

    protected void setItemAutoOutput(boolean isItemAutoOutput) {
        this.isItemAutoOutput = isItemAutoOutput;
        if (!this.getWorld().isRemote) {
            this.writeCustomData(101, buffer -> buffer.writeBoolean(isItemAutoOutput));
            this.markDirty();
        }
    }

    protected void setFluidAutoOutput(boolean isFluidAutoOutput) {
        this.isFluidAutoOutput = isFluidAutoOutput;
        if (!this.getWorld().isRemote) {
            this.writeCustomData(102, buffer -> buffer.writeBoolean(isFluidAutoOutput));
            this.markDirty();
        }
    }

    public void setAllowInputFromOutputSide(boolean allowInputFromOutputSide) {
        this.allowInputFromOutputSide = allowInputFromOutputSide;
        if (!this.getWorld().isRemote) {
            this.getHolder().notifyBlockUpdate();
            this.writeCustomData(103, buffer -> buffer.writeBoolean(allowInputFromOutputSide));
            this.markDirty();
        }
    }

    @Override
    public boolean isAllowInputFromOutputSideFluids() {
        return this.allowInputFromOutputSide;
    }

    @Override
    public boolean isAllowInputFromOutputSideItems() {
        return this.allowInputFromOutputSide;
    }

    public EnumFacing getOutputFacing() {
        return this.outputFacing;
    }

    public EnumFacing getInitialFacing() {
        return this.frontFacing.getOpposite();
    }

    @Override
    public boolean isAutoOutputItems() {
        return this.isItemAutoOutput;
    }

    @Override
    public boolean isAutoOutputFluids() {
        return isFluidAutoOutput;
    }

    /**
     * Recipe Uid for JEI recipe click area.
     */
    @Override
    public String getRecipeUid() {
        return null;
    }
}
