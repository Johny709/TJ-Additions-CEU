package tja.mui.sync;

import com.cleanroommc.modularui.utils.IMultiFluidTankHandler;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import gregtech.api.capability.IMultipleTankHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;
import tja.TJA;
import tja.fluids.GTMultipleTankHandlerWrapper;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Predicate;


public class TJAPhantomFluidSlotSH extends SyncHandler {

    private final Consumer<FluidStack> onUpdate;
    private final Consumer<FluidStack> onExtracted;
    private final IMultiFluidTankHandler tanks;
    private final int slotIndex;
    @Nullable
    private FluidStack fluidStack;
    private boolean specialDrainingMode;
    private Predicate<FluidStack> putFluidsPredicate;
    private Predicate<FluidStack> takeFluidsPredicate;

    public TJAPhantomFluidSlotSH(int slotIndex, IMultipleTankHandler tanks, Consumer<FluidStack> onUpdate) {
        this(slotIndex, new GTMultipleTankHandlerWrapper(tanks), onUpdate);
    }

    public TJAPhantomFluidSlotSH(int slotIndex, IMultiFluidTankHandler tanks, Consumer<FluidStack> onUpdate) {
        this(slotIndex, tanks, onUpdate, null);
    }

    public TJAPhantomFluidSlotSH(int slotIndex, IMultipleTankHandler tanks, Consumer<FluidStack> onUpdate, Consumer<FluidStack> onExtracted) {
        this(slotIndex, new GTMultipleTankHandlerWrapper(tanks), onUpdate, onExtracted);
    }

    public TJAPhantomFluidSlotSH(int slotIndex, IMultiFluidTankHandler tanks, Consumer<FluidStack> onUpdate, Consumer<FluidStack> onExtracted) {
        this.tanks = tanks;
        this.slotIndex = slotIndex;
        this.onUpdate = onUpdate;
        this.onExtracted = onExtracted;
    }

    /**
     * Extracts {@link Integer#MIN_VALUE} of fluid from fluid tank handler. Use this for special fluid tank handler behaviours.
     */
    public TJAPhantomFluidSlotSH setSpecialDrainingMode(boolean specialDrainingMode) {
        this.specialDrainingMode = specialDrainingMode;
        return this;
    }

    /**
     * Set condition for fluid to be placed into slot either by dragging from JEI or placing fluid held by mouse into slot.
     * Will always be true if supplier not set or null.
     */
    public TJAPhantomFluidSlotSH setPutFluidsPredicate(Predicate<FluidStack> putFluidsPredicate) {
        this.putFluidsPredicate = putFluidsPredicate;
        return this;
    }

    /**
     * Set condition for fluid to be removed from slot. Will always be true if supplier not set or null.
     */
    public TJAPhantomFluidSlotSH setTakeFluidsPredicate(Predicate<FluidStack> takeItemsPredicate) {
        this.takeFluidsPredicate = takeItemsPredicate;
        return this;
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) {
        if (id == 1) {
            try {
                this.fluidStack = FluidStack.loadFluidStackFromNBT(buf.readCompoundTag());
            } catch (IOException e) {
                TJA.LOGGER.info(e.getMessage());
            }
        } else if (id == 2) {
            this.fluidStack = null;
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) {
        if (id == 1) {
            try {
                final FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(buf.readCompoundTag());
                if (this.putFluidsPredicate == null || this.putFluidsPredicate.test(fluidStack)) {
                    this.fluidStack = fluidStack;
                    this.tanks.getFluidTank(this.slotIndex).drain(this.specialDrainingMode ? Integer.MIN_VALUE : Integer.MAX_VALUE, true);
                    this.tanks.getFluidTank(this.slotIndex).fill(this.fluidStack, true);
                    if (this.onUpdate != null)
                        this.onUpdate.accept(this.fluidStack);
                } else if (this.fluidStack != null) {
                    this.syncToClient(1, buffer -> buffer.writeCompoundTag(this.fluidStack.writeToNBT(new NBTTagCompound())));
                } else this.syncToClient(2, buffer1 -> {});
            } catch (IOException e) {
                TJA.LOGGER.info(e.getMessage());
            }
        } else if (id == 2) {
            final FluidStack extracted = this.tanks.getFluidTank(this.slotIndex).drain(this.specialDrainingMode ?
                    Integer.MIN_VALUE : Integer.MAX_VALUE, false);
            if (extracted == null) return;
            if (this.takeFluidsPredicate == null || this.takeFluidsPredicate.test(extracted)) {
                final FluidStack fluidStack = this.tanks.getFluidTank(this.slotIndex).drain(this.specialDrainingMode ?
                        Integer.MIN_VALUE : Integer.MAX_VALUE, true);
                if (this.onUpdate != null)
                    this.onUpdate.accept(null);
                if (this.onExtracted != null && fluidStack != null)
                    this.onExtracted.accept(fluidStack);
            } else this.syncToClient(1, buffer -> buffer.writeCompoundTag(extracted.writeToNBT(new NBTTagCompound())));
        }
    }

    @Override
    public void detectAndSendChanges(boolean init) {
        final FluidStack stack = this.tanks.getFluidTank(this.slotIndex).getFluid();
        if (stack != null) {
            this.fluidStack = stack;
            this.syncToClient(1, buffer -> buffer.writeCompoundTag(this.fluidStack.writeToNBT(new NBTTagCompound())));
        }
    }

    public void setFluidStack(@Nullable FluidStack fluidStack) {
        this.fluidStack = fluidStack;
    }

    @Nullable
    public FluidStack getFluidStack() {
        return this.fluidStack;
    }
}
