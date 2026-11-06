package tja.fluids;

import com.cleanroommc.modularui.utils.IMultiFluidTankHandler;
import gregtech.api.capability.IMultipleTankHandler;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;

public class GTMultipleTankHandlerWrapper implements IMultiFluidTankHandler {

    final IMultipleTankHandler multipleTankHandler;

    public GTMultipleTankHandlerWrapper(IMultipleTankHandler multipleTankHandler) {
        this.multipleTankHandler = multipleTankHandler;
    }

    @Override
    public int getTankCount() {
        return this.multipleTankHandler.getTanks();
    }

    @Override
    public IFluidTank getFluidTank(int index) {
        return this.multipleTankHandler.getTankAt(index);
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return this.multipleTankHandler.getTankProperties();
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        return this.multipleTankHandler.fill(resource, doFill);
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        return this.multipleTankHandler.drain(resource, doDrain);
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        return this.multipleTankHandler.drain(maxDrain, doDrain);
    }
}
