package tja;

import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;

import java.text.DecimalFormat;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TJAValues {

    public static final String GREGTECH_MOD_ID = "gregtech";
    public static final String AE2_MOD_ID = "appliedenergistics2";
    public static final String GCYL_MOD_ID = "gcyl";
    public static final String THEONEPROBE_MOD_ID = "theoneprobe";
    public static final String SUPERCRITICAL_MOD_ID = "supercritical";
    public static final String AE2FC_MOD_ID = "ae2fc";
    public static final String NAE2_MOD_ID = "nae2";
    public static final String RANDOM_COMPLEMENT_MOD_ID = "random_complement";
    public static final String BAUBLES_MOD_ID = "baubles";
    public static final String ACTUALLY_ADDITIONS_MOD_ID = "actuallyadditions";
    public static final BlockPos DUMMY_POS = new BlockPos(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
    public static final IFluidTank DUMMY_TANK = new FluidTank(0);
    public static final DecimalFormat thousandFormat = new DecimalFormat(",###");
    public static final DecimalFormat thousandTwoPlaceFormat = new DecimalFormat(",##0.00");
    public static final String[] LOCKING_MODE_TOOLTIP_TITLE = new String[]{"gui.tooltips.appliedenergistics2.LockCraftingMode", "gui.tooltips.appliedenergistics2.LockCraftingMode", "gui.tooltips.appliedenergistics2.LockCraftingMode", "gui.tooltips.appliedenergistics2.LockCraftingMode", "gui.tooltips.appliedenergistics2.LockCraftingMode"};
    public static final String[] LOCKING_MODE_TOOLTIP_DESCRIPTION = new String[]{"gui.tooltips.appliedenergistics2.LockCraftingModeNone", "gui.tooltips.appliedenergistics2.LockCraftingUntilRedstonePulse", "gui.tooltips.appliedenergistics2.LockCraftingWhileRedstoneHigh", "gui.tooltips.appliedenergistics2.LockCraftingWhileRedstoneLow", "gui.tooltips.appliedenergistics2.LockCraftingUntilResultReturned"};
    private static final ConcurrentMap<String, Boolean> IS_MOD_LOADED_CACHE = new ConcurrentHashMap<>();

    public static boolean isModLoaded(String modid) {
        if (IS_MOD_LOADED_CACHE.containsKey(modid)) {
            return IS_MOD_LOADED_CACHE.get(modid);
        }
        boolean isLoaded = Loader.instance().getIndexedModList().containsKey(modid);
        IS_MOD_LOADED_CACHE.put(modid, isLoaded);
        return isLoaded;
    }

    public static boolean isClientSide() {
        return FMLCommonHandler.instance().getSide().isClient();
    }
}
