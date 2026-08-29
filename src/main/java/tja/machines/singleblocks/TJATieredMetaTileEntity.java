package tja.machines.singleblocks;

import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.EnergyContainerHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.mui.IMetaTileEntityGuiHolder;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import tja.capability.handler.IMachineHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class TJATieredMetaTileEntity extends TieredMetaTileEntity implements IMachineHandler, IMetaTileEntityGuiHolder {

    protected final EnergyContainerHandler energyContainerHandler = (EnergyContainerHandler) this.energyContainer;

    public TJATieredMetaTileEntity(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, @Nonnull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_in", this.getMaxVoltage(), GTValues.VOCNF[this.getTier()]));
        tooltip.add(I18n.format("gregtech.universal.tooltip.energy_storage_capacity", this.energyContainer.getEnergyCapacity()));
    }

    @Override
    protected void reinitializeEnergyContainer() {
        final long tierVoltage = GTValues.V[getTier()];
        if (this.isEnergyEmitter()) {
            this.energyContainer = EnergyContainerHandler.emitterContainer(this, tierVoltage * 64L, tierVoltage, this.getMaxInputOutputAmperage());
        } else this.energyContainer = new EnergyContainerHandler(this, tierVoltage * 64L, tierVoltage, 2, 0L, 0L) {

            @Override
            public long getInputAmperage() {
                if (getEnergyCapacity() / 2 > getEnergyStored() && isActive()) {
                    return 2;
                }
                return 1;
            }
        };
        ((EnergyContainerHandler) this.energyContainer).setSideOutputCondition(side -> side == getFrontFacing());
    }

    @Override
    protected boolean shouldUpdate(MTETrait trait) {
        return false;
    }

    @Override
    public void update() {
        super.update();
        if (!this.getWorld().isRemote) {
            this.energyContainerHandler.update();
        }
    }

    @Override
    public IItemHandlerModifiable getImportItemInventory() {
        return this.importItems;
    }

    @Override
    public IItemHandlerModifiable getExportItemInventory() {
        return this.exportItems;
    }

    @Override
    public IMultipleTankHandler getImportFluidTank() {
        return this.importFluids;
    }

    @Override
    public FluidTankList getExportFluids() {
        return this.exportFluids;
    }

    @Override
    public IEnergyContainer getInputEnergyContainer() {
        return this.energyContainer;
    }

    @Override
    public long getMaxVoltage() {
        return GTValues.VOC[this.getTier()];
    }
}
