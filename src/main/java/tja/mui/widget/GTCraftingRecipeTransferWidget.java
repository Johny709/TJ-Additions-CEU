package tja.mui.widget;

import com.cleanroommc.modularui.api.value.ISyncOrValue;
import com.cleanroommc.modularui.widget.Widget;
import tja.mui.sync.GTCraftingRecipeTransferSH;

import javax.annotation.Nonnull;

public class GTCraftingRecipeTransferWidget extends Widget<GTCraftingRecipeTransferWidget> {

    @Override
    public boolean isValidSyncOrValue(@Nonnull ISyncOrValue syncOrValue) {
        return syncOrValue instanceof GTCraftingRecipeTransferSH;
    }
}
