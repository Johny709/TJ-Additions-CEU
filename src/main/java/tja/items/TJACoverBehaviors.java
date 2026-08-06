package tja.items;

import gregtech.common.covers.CoverBehaviors;
import net.minecraft.util.ResourceLocation;
import tja.TJA;
import tja.items.covers.CoverCreativeEnergy;
import tja.items.covers.CoverCreativeFluid;
import tja.items.covers.CoverCreativeItem;
import tja.items.covers.CoverSupraSolarPanel;

public final class TJACoverBehaviors {

    public static void init() {
        CoverBehaviors.registerBehavior(new ResourceLocation(TJA.MOD_ID, "supra_solar_panel"), TJAMetaItems.SUPRA_SOLAR_PANEL, CoverSupraSolarPanel::new);
        CoverBehaviors.registerBehavior(new ResourceLocation(TJA.MOD_ID, "creative_energy_cover"), TJAMetaItems.CREATIVE_ENERGY_COVER, CoverCreativeEnergy::new);
        CoverBehaviors.registerBehavior(new ResourceLocation(TJA.MOD_ID, "creative_item_cover"), TJAMetaItems.CREATIVE_ITEM_COVER, CoverCreativeItem::new);
        CoverBehaviors.registerBehavior(new ResourceLocation(TJA.MOD_ID, "creative_fluid_cover"), TJAMetaItems.CREATIVE_FLUID_COVER, CoverCreativeFluid::new);
    }
}
