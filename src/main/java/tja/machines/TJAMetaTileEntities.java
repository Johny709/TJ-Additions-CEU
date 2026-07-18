package tja.machines;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.common.metatileentities.multi.BoilerType;
import net.minecraft.util.ResourceLocation;
import tja.TJA;
import tja.machines.multiblocks.MetaTileEntityMegaBoiler;
import tja.machines.multiblocks.MetaTileEntityMegaCokeOven;

public class TJAMetaTileEntities {

    public static final MetaTileEntity MEGA_COKE_OVEN = MetaTileEntities.registerMetaTileEntity(0, new MetaTileEntityMegaCokeOven(resource("mega_coke_oven")));
    public static final MetaTileEntity MEGA_BRONZE_BOILER = MetaTileEntities.registerMetaTileEntity(1, new MetaTileEntityMegaBoiler(resource("mega_bronze_boiler"), BoilerType.BRONZE));
    public static final MetaTileEntity MEGA_STEEL_BOILER = MetaTileEntities.registerMetaTileEntity(2, new MetaTileEntityMegaBoiler(resource("mega_steel_boiler"), BoilerType.STEEL));
    public static final MetaTileEntity MEGA_TITANIUM_BOILER = MetaTileEntities.registerMetaTileEntity(3, new MetaTileEntityMegaBoiler(resource("mega_titanium_boiler"), BoilerType.TITANIUM));
    public static final MetaTileEntity MEGA_TUNGSTENSTEEL_BOILER = MetaTileEntities.registerMetaTileEntity(4, new MetaTileEntityMegaBoiler(resource("mega_tungstensteel_boiler"), BoilerType.TUNGSTENSTEEL));

    public static void init() {
        // call on initialization
    }

    private static ResourceLocation resource(String locale) {
        return new ResourceLocation(TJA.MOD_ID, locale);
    }
}
