package tja.util.map;

import gregtech.api.recipes.chance.output.impl.ChancedFluidOutput;
import gregtech.api.recipes.chance.output.impl.ChancedItemOutput;
import it.unimi.dsi.fastutil.Hash;
import net.minecraft.item.ItemStack;

import java.util.Arrays;

public final class Strategies {
    public static final ItemStackStrategy ITEMSTACK_STRATEGY = new ItemStackStrategy();
    public static final IngredientStrategy INGREDIENT_STRATEGY = new IngredientStrategy();
    public static final ChancedItemStrategy CHANCED_ITEM_STRATEGY = new ChancedItemStrategy();
    public static final ChancedFluidStrategy CHANCED_FLUID_STRATEGY = new ChancedFluidStrategy();

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

    public static class ChancedItemStrategy implements Hash.Strategy<ChancedItemOutput> {

        @Override
        public int hashCode(ChancedItemOutput o) {
            return o.getIngredient().getItem().hashCode();
        }

        @Override
        public boolean equals(ChancedItemOutput a, ChancedItemOutput b) {
            return b != null &&
                    a.getIngredient().getItem() == b.getIngredient().getItem() &&
                    a.getChance() == b.getChance() &&
                    a.getChanceBoost() == b.getChanceBoost();
        }
    }

    public static class ChancedFluidStrategy implements Hash.Strategy<ChancedFluidOutput> {

        @Override
        public int hashCode(ChancedFluidOutput o) {
            return o.getIngredient().hashCode();
        }

        @Override
        public boolean equals(ChancedFluidOutput a, ChancedFluidOutput b) {
            return b != null &&
                    a.getIngredient().isFluidEqual(b.getIngredient()) &&
                    a.getChance() == b.getChance() &&
                    a.getChanceBoost() == b.getChanceBoost();
        }
    }
}
