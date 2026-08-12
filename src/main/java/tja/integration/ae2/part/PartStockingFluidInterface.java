package tja.integration.ae2.part;

import appeng.api.AEApi;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartModel;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.core.settings.TickRates;
import appeng.fluids.parts.PartFluidInterface;
import appeng.fluids.util.AEFluidStack;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;
import appeng.parts.PartModel;
import appeng.tile.networking.TileCableBus;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.GuiFactories;
import com.cleanroommc.modularui.factory.SidedPosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import tja.TJA;
import tja.integration.ae2.ISuperFluidInterface;
import tja.integration.ae2.blocks.BlockStockingFluidInterface;
import tja.integration.ae2.helpers.DualitySuperFluidInterface;
import tja.items.TJAItems;

import javax.annotation.Nonnull;


public class PartStockingFluidInterface extends PartFluidInterface implements IGuiHolder<SidedPosGuiData>, ISuperFluidInterface {

    public static final ResourceLocation MODEL_BASE = new ResourceLocation(TJA.MOD_ID, "part/me.part.stocking_fluid_interface_base");

    @PartModels
    public static final PartModel MODELS_OFF = new PartModel(MODEL_BASE, new ResourceLocation(TJA.MOD_ID, "part/me.part.stocking_fluid_interface_off"));

    @PartModels
    public static final PartModel MODELS_ON = new PartModel(MODEL_BASE, new ResourceLocation(TJA.MOD_ID, "part/me.part.stocking_fluid_interface_on"));

    @PartModels
    public static final PartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, new ResourceLocation(TJA.MOD_ID, "part/me.part.stocking_fluid_interface_has_channel"));

    private final BlockPos.MutableBlockPos interfacePos = new BlockPos.MutableBlockPos();
    private int tickTime = 100;

    public PartStockingFluidInterface(ItemStack is) {
        super(is);
        ObfuscationReflectionHelper.setPrivateValue(PartFluidInterface.class, this, new DualitySuperFluidInterface(this.getProxy(), this, 36), "duality");
        this.getDualityFluidInterface().getConfigManager().registerSetting(Settings.STICKY_MODE, YesNo.NO);
    }

    @Override
    public boolean onPartActivate(EntityPlayer player, EnumHand hand, Vec3d pos) {
        final TileCableBus tileCableBus = (TileCableBus) this.getTile();
        if (tileCableBus != null && !player.getEntityWorld().isRemote) {
            GuiFactories.sidedTileEntity().open(player, tileCableBus.getPos(), this.getSide().getFacing());
        }
        return true;
    }

    @Override
    public ModularPanel buildUI(SidedPosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return BlockStockingFluidInterface.createFluidInterfaceGUI(data, syncManager, settings, this);
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("tickTime", this.tickTime);
        data.setInteger("autoOutputFluid", this.getDualityFluidInterface().getConfigManager().getSetting(Settings.STICKY_MODE).ordinal());
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
        return TJAItems.PART_STOCKING_FLUID_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY);
    }

    @Nonnull
    @Override
    public IPartModel getStaticModels() {
        if (this.isActive() && this.isPowered()) {
            return MODELS_HAS_CHANNEL;
        } else if (this.isPowered()) {
            return MODELS_ON;
        } else {
            return MODELS_OFF;
        }
    }

    @Override
    public void setFluidAutoPull(boolean blockingMode) {
        this.getDualityFluidInterface().getConfigManager().putSetting(Settings.BLOCK, blockingMode ? YesNo.YES : YesNo.NO);
        this.getTile().markDirty();
    }

    @Override
    public void setFluidAutoPush(boolean autoPush) {
        this.getDualityFluidInterface().getConfigManager().putSetting(Settings.STICKY_MODE, autoPush ? YesNo.YES : YesNo.NO);
        this.getTile().markDirty();
    }

    @Override
    public void setTickTime(String tickTime) {
        this.tickTime = (int) Math.max(1, Math.min(Integer.MAX_VALUE, Long.parseLong(tickTime)));
        this.getTile().markDirty();
    }

    @Override
    public void setPriority(String priority) {
        this.getDualityFluidInterface().setPriority((int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, Long.parseLong(priority))));
        this.getTile().markDirty();
    }
}
