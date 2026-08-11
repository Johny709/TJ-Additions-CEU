package tja.integration.ae2.inventory;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.fluids.util.AEFluidInventory;
import appeng.fluids.util.AEFluidStack;
import appeng.fluids.util.IAEFluidInventory;
import com.cleanroommc.modularui.utils.IMultiFluidTankHandler;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import tja.integration.ae2.helpers.IDualitySuperFluidInterface;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class TJAENetworkFluidInventory extends AEFluidInventory implements IMultiFluidTankHandler {

    private final Supplier<IStorageGrid> supplier;
    private final IActionSource source;
    private final IDualitySuperFluidInterface duality;

    public TJAENetworkFluidInventory(Supplier<IStorageGrid> networkSupplier, IActionSource source, IAEFluidInventory handler, int slots, int capcity) {
        super(handler, slots, capcity);
        this.supplier = networkSupplier;
        this.source = source;
        this.duality = (IDualitySuperFluidInterface) handler;
    }

    @Override
    public int fill(final FluidStack fluid, final boolean doFill) {
        if (fluid == null || fluid.amount <= 0) {
            return 0;
        }
        IStorageGrid storage = supplier.get();
        if (storage != null) {
            int originAmt = fluid.amount;
            IMEInventory<IAEFluidStack> dest = storage.getInventory(AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class));
            IAEFluidStack overflow = dest.injectItems(AEFluidStack.fromFluidStack(fluid), doFill ? Actionable.MODULATE : Actionable.SIMULATE, this.source);
            if (overflow != null && overflow.getStackSize() == originAmt) {
                return super.fill(fluid, doFill);
            } else if (overflow != null) {
                if (doFill) {
                    FluidStack added = fluid.copy();
                    added.amount = (int) (fluid.amount - overflow.getStackSize());
                    this.duality.onFluidInventoryHasChanged(this, added, null);
                }
                return (int) (originAmt - overflow.getStackSize());
            } else {
                if (doFill) {
                    this.duality.onFluidInventoryHasChanged(this, fluid, null);
                }
                return originAmt;
            }
        } else {
            return super.fill(fluid, doFill);
        }
    }

    @Override
    public int getTankCount() {
        return this.getSlots();
    }

    @Override
    public IFluidTank getFluidTank(int index) {
        return new IFluidTank() {

            @Nullable
            @Override
            public FluidStack getFluid() {
                return getFluidInSlot(index) == null ? null : getFluidInSlot(index).getFluidStack();
            }

            @Override
            public int getFluidAmount() {
                return getFluidInSlot(index) == null ? 0 : getFluidInSlot(index).getFluidStack() != null ? getFluidInSlot(index).getFluidStack().amount : 0;
            }

            @Override
            public int getCapacity() {
                return getTankProperties()[index].getCapacity();
            }

            @Override
            public FluidTankInfo getInfo() {
                return new FluidTankInfo(this);
            }

            @Override
            public int fill(FluidStack resource, boolean doFill) {
                return TJAENetworkFluidInventory.this.fill(index, resource, doFill);
            }

            @Nullable
            @Override
            public FluidStack drain(int maxDrain, boolean doDrain) {
                return TJAENetworkFluidInventory.this.drain(index, maxDrain, doDrain);
            }
        };
    }
}
