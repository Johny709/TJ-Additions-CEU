package tja.integration.ae2.tile;

import appeng.api.config.*;
import appeng.helpers.DualityInterface;
import appeng.tile.misc.TileInterface;
import com.circulation.random_complement.client.RCSettings;
import com.circulation.random_complement.client.buttonsetting.IntelligentBlocking;
import com.circulation.random_complement.common.interfaces.RCIConfigurableObject;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import tja.blocks.TJABlocks;
import tja.integration.ae2.ISuperInterface;
import tja.integration.ae2.blocks.BlockPatternInterface;
import tja.integration.ae2.helpers.DualitySuperInterface;


public class TilePatternInterface extends TileInterface implements IGuiHolder<PosGuiData>, ISuperInterface {

    public TilePatternInterface() {
        ObfuscationReflectionHelper.setPrivateValue(TileInterface.class, this, new DualitySuperInterface(this.getProxy(), this, 40, 36, 288), "duality");
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return BlockPatternInterface.createInterfaceGUI(data, syncManager, settings, this);
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return TJABlocks.PATTERN_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY);
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
        this.getInterfaceDuality().getConfigManager().putSetting(Settings.INTERFACE_TERMINAL, interfaceTerminal ? YesNo.YES : YesNo.NO);
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
    public void setStackSize(String size, String id) {
        // No config slots
    }

    @Override
    public String getStackSize(int index) {
        return null; // No config slots
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
