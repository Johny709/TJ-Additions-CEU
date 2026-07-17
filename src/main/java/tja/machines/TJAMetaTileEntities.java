package tja.machines;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.util.ResourceLocation;
import tja.TJA;
import tja.machines.multiblocks.MetaTileEntityMegaCokeOven;

public class TJAMetaTileEntities {

    public static final MetaTileEntity MEGA_COKE_OVEN = MetaTileEntities.registerMetaTileEntity(0, new MetaTileEntityMegaCokeOven(resource("mega_coke_oven")));

    public static void init() {
        // call on initialization
    }

    private static ResourceLocation resource(String locale) {
        return new ResourceLocation(TJA.MOD_ID, locale);
    }
}
