package tja.mui.slot;

import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import tja.integration.ae2.helpers.DualitySuperInterface;

import javax.annotation.Nonnull;

public class TJAModularSlot extends ModularSlot {
    /**
     * Creates a ModularSlot
     *
     * @param itemHandler item handler of the slot
     * @param index       slot index in the item handler
     */
    public TJAModularSlot(IItemHandler itemHandler, int index) {
        super(itemHandler, index);
    }

    @Override
    public void putStack(@Nonnull ItemStack stack) {
        super.putStack(stack);
        if (this.getItemHandler() instanceof DualitySuperInterface.DualityUpgradeInventory) {
            DualitySuperInterface.DualityUpgradeInventory upgradeInventory = ((DualitySuperInterface.DualityUpgradeInventory) this.getItemHandler());
            if (this.slotNumber < upgradeInventory.getSlots())
                upgradeInventory.updateContentsAt(this.slotNumber);
        }
    }
}
