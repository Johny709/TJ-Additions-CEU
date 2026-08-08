package tja.capability;

import gregtech.api.capability.SimpleCapabilityManager;

public final class TJASimpleCapabilityManager {

    public static void init() {
        SimpleCapabilityManager.registerCapabilityWithNoDefault(IHeatInfo.class);
        SimpleCapabilityManager.registerCapabilityWithNoDefault(IRecipeInfo.class);
    }
}
