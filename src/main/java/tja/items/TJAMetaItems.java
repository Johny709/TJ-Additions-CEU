package tja.items;

import gregtech.api.items.metaitem.MetaItem;

public final class TJAMetaItems {

    public static MetaItem<?>.MetaValueItem SUPRA_SOLAR_PANEL;
    public static MetaItem<?>.MetaValueItem CREATIVE_ENERGY_COVER;
    public static MetaItem<?>.MetaValueItem CREATIVE_ITEM_COVER;
    public static MetaItem<?>.MetaValueItem CREATIVE_FLUID_COVER;

    public static MetaItem<?>.MetaValueItem MIXED_METAL_INGOT;
    public static MetaItem<?>.MetaValueItem ADVANCED_ALLOY_PLATE;

    public static void init() {
        final TJAMetaItem1 tjaMetaItem1 = new TJAMetaItem1();
        tjaMetaItem1.setRegistryName("meta_item");
        final TJAMetaItem2 tjaMetaItem2 = new TJAMetaItem2();
        tjaMetaItem2.setRegistryName("meta_material");
    }
}
