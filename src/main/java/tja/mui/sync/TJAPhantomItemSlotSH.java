package tja.mui.sync;

import com.cleanroommc.modularui.value.sync.SyncHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import tja.TJA;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Predicate;


public class TJAPhantomItemSlotSH extends SyncHandler {

    private final Consumer<ItemStack> onExtracted;
    private final Consumer<ItemStack> onItemUpdate;
    private final IItemHandler itemHandler;
    private Predicate<ItemStack> putItemsPredicate;
    private Predicate<ItemStack> takeItemsPredicate;
    private boolean specialExtractingMode;
    private final int slotIndex;

    @Nonnull
    private ItemStack itemStack = ItemStack.EMPTY;

    public TJAPhantomItemSlotSH(IItemHandler itemHandler, int slotIndex) {
        this(itemHandler, slotIndex, null, null);
    }

    public TJAPhantomItemSlotSH(IItemHandler itemHandler, int slotIndex, Consumer<ItemStack> onItemUpdate) {
        this(itemHandler, slotIndex, onItemUpdate, null);
    }

    public TJAPhantomItemSlotSH(IItemHandler itemHandler, int slotIndex, Consumer<ItemStack> onItemUpdate, Consumer<ItemStack> onExtracted) {
        this.slotIndex = slotIndex;
        this.itemHandler = itemHandler;
        this.onItemUpdate = onItemUpdate;
        this.onExtracted = onExtracted;
    }

    /**
     * Extracts {@link Integer#MIN_VALUE} of item from item tank handler. Use this for special item tank handler behaviours.
     */
    public TJAPhantomItemSlotSH setSpecialExtractingMode(boolean specialExtractingMode) {
        this.specialExtractingMode = specialExtractingMode;
        return this;
    }

    /**
     * Set condition for item to be placed into slot either by dragging from JEI or placing item held by mouse into slot.
     * Will always be true if supplier not set or null.
     */
    public TJAPhantomItemSlotSH setPutItemsPredicate(Predicate<ItemStack> putItemsPredicate) {
        this.putItemsPredicate = putItemsPredicate;
        return this;
    }

    /**
     * Set condition for item to be removed from slot. Will always be true if supplier not set or null.
     */
    public TJAPhantomItemSlotSH setTakeItemsPredicate(Predicate<ItemStack> takeItemsPredicate) {
        this.takeItemsPredicate = takeItemsPredicate;
        return this;
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) {
        if (id == 1) {
            try {
                this.itemStack = buf.readItemStack();
            } catch (IOException e) {
                TJA.LOGGER.info(e.getMessage());
            }
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) {
        if (id == 1) {
            try {
                final ItemStack itemStack = buf.readItemStack();
                if (this.putItemsPredicate == null || this.putItemsPredicate.test(itemStack)) {
                    this.itemStack = itemStack;
                    if (!(this.itemHandler instanceof IItemHandlerModifiable)) {
                        this.itemHandler.extractItem(this.slotIndex, this.specialExtractingMode ? Integer.MIN_VALUE : Integer.MAX_VALUE, false);
                        this.itemHandler.insertItem(this.slotIndex, this.itemStack, false);
                    } else ((IItemHandlerModifiable) this.itemHandler).setStackInSlot(this.slotIndex, this.itemStack);
                    if (this.onItemUpdate != null)
                        this.onItemUpdate.accept(this.itemStack);
                } else this.syncToClient(1, buffer -> buffer.writeItemStack(this.itemStack));
            } catch (IOException e) {
                TJA.LOGGER.info(e.getMessage());
            }
        } else if (id == 2) {
            final ItemStack extracted = this.itemHandler.extractItem(this.slotIndex, this.specialExtractingMode ?
                    Integer.MIN_VALUE : Integer.MAX_VALUE, true);
            if (this.takeItemsPredicate == null || this.takeItemsPredicate.test(extracted)) {
                this.itemStack = ItemStack.EMPTY;
                final ItemStack itemStack = this.itemHandler.extractItem(this.slotIndex, this.specialExtractingMode ?
                        Integer.MIN_VALUE : Integer.MAX_VALUE, false);
                if (this.onItemUpdate != null)
                    this.onItemUpdate.accept(this.itemStack);
                if (this.onExtracted != null)
                    this.onExtracted.accept(itemStack);
            } else this.syncToClient(1, buffer -> buffer.writeItemStack(extracted));
        }
    }

    @Override
    public void detectAndSendChanges(boolean init) {
        final ItemStack stack = this.itemHandler.getStackInSlot(this.slotIndex);
        if (!stack.isEmpty()) {
            this.itemStack = stack;
            this.syncToClient(1, buffer -> buffer.writeItemStack(this.itemStack));
        }
    }

    public void setItemStack(@Nonnull ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Nonnull
    public ItemStack getItemStack() {
        return this.itemStack;
    }
}
