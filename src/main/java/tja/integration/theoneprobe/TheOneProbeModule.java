package tja.integration.theoneprobe;

import mcjty.theoneprobe.TheOneProbe;
import mcjty.theoneprobe.api.ITheOneProbe;
import tja.TJAValues;
import tja.integration.theoneprobe.impl.ElementFluidList;
import tja.integration.theoneprobe.impl.ElementFluidStack;
import tja.integration.theoneprobe.impl.ElementItemList;
import tja.integration.theoneprobe.impl.ElementTJAText;
import tja.integration.theoneprobe.providers.TJARecipeInfoProvider;

public final class TheOneProbeModule {

    public static int ELEMENT_FLUIDSTACK;
    public static int ELEMENT_TJA_TEXT;
    public static int ELEMENT_ITEMLIST;
    public static int ELEMENT_FLUIDLIST;

    public static void registerElements() {
        ELEMENT_FLUIDSTACK = TheOneProbe.theOneProbeImp.registerElementFactory(ElementFluidStack::new);
        ELEMENT_TJA_TEXT = TheOneProbe.theOneProbeImp.registerElementFactory(ElementTJAText::new);
        ELEMENT_ITEMLIST = TheOneProbe.theOneProbeImp.registerElementFactory(ElementItemList::new);
        ELEMENT_FLUIDLIST = TheOneProbe.theOneProbeImp.registerElementFactory(ElementFluidList::new);
    }

    public static void init() {
        final ITheOneProbe probe = TheOneProbe.theOneProbeImp;
        if (TJAValues.isModLoaded(TJAValues.GREGTECH_MOD_ID))
            probe.registerProvider(new TJARecipeInfoProvider());
    }
}
