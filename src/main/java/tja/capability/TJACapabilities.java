package tja.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public final class TJACapabilities {

    @CapabilityInject(IHeatInfo.class)
    public static Capability<IHeatInfo> CAPABILITY_HEAT = null;

    @CapabilityInject(IItemFluidHandlerInfo.class)
    public static Capability<IItemFluidHandlerInfo> CAPABILITY_ITEM_FLUID_HANDLING = null;
}
