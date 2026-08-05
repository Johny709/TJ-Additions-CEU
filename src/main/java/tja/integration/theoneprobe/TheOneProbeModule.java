package tja.integration.theoneprobe;

import mcjty.theoneprobe.TheOneProbe;
import mcjty.theoneprobe.api.ITheOneProbe;

public class TheOneProbeModule {

    public static void init() {
        ITheOneProbe probe = TheOneProbe.theOneProbeImp;
        probe.registerProvider(new TJARecipeInfoProvider());
    }
}
