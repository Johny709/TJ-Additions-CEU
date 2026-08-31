package tja.integration.hwyla;

import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.WailaPlugin;
import tja.TJAValues;

@WailaPlugin(TJAValues.GREGTECH_MOD_ID)
public final class HWYLAGTModule implements IWailaPlugin {

    @Override
    public void register(IWailaRegistrar iWailaRegistrar) {
        RecipeInfoDataProvider.INSTANCE.register(iWailaRegistrar);
    }
}
