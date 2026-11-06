package tja.integration.ae2.tile;

import appeng.api.storage.data.IAEFluidStack;
import appeng.fluids.tile.TileFluidInterface;
import appeng.fluids.util.AEFluidStack;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tja.TJA;
import tja.blocks.TJAAE2Blocks;
import tja.integration.ae2.ISuperFluidInterface;
import tja.integration.ae2.blocks.BlockSuperFluidInterface;
import tja.integration.ae2.helpers.DualitySuperFluidInterface;


public class TileSuperFluidInterface extends TileFluidInterface implements IGuiHolder<PosGuiData>, ISuperFluidInterface {

    public TileSuperFluidInterface() {
        ObfuscationReflectionHelper.setPrivateValue(TileFluidInterface.class, this,
                new DualitySuperFluidInterface(this.getProxy(), this, 18), "duality");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ModularScreen createScreen(PosGuiData data, ModularPanel mainPanel) {
        return new ModularScreen(TJA.MOD_ID, mainPanel);
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return BlockSuperFluidInterface.createFluidInterfaceGUI(data, syncManager, settings, this);
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return TJAAE2Blocks.SUPER_FLUID_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY);
    }

    @Override
    public void setFluidCount(String text, int index) {
        if (index < 0 || index >= this.getDualityFluidInterface().getConfig().getSlots()) return;
        final IAEFluidStack aeFluidStack = this.getDualityFluidInterface().getConfig().getFluidInSlot(index);
        if (aeFluidStack == null) return;
        final FluidStack fluidStack = aeFluidStack.getFluidStack();
        fluidStack.amount = (int) Math.min(Integer.MAX_VALUE, Long.parseLong(text));
        this.getDualityFluidInterface().getConfig().setFluidInSlot(index, AEFluidStack.fromFluidStack(fluidStack));
        this.markDirty();
    }

    @Override
    public String getFluidCount(int index) {
        return String.valueOf(index < 0 || index >= this.getDualityFluidInterface().getConfig().getSlots() ? 0 :
                this.getDualityFluidInterface().getConfig().getFluidInSlot(index) == null ? 0 :
                        this.getDualityFluidInterface().getConfig().getFluidInSlot(index).getStackSize());
    }

    @Override
    public void setPriority(String priority) {
        this.getDualityFluidInterface().setPriority((int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, Long.parseLong(priority))));
        this.markDirty();
    }

    @Override
    public void setFluidAutoPull(boolean autoPull) {
        // No such features
    }
}
