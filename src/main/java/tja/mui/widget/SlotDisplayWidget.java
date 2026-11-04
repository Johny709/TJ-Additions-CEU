package tja.mui.widget;

import com.cleanroommc.modularui.widgets.slot.ItemSlot;

import javax.annotation.Nonnull;

public class SlotDisplayWidget extends ItemSlot {

    @Nonnull
    @Override
    public Result onMousePressed(int mouseButton) {
        if (!this.isLockedByBogosorter()) {
            this.getSyncHandler().syncToServer(3, buffer -> {
                buffer.writeInt(mouseButton);
                buffer.writeInt(this.getSyncHandler().getSlot().getSlotIndex());
                buffer.writeItemStack(this.getSyncHandler().getSlot().getStack());
            });
        }
        return Result.SUCCESS;
    }
}
