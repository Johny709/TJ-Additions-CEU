package tja.recipes;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import tja.util.wrappers.GTIngredientWrapper;

import java.util.Arrays;

public final class RecipeUtils {

    private RecipeUtils() {}

    public static NonNullList<CountableIngredient> mergeIngredients(NonNullList<Ingredient> ingredients) {
        final Object2ObjectMap<String, GTIngredientWrapper> itemStacksMap = new Object2ObjectOpenHashMap<>();
        for (Ingredient ingredient : ingredients) {
            itemStacksMap.computeIfAbsent(Arrays.toString(ingredient.getMatchingStacks()), k -> new GTIngredientWrapper(ingredient, 0))
                    .increment(1);
        }
        final NonNullList<CountableIngredient> countableIngredients = NonNullList.create();
        for (GTIngredientWrapper gtIngredientWrapper : itemStacksMap.values())
            countableIngredients.add(new CountableIngredient(gtIngredientWrapper.getIngredient(), gtIngredientWrapper.getCount()));
        return countableIngredients;
    }
}
