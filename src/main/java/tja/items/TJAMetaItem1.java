package tja.items;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.StandardMetaItem;
import net.minecraft.util.ResourceLocation;
import tja.TJA;
import tja.items.behaviors.CreativeEnergyCoverBehavior;
import tja.items.behaviors.CreativeItemCoverBehavior;
import tja.items.behaviors.SupraSolarPanelBehavior;

public class TJAMetaItem1 extends StandardMetaItem {

    @Override
    public void registerSubItems() {
        TJAMetaItems.SUPRA_SOLAR_PANEL = this.addItem(0, "supra_solar_panel").addComponents(new SupraSolarPanelBehavior());
        TJAMetaItems.CREATIVE_ENERGY_COVER = this.addItem(1, "creative_energy_cover").addComponents(new CreativeEnergyCoverBehavior());
        TJAMetaItems.CREATIVE_ITEM_COVER = this.addItem(2, "creative_item_cover").addComponents(new CreativeItemCoverBehavior());
    }

    @Override
    public ResourceLocation createItemModelPath(MetaItem<?>.MetaValueItem metaValueItem, String postfix) {
        return new ResourceLocation(TJA.MOD_ID, this.formatModelPath(metaValueItem) + postfix);
    }
}
