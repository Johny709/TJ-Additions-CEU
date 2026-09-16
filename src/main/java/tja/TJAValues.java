package tja;

import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;

import java.text.DecimalFormat;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class TJAValues {

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
    public static final int[] VC = {0x6b5f55, 0x9b9b9b, 0xf78b2c, 0xffb900, 0x656565, 0xffffff, 0xffb0b0, 0xffe9e9, 0xc6ffad, 0xffc4f8, 0x2776ff, 0xf0ff00, 0x9500cd, 0xff6565, 0xfff7f7};
    public static final String[] VCC = {"§c", "§7", "§6", "§e", "§8", "§a", "§d", "§b", "§2", "§9", "§5", "§1§l", "§c§l§n", "§4§l§n", "§f§l§n"};
    public static final BlockPos DUMMY_POS = new BlockPos(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
    public static final IFluidTank DUMMY_TANK = new FluidTank(0);
    public static final DecimalFormat thousandFormat = new DecimalFormat(",###");
    public static final DecimalFormat thousandTwoPlaceFormat = new DecimalFormat(",##0.00");
    public static final String[] LOCKING_MODE_TOOLTIP_TITLE = new String[]{"gui.tooltips.appliedenergistics2.LockCraftingMode", "gui.tooltips.appliedenergistics2.LockCraftingMode", "gui.tooltips.appliedenergistics2.LockCraftingMode", "gui.tooltips.appliedenergistics2.LockCraftingMode", "gui.tooltips.appliedenergistics2.LockCraftingMode"};
    public static final String[] LOCKING_MODE_TOOLTIP_DESCRIPTION = new String[]{"gui.tooltips.appliedenergistics2.LockCraftingModeNone", "gui.tooltips.appliedenergistics2.LockCraftingUntilRedstonePulse", "gui.tooltips.appliedenergistics2.LockCraftingWhileRedstoneHigh", "gui.tooltips.appliedenergistics2.LockCraftingWhileRedstoneLow", "gui.tooltips.appliedenergistics2.LockCraftingUntilResultReturned"};
    private static final ConcurrentMap<String, Boolean> IS_MOD_LOADED_CACHE = new ConcurrentHashMap<>();

    private TJAValues() {}

    public static boolean isModLoaded(String modid) {
        if (IS_MOD_LOADED_CACHE.containsKey(modid)) {
            return IS_MOD_LOADED_CACHE.get(modid);
        }
        boolean isLoaded = Loader.instance().getIndexedModList().containsKey(modid);
        IS_MOD_LOADED_CACHE.put(modid, isLoaded);
        return isLoaded;
    }
}
