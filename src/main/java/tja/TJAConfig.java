package tja;

import net.minecraftforge.common.config.Config;

@Config(modid = TJA.MOD_ID)
public final class TJAConfig {

    @Config.Name("Modids")
    @Config.Comment("Modids to check if that mod is loaded. If not, then forces a crash.")
    @Config.RequiresMcRestart
    public static String[] modids = new String[0];

    @Config.Name("LCR Coil Bonus")
    @Config.Comment("enable coil bonus for Large Chemical Reactor (LCR). 5% energy discount every tier higher than cupronickel.")
    @Config.RequiresMcRestart
    public static boolean enableLCRBonus;
}
