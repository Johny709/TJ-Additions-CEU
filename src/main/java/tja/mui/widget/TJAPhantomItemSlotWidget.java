package tja.mui.widget;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.MCHelper;
import com.cleanroommc.modularui.api.value.ISyncOrValue;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.integration.jei.ModularUIJeiPlugin;
import com.cleanroommc.modularui.integration.recipeviewer.RecipeViewerGhostIngredientSlot;
import com.cleanroommc.modularui.integration.recipeviewer.RecipeViewerIngredientProvider;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.sizer.Area;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.config.GuiUtils;
import tja.mui.sync.TJAPhantomItemSlotSH;
import tja.rendering.TJAGuiUtils;

import javax.annotation.Nonnull;
import java.awt.*;

public class TJAPhantomItemSlotWidget extends Widget<TJAPhantomItemSlotWidget> implements RecipeViewerGhostIngredientSlot<ItemStack>,
        RecipeViewerIngredientProvider,
        Interactable {

    public TJAPhantomItemSlotWidget() {
        super();
        this.size(18);
        this.background(GuiTextures.SLOT_ITEM);
    }

    public TJAPhantomItemSlotWidget syncHandler(TJAPhantomItemSlotSH phantomItemSlotSH) {
        this.setSyncOrValue(ISyncOrValue.orEmpty(phantomItemSlotSH));
        return this;
    }

    @Override
    public void onInit() {
        super.onInit();
        this.getScreen().getContext().getRecipeViewerSettings().addGhostIngredientSlot(this);
    }

    @Override
    public void drawOverlay(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        super.drawOverlay(context, widgetTheme);
        if (ModularUI.Mods.JEI.isLoaded() && (ModularUIJeiPlugin.draggingValidIngredient(this) ||
                ModularUIJeiPlugin.hoveringOverIngredient(this))) {
            this.drawHighlight(this.getArea(), this.isHovering());
        }
    }

    @Override
    public void drawForeground(ModularGuiContext context) {
        super.drawForeground(context);
        final ItemStack itemStack = this.getSyncHandler().getItemStack();
        if (itemStack.isEmpty() || !this.isBelowMouse()) return;
        final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        final int screenWidth = Minecraft.getMinecraft().displayWidth;
        final int screenHeight = Minecraft.getMinecraft().displayHeight;
        GuiUtils.drawHoveringText(MCHelper.getItemToolTip(itemStack), context.getAbsMouseX() + 1, context.getAbsMouseY() + 1,
                screenWidth, screenHeight, 100, fontRenderer);
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        super.draw(context, widgetTheme);
        final Area area = this.getArea();
        final ItemStack itemStack = this.getSyncHandler().getItemStack();
        if (!itemStack.isEmpty())
            TJAGuiUtils.drawItemStack(1, 1, itemStack, itemStack.getCount());
        if (this.isBelowMouse())
            TJAGuiUtils.drawSelectionOverlay(1, 1, area.width - 1, area.height - 1);
    }

    @Nonnull
    @Override
    public Result onMousePressed(int mouseButton) {
        if (this.isBelowMouse()) {
            if (mouseButton == 0) { // Left-Click
                final ItemStack stack = this.getScreen().getContainer().getPlayer().inventory.getItemStack();
                if (!stack.isEmpty()) {
                    this.getSyncHandler().syncToServer(1, buffer -> buffer.writeItemStack(stack));
                    return Result.SUCCESS;
                }
            } else if (mouseButton == 1) { // Right-Click
                this.getSyncHandler().syncToServer(2, buffer -> {});
                this.getSyncHandler().setItemStack(ItemStack.EMPTY);
                return Result.SUCCESS;
            }
        }
        return Result.IGNORE;
    }

    @Override
    public boolean isValidSyncOrValue(@Nonnull ISyncOrValue syncOrValue) {
        return syncOrValue instanceof TJAPhantomItemSlotSH;
    }

    @Override
    public void setGhostIngredient(@Nonnull ItemStack ingredient) {
        this.getSyncHandler().syncToServer(1, buffer -> buffer.writeItemStack(ingredient.copy()));
    }

    @Override
    public ItemStack castGhostIngredientIfValid(@Nonnull Object ingredient) {
        return ingredient instanceof ItemStack ? (ItemStack) ingredient : null;
    }

    @Override
    public @Nonnull TJAPhantomItemSlotSH getSyncHandler() {
        return (TJAPhantomItemSlotSH) super.getSyncHandler();
    }

    @Nonnull
    @Override
    public Object getIngredient() {
        return this.getSyncHandler().getItemStack();
    }
}
