package tja.items;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import tja.TJA;

public final class TJAItems {

    public static final Object2ObjectMap<ResourceLocation, Item> TJ_ITEM_REGISTRY = new Object2ObjectOpenHashMap<>();

    public static Item UNBREAKABLE_AXE;
    public static Item UNBREAKABLE_HOE;
    public static Item UNBREAKABLE_SHEARS;
    public static Item MAX_CAPACITY_UPGRADE;

    public static void init(IForgeRegistry<Item> registry) {
        MAX_CAPACITY_UPGRADE = registerItem(registry, "me.max_capacity_upgrade", new ItemMaxCapacityUpgrade());
    }

    private static Item registerItem(IForgeRegistry<Item> registry, String location, Item item) {
        item.setRegistryName(new ResourceLocation(TJA.MOD_ID, location));
        item.setTranslationKey(location);
        registry.register(item);
        TJ_ITEM_REGISTRY.put(item.getRegistryName(), item);
        return item;
    }
}
