package tja.machines.controllers;

import gregicality.multiblocks.api.metatileentity.GCYMMultiblockAbility;
import gregicality.multiblocks.common.metatileentities.multiblockpart.MetaTileEntityTieredHatch;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.IDistinctBusController;
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
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.common.metatileentities.electric.MetaTileEntityHull;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import tja.TJAValues;
import tja.blocks.BlockTieredGlass;
import tja.blocks.TJAMetaBlocks;
import tja.capability.handler.IMachineHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class TJAMultiblockControllerBase extends MultiblockWithDisplayBase implements IMachineHandler, IControllable, IDistinctBusController {

    protected IItemHandlerModifiable importItemInventory;
    protected IItemHandlerModifiable exportItemInventory;
    protected IMultipleTankHandler importFluidTank;
    protected IMultipleTankHandler exportFluidTank;
    protected IEnergyContainer inputEnergyContainer;
    protected IEnergyContainer outputEnergyContainer;
    protected long maxVoltage;
    protected int tier;

    public TJAMultiblockControllerBase(ResourceLocation metaTileEntityId) {
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
        final List<IEnergyContainer> inputEnergy = new ArrayList<>(this.getAbilities(MultiblockAbility.INPUT_ENERGY));
        inputEnergy.addAll(this.getAbilities(MultiblockAbility.INPUT_LASER));
        final List<IEnergyContainer> outputEnergy = new ArrayList<>(this.getAbilities(MultiblockAbility.OUTPUT_ENERGY));
        outputEnergy.addAll(this.getAbilities(MultiblockAbility.OUTPUT_LASER));
        this.inputEnergyContainer = new EnergyContainerList(inputEnergy);
        this.outputEnergyContainer = new EnergyContainerList(outputEnergy);
        for (IFluidTank tank : this.getAbilities(MultiblockAbility.IMPORT_FLUIDS)) {
            this.getNotifiedFluidInputList().add((IFluidHandler)tank);
        }
        this.getNotifiedItemInputList().addAll(this.getAbilities(MultiblockAbility.IMPORT_ITEMS));
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
        this.maxVoltage = 0;
        this.tier = 0;
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

    @Override
    public long getMaxVoltage() {
        return this.maxVoltage;
    }

    @Override
    public int getTier() {
        return this.tier;
    }

    @Override
    public boolean isWorkingEnabled() {
        return false;
    }

    @Override
    public void setWorkingEnabled(boolean b) {}

    @Override
    public boolean canBeDistinct() {
        return false;
    }

    @Override
    public boolean isDistinct() {
        return false;
    }

    @Override
    public void setDistinct(boolean b) {}

    public static TraceabilityPredicate tieredHatchPredicate() {
        return new TraceabilityPredicate(blockWorldState -> {
            if (blockWorldState.getTileEntity() instanceof MetaTileEntityHolder) {
                final MetaTileEntityHolder holder = (MetaTileEntityHolder) blockWorldState.getTileEntity();
                final MetaTileEntity metaTileEntity = holder.getMetaTileEntity();
                if (TJAValues.isModLoaded(TJAValues.GCYM_MOD_ID)) {
                    if (metaTileEntity instanceof MetaTileEntityTieredHatch) {
                        final MetaTileEntityTieredHatch tieredHatch = (MetaTileEntityTieredHatch) metaTileEntity;
                        final IntArrayList tieredHatches = blockWorldState.getMatchContext().getOrCreate("hatch_tier", IntArrayList::new);
                        tieredHatches.add(tieredHatch.getTier());
                        return tieredHatches.getInt(0) == tieredHatch.getTier();
                    }
                } else if (metaTileEntity instanceof MetaTileEntityHull) {
                    final MetaTileEntityHull hull = (MetaTileEntityHull) metaTileEntity;
                    final IntArrayList tieredHatches = blockWorldState.getMatchContext().getOrCreate("hatch_tier", IntArrayList::new);
                    tieredHatches.add(hull.getTier());
                    return tieredHatches.getInt(0) == hull.getTier();
                }
            }
            return false;
        }, TJAValues.isModLoaded(TJAValues.GCYM_MOD_ID) ?
                () -> MultiblockAbility.REGISTRY.get(GCYMMultiblockAbility.TIERED_HATCH).stream()
                .map(TJAMultiblockControllerBase::mapMTEToBlockInfo).toArray(BlockInfo[]::new) :
                () -> Arrays.stream(MetaTileEntities.HULL)
                        .map(TJAMultiblockControllerBase::mapMTEToBlockInfo)
                        .toArray(BlockInfo[]::new));
    }

    private static BlockInfo mapMTEToBlockInfo(MetaTileEntity metaTileEntity) {
        final MetaTileEntityHolder holder = new MetaTileEntityHolder();
        holder.setMetaTileEntity(metaTileEntity);
        holder.getMetaTileEntity().onPlacement();
        return new BlockInfo(metaTileEntity.getBlock().getDefaultState(), holder);
    }

    public static TraceabilityPredicate glassPredicate() {
        return new TraceabilityPredicate(blockWorldState -> {
            if (blockWorldState.getBlockState().getBlock() instanceof BlockTieredGlass) {
                final BlockTieredGlass.CasingType casingType = TJAMetaBlocks.TIERED_GLASS.getState(blockWorldState.getBlockState());
                final List<BlockTieredGlass.CasingType> casingTypeList = blockWorldState.getMatchContext().getOrCreate("tiered_glass", ArrayList::new);
                casingTypeList.add(casingType);
                return casingTypeList.get(0).getName().equals(casingType.getName());
            }
            return false;
        }, () -> Arrays.stream(BlockTieredGlass.CasingType.values())
                .map(TJAMetaBlocks.TIERED_GLASS::getState)
                .map(BlockInfo::new)
                .toArray(BlockInfo[]::new));
    }

    public static TraceabilityPredicate parallelHatchPredicate() {
        return TJAValues.isModLoaded(TJAValues.GCYM_MOD_ID) ? abilities(GCYMMultiblockAbility.PARALLEL_HATCH).setMaxGlobalLimited(1) : abilities();
    }
}
