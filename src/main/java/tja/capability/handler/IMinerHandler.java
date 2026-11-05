package tja.capability.handler;

import net.minecraftforge.fluids.FluidStack;

public interface IMinerHandler extends IMachineHandler {

    /**
     * Miner doesn't apply the hammer effect if fortune lvl < 1.
     * @return fortune lvl
     */
    int getFortuneLvl();

    int getDiameter();

    FluidStack getDrillingFluid();
}
