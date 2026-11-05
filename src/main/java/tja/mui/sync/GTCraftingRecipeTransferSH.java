package tja.mui.sync;

import gregtech.api.mui.sync.RecipeSyncHandler;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tja.TJA;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;


public class GTCraftingRecipeTransferSH extends RecipeSyncHandler {

    private final BiConsumer<Integer, ItemStack> itemStackBiConsumer;

    public GTCraftingRecipeTransferSH(BiConsumer<Integer, ItemStack> itemStackBiConsumer) {
        this.itemStackBiConsumer = itemStackBiConsumer;
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) {

    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) {
        if (id == 1) {
            final Map<Integer, ItemStack> itemStackMap = new HashMap<>();
            for (int i = 0; i < 9; i++)
                itemStackMap.put(i, ItemStack.EMPTY);
            try {
                final int size = buf.readVarInt();
                for (int i = 0; i < size; i++)
                    itemStackMap.put(buf.readVarInt(), buf.readItemStack());
                for (Map.Entry<Integer, ItemStack> itemStackEntry : itemStackMap.entrySet())
                    this.itemStackBiConsumer.accept(itemStackEntry.getKey(), itemStackEntry.getValue());
            } catch (IOException e) {
                TJA.LOGGER.info(e);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IRecipeTransferError receiveRecipe(@Nonnull IRecipeLayout recipeLayout, boolean maxTransfer, boolean simulate) {
        if (simulate) {
            return null;
        }
        final Map<Integer, ? extends IGuiIngredient<ItemStack>> ingredients = recipeLayout.getItemStacks().getGuiIngredients();
        ingredients.values().removeIf(it -> !it.isInput());
        this.syncToServer(1, buf -> {
            buf.writeVarInt(ingredients.size());
            for (Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>> entry : ingredients.entrySet()) {
                buf.writeVarInt(entry.getKey() - 1);
                final ItemStack itemStack = entry.getValue().getDisplayedIngredient();
                buf.writeItemStack(itemStack != null ? itemStack : ItemStack.EMPTY);
            }
        });
        return null;
    }
}
