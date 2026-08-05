package tja.capability.workables;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.ore.OrePrefix;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import tja.capability.AbstractWorkableHandler;
import tja.capability.IHeatInfo;
import tja.capability.IItemFluidHandlerInfo;
import tja.capability.handler.IBoilerHandler;
import tja.util.TJAItemUtils;

import javax.annotation.Nonnull;
import java.util.*;

import static gregtech.api.unification.material.Materials.*;
import static tja.capability.TJACapabilities.CAPABILITY_HEAT;
import static tja.capability.TJACapabilities.CAPABILITY_ITEM_FLUID_HANDLING;

public class MegaBoilerRecipeLogic extends AbstractWorkableHandler<IBoilerHandler> implements IHeatInfo, IItemFluidHandlerInfo {

    private static final int CONSUMPTION_MULTIPLIER = 100;
    private static final int BOILING_TEMPERATURE = 100;
    private static final double COAL_BURNTIME = 1600;
    private final List<FluidStack> fluidInput = new ArrayList<>();
    private final List<FluidStack> fluidOutput = new ArrayList<>();
    private final List<ItemStack> itemInput = new ArrayList<>();
    private final List<ItemStack> itemOutput = new ArrayList<>();
    private final Set<FluidStack> lastSearchedFluid = new HashSet<>();

    private FluidStack lastBurnFluid;
    private boolean hasNoWater;
    private int currentTemperature;
    private int waterConsumption;
    private int steamProduction;
    private int searchCount;
    private int throttlePercentage = 100;

    public MegaBoilerRecipeLogic(MetaTileEntity metaTileEntity) {
        super(metaTileEntity);
    }

    @Override
    protected boolean startRecipe() {
        int fuelMaxBurnTime = this.findFluidInputs();
        if (fuelMaxBurnTime == 0)
            fuelMaxBurnTime = this.findItemInputs();
        if (fuelMaxBurnTime > 0) {
            this.maxProgress = (int) (fuelMaxBurnTime * this.handler.getHeatEfficiencyMultiplier());
        } else return false;
        return true;
    }

    @Override
    protected void progressRecipe(int progress) {
        this.progress++;
        if (this.metaTileEntity.getOffsetTimer() % 20 == 0) {
            final double outputMultiplier = this.currentTemperature / (this.maxHeat() * 1.0) * this.getThrottleMultiplier() * this.getThrottleEfficiency();
            this.steamProduction = (int) (this.handler.getBaseSteamOutput() * this.handler.getParallel() * outputMultiplier);
            if (this.currentTemperature < this.maxHeat())
                this.currentTemperature++;
        }

        if (this.currentTemperature < BOILING_TEMPERATURE) {
            this.hasNoWater = false;
            return;
        }
        this.waterConsumption = Math.round((float) this.steamProduction / 160);
        final boolean hasEnoughWater = this.hasEnoughFluid(Water.getFluid(this.waterConsumption), this.waterConsumption);
        if (hasEnoughWater && this.hasNoWater) {
            this.metaTileEntity.getWorld().setBlockToAir(this.metaTileEntity.getPos());
            this.metaTileEntity.getWorld().createExplosion(null,
                    this.metaTileEntity.getPos().getX() + 0.5, this.metaTileEntity.getPos().getY() + 0.5, this.metaTileEntity.getPos().getZ() + 0.5,
                    2.0f, true);
        } else {
            if (hasEnoughWater) {
                this.handler.getExportFluidTank().fill(Steam.getFluid(this.steamProduction), true);
                this.handler.getImportFluidTank().drain(Water.getFluid(this.waterConsumption), true);
            } else {
                this.hasNoWater = true;
            }
        }
    }

    @Override
    protected void sleepRecipe() {
        super.sleepRecipe();
        this.stopRecipe();
    }

    @Override
    protected void stopRecipe() {
        if (this.metaTileEntity.getOffsetTimer() % 20 == 0)
            if (this.currentTemperature > 0)
                this.currentTemperature--;
    }

    @Override
    protected boolean completeRecipe() {
        if (!this.itemOutput.isEmpty())
            TJAItemUtils.insertIntoItemHandler(this.handler.getExportItemInventory(), this.itemOutput.remove(0), false);
        if (!this.fluidOutput.isEmpty())
            this.handler.getExportFluidTank().fill(this.fluidOutput.remove(0), true);
        this.itemInput.clear();
        this.fluidInput.clear();
        return true;
    }

    private int findFluidInputs() {
        FluidStack fuelStack = null;
        for (int i = 0; i < this.handler.getImportFluidTank().getTanks(); i++) {
            final IFluidTank tank = this.handler.getImportFluidTank().getTankAt(i);
            final FluidStack stack = tank.getFluid();
            if (stack == null) continue;
            if (fuelStack == null) {
                if (this.lastSearchedFluid.contains(stack)) continue;
                fuelStack = stack.copy();
                this.lastSearchedFluid.add(fuelStack);
            } else if (fuelStack.isFluidEqual(stack)) {
                long amount = fuelStack.amount + stack.amount;
                fuelStack.amount = (int) Math.min(Integer.MAX_VALUE, amount);
            }
        }
        final Recipe dieselRecipe = RecipeMaps.COMBUSTION_GENERATOR_FUELS.findRecipe(GTValues.V[GTValues.MAX], Collections.emptyList(), Collections.singletonList(fuelStack));
        if (dieselRecipe != null) {
            this.lastBurnFluid = fuelStack;
            fuelStack.amount = (int) Math.ceil(dieselRecipe.getFluidInputs().get(0).getAmount() * CONSUMPTION_MULTIPLIER * this.handler.getParallel() * this.handler.getFuelConsumptionMultiplier() * getThrottleMultiplier());
            if (fuelStack.isFluidStackIdentical(this.handler.getImportFluidTank().drain(fuelStack, false))) {
                this.fluidInput.add(this.handler.getImportFluidTank().drain(fuelStack, true));
                final int burnTime = (int) Math.ceil(dieselRecipe.getDuration() * CONSUMPTION_MULTIPLIER / 2.0 * this.getThrottleMultiplier());
                this.getCarbonDioxideByproduct(burnTime, fuelStack.amount);
                this.lastSearchedFluid.clear();
                return burnTime;
            }
        }
        final Recipe denseFuelRecipe = RecipeMaps.SEMI_FLUID_GENERATOR_FUELS.findRecipe(GTValues.V[GTValues.MAX], Collections.emptyList(), Collections.singletonList(fuelStack));
        if (denseFuelRecipe != null) {
            this.lastBurnFluid = fuelStack;
            fuelStack.amount = (int) Math.ceil(denseFuelRecipe.getFluidInputs().get(0).getAmount() * CONSUMPTION_MULTIPLIER * this.handler.getParallel() * this.handler.getFuelConsumptionMultiplier() * getThrottleMultiplier());
            if (fuelStack.isFluidStackIdentical(this.handler.getImportFluidTank().drain(fuelStack, false))) {
                this.fluidInput.add(this.handler.getImportFluidTank().drain(fuelStack, true));
                final int burnTime = (int) Math.ceil(denseFuelRecipe.getDuration() * CONSUMPTION_MULTIPLIER * 2 * this.getThrottleMultiplier());
                this.getCarbonDioxideByproduct(burnTime, fuelStack.amount);
                this.lastSearchedFluid.clear();
                return burnTime;
            }
        }
        if (++this.searchCount >= this.handler.getImportFluidTank().getTanks()) {
            this.lastSearchedFluid.clear();
            this.searchCount = 0;
        }
        return 0;
    }

    private void getCarbonDioxideByproduct(int burnTime, int fuelAmount) {
        final double carbonBurnTime = COAL_BURNTIME / this.handler.getParallel();
        if (burnTime >= carbonBurnTime) {
            final int amount = (int) (fuelAmount * Math.max(0.4, Math.random()));
            this.fluidOutput.add(CarbonDioxide.getFluid(amount));
        }
    }

    private int findItemInputs() {
        int burnTime = 0, count = 0;
        ItemStack fuelStack = null;
        int availableParallels = this.handler.getParallel();
        for (int i = 0; i < this.handler.getImportItemInventory().getSlots(); i++) {
            final ItemStack stack = this.handler.getImportItemInventory().getStackInSlot(i);
            if (fuelStack == null) {
                int fuelBurnValue = (int) (this.getBurnValue(stack) * stack.getCount());
                if (fuelBurnValue > 0) {
                    fuelStack = stack.copy();
                    this.itemInput.add(fuelStack);
                }
            }
            if (fuelStack != null && fuelStack.isItemEqual(stack)) {
                final int extracted = Math.min(availableParallels, stack.getCount());
                final int fuelBurnValue = (int) (this.getBurnValue(stack) * extracted);
                burnTime += fuelBurnValue;
                availableParallels -= extracted;
                count += extracted;
                fuelStack.setCount(count);
                if (this.handler.getImportItemInventory().extractItem(i, extracted, true).getCount() == extracted)
                    this.handler.getImportItemInventory().extractItem(i, extracted, false);
                else return 0;
            }
            if (availableParallels < 1)
                break;
        }
        final double ashBurnTime = COAL_BURNTIME / this.handler.getParallel();
        if (burnTime >= ashBurnTime) {
            final int amount = (int) ((burnTime / ashBurnTime) * Math.max(0.4, Math.random()));
            this.itemOutput.add(OreDictUnifier.get(OrePrefix.dust, DarkAsh, amount)); // dark ashes
        }
        return burnTime;
    }

    private double getBurnValue(ItemStack stack) {
        return Math.ceil(TileEntityFurnace.getItemBurnTime(stack) / (50.0 * this.handler.getFuelConsumptionMultiplier() * this.getThrottleMultiplier())) / this.handler.getParallel();
    }

    @Nonnull
    @Override
    public NBTTagCompound serializeNBT() {
        final NBTTagCompound compound = super.serializeNBT();
        compound.setInteger("CurrentTemperature", this.currentTemperature);
        compound.setBoolean("HasNoWater", this.hasNoWater);
        compound.setInteger("ThrottlePercentage", this.throttlePercentage);
        if (!this.itemInput.isEmpty())
            compound.setTag("itemInput", this.itemInput.get(0).serializeNBT());
        if (!this.itemOutput.isEmpty())
            compound.setTag("itemOutput", this.itemOutput.get(0).serializeNBT());
        if (!this.fluidInput.isEmpty())
            compound.setTag("fluidInput", this.fluidInput.get(0).writeToNBT(new NBTTagCompound()));
        if (!this.fluidOutput.isEmpty())
            compound.setTag("fluidOutput", this.fluidOutput.get(0).writeToNBT(new NBTTagCompound()));
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        super.deserializeNBT(compound);
        this.currentTemperature = compound.getInteger("CurrentTemperature");
        this.hasNoWater = compound.getBoolean("HasNoWater");
        if (compound.hasKey("ThrottlePercentage"))
            this.throttlePercentage = compound.getInteger("ThrottlePercentage");
        if (compound.hasKey("itemInput"))
            this.itemInput.add(new ItemStack(compound.getCompoundTag("itemInput")));
        if (compound.hasKey("itemOutput"))
            this.itemOutput.add(new ItemStack(compound.getCompoundTag("itemOutput")));
        if (compound.hasKey("fluidInput"))
            this.fluidInput.add(FluidStack.loadFluidStackFromNBT(compound.getCompoundTag("fluidInput")));
        if (compound.hasKey("fluidOutput"))
            this.fluidOutput.add(FluidStack.loadFluidStackFromNBT(compound.getCompoundTag("fluidOutput")));
    }

    @Override
    public <T> T getCapability(Capability<T> capability) {
        if (capability == CAPABILITY_HEAT)
            return CAPABILITY_HEAT.cast(this);
        if (capability == CAPABILITY_ITEM_FLUID_HANDLING)
            return CAPABILITY_ITEM_FLUID_HANDLING.cast(this);
        return super.getCapability(capability);
    }

    public FluidStack getLastBurnFluid() {
        return this.lastBurnFluid;
    }

    public double getThrottleMultiplier() {
        return this.throttlePercentage / 100.0;
    }

    public double getThrottleEfficiency() {
        return MathHelper.clamp(1.0 + 0.3*Math.log(this.getThrottleMultiplier()), 0.4, 1.0);
    }

    public void setThrottlePercentage(int throttlePercentage) {
        this.throttlePercentage = throttlePercentage;
    }

    public int getThrottlePercentage() {
        return this.throttlePercentage;
    }

    public int getProduction() {
        return this.steamProduction;
    }

    @Override
    public long heat() {
        return this.currentTemperature;
    }

    @Override
    public long maxHeat() {
        return this.handler.getMaxTemperature();
    }

    @Override
    public List<ItemStack> getItemInputs() {
        return this.itemInput;
    }

    @Override
    public List<ItemStack> getItemOutputs() {
        return this.itemOutput;
    }

    @Override
    public List<FluidStack> getFluidInputs() {
        return this.fluidInput;
    }

    @Override
    public List<FluidStack> getFluidOutputs() {
        return this.fluidOutput;
    }
}
