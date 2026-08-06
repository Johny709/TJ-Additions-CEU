package tja.items;

import gregtech.common.covers.CoverBehaviors;
import net.minecraft.util.ResourceLocation;
import tja.TJA;
import tja.items.behaviors.CoverSupraSolarPanel;

public final class TJACoverBehaviors {

    public static void init() {
        CoverBehaviors.registerBehavior(new ResourceLocation(TJA.MOD_ID, "supra_solar_panel"), TJAMetaItems.SUPRA_SOLAR_PANEL, CoverSupraSolarPanel::new);
    }
}
