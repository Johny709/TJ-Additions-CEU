package tja.mui.slot;

import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import tja.TJA;

import javax.annotation.Nonnull;

public class TJAModularSlot extends ModularSlot {

    private final int slotIndex;

    /**
     * Creates a ModularSlot
     *
     * @param itemHandler item handler of the slot
     * @param index       slot index in the item handler
     */
    public TJAModularSlot(IItemHandler itemHandler, int index) {
        super(itemHandler, index);
        this.slotIndex = index;
    }

    @Override
    public void putStack(@Nonnull ItemStack stack) {
        super.putStack(stack);
        final IItemHandler itemHandler = this.getItemHandler();
        if (itemHandler instanceof ISlotUpdate) {
            final ISlotUpdate handler = (ISlotUpdate) itemHandler;
            try {
                handler.updateContentsAt(this.slotIndex);
            } catch (Exception e) {
                TJA.LOGGER.info("failed to detect slot change at: {}, {}", this.slotIndex, e.getMessage());
                TJA.LOGGER.info("force changes by updating all slot indexes");
                for (int i = 0; i < itemHandler.getSlots(); i++) {
                    handler.updateContentsAt(i);
                }
            }
        }
    }
}
