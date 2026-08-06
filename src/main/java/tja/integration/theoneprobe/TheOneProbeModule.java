package tja.integration.theoneprobe;

import mcjty.theoneprobe.TheOneProbe;
import mcjty.theoneprobe.api.ITheOneProbe;
import tja.integration.theoneprobe.impl.ElementFluidStack;

public class TheOneProbeModule {

    public static int ELEMENT_FLUIDSTACK;

    public static void registerElements() {
        ELEMENT_FLUIDSTACK = TheOneProbe.theOneProbeImp.registerElementFactory(ElementFluidStack::new);
    }

    public static void init() {
        ITheOneProbe probe = TheOneProbe.theOneProbeImp;
        probe.registerProvider(new TJARecipeInfoProvider());
    }
}
