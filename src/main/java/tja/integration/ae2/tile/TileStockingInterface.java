package tja.integration.ae2.tile;

import appeng.api.AEApi;
import appeng.api.config.*;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.core.settings.TickRates;
import appeng.me.GridAccessException;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.tile.misc.TileInterface;
import appeng.util.item.AEItemStack;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import tja.TJA;
import tja.blocks.TJAAE2Blocks;
import tja.integration.ae2.ISuperInterface;
import tja.integration.ae2.blocks.BlockStockingInterface;
import tja.integration.ae2.helpers.DualitySuperInterface;
import tja.util.TJAItemUtils;

import javax.annotation.Nonnull;


public class TileStockingInterface extends TileInterface implements IGuiHolder<PosGuiData>, ISuperInterface {

    private final BlockPos.MutableBlockPos interfacePos = new BlockPos.MutableBlockPos();
    private int tickTime = 100;

    public TileStockingInterface() {
        ObfuscationReflectionHelper.setPrivateValue(TileInterface.class, this, new DualitySuperInterface(this.getProxy(), this, 10, 36, 9), "duality");
        this.getInterfaceDuality().getConfigManager().registerSetting(Settings.STICKY_MODE, YesNo.NO);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ModularScreen createScreen(PosGuiData data, ModularPanel mainPanel) {
        return new ModularScreen(TJA.MOD_ID, mainPanel);
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return BlockStockingInterface.createInterfaceGUI(data, syncManager, settings, this);
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("tickTime", this.tickTime);
        data.setInteger("autoOutputItem", this.getInterfaceDuality().getConfigManager().getSetting(Settings.STICKY_MODE).ordinal());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.tickTime = Math.max(1, data.getInteger("tickTime"));
        this.getInterfaceDuality().getConfigManager().putSetting(Settings.STICKY_MODE, YesNo.values()[data.getInteger("autoOutputItem")]);
    }

    @Nonnull
    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (!this.getProxy().isActive())
            return TickRateModulation.SLEEP;
        final TickRateModulation tickRateModulation = super.tickingRequest(node, ticksSinceLastCall);
        if (this.getInterfaceDuality().getConfigManager().getSetting(Settings.BLOCK) == YesNo.YES) {
            try {
                int index = 0;
                final int stackSize = (int) Math.min(Integer.MAX_VALUE, 1024L << this.getInstalledUpgrades(Upgrades.CAPACITY) * 2);
                final IItemList<?> iItemList = this.getProxy().getStorage().getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class)).getStorageList();
                for (IAEStack<?> items : iItemList) {
                    if (index < this.getInterfaceDuality().getConfig().getSlots()) {
                        if (!items.isItem()) continue;
                        final AEItemStack aeItemStack = (AEItemStack) items;
                        final ItemStack itemStack = aeItemStack.createItemStack();
                        if (itemStack.isEmpty()) continue;
                        itemStack.setCount(Math.min(itemStack.getCount(), stackSize));
                        ((AppEngInternalAEInventory) this.getInterfaceDuality().getConfig()).setStackInSlot(index++, itemStack);
                    } else break;
                }
            } catch (GridAccessException ignored) {}
        }
        if (this.getInterfaceDuality().getConfigManager().getSetting(Settings.STICKY_MODE) == YesNo.YES) {
            final BlockPos pos = this.getTile().getPos();
            for (EnumFacing facing : this.getTargets()) {
                this.interfacePos.setPos(pos.getX(), pos.getY(), pos.getZ());
                final TileEntity tileEntity = this.getTile().getWorld().getTileEntity(this.interfacePos.move(facing));
                if (tileEntity != null) {
                    final IItemHandler itemHandler = this.getInterfaceDuality().getStorage();
                    final IItemHandler destItemHandler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite());
                    if (destItemHandler != null) {
                        for (int i = 0; i < itemHandler.getSlots(); i++) {
                            final ItemStack stack = itemHandler.getStackInSlot(i);
                            if (!stack.isEmpty()) {
                                final int inserted = TJAItemUtils.insertIntoItemHandler(destItemHandler, stack, true).getCount();
                                final int extract = stack.getCount() - inserted;
                                if (extract < 1) continue;
                                final ItemStack otherStack = itemHandler.extractItem(i, extract, false);
                                TJAItemUtils.insertIntoItemHandler(destItemHandler, otherStack, false);
                            }
                        }
                    }
                }
            }
        }
        return TickRateModulation.values()[Math.max(tickRateModulation.ordinal(), this.tickTime > ticksSinceLastCall ? TickRateModulation.SLOWER.ordinal() : this.tickTime < ticksSinceLastCall ? TickRateModulation.FASTER.ordinal() : TickRateModulation.SAME.ordinal())];
    }

    @Nonnull
    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(TickRates.Interface.getMin(), TickRates.Interface.getMax(), super.getTickingRequest(node).isSleeping && this.getInterfaceDuality().getConfigManager().getSetting(Settings.BLOCK) == YesNo.NO, false);
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return TJAAE2Blocks.STOCKING_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY);
    }

    @Override
    public void setItemAutoPull(boolean blockingMode) {
        this.getInterfaceDuality().getConfigManager().putSetting(Settings.BLOCK, blockingMode ? YesNo.YES : YesNo.NO);
        this.markDirty();
    }

    @Override
    public void setItemAutoPush(boolean autoPush) {
        this.getInterfaceDuality().getConfigManager().putSetting(Settings.STICKY_MODE, autoPush ? YesNo.YES : YesNo.NO);
        this.getTile().markDirty();
    }

    @Override
    public void setBlockingMode(boolean blockingMode) {
        // No pattern slots
    }

    @Override
    public void setLockCrafting(LockCraftingMode lockCraftingMode) {
        // No pattern slots
    }

    @Override
    public void setInterfaceTerminal(boolean interfaceTerminal) {
        // No pattern slots
    }

    @Override
    public void setFluidPacket(boolean fluidPacket) {
        // No pattern slots
    }

    @Override
    public void setSplittingItemsFluids(boolean splittingItemsFluids) {
        // No pattern slots
    }

    @Override
    public void setBlockModeEx(CondenserOutput blockModeEx) {
        // No pattern slots
    }

    @Override
    public void setIntelligentBlocking(boolean intelligentBlocking) {
        // No pattern slots
    }

    @Override
    public void setStackSize(String text, String id) {
        final int slot = Integer.parseInt(id);
        final int maxSize = this.getInterfaceDuality().getConfig().getSlotLimit(0);
        final int stackSize = (int) Math.max(1, Math.min(Long.parseLong(text), maxSize));
        final ItemStack itemStack = this.getInterfaceDuality().getConfig().extractItem(slot, Integer.MAX_VALUE, false);
        if (itemStack.isEmpty()) return;
        itemStack.setCount(stackSize);
        ((AppEngInternalAEInventory) this.getInterfaceDuality().getConfig()).setStackInSlot(slot, itemStack);
        this.markDirty();
    }

    @Override
    public String getStackSize(int index) {
        return String.valueOf(this.getInterfaceDuality().getConfig().getStackInSlot(index).getCount());
    }

    @Override
    public void setTickTime(String tickTime) {
        this.tickTime = (int) Math.max(1, Math.min(Integer.MAX_VALUE, Long.parseLong(tickTime)));
        this.markDirty();
    }

    @Override
    public void setPriority(String priority) {
        this.getInterfaceDuality().setPriority((int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, Long.parseLong(priority))));
        this.markDirty();
    }
}
