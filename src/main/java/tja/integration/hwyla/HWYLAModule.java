package tja.integration.hwyla;

import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.WailaPlugin;
import tja.TJAValues;
import tja.integration.hwyla.providers.HeatInfoDataProvider;
import tja.integration.hwyla.providers.RecipeInfoDataProvider;

@WailaPlugin
public final class HWYLAModule implements IWailaPlugin {

    @Override
    public void register(IWailaRegistrar iWailaRegistrar) {
        if (TJAValues.isModLoaded(TJAValues.GREGTECH_MOD_ID)) {
            HeatInfoDataProvider.INSTANCE.register(iWailaRegistrar);
            RecipeInfoDataProvider.INSTANCE.register(iWailaRegistrar);
        }
    }
}
