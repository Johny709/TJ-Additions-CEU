package tja.mui.widget;

import com.cleanroommc.modularui.api.value.ISyncOrValue;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widgets.layout.Grid;
import tja.mui.TJAGuiUtils;
import tja.mui.sync.SelectionWidgetGroupSH;

import javax.annotation.Nonnull;
import java.util.List;

public class SelectionWidgetGroup extends Grid {

    public SelectionWidgetGroup syncHandler(SelectionWidgetGroupSH selectionWidgetGroupSH) {
        this.setSyncOrValue(ISyncOrValue.orEmpty(selectionWidgetGroupSH));
        return this;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        super.draw(context, widgetTheme);
        final int selectedIndex = this.getSyncHandler().getSelectedIndex();
        final List<IWidget> widgetList = this.getChildren();
        if (selectedIndex < 0 || selectedIndex >= widgetList.size()) return;
        final Area parentArea = this.getArea();
        final Area area = widgetList.get(selectedIndex).getArea();
        TJAGuiUtils.drawRectangle(area.x - parentArea.x, area.y - parentArea.y, area.width, area.height, 2, 0xEBB434);
    }

    @Nonnull
    @Override
    public Result onMousePressed(int mouseButton) {
        boolean success = false;
        for (int i = 0; i < this.getChildren().size(); i++) {
            final IWidget widget = this.getChildren().get(i);
            if (widget.isBelowMouse()) {
                final int finalI = i;
                this.getSyncHandler().syncToServer(1, buffer -> buffer.writeInt(finalI));
                success = true;
                break;
            }
        }
        if (!success)
            this.getSyncHandler().syncToServer(1, buffer -> buffer.writeInt(-1));
        return super.onMousePressed(mouseButton);
    }

    @Override
    public boolean isValidSyncOrValue(@Nonnull ISyncOrValue syncOrValue) {
        return syncOrValue instanceof SelectionWidgetGroupSH;
    }

    @Override
    public @Nonnull SelectionWidgetGroupSH getSyncHandler() {
        return (SelectionWidgetGroupSH) super.getSyncHandler();
    }
}
