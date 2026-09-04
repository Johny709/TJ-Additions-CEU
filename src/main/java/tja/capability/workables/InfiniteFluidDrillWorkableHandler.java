package tja.capability.workables;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.worldgen.bedrockFluids.BedrockFluidVeinHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import tja.capability.AbstractWorkableHandler;
import tja.capability.handler.IMachineHandler;
import tja.util.TJAFluidUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static com.fulltrix.gcyl.materials.GCYLMaterials.DrillingMud;
import static com.fulltrix.gcyl.materials.GCYLMaterials.UsedDrillingMud;
import static tja.machines.multiblocks.MetaTileEntityInfiniteFluidDrill.DRILLING_MUD;


public class InfiniteFluidDrillWorkableHandler extends AbstractWorkableHandler<IMachineHandler> {

    private final List<FluidStack> fluidInputsList = new ArrayList<>();
    private final List<FluidStack> fluidOutputsList = new ArrayList<>();
    private Fluid veinFluid;
    private int outputIndex;
    private long drillingMudAmount;
    private long outputFluidAmount;

    public InfiniteFluidDrillWorkableHandler(MetaTileEntity metaTileEntity) {
        super(metaTileEntity);
    }

    @Override
    public void initialize(int tier) {
        super.initialize(tier);
        final World world = this.metaTileEntity.getWorld();
        this.veinFluid = BedrockFluidVeinHandler.getFluidInChunk(world, this.metaTileEntity.getPos().getX(), this.metaTileEntity.getPos().getZ());
        if (this.veinFluid == null) return;
        this.drillingMudAmount = (long) (Math.pow(4, (tier - GTValues.EV)) * 10);
        this.outputFluidAmount = (long) (Math.pow(4, (tier - GTValues.EV)) * 4000);
    }

    @Override
    protected boolean startRecipe() {
        if (TJAFluidUtils.drainFromTanksLong(this.handler.getImportFluidTank(), DRILLING_MUD, this.drillingMudAmount, false) == this.drillingMudAmount) {
            TJAFluidUtils.drainFromTanksLong(this.handler.getImportFluidTank(), DRILLING_MUD, this.drillingMudAmount, true);
            long amount = this.drillingMudAmount;
            for (; amount > 0; amount -= Integer.MAX_VALUE)
                this.fluidInputsList.add(DrillingMud.getFluid((int) Math.min(Integer.MAX_VALUE, amount)));
            for (amount = this.drillingMudAmount; amount > 0; amount -= Integer.MAX_VALUE)
                this.fluidOutputsList.add(UsedDrillingMud.getFluid((int) Math.min(Integer.MAX_VALUE, amount)));
            amount = this.outputFluidAmount / (long) (1.00 + 0.05 * this.handler.getNumMaintenanceProblems());
            for (; amount > 0; amount -= Integer.MAX_VALUE)
                this.fluidOutputsList.add(new FluidStack(this.veinFluid, (int) Math.min(Integer.MAX_VALUE, amount)));
            this.energyPerTick = this.handler.getMaxVoltage();
            this.maxProgress = 20;
            return true;
        } else return false;
    }

    @Override
    protected boolean completeRecipe() {
        for (int i = this.outputIndex; i < this.fluidOutputsList.size(); i++) {
            final FluidStack stack = this.fluidOutputsList.get(i);
            final boolean voidingFluids = this.handler.getVoidingModeInt() >= 2;
            if (voidingFluids || this.handler.getExportFluidTank().fill(stack, false) == stack.amount) {
                this.handler.getExportFluidTank().fill(stack, true);
                this.outputIndex++;
            } else return false;
        }
        this.fluidInputsList.clear();
        this.fluidOutputsList.clear();
        this.outputIndex = 0;
        return true;
    }

    @Nonnull
    @Override
    public NBTTagCompound serializeNBT() {
        final NBTTagCompound compound = super.serializeNBT();
        final NBTTagList fluidInputsList = new NBTTagList(), fluidOutputsList = new NBTTagList();
        for (FluidStack fluid : this.fluidInputsList)
            fluidInputsList.appendTag(fluid.writeToNBT(new NBTTagCompound()));
        for (FluidStack fluid : this.fluidOutputsList)
            fluidOutputsList.appendTag(fluid.writeToNBT(new NBTTagCompound()));
        compound.setTag("fluidInputsList", fluidInputsList);
        compound.setTag("fluidOutputsList", fluidOutputsList);
        compound.setInteger("outputIndex", this.outputIndex);
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        super.deserializeNBT(compound);
        this.outputIndex = compound.getInteger("outputIndex");
        final NBTTagList fluidInputsList = compound.getTagList("fluidInputsList", 10);
        final NBTTagList fluidOutputsList = compound.getTagList("fluidOutputsList", 10);
        for (int i = 0; i < fluidInputsList.tagCount(); i++)
            this.fluidInputsList.add(FluidStack.loadFluidStackFromNBT(fluidInputsList.getCompoundTagAt(i)));
        for (int i = 0; i < fluidOutputsList.tagCount(); i++)
            this.fluidOutputsList.add(FluidStack.loadFluidStackFromNBT(fluidOutputsList.getCompoundTagAt(i)));
    }

    public Fluid getVeinFluid() {
        return this.veinFluid;
    }

    public long getDrillingMudAmount() {
        return this.drillingMudAmount;
    }

    @Nonnull
    @Override
    public List<FluidStack> getFluidInputs() {
        return this.fluidInputsList;
    }

    @Nonnull
    @Override
    public List<FluidStack> getFluidOutputs() {
        return this.fluidOutputsList;
    }
}
