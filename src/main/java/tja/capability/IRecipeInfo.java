package tja.capability;

public interface IRecipeInfo extends IItemFluidHandlerInfo {

    boolean hasProblem();

    long getEnergyPerTick();
}
