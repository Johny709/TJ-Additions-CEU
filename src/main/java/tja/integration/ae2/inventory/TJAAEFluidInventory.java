package tja.integration.ae2.inventory;

import appeng.fluids.util.AEFluidInventory;
import appeng.fluids.util.IAEFluidInventory;
import com.cleanroommc.modularui.utils.IMultiFluidTankHandler;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;

import javax.annotation.Nullable;

public class TJAAEFluidInventory extends AEFluidInventory implements IMultiFluidTankHandler {

    public TJAAEFluidInventory(IAEFluidInventory handler, int slots) {
        super(handler, slots);
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
                return TJAAEFluidInventory.this.fill(index, resource, doFill);
            }

            @Nullable
            @Override
            public FluidStack drain(int maxDrain, boolean doDrain) {
                return TJAAEFluidInventory.this.drain(index, maxDrain, doDrain);
            }
        };
    }
}
