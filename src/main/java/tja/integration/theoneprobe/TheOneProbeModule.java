package tja.integration.theoneprobe;

import mcjty.theoneprobe.TheOneProbe;
import mcjty.theoneprobe.api.ITheOneProbe;
import tja.TJAValues;
import tja.integration.theoneprobe.impl.ElementFluidStack;

public class TheOneProbeModule {

    public static int ELEMENT_FLUIDSTACK;

    public static void registerElements() {
        ELEMENT_FLUIDSTACK = TheOneProbe.theOneProbeImp.registerElementFactory(ElementFluidStack::new);
    }

    public static void init() {
        final ITheOneProbe probe = TheOneProbe.theOneProbeImp;
        if (TJAValues.isModLoaded(TJAValues.GREGTECH_MOD_ID))
            probe.registerProvider(new TJARecipeInfoProvider());
    }
}
