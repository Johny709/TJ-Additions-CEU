package tja.mui.widget;

import com.cleanroommc.modularui.api.value.ISyncOrValue;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.Stencil;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.sizer.Area;
import mezz.jei.Internal;
import mezz.jei.runtime.JeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.client.config.GuiUtils;
import tja.TJAValues;
import tja.mui.sync.TJALabelSH;
import tja.rendering.TJAGuiUtils;

import javax.annotation.Nonnull;
import java.util.Collections;

public class TJALabelWidget extends Widget<TJALabelWidget> implements Interactable {

    private int color = 0x404040;
    private int offsetX;
    private int tickCounter;
    private int hoverTicks;
    private boolean slideAtEnd;
    private boolean canSlide = true;
    private boolean centered = true;
    private ItemStack itemLabel;
    private FluidStack fluidLabel;

    public TJALabelWidget setItemLabel(ItemStack itemLabel) {
        this.itemLabel = itemLabel;
        return this;
    }

    public TJALabelWidget setFluidLabel(FluidStack fluidLabel) {
        this.fluidLabel = fluidLabel;
        return this;
    }

    public TJALabelWidget setColor(int color) {
        this.color = color;
        return this;
    }

    /**
     * set for text to start sliding to the left gradually when hovered over. Default: True
     */
    public TJALabelWidget setCanSlide(boolean canSlide) {
        this.canSlide = canSlide;
        return this;
    }

    /**
     * set to display text on center of this label. Default: True
     */
    public TJALabelWidget setCentered(boolean centered) {
        this.centered = centered;
        return this;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        this.tickCounter++;
    }

    @Override
    public void drawForeground(ModularGuiContext context) {
        super.drawForeground(context);
        final Area area = this.getArea();
        if (this.isBelowMouse()) {
            final String uid = this.getSyncHandler().getRecipeUid();
            if (this.slideAtEnd) {
                this.slideAtEnd = false;
                this.offsetX = -(area.width - 24);
            } else if (this.canSlide && (this.tickCounter % 2 == 0 || this.tickCounter % 3 == 0))
                this.offsetX++;
            if (++this.hoverTicks > 20 && uid != null) {
                final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
                final String format = I18n.format("jei.tooltip.show.recipes");
                GuiUtils.drawHoveringText(Collections.singletonList(format), context.getAbsMouseX(), context.getAbsMouseY(),
                        Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight, 256, fontRenderer);
            }
        } else {
            this.hoverTicks = 0;
            this.offsetX = 0;
            final String locale = this.getSyncHandler().getLocale();
            if (this.centered && locale != null) {
                final String locale1 = I18n.format(locale);
                final int length = Minecraft.getMinecraft().fontRenderer.getStringWidth(locale1);
                int widthApplied = 3;
                if (this.itemLabel != null)
                    widthApplied += 16;
                if (this.fluidLabel != null)
                    widthApplied += 18;
                final int boxLength = area.width - widthApplied;
                if (length < boxLength) {
                    this.offsetX = (length - boxLength) / 2;
                }
            }
        }
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        super.draw(context, widgetTheme);
        int widthApplied = 3;
        final Area area = this.getArea();
        final String locale = this.getSyncHandler().getLocale();
        final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        if (this.itemLabel != null) {
            TJAGuiUtils.drawItemStack(3, 2, this.itemLabel, 1);
            widthApplied += 16;
        }
        if (this.fluidLabel != null) {
            TJAGuiUtils.drawFluidForGui(this.fluidLabel, this.fluidLabel.amount, this.fluidLabel.amount, 0, 0, 18, 18);
            widthApplied += 18;
        }
        if (locale != null) {
            final int finalX = widthApplied - this.offsetX;
            final String locale1 = I18n.format(locale);
            int length = fontRenderer.getStringWidth(locale1);
            length -= this.offsetX;
            if (length < 0)
                this.slideAtEnd = true;
            Stencil.apply(() -> fontRenderer.drawString(locale, finalX, 6, this.color), widthApplied, 0, area.width - 22, area.height, context);
        }
    }

    @Nonnull
    @Override
    public Result onMousePressed(int mouseButton) {
        if (this.isBelowMouse() && TJAValues.isModLoaded(TJAValues.JEI_MOD_ID)) {
            final String uuid = this.getSyncHandler().getRecipeUid();
            final JeiRuntime jeiRuntime = Internal.getRuntime();
            if (jeiRuntime != null && uuid != null && !uuid.isEmpty()) {
                jeiRuntime.getRecipesGui().showCategories(Collections.singletonList(uuid));
            }
        }
        return Result.IGNORE;
    }

    @Override
    public boolean isValidSyncOrValue(@Nonnull ISyncOrValue syncOrValue) {
        return syncOrValue instanceof TJALabelSH;
    }

    @Override
    public @Nonnull TJALabelSH getSyncHandler() {
        return (TJALabelSH) super.getSyncHandler();
    }
}
