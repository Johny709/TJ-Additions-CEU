package tja.machines.controllers;

import gregicality.multiblocks.api.metatileentity.GCYMMultiblockAbility;
import gregicality.multiblocks.common.metatileentities.multiblockpart.MetaTileEntityTieredHatch;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.util.BlockInfo;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import tja.capability.handler.IMachineHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class TJMultiblockControllerBase extends MultiblockWithDisplayBase implements IMachineHandler {

    protected IItemHandlerModifiable importItemInventory;
    protected IItemHandlerModifiable exportItemInventory;
    protected IMultipleTankHandler importFluidTank;
    protected IMultipleTankHandler exportFluidTank;
    protected IEnergyContainer inputEnergyContainer;
    protected IEnergyContainer outputEnergyContainer;

    public TJMultiblockControllerBase(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected boolean shouldUpdate(MTETrait trait) {
        return false;
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.importItemInventory = new ItemHandlerList(this.getAbilities(MultiblockAbility.IMPORT_ITEMS));
        this.exportItemInventory = new ItemHandlerList(this.getAbilities(MultiblockAbility.EXPORT_ITEMS));
        this.importFluidTank = new FluidTankList(true, this.getAbilities(MultiblockAbility.IMPORT_FLUIDS));
        this.exportFluidTank = new FluidTankList(true, this.getAbilities(MultiblockAbility.EXPORT_FLUIDS));
        this.inputEnergyContainer = new EnergyContainerList(this.getAbilities(MultiblockAbility.INPUT_ENERGY));
        this.outputEnergyContainer = new EnergyContainerList(this.getAbilities(MultiblockAbility.OUTPUT_ENERGY));
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.importItemInventory = new ItemHandlerList(Collections.emptyList());
        this.exportItemInventory = new ItemHandlerList(Collections.emptyList());
        this.importFluidTank = new FluidTankList(true);
        this.exportFluidTank = new FluidTankList(true);
        this.inputEnergyContainer = new EnergyContainerList(Collections.emptyList());
        this.outputEnergyContainer = new EnergyContainerList(Collections.emptyList());
    }

    protected int[] getTotalFluidAmount(FluidStack testStack, IMultipleTankHandler multiTank) {
        int fluidAmount = 0;
        int fluidCapacity = 0;
        if (multiTank != null) {
            for (IMultipleTankHandler.ITankEntry tank : multiTank) {
                if (tank != null) {
                    FluidStack drainStack = tank.drain(testStack, false);
                    if (drainStack != null && drainStack.amount > 0) {
                        fluidAmount += drainStack.amount;
                        fluidCapacity += tank.getCapacity();
                    }
                }
            }
        }
        return new int[] { fluidAmount, fluidCapacity };
    }

    @Override
    public IItemHandlerModifiable getImportItemInventory() {
        return this.importItemInventory;
    }

    @Override
    public IItemHandlerModifiable getExportItemInventory() {
        return this.exportItemInventory;
    }

    @Override
    public IMultipleTankHandler getImportFluidTank() {
        return this.importFluidTank;
    }

    @Override
    public IMultipleTankHandler getExportFluidTank() {
        return this.exportFluidTank;
    }

    @Override
    public IEnergyContainer getInputEnergyContainer() {
        return this.inputEnergyContainer;
    }

    @Override
    public IEnergyContainer getOutputEnergyContainer() {
        return this.outputEnergyContainer;
    }

    @Override
    public int getNumMaintenanceProblems() {
        return super.getNumMaintenanceProblems();
    }

    public static TraceabilityPredicate tieredHatchPredicate() {
        return new TraceabilityPredicate(blockWorldState -> {
            if (blockWorldState.getTileEntity() instanceof MetaTileEntityHolder) {
                final MetaTileEntityHolder holder = (MetaTileEntityHolder) blockWorldState.getTileEntity();
                final MetaTileEntity tileEntity = holder.getMetaTileEntity();
                if (tileEntity instanceof MetaTileEntityTieredHatch) {
                    final MetaTileEntityTieredHatch tieredHatch = (MetaTileEntityTieredHatch) tileEntity;
                    final List<MetaTileEntityTieredHatch> tieredHatches = blockWorldState.getMatchContext().getOrCreate("tiered_hatches", ArrayList::new);
                    tieredHatches.add(tieredHatch);
                    return tieredHatches.get(0).getTier() == tieredHatch.getTier();
                }
            }
            return false;
        }, () -> MultiblockAbility.REGISTRY.get(GCYMMultiblockAbility.TIERED_HATCH).stream()
                .map(tileEntity -> {
                    final MetaTileEntityHolder holder = new MetaTileEntityHolder();
                    holder.setMetaTileEntity(tileEntity);
                    holder.getMetaTileEntity().onPlacement();
                    return new BlockInfo(tileEntity.getBlock().getDefaultState(), holder);
                }).toArray(BlockInfo[]::new));
    }
}
