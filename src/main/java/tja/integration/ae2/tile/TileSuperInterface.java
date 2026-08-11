package tja.integration.ae2.tile;

import appeng.api.config.*;
import appeng.helpers.DualityInterface;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.tile.misc.TileInterface;
import com.circulation.random_complement.client.RCSettings;
import com.circulation.random_complement.client.buttonsetting.IntelligentBlocking;
import com.circulation.random_complement.common.interfaces.RCIConfigurableObject;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tja.TJA;
import tja.blocks.TJABlocks;
import tja.integration.ae2.ISuperInterface;
import tja.integration.ae2.blocks.BlockSuperInterface;
import tja.integration.ae2.helpers.DualitySuperInterface;


public class TileSuperInterface extends TileInterface implements IGuiHolder<PosGuiData>, ISuperInterface {

    public TileSuperInterface() {
        ObfuscationReflectionHelper.setPrivateValue(TileInterface.class, this, new DualitySuperInterface(this.getProxy(), this, 10, 18, 72), "duality");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ModularScreen createScreen(PosGuiData data, ModularPanel mainPanel) {
        return new ModularScreen(TJA.MOD_ID, mainPanel);
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return BlockSuperInterface.createInterfaceGUI(data, syncManager, settings, this);
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return TJABlocks.SUPER_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY);
    }

    @Override
    public void setBlockingMode(boolean blockingMode) {
        this.getInterfaceDuality().getConfigManager().putSetting(Settings.BLOCK, blockingMode ? YesNo.YES : YesNo.NO);
        this.markDirty();
    }

    @Override
    public void setLockCrafting(LockCraftingMode lockCrafting) {
        this.getInterfaceDuality().getConfigManager().putSetting(Settings.UNLOCK, lockCrafting);
        this.markDirty();
    }

    @Override
    public void setInterfaceTerminal(boolean interfaceTerminal) {
        this.getInterfaceDuality().getConfigManager().putSetting(Settings.INTERFACE_TERMINAL, interfaceTerminal ? YesNo.NO : YesNo.YES);
        this.markDirty();
    }

    @Override
    public void setFluidPacket(boolean fluidPacket) {
        this.getInterfaceDuality().getConfigManager().putSetting(Settings.OPERATION_MODE, fluidPacket ? OperationMode.FILL : OperationMode.EMPTY);
        ObfuscationReflectionHelper.setPrivateValue(DualityInterface.class, this.getInterfaceDuality(), fluidPacket, "fluidPacket");
        this.markDirty();
    }

    @Override
    public void setSplittingItemsFluids(boolean splittingItemsFluids) {
        this.getInterfaceDuality().getConfigManager().putSetting(Settings.LEVEL_TYPE, splittingItemsFluids ? LevelType.ITEM_LEVEL : LevelType.ENERGY_LEVEL);
        ObfuscationReflectionHelper.setPrivateValue(DualityInterface.class, this.getInterfaceDuality(), splittingItemsFluids, "allowSplitting");
        this.markDirty();
    }

    @Override
    public void setBlockModeEx(CondenserOutput blockModeEx) {
        this.getInterfaceDuality().getConfigManager().putSetting(Settings.CONDENSER_OUTPUT, blockModeEx);
        ObfuscationReflectionHelper.setPrivateValue(DualityInterface.class, this.getInterfaceDuality(), blockModeEx.ordinal(), "blockModeEx");
        this.markDirty();
    }

    @Override
    public void setIntelligentBlocking(boolean intelligentBlocking) {
        ((RCIConfigurableObject) this.getInterfaceDuality()).r$getConfigManager().putSetting(RCSettings.IntelligentBlocking, intelligentBlocking ? IntelligentBlocking.OPEN : IntelligentBlocking.CLOSE);
        this.markDirty();
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
    public void setPriority(String priority) {
        this.getInterfaceDuality().setPriority((int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, Long.parseLong(priority))));
        this.markDirty();
    }

    @Override
    public void setAutoPull(boolean autoPull) {
        // No such feature
    }
}
