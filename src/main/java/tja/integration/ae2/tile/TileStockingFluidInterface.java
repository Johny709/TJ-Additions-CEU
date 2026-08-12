package tja.integration.ae2.tile;

import appeng.api.AEApi;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.core.settings.TickRates;
import appeng.fluids.tile.TileFluidInterface;
import appeng.fluids.util.AEFluidStack;
import appeng.me.GridAccessException;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import tja.blocks.TJABlocks;
import tja.integration.ae2.ISuperFluidInterface;
import tja.integration.ae2.blocks.BlockStockingFluidInterface;
import tja.integration.ae2.helpers.DualitySuperFluidInterface;

import javax.annotation.Nonnull;


public class TileStockingFluidInterface extends TileFluidInterface implements IGuiHolder<PosGuiData>, ISuperFluidInterface {

    private final BlockPos.MutableBlockPos interfacePos = new BlockPos.MutableBlockPos();
    private int tickTime = 100;

    public TileStockingFluidInterface() {
        ObfuscationReflectionHelper.setPrivateValue(TileFluidInterface.class, this, new DualitySuperFluidInterface(this.getProxy(), this, 36), "duality");
        this.getDualityFluidInterface().getConfigManager().registerSetting(Settings.STICKY_MODE, YesNo.NO);
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return BlockStockingFluidInterface.createFluidInterfaceGUI(data, syncManager, settings, this);
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("tickTime", this.tickTime);
        data.setInteger("autoOutputFluid", this.getDualityFluidInterface().getConfigManager().getSetting(Settings.STICKY_MODE).ordinal());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.tickTime = data.getInteger("tickTime");
        this.getDualityFluidInterface().getConfigManager().putSetting(Settings.STICKY_MODE, YesNo.values()[data.getInteger("autoOutputFluid")]);
    }

    @Nonnull
    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (!this.getProxy().isActive())
            return TickRateModulation.SLEEP;
        final TickRateModulation tickRateModulation = super.tickingRequest(node, ticksSinceLastCall);
        if (this.getDualityFluidInterface().getConfigManager().getSetting(Settings.BLOCK) == YesNo.YES) {
            try {
                int index = 0;
                final int stackSize = (int) Math.min(Integer.MAX_VALUE, 64000L << this.getInstalledUpgrades(Upgrades.CAPACITY) * 2);
                final IItemList<?> iItemList = this.getProxy().getStorage().getInventory(AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class)).getStorageList();
                for (IAEStack<?> fluids : iItemList) {
                    if (index < this.getDualityFluidInterface().getConfig().getSlots()) {
                        if (fluids.isItem()) continue;
                        final AEFluidStack aeFluidStack = (AEFluidStack) fluids;
                        final FluidStack fluidStack = aeFluidStack.getFluidStack();
                        fluidStack.amount = Math.min(fluidStack.amount, stackSize);
                        this.getDualityFluidInterface().getConfig().setFluidInSlot(index++, AEFluidStack.fromFluidStack(fluidStack));
                    } else break;
                }
            } catch (GridAccessException ignored) {}
        }
        if (this.getDualityFluidInterface().getConfigManager().getSetting(Settings.STICKY_MODE) == YesNo.YES) {
            final BlockPos pos = this.getTile().getPos();
            for (EnumFacing facing : this.getTargets()) {
                this.interfacePos.setPos(pos.getX(), pos.getY(), pos.getZ());
                final TileEntity tileEntity = this.getTile().getWorld().getTileEntity(this.interfacePos.move(facing));
                if (tileEntity != null) {
                    final IFluidHandler fluidHandler = this.getDualityFluidInterface().getTanks();
                    final IFluidHandler destFluidHandler = tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite());
                    if (destFluidHandler != null) {
                        for (IFluidTankProperties tank : fluidHandler.getTankProperties()) {
                            FluidStack fluidStack = tank.getContents();
                            if (fluidStack != null) {
                                fluidStack = fluidHandler.drain(fluidStack, false);
                                if (fluidStack == null) continue;
                                fluidStack.amount = destFluidHandler.fill(fluidStack, true);
                                fluidHandler.drain(fluidStack, true);
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
        return new TickingRequest(TickRates.Interface.getMin(), TickRates.Interface.getMax(), super.getTickingRequest(node).isSleeping && this.getDualityFluidInterface().getConfigManager().getSetting(Settings.BLOCK) == YesNo.NO, false);
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return TJABlocks.STOCKING_FLUID_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY);
    }

    @Override
    public void setFluidAutoPull(boolean blockingMode) {
        this.getDualityFluidInterface().getConfigManager().putSetting(Settings.BLOCK, blockingMode ? YesNo.YES : YesNo.NO);
        this.markDirty();
    }

    @Override
    public void setFluidAutoPush(boolean autoPush) {
        this.getDualityFluidInterface().getConfigManager().putSetting(Settings.STICKY_MODE, autoPush ? YesNo.YES : YesNo.NO);
        this.markDirty();
    }

    @Override
    public void setTickTime(String tickTime) {
        this.tickTime = (int) Math.max(1, Math.min(Integer.MAX_VALUE, Long.parseLong(tickTime)));
        this.markDirty();
    }

    @Override
    public int getTickTime() {
        return this.tickTime;
    }

    @Override
    public void setPriority(String priority) {
        this.getDualityFluidInterface().setPriority((int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, Long.parseLong(priority))));
        this.markDirty();
    }
}
