package tja;

import net.minecraftforge.common.config.Config;

@Config(modid = TJA.MOD_ID)
public final class TJAConfig {

    @Config.Name("Modids")
    @Config.Comment("Modids to check if that mod is loaded. If not, then forces a crash.")
    @Config.RequiresMcRestart
    public static String[] modids = new String[0];

    @Config.Name("Singleblock 64B fluid slots")
    @Config.Comment("All fluid slots of singleblocks can hold up to 64,000L.")
    @Config.RequiresMcRestart
    public static boolean singleblock64B;

    @Config.Name("LCR Coil Bonus")
    @Config.Comment("enable coil bonus for Large Chemical Reactor (LCR). 5% energy discount every tier higher than cupronickel.")
    @Config.RequiresMcRestart
    public static boolean enableLCRBonus;

    @Config.Comment("Large Architect's Workbench")
    public static LargeArchitectWorkbench largeArchitectWorkbench = new LargeArchitectWorkbench();

    public static class LargeArchitectWorkbench {

        @Config.Name("EU/t Percentage")
        @Config.RequiresMcRestart
        public int eutPercentage = 90;

        @Config.Name("Duration Percentage")
        @Config.RequiresMcRestart
        public int durationPercentage = 80;

        @Config.Name("Chance Percentage")
        @Config.RequiresMcRestart
        public int chancePercentage = 100;

        @Config.Name("Stack Size")
        @Config.RequiresMcRestart
        public int stack = 16;

        @Config.Name("Slice Limit")
        @Config.Comment("Adjust the maximum number of slices the Large Architect Workbench can have")
        @Config.RequiresMcRestart
        public int maximumSlices = 64;
    }
}
