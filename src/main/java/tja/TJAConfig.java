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

    @Config.Comment("Large Chisel Workbench")
    public static LargeChiselWorkbench largeChiselWorkbench = new LargeChiselWorkbench();

    @Config.Comment("Large Chisel Workbench")
    public static LargeEnchanter largeEnchanter = new LargeEnchanter();

    @Config.Comment("Large Naming Machine")
    public static LargeNamingMachine largeNamingMachine = new LargeNamingMachine();

    @Config.Comment("Large Super Fisher")
    public static LargeSuperFisher largeSuperFisher = new LargeSuperFisher();

    @Config.Comment("Large Crafter")
    public static LargeCrafter largeCrafter = new LargeCrafter();

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

    public static class LargeChiselWorkbench {

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
        @Config.Comment("Adjust the maximum number of slices the Large Chisel Workbench can have")
        @Config.RequiresMcRestart
        public int maximumSlices = 64;
    }

    public static class LargeEnchanter {

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
    }

    public static class LargeNamingMachine {
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
    }

    public static class LargeSuperFisher {
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
    }

    public static class LargeCrafter {
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
    }
}
