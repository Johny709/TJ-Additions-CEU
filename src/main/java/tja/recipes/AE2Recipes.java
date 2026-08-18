package tja.recipes;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.NonNullList;
import net.minecraftforge.registries.IForgeRegistry;
import tja.blocks.TJAAE2Blocks;
import tja.items.TJAAE2Items;

import static tja.recipes.TJARecipes.resource;

public class AE2Recipes {

    public static void init(IForgeRegistry<IRecipe> recipes) {
        // ME super interface
        recipes.register(new ShapelessRecipes(resource("me.super_interface.block").toString(),
                TJAAE2Blocks.SUPER_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY),
                NonNullList.from(Ingredient.EMPTY, Ingredient.fromStacks(TJAAE2Items.PART_SUPER_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY))))
                .setRegistryName(resource("me.super_interface.block")));
        recipes.register(new ShapelessRecipes(resource("me.super_interface.part").toString(),
                TJAAE2Items.PART_SUPER_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY),
                NonNullList.from(Ingredient.EMPTY, Ingredient.fromStacks(TJAAE2Blocks.SUPER_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY))))
                .setRegistryName(resource("me.super_interface.part")));
        // ME super fluid interface
        recipes.register(new ShapelessRecipes(resource("me.super_fluid_interface.block").toString(),
                TJAAE2Blocks.SUPER_FLUID_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY),
                NonNullList.from(Ingredient.EMPTY, Ingredient.fromStacks(TJAAE2Items.PART_SUPER_FLUID_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY))))
                .setRegistryName(resource("me.super_fluid_interface.block")));
        recipes.register(new ShapelessRecipes(resource("me.super_fluid_interface.part").toString(),
                TJAAE2Items.PART_SUPER_FLUID_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY),
                NonNullList.from(Ingredient.EMPTY, Ingredient.fromStacks(TJAAE2Blocks.SUPER_FLUID_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY))))
                .setRegistryName(resource("me.super_fluid_interface.part")));
        // ME super dual interface
        recipes.register(new ShapelessRecipes(resource("me.super_dual_interface.block").toString(),
                TJAAE2Blocks.SUPER_DUAL_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY),
                NonNullList.from(Ingredient.EMPTY, Ingredient.fromStacks(TJAAE2Items.PART_SUPER_DUAL_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY))))
                .setRegistryName(resource("me.super_dual_interface.block")));
        recipes.register(new ShapelessRecipes(resource("me.super_dual_interface.part").toString(),
                TJAAE2Items.PART_SUPER_DUAL_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY),
                NonNullList.from(Ingredient.EMPTY, Ingredient.fromStacks(TJAAE2Blocks.SUPER_DUAL_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY))))
                .setRegistryName(resource("me.super_dual_interface.part")));
        recipes.register(new ShapelessRecipes(resource("me.super_dual_interface").toString(),
                TJAAE2Blocks.SUPER_DUAL_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY),
                NonNullList.from(Ingredient.EMPTY, Ingredient.fromStacks(TJAAE2Blocks.SUPER_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY),
                                TJAAE2Items.PART_SUPER_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY)),
                        Ingredient.fromStacks(TJAAE2Blocks.SUPER_FLUID_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY),
                                TJAAE2Items.PART_SUPER_FLUID_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY))))
                .setRegistryName(resource("me.super_dual_interface")));
        // ME stocking interface
        recipes.register(new ShapelessRecipes(resource("me.stocking_inteface.block").toString(),
                TJAAE2Blocks.STOCKING_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY),
                NonNullList.from(Ingredient.EMPTY, Ingredient.fromStacks(TJAAE2Items.PART_STOCKING_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY))))
                .setRegistryName(resource("me.stocking_interface.block")));
        recipes.register(new ShapelessRecipes(resource("me.stocking_interface.part").toString(),
                TJAAE2Items.PART_STOCKING_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY),
                NonNullList.from(Ingredient.EMPTY, Ingredient.fromStacks(TJAAE2Blocks.STOCKING_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY))))
                .setRegistryName(resource("me.stocking_interface.part")));
        // ME stocking fluid interface
        recipes.register(new ShapelessRecipes(resource("me.stocking_fluid_interface.block").toString(),
                TJAAE2Blocks.STOCKING_FLUID_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY),
                NonNullList.from(Ingredient.EMPTY, Ingredient.fromStacks(TJAAE2Items.PART_STOCKING_FLUID_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY))))
                .setRegistryName(resource("me.stocking_fluid_interface.block")));
        recipes.register(new ShapelessRecipes(resource("me.stocking_fluid_interface.part").toString(),
                TJAAE2Items.PART_STOCKING_FLUID_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY),
                NonNullList.from(Ingredient.EMPTY, Ingredient.fromStacks(TJAAE2Blocks.STOCKING_FLUID_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY))))
                .setRegistryName(resource("me.stocking_fluid_interface.part")));
        // ME stocking dual interface
        recipes.register(new ShapelessRecipes(resource("me.stocking_dual_interface.block").toString(),
                TJAAE2Blocks.STOCKING_DUAL_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY),
                NonNullList.from(Ingredient.EMPTY, Ingredient.fromStacks(TJAAE2Items.PART_STOCKING_DUAL_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY))))
                .setRegistryName(resource("me.stocking_dual_interface.block")));
        recipes.register(new ShapelessRecipes(resource("me.stocking_dual_interface.part").toString(),
                TJAAE2Items.PART_STOCKING_DUAL_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY),
                NonNullList.from(Ingredient.EMPTY, Ingredient.fromStacks(TJAAE2Blocks.STOCKING_DUAL_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY))))
                .setRegistryName(resource("me.stocking_dual_interface.part")));
        recipes.register(new ShapelessRecipes(resource("me.stocking_dual_inteface").toString(),
                TJAAE2Blocks.STOCKING_DUAL_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY),
                NonNullList.from(Ingredient.EMPTY, Ingredient.fromStacks(TJAAE2Blocks.STOCKING_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY),
                                TJAAE2Items.PART_STOCKING_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY)),
                        Ingredient.fromStacks(TJAAE2Blocks.STOCKING_FLUID_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY),
                                TJAAE2Items.PART_STOCKING_FLUID_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY))))
                .setRegistryName(resource("me.stocking_dual_interface")));
        // ME pattern interface
        recipes.register(new ShapelessRecipes(resource("me.pattern_interface.block").toString(),
                TJAAE2Blocks.PATTERN_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY),
                NonNullList.from(Ingredient.EMPTY, Ingredient.fromStacks(TJAAE2Items.PART_PATTERN_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY))))
                .setRegistryName(resource("me.pattern_interface.block")));
        recipes.register(new ShapelessRecipes(resource("me.pattern_interface.part").toString(),
                TJAAE2Items.PART_PATTERN_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY),
                NonNullList.from(Ingredient.EMPTY, Ingredient.fromStacks(TJAAE2Blocks.PATTERN_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY))))
                .setRegistryName(resource("me.pattern_interface.part")));
        // ME super ultimate interface
        recipes.register(new ShapelessRecipes(resource("me.super_ultimate_interface.block").toString(),
                TJAAE2Blocks.SUPER_ULTIMATE_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY),
                NonNullList.from(Ingredient.EMPTY, Ingredient.fromStacks(TJAAE2Items.PART_SUPER_ULTIMATE_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY))))
                .setRegistryName(resource("me.super_ultimate_interface.block")));
        recipes.register(new ShapelessRecipes(resource("me.super_ultimate_interface.part").toString(),
                TJAAE2Items.PART_SUPER_ULTIMATE_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY),
                NonNullList.from(Ingredient.EMPTY, Ingredient.fromStacks(TJAAE2Blocks.SUPER_ULTIMATE_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY))))
                .setRegistryName("me.super_ultimate_interface.part"));
    }
}
