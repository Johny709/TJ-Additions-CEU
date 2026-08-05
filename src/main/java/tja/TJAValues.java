package tja;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.items.IItemHandlerModifiable;
import tja.capability.workables.BasicEnergyHandler;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TJAValues {

    public static final String GREGTECH_MOD_ID = "gregtech";
    public static final String AE2_MOD_ID = "appliedenergistics2";
    public static final String GCYL_MOD_ID = "gcyl";
    public static final String THEONEPROBE_MOD_ID = "theoneprobe";
    public static final BlockPos DUMMY_POS = new BlockPos(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
    public static final IFluidTank DUMMY_TANK = new FluidTank(0);
    public static final BasicEnergyHandler DUMMY_ENERGY = new BasicEnergyHandler(0);
    public static final IMultipleTankHandler DUMMY_FLUID_HANDLER = new FluidTankList(true);
    public static final IItemHandlerModifiable DUMMY_ITEM_HANDLER = new ItemHandlerList(Collections.emptyList());
    public static final DecimalFormat thousandFormat = new DecimalFormat(",###");
    public static final DecimalFormat thousandTwoPlaceFormat = new DecimalFormat(",##0.00");
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
