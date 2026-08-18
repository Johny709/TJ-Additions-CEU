package tja.capability.handler;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import tja.util.TJAEnergyUtils;
import tja.util.TJAFluidUtils;
import tja.util.TJAGTUtils;

public interface IMachineHandler {

    default IItemHandlerModifiable getImportItemInventory() {
        return TJAGTUtils.DUMMY_ITEM_HANDLER;
    }

    default IItemHandlerModifiable getExportItemInventory() {
        return TJAGTUtils.DUMMY_ITEM_HANDLER;
    }

    default IItemHandlerModifiable getInputBus(int index) {
        return TJAGTUtils.DUMMY_ITEM_HANDLER;
    }

    default IMultipleTankHandler getImportFluidTank() {
        return TJAFluidUtils.DUMMY_FLUID_HANDLER;
    }

    default IMultipleTankHandler getExportFluidTank() {
        return TJAFluidUtils.DUMMY_FLUID_HANDLER;
    }

    default IEnergyContainer getInputEnergyContainer() {
        return TJAEnergyUtils.DUMMY_ENERGY;
    }

    default IEnergyContainer getOutputEnergyContainer() {
        return TJAEnergyUtils.DUMMY_ENERGY;
    }

    default int getTier() {
        return 0;
    }

    default int getParallel() {
        return 1;
    }

    default long getMaxVoltage() {
        return 0;
    }

    default boolean hasMaintenanceHatch() {
        return false;
    }

    default byte getMaintenanceProblems() {
        return 0;
    }

    default void calculateMaintenance(int duration) {}
}
