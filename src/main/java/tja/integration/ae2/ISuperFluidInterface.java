package tja.integration.ae2;

import appeng.fluids.helper.IConfigurableFluidInventory;
import appeng.fluids.helper.IFluidInterfaceHost;
import appeng.helpers.ICustomNameObject;
import appeng.helpers.IPriorityHost;

public interface ISuperFluidInterface extends ICustomNameObject, IPriorityHost, IFluidInterfaceHost, IConfigurableFluidInventory {

    void setPriority(String priority);

    default void setFluidAutoPull(boolean autoPull) {}

    default int getTickTime() {
        return 1;
    }

    default void setTickTime(String tickTime, String id) {}
}
