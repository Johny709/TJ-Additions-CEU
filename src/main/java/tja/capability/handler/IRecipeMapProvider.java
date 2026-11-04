package tja.capability.handler;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import org.apache.commons.lang3.tuple.Triple;
import tja.recipes.CountableIngredient;


public interface IRecipeMapProvider {

    Int2ObjectMap<Triple<IRecipe, NonNullList<CountableIngredient>, NonNullList<ItemStack>>> getCrafterRecipeMap();

    default void clearRecipeCache() {}
}
