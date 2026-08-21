package tja.machines;

import com.google.common.collect.ImmutableList;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.common.metatileentities.multi.BoilerType;
import net.minecraft.util.ResourceLocation;
import tja.TJA;
import tja.machines.multiblocks.MetaTileEntityIndustrialFusionReactor;
import tja.machines.multiblocks.MetaTileEntityMegaBoiler;
import tja.machines.multiblocks.MetaTileEntityMegaCokeOven;
import tja.machines.singleblocks.MetaTileEntityCompressedChest;
import tja.machines.singleblocks.MetaTileEntityCompressedCrate;
import tja.machines.singleblocks.MetaTileEntityFluidSampler;

import java.util.List;
import java.util.function.IntFunction;

public final class TJAMetaTileEntities {

    public static final MetaTileEntity MEGA_COKE_OVEN = MetaTileEntities.registerMetaTileEntity(0, new MetaTileEntityMegaCokeOven(resource("mega_coke_oven")));
    public static final MetaTileEntity MEGA_BRONZE_BOILER = MetaTileEntities.registerMetaTileEntity(1, new MetaTileEntityMegaBoiler(resource("mega_bronze_boiler"), BoilerType.BRONZE));
    public static final MetaTileEntity MEGA_STEEL_BOILER = MetaTileEntities.registerMetaTileEntity(2, new MetaTileEntityMegaBoiler(resource("mega_steel_boiler"), BoilerType.STEEL));
    public static final MetaTileEntity MEGA_TITANIUM_BOILER = MetaTileEntities.registerMetaTileEntity(3, new MetaTileEntityMegaBoiler(resource("mega_titanium_boiler"), BoilerType.TITANIUM));
    public static final MetaTileEntity MEGA_TUNGSTENSTEEL_BOILER = MetaTileEntities.registerMetaTileEntity(4, new MetaTileEntityMegaBoiler(resource("mega_tungstensteel_boiler"), BoilerType.TUNGSTENSTEEL));
    public static final MetaTileEntity INDUSTRIAL_FUSION_REACTOR_MK1 = MetaTileEntities.registerMetaTileEntity(5, new MetaTileEntityIndustrialFusionReactor(resource("industrial_fusion_reactor.mk1"), GTValues.LuV));
    public static final MetaTileEntity INDUSTRIAL_FUSION_REACTOR_MK2 = MetaTileEntities.registerMetaTileEntity(6, new MetaTileEntityIndustrialFusionReactor(resource("industrial_fusion_reactor.mk2"), GTValues.ZPM));
    public static final MetaTileEntity INDUSTRIAL_FUSION_REACTOR_MK3 = MetaTileEntities.registerMetaTileEntity(7, new MetaTileEntityIndustrialFusionReactor(resource("industrial_fusion_reactor.mk3"), GTValues.UV));

    public static final MetaTileEntity COMPRESSED_CHEST = MetaTileEntities.registerMetaTileEntity(2000, new MetaTileEntityCompressedChest(resource("compressed_chest"), false));
    public static final MetaTileEntity COMPRESSED_CRATE = MetaTileEntities.registerMetaTileEntity(2001, new MetaTileEntityCompressedCrate(resource("compressed_crate"), false));
    public static final MetaTileEntity INFINITY_CHEST = MetaTileEntities.registerMetaTileEntity(2002, new MetaTileEntityCompressedChest(resource("infinity_chest"), true));
    public static final MetaTileEntity INFINITY_CRATE = MetaTileEntities.registerMetaTileEntity(2003, new MetaTileEntityCompressedCrate(resource("infinity_crate"), true));

    public static final List<MetaTileEntity> LARGE_BOILERS = ImmutableList.of(MetaTileEntities.LARGE_BRONZE_BOILER, MetaTileEntities.LARGE_STEEL_BOILER, MetaTileEntities.LARGE_TITANIUM_BOILER, MetaTileEntities.LARGE_TUNGSTENSTEEL_BOILER);
    public static final List<MetaTileEntity> MEGA_BOILERS = ImmutableList.of(MEGA_BRONZE_BOILER, MEGA_STEEL_BOILER, MEGA_TITANIUM_BOILER, MEGA_TUNGSTENSTEEL_BOILER);
    /** occupies id range 1000 - 1014 */
    public static final List<MetaTileEntity> FLUID_SAMPLERS = mteList(1000, i -> new MetaTileEntityFluidSampler(resource("fluid_sampler." + GTValues.VN[i]), i));

    public static void init() {
        // call on initialization
        // only works if there's no exceptions or errors.
    }

    private static List<MetaTileEntity> mteList(int startId, IntFunction<MetaTileEntity> metaTileEntityIntFunction) {
        return mteList(startId, 15, metaTileEntityIntFunction);
    }

    private static List<MetaTileEntity> mteList(int startId, int end, IntFunction<MetaTileEntity> metaTileEntityIntFunction) {
        final ImmutableList.Builder<MetaTileEntity> builder = ImmutableList.builder();
        for (int i = 0; i < end; i++) {
            final MetaTileEntity metaTileEntity = metaTileEntityIntFunction.apply(i);
            builder.add(MetaTileEntities.registerMetaTileEntity(startId + i, metaTileEntity));
        }
        return builder.build();
    }

    private static ResourceLocation resource(String locale) {
        return new ResourceLocation(TJA.MOD_ID, locale);
    }
}
