package tja.mui.slot;

import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import tja.TJA;
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
            final DualitySuperInterface.DualityUpgradeInventory upgradeInventory = ((DualitySuperInterface.DualityUpgradeInventory) this.getItemHandler());
            try {
                upgradeInventory.updateContentsAt(this.slotNumber);
            } catch (Exception e) {
                TJA.LOGGER.info("failed to detect slot change at: {}, {}", this.slotNumber, e.getMessage());
                TJA.LOGGER.info("force changes by updating all slot indexes");
                for (int i = 0; i < upgradeInventory.getSlots(); i++) {
                    upgradeInventory.updateContentsAt(i);
                }
            }
        }
    }
}
