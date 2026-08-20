package tja.items;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.StandardMetaItem;
import net.minecraft.util.ResourceLocation;
import tja.TJA;

public class TJAMetaItem2 extends StandardMetaItem {

    @Override
    public void registerSubItems() {
        TJAMetaItems.MIXED_METAL_INGOT = this.addItem(0, "ingot.mixed_metal");
        TJAMetaItems.ADVANCED_ALLOY_PLATE = this.addItem(1, "plate.advanced_alloy");
    }

    @Override
    public ResourceLocation createItemModelPath(MetaItem<?>.MetaValueItem metaValueItem, String postfix) {
        return new ResourceLocation(TJA.MOD_ID, this.formatModelPath(metaValueItem) + postfix);
    }
}
