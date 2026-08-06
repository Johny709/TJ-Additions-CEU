package tja.items;

import gregtech.api.items.metaitem.MetaItem;

public final class TJAMetaItems {

    public static MetaItem<?>.MetaValueItem SUPRA_SOLAR_PANEL;
    public static MetaItem<?>.MetaValueItem CREATIVE_ENERGY_COVER;

    public static void init() {
        final TJAMetaItem1 tjaMetaItem1 = new TJAMetaItem1();
        tjaMetaItem1.setRegistryName("meta_item");
    }
}
