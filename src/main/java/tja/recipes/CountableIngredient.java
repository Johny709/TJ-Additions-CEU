package tja.recipes;

import net.minecraft.item.crafting.Ingredient;

public class CountableIngredient {

    private final Ingredient ingredient;
    private final int count;

    public CountableIngredient(Ingredient ingredient, int count) {
        this.ingredient = ingredient;
        this.count = count;
    }

    public Ingredient getIngredient() {
        return this.ingredient;
    }

    public int getCount() {
        return this.count;
    }
}
