package tja.items;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.StandardMetaItem;
import net.minecraft.util.ResourceLocation;
import tja.TJA;
import tja.items.behaviors.SupraSolarPanelBehavior;

public class TJAMetaItem1 extends StandardMetaItem {

    @Override
    public void registerSubItems() {
        TJAMetaItems.SUPRA_SOLAR_PANEL = this.addItem(0, "supra_solar_panel").addComponents(new SupraSolarPanelBehavior());
    }

    @Override
    public ResourceLocation createItemModelPath(MetaItem<?>.MetaValueItem metaValueItem, String postfix) {
        return new ResourceLocation(TJA.MOD_ID, this.formatModelPath(metaValueItem) + postfix);
    }
}
