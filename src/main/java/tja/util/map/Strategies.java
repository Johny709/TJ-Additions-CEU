package tja.util.map;

import it.unimi.dsi.fastutil.Hash;
import net.minecraft.item.ItemStack;

import java.util.Arrays;

public final class Strategies {
    public static final ItemStackStrategy ITEMSTACK_STRATEGY = new ItemStackStrategy();
    public static final IngredientStrategy INGREDIENT_STRATEGY = new IngredientStrategy();

    public static class ItemStackStrategy implements Hash.Strategy<ItemStack> {
        @Override
        public int hashCode(ItemStack o) {
            return o.getItem().hashCode();
        }

        @Override
        public boolean equals(ItemStack a, ItemStack b) {
            return b != null && a.getItem() == b.getItem() && a.getMetadata() == b.getMetadata();
        }
    }

    public static class IngredientStrategy implements Hash.Strategy<ItemStack[]> {

        @Override
        public int hashCode(ItemStack[] o) {
            return Arrays.hashCode(o);
        }

        @Override
        public boolean equals(ItemStack[] a, ItemStack[] b) {
            if (b != null && a.length == b.length) {
                for (int i = 0; i < a.length; i++) {
                    if (!a[i].isItemEqual(b[i]))
                        return false;
                }
                return true;
            }
            return false;
        }
    }
}
