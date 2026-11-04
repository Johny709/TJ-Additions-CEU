package tja.recipes;

import net.minecraft.item.crafting.Ingredient;

import java.util.Arrays;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CountableIngredient that = (CountableIngredient) o;
        return count == that.count &&
                Objects.equals(ingredient, that.ingredient);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ingredient, count);
    }

    @Override
    public String toString() {
        return "CountableIngredient{" +
                "ingredient=" + Arrays.toString(ingredient.getMatchingStacks()) +
                ", count=" + count +
                '}';
    }
}
