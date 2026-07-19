package tja.capability.handler;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import tja.TJAValues;

public interface IMachineHandler {

    default IItemHandlerModifiable getImportItemInventory() {
        return TJAValues.DUMMY_ITEM_HANDLER;
    }

    default IItemHandlerModifiable getExportItemInventory() {
        return TJAValues.DUMMY_ITEM_HANDLER;
    }

    default IItemHandlerModifiable getInputBus(int index) {
        return TJAValues.DUMMY_ITEM_HANDLER;
    }

    default IMultipleTankHandler getImportFluidTank() {
        return TJAValues.DUMMY_FLUID_HANDLER;
    }

    default IMultipleTankHandler getExportFluidTank() {
        return TJAValues.DUMMY_FLUID_HANDLER;
    }

    default IEnergyContainer getInputEnergyContainer() {
        return TJAValues.DUMMY_ENERGY;
    }

    default IEnergyContainer getOutputEnergyContainer() {
        return TJAValues.DUMMY_ENERGY;
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
