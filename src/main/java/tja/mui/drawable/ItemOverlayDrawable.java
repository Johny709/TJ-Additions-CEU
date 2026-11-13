package tja.mui.drawable;

import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tja.mui.TJAGuiUtils;

public class ItemOverlayDrawable extends ItemDrawable {

    public ItemOverlayDrawable(Item item) {
        super(item);
    }

    public ItemOverlayDrawable(ItemStack stack) {
        super(stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        this.applyColor(widgetTheme.getColor());
        TJAGuiUtils.drawItemStackOverlay(x + 1, y + 1, this.getItem(), 1);
    }
}
