package tja.mui.widget;

import com.cleanroommc.modularui.ModularUI;
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
import gregtech.api.util.FluidTooltipUtil;
import gregtech.api.util.TextFormattingUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.client.config.GuiUtils;
import tja.TJAValues;
import tja.mui.sync.TJAPhantomFluidSlotSH;
import tja.rendering.TJAGuiUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TJAPhantomFluidSlotWidget extends Widget<TJAPhantomFluidSlotWidget> implements RecipeViewerGhostIngredientSlot<FluidStack>,
        RecipeViewerIngredientProvider,
        Interactable {

    public TJAPhantomFluidSlotWidget() {
        super();
        this.size(18);
        this.background(GuiTextures.SLOT_FLUID);
    }

    public TJAPhantomFluidSlotWidget syncHandler(TJAPhantomFluidSlotSH phantomFluidSlotSH) {
        this.setSyncOrValue(ISyncOrValue.orEmpty(phantomFluidSlotSH));
        return this;
    }

    @Override
    public void onInit() {
        super.onInit();
        this.getContext().getRecipeViewerSettings().addGhostIngredientSlot(this);
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
        if (!this.isBelowMouse()) return;
        final FluidStack fluidStack = this.getSyncHandler().getFluidStack();
        final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        final int screenWidth = Minecraft.getMinecraft().displayWidth;
        final int screenHeight = Minecraft.getMinecraft().displayHeight;
        if (fluidStack != null) {
            final List<String> tooltips = new ArrayList<>(this.getFluidTooltip(fluidStack));
            if (tooltips.isEmpty())
                tooltips.add(fluidStack.getLocalizedName());
            GuiUtils.drawHoveringText(tooltips, context.getAbsMouseX(), context.getAbsMouseY(),
                    screenWidth, screenHeight, 100, fontRenderer);
        }
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        final Area area = this.getArea();
        final FluidStack fluidStack = this.getSyncHandler().getFluidStack();
        if (fluidStack != null) {
            final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            TJAGuiUtils.drawFluidForGui(fluidStack, fluidStack.amount, fluidStack.amount, 1, 1, area.width - 1, area.height - 2);
            GlStateManager.pushMatrix();
            GlStateManager.disableBlend();
            GlStateManager.scale(0.5, 0.5, 1);
            final String s = TextFormattingUtil.formatLongToCompactString(fluidStack.amount, 4) + "L";
            fontRenderer.drawStringWithShadow(s, 6 * 2 - fontRenderer.getStringWidth(s) + 21, 12 * 2, 0xFFFFFF);
            GlStateManager.popMatrix();
            GlStateManager.enableBlend();
            GlStateManager.color(1.0f, 1.0f, 1.0f);
        }
        if (this.isBelowMouse())
            TJAGuiUtils.drawSelectionOverlay(1, 1, area.width - 1, area.height - 1);
    }

    @Nonnull
    @Override
    public Result onMousePressed(int mouseButton) {
        if (this.isBelowMouse()) {
            if (mouseButton == 0) { // Left-Click
                final FluidStack fluidStack = FluidUtil.getFluidContained(this.getScreen().getContainer().getPlayer().inventory.getItemStack());
                if (fluidStack != null) {
                    this.getSyncHandler().setFluidStack(fluidStack.copy());
                    this.getSyncHandler().syncToServer(1, buffer -> buffer.writeCompoundTag(fluidStack.writeToNBT(new NBTTagCompound())));
                    return Result.SUCCESS;
                }
            } else if (mouseButton == 1) { // Right-Click
                this.getSyncHandler().syncToServer(2, buffer -> {});
                this.getSyncHandler().setFluidStack(null);
                return Result.SUCCESS;
            }
        }
        return Result.IGNORE;
    }

    @Override
    public boolean isValidSyncOrValue(@Nonnull ISyncOrValue syncOrValue) {
        return syncOrValue instanceof TJAPhantomFluidSlotSH;
    }

    @Override
    public void setGhostIngredient(@Nonnull FluidStack ingredient) {
        this.getSyncHandler().syncToServer(1, buffer -> buffer.writeCompoundTag(ingredient.writeToNBT(new NBTTagCompound())));
        this.getSyncHandler().setFluidStack(ingredient.copy());
    }

    @Override
    public FluidStack castGhostIngredientIfValid(@Nonnull Object ingredient) {
        return ingredient instanceof FluidStack ? (FluidStack) ingredient : null;
    }

    @Nullable
    @Override
    public Object getIngredient() {
        return this.getSyncHandler().getFluidStack();
    }

    @Override
    public @Nonnull TJAPhantomFluidSlotSH getSyncHandler() {
        return (TJAPhantomFluidSlotSH) super.getSyncHandler();
    }

    private List<String> getFluidTooltip(FluidStack fluidStack) {
        return TJAValues.isModLoaded(TJAValues.GREGTECH_MOD_ID) ? FluidTooltipUtil.getFluidTooltip(fluidStack) : Collections.emptyList();
    }
}
