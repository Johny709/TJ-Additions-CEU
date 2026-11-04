package tja.capability.handler;

import net.minecraftforge.items.IItemHandlerModifiable;

public interface IFarmerHandler extends IMachineHandler {

    IItemHandlerModifiable getToolInventory();

    IItemHandlerModifiable getFertilizerInventory();
}
