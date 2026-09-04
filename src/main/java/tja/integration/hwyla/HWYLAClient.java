package tja.integration.hwyla;

import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.WailaPlugin;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tja.TJAValues;
import tja.integration.hwyla.renderers.ProgressInfoRenderer;
import tja.integration.hwyla.renderers.RecipeInfoRenderer;

@SideOnly(Side.CLIENT)
@WailaPlugin
public final class HWYLAClient implements IWailaPlugin {

    @Override
    public void register(IWailaRegistrar iWailaRegistrar) {
        if (TJAValues.isModLoaded(TJAValues.GREGTECH_MOD_ID)) {
            iWailaRegistrar.registerTooltipRenderer("tja.progressinfo", new ProgressInfoRenderer());
            iWailaRegistrar.registerTooltipRenderer("tja.recipeinfo", new RecipeInfoRenderer());
        }
    }
}
