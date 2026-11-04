package tja.mui.sync;

import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import gregtech.api.util.function.TriConsumer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.IItemHandler;
import tja.TJA;

import java.io.IOException;

public class SlotDisplayWidgetSH extends ItemSlotSH {

    private TriConsumer<Integer, Integer, ItemStack> onPressed;

    public SlotDisplayWidgetSH(IItemHandler itemHandler, int index) {
        super(new ModularSlot(itemHandler, index)
                .canPut(false)
                .canTake(false)
                .canDragInto(false));
    }

    public SlotDisplayWidgetSH onPressedConsumer(TriConsumer<Integer, Integer, ItemStack> onPressed) {
        this.onPressed = onPressed;
        return this;
    }


    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {
        super.readOnServer(id, buf);
        if (id == 3) {
            try {
                final int button = buf.readInt();
                final int index = buf.readInt();
                final ItemStack stack = buf.readItemStack();
                if (this.onPressed != null)
                    this.onPressed.accept(button, index, stack);
            } catch (IOException e) {
                TJA.LOGGER.info(e.getMessage());
            }
        }
    }
}
