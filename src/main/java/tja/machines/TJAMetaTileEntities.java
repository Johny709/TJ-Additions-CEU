package tja.machines;

import com.google.common.collect.ImmutableList;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.common.metatileentities.multi.BoilerType;
import net.minecraft.util.ResourceLocation;
import tja.TJA;
import tja.TJAValues;
import tja.machines.multiblocks.*;
import tja.machines.singleblocks.*;
import tja.multiblockpart.*;

import java.util.Collections;
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
    public static final MetaTileEntity INFINITE_FLUID_DRILL = MetaTileEntities.registerMetaTileEntity(10, new MetaTileEntityInfiniteFluidDrill(resource("infinite_fluid_drill")));
    public static final MetaTileEntity VOID_MORE_MINER = MetaTileEntities.registerMetaTileEntity(11, new MetaTileEntityVoidMOreMiner(resource("void_more_miner")));
    public static final MetaTileEntity LARGE_ARCHITECT_WORKBENCH = MetaTileEntities.registerMetaTileEntity(12, new MetaTileEntityLargeArchitectWorkbench(resource("large_architect_workbench")));
    public static final MetaTileEntity LARGE_CHISEL_WORKBENCH = MetaTileEntities.registerMetaTileEntity(13, new MetaTileEntityLargeChiselWorkbench(resource("large_chisel_workbench")));
    public static final MetaTileEntity LARGE_ENCHANTER = MetaTileEntities.registerMetaTileEntity(14, new MetaTileEntityLargeEnchanter(resource("large_enchanter")));
    public static final MetaTileEntity LARGE_NAMING_MACHINE = MetaTileEntities.registerMetaTileEntity(15, new MetaTileEntityLargeNamingMachine(resource("large_naming_machine")));
    public static final MetaTileEntity LARGE_SUPER_FISHER = MetaTileEntities.registerMetaTileEntity(16, new MetaTileEntityLargeSuperFisher(resource("large_super_fisher")));
    public static final MetaTileEntity LARGE_CRAFTER = MetaTileEntities.registerMetaTileEntity(17, new MetaTileEntityLargeCrafter(resource("large_crafter")));

    /** occupies id range 1000 - 1014 */
    public static final List<MetaTileEntity> FLUID_SAMPLERS = mteList(1000, i -> new MetaTileEntityFluidSampler(resource("fluid_sampler." + GTValues.VN[i]), i));
    /** occupies id range 1015 - 1029 */
    public static final List<MetaTileEntity> ARCHITECT_WORKBENCHES = !TJAValues.isModLoaded(TJAValues.ARCHITECTURE_CRAFT_MOD_ID) ? Collections.emptyList() :
            mteList(1015, i -> new MetaTileEntityArchitectWorkbench(resource("architect_workbench." + GTValues.VN[i]), i));
    /** occupies id range 1030 - 1044 */
    public static final List<MetaTileEntity> CHISEL_WORKBENCHES = !TJAValues.isModLoaded(TJAValues.CHISEL_MOD_ID) ? Collections.emptyList() :
            mteList(1030, i -> new MetaTileEntityChiselWorkbench(resource("chisel_workbench." + GTValues.VN[i]), i));
    /** occupies id range 1045 - 1059 */
    public static final List<MetaTileEntity> ENCHANTERS = mteList(1045, i -> new MetaTileEntityEnchanter(resource("enchanter." + GTValues.VN[i]), i));
    /** occupies id range 1060 - 1074 */
    public static final List<MetaTileEntity> NAMING_MACHINES = mteList(1060, i -> new MetaTileEntityNamingMachine(resource("naming_machine." + GTValues.VN[i]), i));
    /** occupies id range 1075 - 1089 */
    public static final List<MetaTileEntity> SUPER_FISHERS = mteList(1075, i -> new MetaTileEntitySuperFisher(resource("super_fisher." + GTValues.VN[i]), i));
    /** occupies id range 1090 - 1104 */
    public static final List<MetaTileEntity> CRAFTERS = mteList(1090, i -> new MetaTileEntityCrafter(resource("crafter." + GTValues.VN[i]), i));
    /** occupies id range 1105 - 1119 */
    public static final List<MetaTileEntity> FARMING_STATION = mteList(1105, i -> new MetaTileEntityFarmingStation(resource("farming_station." + GTValues.VN[i]), i));
    /** occupies id range 1120 - 1134 */
    public static final List<MetaTileEntity> ADVANCED_CHUNK_MINERS = mteList(1120, i -> new MetaTileEntityAdvancedChunkMiner(resource("advanced_chunk_miner." + GTValues.VN[i]), i));

    public static final MetaTileEntity COMPRESSED_CHEST = MetaTileEntities.registerMetaTileEntity(2000, new MetaTileEntityCompressedChest(resource("compressed_chest"), false));
    public static final MetaTileEntity COMPRESSED_CRATE = MetaTileEntities.registerMetaTileEntity(2001, new MetaTileEntityCompressedCrate(resource("compressed_crate"), false));
    public static final MetaTileEntity INFINITY_CHEST = MetaTileEntities.registerMetaTileEntity(2002, new MetaTileEntityCompressedChest(resource("infinity_chest"), true));
    public static final MetaTileEntity INFINITY_CRATE = MetaTileEntities.registerMetaTileEntity(2003, new MetaTileEntityCompressedCrate(resource("infinity_crate"), true));

    public static final MetaTileEntity CREATIVE_ITEM_BUS = MetaTileEntities.registerMetaTileEntity(3000, new MetaTileEntityCreativeItemBus(resource("creative_item_bus")));
    public static final MetaTileEntity CREATIVE_FLUID_HATCH = MetaTileEntities.registerMetaTileEntity(3001, new MetaTileEntityCreativeFluidHatch(resource("creative_fluid_hatch")));
    public static final MetaTileEntity CREATIVE_ENERGY_HATCH = MetaTileEntities.registerMetaTileEntity(3002, new MetaTileEntityCreativeEnergyHatch(resource("creative_energy_hatch")));
    public static final MetaTileEntity WATER_RESERVOIR_HATCH = MetaTileEntities.registerMetaTileEntity(3003, new MetaTileEntityWaterReservoirHatch(resource("water_reservoir_hatch")));

    /** occupies id range 3010 - 3024 */
    public static final List<MetaTileEntity> CRAFTER_HATCHES = mteList(3010, i -> new MetaTileEntityCrafterHatch(resource("crafter_hatch." + GTValues.VN[i]), i));
    /** occupies id range 3025 - 3039 */
    public static final List<MetaTileEntity> SUPER_INPUT_BUSES = mteList(3025, i -> new MetaTileEntitySuperItemBus(resource("super_input_bus." + GTValues.VN[i]), i, false));
    /** occupies id range 3040 - 3054 */
    public static final List<MetaTileEntity> SUPER_OUTPUT_BUSES = mteList(3040, i -> new MetaTileEntitySuperItemBus(resource("super_output_bus." + GTValues.VN[i]), i, true));
    /** occupies id range 3055 - 3069 */
    public static final List<MetaTileEntity> SUPER_INPUT_HATCHES = mteList(3055, i -> new MetaTileEntitySuperFluidHatch(resource("super_input_hatch." + GTValues.VN[i]), i, false));
    /** occupies id range 3070 - 3084 */
    public static final List<MetaTileEntity> SUPER_OUTPUT_HATCHES = mteList(3070, i -> new MetaTileEntitySuperFluidHatch(resource("super_output_hatch." + GTValues.VN[i]), i, true));

    public static final List<MetaTileEntity> LARGE_BOILERS = ImmutableList.of(MetaTileEntities.LARGE_BRONZE_BOILER, MetaTileEntities.LARGE_STEEL_BOILER, MetaTileEntities.LARGE_TITANIUM_BOILER, MetaTileEntities.LARGE_TUNGSTENSTEEL_BOILER);
    public static final List<MetaTileEntity> MEGA_BOILERS = ImmutableList.of(MEGA_BRONZE_BOILER, MEGA_STEEL_BOILER, MEGA_TITANIUM_BOILER, MEGA_TUNGSTENSTEEL_BOILER);

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
