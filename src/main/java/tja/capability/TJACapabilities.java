package tja.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public final class TJACapabilities {

    @CapabilityInject(IHeatInfo.class)
    public static Capability<IHeatInfo> CAPABILITY_HEAT = null;

    @CapabilityInject(IRecipeInfo.class)
    public static Capability<IRecipeInfo> CAPABILITY_RECIPE_INFO = null;
}
