package tja.integration.ae2;

import appeng.fluids.helper.IConfigurableFluidInventory;
import appeng.fluids.helper.IFluidInterfaceHost;
import appeng.helpers.ICustomNameObject;

public interface ISuperFluidInterface extends ICustomNameObject, IPrioritySetter, IFluidInterfaceHost, IConfigurableFluidInventory, ITIckSetter {

    default void setFluidAutoPull(boolean autoPull) {}

    default void setFluidAutoPush(boolean autoPush) {}
}
