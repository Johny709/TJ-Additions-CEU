package tja.recipes;

import appeng.core.Api;
import com.fulltrix.gcyl.item.GCYLCoreItems;
import com.fulltrix.gcyl.materials.GCYLMaterials;
import gregtech.api.GTValues;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.ingredients.GTRecipeItemInput;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.commons.lang3.ArrayUtils;
import supercritical.api.unification.material.SCMaterials;
import tja.TJAValues;
import tja.blocks.BlockBatteryCell;
import tja.blocks.BlockTieredGlass;
import tja.blocks.TJAAE2Blocks;
import tja.blocks.TJAMetaBlocks;
import tja.items.TJAAE2Items;
import tja.items.TJAMetaItems;
import tja.machines.TJAMetaTileEntities;
import tja.util.TJAItemUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GTRecipes {

    public static void init(IForgeRegistry<IRecipe> recipes) {
        final boolean isAE2Loaded = TJAValues.isModLoaded(TJAValues.AE2_MOD_ID);
        final boolean isNAE2Loaded = TJAValues.isModLoaded(TJAValues.NAE2_MOD_ID);
        final boolean isGregicalityLoaded = TJAValues.isModLoaded(TJAValues.GCYL_MOD_ID);
        final boolean isSuperCriticalLoaded = TJAValues.isModLoaded(TJAValues.SUPERCRITICAL_MOD_ID);
        final boolean isActuallyAdditionsLoaded = TJAValues.isModLoaded(TJAValues.ACTUALLY_ADDITIONS_MOD_ID);

        Material[] materials = {Materials.WroughtIron, Materials.Steel, Materials.Aluminium, Materials.StainlessSteel,
                Materials.Titanium, Materials.TungstenSteel, Materials.RhodiumPlatedPalladium, Materials.Duranium, Materials.Tritanium};
        Material[] cableMaterials = {Materials.RedAlloy, Materials.Tin, Materials.Copper, Materials.Gold,
                Materials.Aluminium, Materials.Platinum, Materials.Niobium, Materials.Naquadah, Materials.NaquadahAlloy};
        MetaItem<?>.MetaValueItem[] motors = {MetaItems.ELECTRIC_PISTON_LV, MetaItems.ELECTRIC_MOTOR_LV,
                MetaItems.ELECTRIC_MOTOR_MV, MetaItems.ELECTRIC_MOTOR_HV, MetaItems.ELECTRIC_MOTOR_EV, MetaItems.ELECTRIC_MOTOR_IV,
                MetaItems.ELECTRIC_MOTOR_LuV, MetaItems.ELECTRIC_MOTOR_ZPM, MetaItems.ELECTRIC_MOTOR_UV};
        MetaItem<?>.MetaValueItem[] pistons = {MetaItems.ELECTRIC_MOTOR_LV, MetaItems.ELECTRIC_PISTON_LV,
                MetaItems.ELECTRIC_PISTON_MV, MetaItems.ELECTRIC_PISTON_HV, MetaItems.ELECTRIC_PISTON_EV, MetaItems.ELECTRIC_PISTON_IV,
                MetaItems.ELECTRIC_PISTON_LUV, MetaItems.ELECTRIC_PISTON_ZPM, MetaItems.ELECTRIC_PISTON_UV};
        MetaItem<?>.MetaValueItem[] conveyors = {MetaItems.CONVEYOR_MODULE_LV, MetaItems.CONVEYOR_MODULE_LV, MetaItems.CONVEYOR_MODULE_MV,
                MetaItems.CONVEYOR_MODULE_HV, MetaItems.CONVEYOR_MODULE_EV, MetaItems.CONVEYOR_MODULE_IV, MetaItems.CONVEYOR_MODULE_LuV,
                MetaItems.CONVEYOR_MODULE_ZPM, MetaItems.CONVEYOR_MODULE_UV};
        MetaItem<?>.MetaValueItem[] pumps = {MetaItems.ELECTRIC_PUMP_LV, MetaItems.ELECTRIC_PUMP_LV, MetaItems.ELECTRIC_PUMP_MV,
                MetaItems.ELECTRIC_PUMP_HV, MetaItems.ELECTRIC_PUMP_EV, MetaItems.ELECTRIC_PUMP_IV, MetaItems.ELECTRIC_PUMP_LuV,
                MetaItems.ELECTRIC_PUMP_ZPM, MetaItems.ELECTRIC_PUMP_UV};
        MetaItem<?>.MetaValueItem[] robotArms = {MetaItems.ROBOT_ARM_LV, MetaItems.ROBOT_ARM_LV, MetaItems.ROBOT_ARM_MV, MetaItems.ROBOT_ARM_HV,
                MetaItems.ROBOT_ARM_EV, MetaItems.ROBOT_ARM_IV, MetaItems.ROBOT_ARM_LuV, MetaItems.ROBOT_ARM_ZPM, MetaItems.ROBOT_ARM_UV};
        MetaItem<?>.MetaValueItem[] sensors = {MetaItems.ROBOT_ARM_LV, MetaItems.ROBOT_ARM_LV, MetaItems.ROBOT_ARM_MV, MetaItems.ROBOT_ARM_HV,
                MetaItems.ROBOT_ARM_EV, MetaItems.ROBOT_ARM_IV, MetaItems.ROBOT_ARM_LuV, MetaItems.ROBOT_ARM_ZPM, MetaItems.ROBOT_ARM_UV};
        final Material[] circuitTiers = new Material[]{MarkerMaterials.Tier.ULV, MarkerMaterials.Tier.LV, MarkerMaterials.Tier.MV, MarkerMaterials.Tier.HV,
                MarkerMaterials.Tier.EV, MarkerMaterials.Tier.IV, MarkerMaterials.Tier.LuV, MarkerMaterials.Tier.ZPM, MarkerMaterials.Tier.UV,
                MarkerMaterials.Tier.UHV, MarkerMaterials.Tier.UEV, MarkerMaterials.Tier.UIV, MarkerMaterials.Tier.UXV, MarkerMaterials.Tier.OpV,
                MarkerMaterials.Tier.MAX};
        if (isGregicalityLoaded) {
            materials = ArrayUtils.addAll(materials, Materials.Seaborgium, Materials.Bohrium, GCYLMaterials.Quantum, GCYLMaterials.BlackTitanium,
                    GCYLMaterials.HeavyQuarkDegenerateMatter, Materials.Neutronium);
            cableMaterials = ArrayUtils.addAll(cableMaterials, GCYLMaterials.AbyssalAlloy, GCYLMaterials.TitanSteel, GCYLMaterials.BlackTitanium,
                    GCYLMaterials.NaquadriaticTaranium, Materials.Neutronium, GCYLMaterials.CosmicNeutronium);
            motors = ArrayUtils.addAll(motors, MetaItems.ELECTRIC_MOTOR_UHV, MetaItems.ELECTRIC_MOTOR_UEV, MetaItems.ELECTRIC_MOTOR_UIV,
                    MetaItems.ELECTRIC_MOTOR_UXV, MetaItems.ELECTRIC_MOTOR_OpV, GCYLCoreItems.ELECTRIC_MOTOR_MAX);
            pistons = ArrayUtils.addAll(pistons, MetaItems.ELECTRIC_PISTON_UHV, MetaItems.ELECTRIC_PISTON_UEV, MetaItems.ELECTRIC_PISTON_UIV,
                    MetaItems.ELECTRIC_PISTON_UXV, MetaItems.ELECTRIC_PISTON_OpV, GCYLCoreItems.ELECTRIC_PISTON_MAX);
            conveyors = ArrayUtils.addAll(conveyors, MetaItems.CONVEYOR_MODULE_UHV, MetaItems.CONVEYOR_MODULE_UEV, MetaItems.CONVEYOR_MODULE_UIV,
                    MetaItems.CONVEYOR_MODULE_UXV, MetaItems.CONVEYOR_MODULE_OpV, GCYLCoreItems.CONVEYOR_MODULE_MAX);
            pumps = ArrayUtils.addAll(pumps, MetaItems.ELECTRIC_PUMP_UHV, MetaItems.ELECTRIC_PUMP_UEV, MetaItems.ELECTRIC_PUMP_UIV,
                    MetaItems.ELECTRIC_PUMP_UXV, MetaItems.ELECTRIC_PUMP_OpV, GCYLCoreItems.ELECTRIC_PUMP_MAX);
            robotArms = ArrayUtils.addAll(robotArms, MetaItems.ROBOT_ARM_UHV, MetaItems.ROBOT_ARM_UEV, MetaItems.ROBOT_ARM_UIV, MetaItems.ROBOT_ARM_UXV,
                    MetaItems.ROBOT_ARM_OpV, GCYLCoreItems.ROBOT_ARM_MAX);
            sensors = ArrayUtils.addAll(sensors, MetaItems.ROBOT_ARM_UHV, MetaItems.ROBOT_ARM_UEV, MetaItems.ROBOT_ARM_UIV, MetaItems.ROBOT_ARM_UXV,
                    MetaItems.ROBOT_ARM_OpV, GCYLCoreItems.ROBOT_ARM_MAX);
        }
        // mega coke oven
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(MetaTileEntities.COKE_OVEN, 64)
                .input(MetaTileEntities.COKE_OVEN, 64)
                .input(MetaTileEntities.COKE_OVEN, 64)
                .input(MetaTileEntities.COKE_OVEN, 64)
                .output(TJAMetaTileEntities.MEGA_COKE_OVEN)
                .EUt(30).duration(1200)
                .buildAndRegister();
        // large architect workbench
        ModHandler.addShapedRecipe("large_architect_workbench", TJAMetaTileEntities.LARGE_ARCHITECT_WORKBENCH.getStackForm(),
                "GCG", "RSB", "GCG",
                'G', new UnificationEntry(OrePrefix.gear, Materials.StainlessSteel),
                'C', new UnificationEntry(OrePrefix.circuit, circuitTiers[5]),
                'R', robotArms[5].getStackForm(),
                'B', conveyors[5].getStackForm(),
                'S', TJAMetaTileEntities.ARCHITECT_WORKBENCHES.get(5).getStackForm());
        // large chisel workbench
        ModHandler.addShapedRecipe("large_chisel_workbench", TJAMetaTileEntities.LARGE_CHISEL_WORKBENCH.getStackForm(),
                "GCG", "RSB", "GCG",
                'G', new UnificationEntry(OrePrefix.gear, Materials.BlueSteel),
                'C', new UnificationEntry(OrePrefix.circuit, circuitTiers[5]),
                'R', robotArms[5].getStackForm(),
                'B', conveyors[5].getStackForm(),
                'S', TJAMetaTileEntities.CHISEL_WORKBENCHES.get(5).getStackForm());
        // large enchanter
        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(MetaTileEntities.HULL[8])
                .input(OrePrefix.plate, Materials.Diamond, 50)
                .input(OrePrefix.plate, Materials.Lapis, 60)
                .input(OrePrefix.circuit, circuitTiers[9], 10)
                .input(OrePrefix.screw, Materials.Tritanium, 64)
                .input(OrePrefix.gear, Materials.Dubnium, 10)
                .input(OrePrefix.cableGtSingle, Materials.NaquadahAlloy, 16)
                .input(MetaItems.ELECTRIC_PUMP_UV, 8)
                .input(MetaItems.ELECTRIC_MOTOR_UV, 4)
                .input(MetaItems.EMITTER_UV, 4)
                .fluidInputs(Materials.SolderingAlloy.getFluid(4608), Materials.Naquadria.getFluid(61140),
                        Materials.Lubricant.getFluid(16000))
                .output(TJAMetaTileEntities.LARGE_ENCHANTER)
                .stationResearch(stationRecipeBuilder -> stationRecipeBuilder.EUt(GTValues.VAOC[9])
                        .CWUt(128, 512000)
                        .researchStack(TJAMetaTileEntities.ENCHANTERS.get(8).getStackForm()))
                .EUt(GTValues.VAOC[8]).duration(600)
                .buildAndRegister();
        // large naming machine
        ModHandler.addShapedRecipe("large_naming_machine", TJAMetaTileEntities.LARGE_NAMING_MACHINE.getStackForm(),
                "GPG", "CHC", "GAG",
                'C', new UnificationEntry(OrePrefix.circuit, circuitTiers[5]),
                'P', pistons[5].getStackForm(),
                'G', new UnificationEntry(OrePrefix.gear, Materials.TungstenSteel),
                'H', TJAMetaTileEntities.NAMING_MACHINES.get(5).getStackForm(),
                'A', new ItemStack(Blocks.ANVIL));
        // large super fisher
        ModHandler.addShapedRecipe("large_super_fisher", TJAMetaTileEntities.LARGE_SUPER_FISHER.getStackForm(),
                "PRP", "DHD", "MCM",
                'P', pumps[6].getStackForm(),
                'R', robotArms[6].getStackForm(),
                'D', new UnificationEntry(OrePrefix.plateDense, Materials.RhodiumPlatedPalladium),
                'H', TJAMetaTileEntities.SUPER_FISHERS.get(6).getStackForm(),
                'M', motors[6].getStackForm(),
                'C', new UnificationEntry(OrePrefix.circuit, circuitTiers[6]));
        // large crafter
        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(OrePrefix.screw, Materials.Naquadah, 64)
                .input(OrePrefix.screw, Materials.HSSS, 64)
                .input(OrePrefix.plate, Materials.RhodiumPlatedPalladium, 48)
                .input(OrePrefix.plate, Materials.Osmiridium, 48)
                .input(OrePrefix.circuit, circuitTiers[8], 24)
                .input(OrePrefix.gear, Materials.HSSE, 16)
                .input(OrePrefix.gear, Materials.HSSG, 16)
                .input(TJAMetaTileEntities.CRAFTERS.get(7), 16)
                .inputs(new ItemStack(Blocks.CRAFTING_TABLE, 64))
                .input(MetaItems.ELECTRIC_PISTON_ZPM, 8)
                .input(MetaItems.CONVEYOR_MODULE_ZPM, 8)
                .input(MetaItems.ROBOT_ARM_ZPM, 8)
                .input(MetaItems.SENSOR_ZPM, 8)
                .input(MetaItems.EMITTER_ZPM, 8)
                .fluidInputs(Materials.SolderingAlloy.getFluid(4608))
                .fluidInputs(Materials.Lubricant.getFluid(16000))
                .output(TJAMetaTileEntities.LARGE_CRAFTER)
                .stationResearch(stationRecipeBuilder -> stationRecipeBuilder.EUt(GTValues.VAOC[7])
                        .CWUt(32, 1024000)
                        .researchStack(TJAMetaTileEntities.CRAFTERS.get(7)))
                .EUt(GTValues.VAOC[7]).duration(2000)
                .buildAndRegister();
        // mega boilers
        for (int i = 0; i <TJAMetaTileEntities.MEGA_BOILERS.size(); i++) {
            MetaTileEntity largeBoiler = TJAMetaTileEntities.LARGE_BOILERS.get(i);
            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .input(largeBoiler, 64)
                    .input(largeBoiler, 64)
                    .input(largeBoiler, 64)
                    .input(largeBoiler, 64)
                    .output(TJAMetaTileEntities.MEGA_BOILERS.get(i))
                    .EUt(GTValues.VAOC[i + (i == 0 ? 1 : 2)]).duration(1200)
                        .buildAndRegister();
        }
        // lv - max power cells
        final List<Material> powerCellPlates = new ArrayList<>(Arrays.asList(Materials.Iron, Materials.WroughtIron, Materials.Lead, Materials.Titanium,
                Materials.TungstenSteel, Materials.Iridium, Materials.NaquadahAlloy, Materials.Tritanium, Materials.Seaborgium));
        final List<Material> powerCellFluids = new ArrayList<>(Arrays.asList(Materials.Fluorine, Materials.Hydrogen, Materials.Oxygen, Materials.Nitrogen,
                Materials.Helium, Materials.Argon, Materials.Radon, Materials.Krypton, Materials.Xenon));
        if (isGregicalityLoaded) {
            powerCellPlates.addAll(Arrays.asList(Materials.Bohrium, GCYLMaterials.Adamantium, GCYLMaterials.Vibranium,
                    GCYLMaterials.HeavyQuarkDegenerateMatter, GCYLMaterials.ChaosAlloy));
            powerCellFluids.addAll(Arrays.asList(GCYLMaterials.FreeAlphaGas, GCYLMaterials.LiquidCrystalDetector, GCYLMaterials.HeavyLeptonMix,
                    GCYLMaterials.ElectronDegenerateRheniumPlasma, GCYLMaterials.QCDMatter));
        }
        for (int i = 0; i < BlockBatteryCell.CasingType.values().length; i++) {
            if (i > 8 && !isGregicalityLoaded) continue;
            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .input(OrePrefix.plateDouble, powerCellPlates.get(i), 4)
                    .inputs(i == 0 ? MetaBlocks.METAL_CASING.getItemVariant(BlockMetalCasing.MetalCasingType.STEEL_SOLID)
                            : TJAMetaBlocks.BATTERY_CELL.getItemVariant(BlockBatteryCell.CasingType.values()[i - 1]))
                    .fluidInputs(powerCellFluids.get(i).getFluid(16000))
                    .outputs(TJAMetaBlocks.BATTERY_CELL.getItemVariant(BlockBatteryCell.CasingType.values()[i]))
                    .EUt(15L << i * 2).duration(1200)
                    .buildAndRegister();
        }
        // fluid samplers
        for (int i = 0; i < TJAMetaTileEntities.FLUID_SAMPLERS.size(); i++) {
            if (i > 8 && !isGregicalityLoaded) break;
            ModHandler.addShapedRecipe("fluid_sampler." + GTValues.VN[i], TJAMetaTileEntities.FLUID_SAMPLERS.get(i).getStackForm(),
                    "GGG", "PHP", "CDC",
                    'G', TJAItemUtils.getItemStackFromName("minecraft:glass"),
                    'P', new UnificationEntry(OrePrefix.plate, materials[i]),
                    'H', MetaTileEntities.HULL[i].getStackForm(),
                    'C', new UnificationEntry(OrePrefix.cableGtSingle, cableMaterials[i]),
                    'D', new UnificationEntry(OrePrefix.toolHeadDrill, materials[i]));
        }
        // architect workbench
        for (int i = 0; i < TJAMetaTileEntities.ARCHITECT_WORKBENCHES.size(); i++) {
            if (i > 8 && !isGregicalityLoaded) break;
            ModHandler.addShapedRecipe("architech_workbench." + GTValues.VN[i], TJAMetaTileEntities.ARCHITECT_WORKBENCHES.get(i).getStackForm(),
                    "PAP", "CSC", "MWM",
                    'P', pistons[i].getStackForm(),
                    'A', TJAItemUtils.getItemStackFromName("architecturecraft:sawbench"),
                    'S', MetaTileEntities.HULL[i].getStackForm(),
                    'C', new UnificationEntry(OrePrefix.circuit, circuitTiers[i]),
                    'W', new UnificationEntry(OrePrefix.cableGtSingle, cableMaterials[i]),
                    'M', motors[i].getStackForm());
        }
        // chisel workbench
        for (int i = 0; i < TJAMetaTileEntities.CHISEL_WORKBENCHES.size(); i++) {
            if (i > 8 && !isGregicalityLoaded) break;
            ModHandler.addShapedRecipe("chisel_workbench." + GTValues.VN[i], TJAMetaTileEntities.CHISEL_WORKBENCHES.get(i).getStackForm(),
                    "CAC", "RSR", "GWG",
                    'R', conveyors[i].getStackForm(),
                    'A', TJAItemUtils.getItemStackFromName("chisel:auto_chisel"),
                    'S', MetaTileEntities.HULL[i].getStackForm(),
                    'C', new UnificationEntry(OrePrefix.circuit, circuitTiers[i]),
                    'W', new UnificationEntry(OrePrefix.cableGtSingle, cableMaterials[i]),
                    'G', new UnificationEntry(OrePrefix.gear, materials[i]));
        }
        // enchanter
        for (int i = 0; i < TJAMetaTileEntities.ENCHANTERS.size(); i++) {
            if (i > 8 && !isGregicalityLoaded) break;
            ModHandler.addShapedRecipe("enchanter." + GTValues.VN[i], TJAMetaTileEntities.ENCHANTERS.get(i).getStackForm(),
                    "CEC", "PSP", "DWD",
                    'C', new UnificationEntry(OrePrefix.circuit, circuitTiers[i]),
                    'E', new ItemStack(Blocks.ENCHANTING_TABLE),
                    'S', MetaTileEntities.HULL[i].getStackForm(),
                    'P', pumps[i].getStackForm(),
                    'D', new UnificationEntry(OrePrefix.block, Materials.Diamond),
                    'W', new UnificationEntry(OrePrefix.cableGtSingle, cableMaterials[i]));
        }
        // naming machine
        for (int i = 0; i < TJAMetaTileEntities.NAMING_MACHINES.size(); i++) {
            if (i > 8 && !isGregicalityLoaded) break;
            ModHandler.addShapedRecipe("naming_machine." + GTValues.VN[i], TJAMetaTileEntities.NAMING_MACHINES.get(i).getStackForm(),
                    "GPG", "CHC", "GAG",
                    'G', new UnificationEntry(OrePrefix.gearSmall, materials[i]),
                    'C', new UnificationEntry(OrePrefix.circuit, circuitTiers[i]),
                    'P', pistons[i].getStackForm(),
                    'H', MetaTileEntities.HULL[i].getStackForm(),
                    'A', new ItemStack(Blocks.ANVIL));
        }
        // super fisher
        for (int i = 0; i < TJAMetaTileEntities.SUPER_FISHERS.size(); i++) {
            if (i > 8 && !isGregicalityLoaded) break;
            ModHandler.addShapedRecipe("super_fisher." + GTValues.VN[i], TJAMetaTileEntities.SUPER_FISHERS.get(i).getStackForm(),
                    "FRF", "PHP", "ECE",
                    'F', TJAItemUtils.getItemStackFromName("minecraft:fishing_rod"),
                    'E', new UnificationEntry(OrePrefix.cableGtSingle, cableMaterials[i]),
                    'R', robotArms[i].getStackForm(),
                    'C', new UnificationEntry(OrePrefix.circuit, circuitTiers[i]),
                    'P', new UnificationEntry(OrePrefix.plate, materials[i]),
                    'H', MetaTileEntities.HULL[i].getStackForm());
        }
        // crafter
        for (int i = 0; i < TJAMetaTileEntities.CRAFTERS.size(); i++) {
            if (i > 8 && !isGregicalityLoaded) break;
            ModHandler.addShapedRecipe("crafter." + GTValues.VN[i], TJAMetaTileEntities.CRAFTERS.get(i).getStackForm(),
                    "CTC", "RSR", "EWE",
                    'C', conveyors[i].getStackForm(),
                    'T', new ItemStack(Blocks.CRAFTING_TABLE),
                    'R', robotArms[i].getStackForm(),
                    'S', MetaTileEntities.HULL[i].getStackForm(),
                    'E', new UnificationEntry(OrePrefix.circuit, circuitTiers[i]),
                    'W', new UnificationEntry(OrePrefix.cableGtSingle, cableMaterials[i]));
        }
        // crafter hatch
        for (int i = 0; i < TJAMetaTileEntities.CRAFTER_HATCHES.size(); i++) {
            if (i > 8 && !isGregicalityLoaded) break;
            ModHandler.addShapedRecipe("crafter_hatch." + GTValues.VN[i], TJAMetaTileEntities.CRAFTER_HATCHES.get(i).getStackForm(),
                    "CCC", "CHC", "CCC",
                    'C', new ItemStack(Blocks.CRAFTING_TABLE),
                    'H', TJAMetaTileEntities.CRAFTERS.get(i).getStackForm());
        }
        // farming station
        for (int i = 0; i < TJAMetaTileEntities.FARMING_STATION.size(); i++) {
            if (i > 8 && !isGregicalityLoaded) break;
            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .circuitMeta(0)
                    .input(OrePrefix.gear, materials[i], i >= GTValues.ZPM ? 16 : i >= GTValues.EV ? 8 : 4)
                    .input(OrePrefix.circuit, circuitTiers[i], 4)
                    .input(Items.DIAMOND_AXE)
                    .input(Items.DIAMOND_HOE)
                    .input(Items.SHEARS)
                    .input(robotArms[i], i >= GTValues.ZPM ? 4 : i >= GTValues.EV ? 2 : 1)
                    .input(MetaTileEntities.HULL[i])
                    .output(TJAMetaTileEntities.FARMING_STATION.get(i))
                    .EUt(GTValues.VA[i]).duration(400)
                    .buildAndRegister();
        }
        // advanced chunk miner
        for (int i = 0; i < TJAMetaTileEntities.ADVANCED_CHUNK_MINERS.size(); i++) {
            if (i > 8 && !isGregicalityLoaded) break;
            ModHandler.addShapedRecipe("advanced_chunk_miner." + GTValues.VN[i], TJAMetaTileEntities.ADVANCED_CHUNK_MINERS.get(i).getStackForm(),
                    "SCS", "RHR", "BCB",
                    'B', i > 4 ? MetaItems.COMPONENT_GRINDER_TUNGSTEN.getStackForm() : MetaItems.COMPONENT_GRINDER_DIAMOND.getStackForm(),
                    'C', new UnificationEntry(OrePrefix.circuit, circuitTiers[i]),
                    'S', sensors[i].getStackForm(),
                    'R', pistons[i].getStackForm(),
                    'H', MetaTileEntities.HULL[i].getStackForm());
        }
        // mixed metal ingot
        ModHandler.addShapedRecipe("mixed_metal_ingot.1", TJAMetaItems.MIXED_METAL_INGOT.getStackForm(1), "A", "B", "C",
                'A', Ingredient.fromStacks(OreDictUnifier.get(OrePrefix.plate, Materials.Iron), OreDictUnifier.get(OrePrefix.plate, Materials.Nickel)),
                'B', Ingredient.fromStacks(OreDictUnifier.get(OrePrefix.plate, Materials.Bronze), OreDictUnifier.get(OrePrefix.plate, Materials.Brass)),
                'C', Ingredient.fromStacks(OreDictUnifier.get(OrePrefix.plate, Materials.Tin), OreDictUnifier.get(OrePrefix.plate, Materials.Zinc),
                        OreDictUnifier.get(OrePrefix.plate, Materials.Aluminium)));
        ModHandler.addShapedRecipe("mixed_metal_ingot.2", TJAMetaItems.MIXED_METAL_INGOT.getStackForm(2), "A", "B", "C",
                'A', Ingredient.fromStacks(OreDictUnifier.get(OrePrefix.plate, Materials.Invar), OreDictUnifier.get(OrePrefix.plate, Materials.Steel)),
                'B', Ingredient.fromStacks(OreDictUnifier.get(OrePrefix.plate, Materials.Bronze), OreDictUnifier.get(OrePrefix.plate, Materials.Brass)),
                'C', Ingredient.fromStacks(OreDictUnifier.get(OrePrefix.plate, Materials.Tin), OreDictUnifier.get(OrePrefix.plate, Materials.Zinc),
                        OreDictUnifier.get(OrePrefix.plate, Materials.Aluminium)));
        ModHandler.addShapedRecipe("mixed_metal_ingot.3", TJAMetaItems.MIXED_METAL_INGOT.getStackForm(3), "A", "B", "C",
                'A', Ingredient.fromStacks(OreDictUnifier.get(OrePrefix.plate, Materials.StainlessSteel),
                        OreDictUnifier.get(OrePrefix.plate, Materials.Titanium)),
                'B', Ingredient.fromStacks(OreDictUnifier.get(OrePrefix.plate, Materials.Bronze), OreDictUnifier.get(OrePrefix.plate, Materials.Brass)),
                'C', Ingredient.fromStacks(OreDictUnifier.get(OrePrefix.plate, Materials.Tin), OreDictUnifier.get(OrePrefix.plate, Materials.Zinc),
                        OreDictUnifier.get(OrePrefix.plate, Materials.Aluminium)));
        ModHandler.addShapedRecipe("mixed_metal_ingot.4", TJAMetaItems.MIXED_METAL_INGOT.getStackForm(4), "A", "B", "C",
                'A', new UnificationEntry(OrePrefix.plate, Materials.Tungsten),
                'B', Ingredient.fromStacks(OreDictUnifier.get(OrePrefix.plate, Materials.Bronze), OreDictUnifier.get(OrePrefix.plate, Materials.Brass)),
                'C', Ingredient.fromStacks(OreDictUnifier.get(OrePrefix.plate, Materials.Tin), OreDictUnifier.get(OrePrefix.plate, Materials.Zinc),
                        OreDictUnifier.get(OrePrefix.plate, Materials.Aluminium)));
        ModHandler.addShapedRecipe("mixed_metal_ingot.5", TJAMetaItems.MIXED_METAL_INGOT.getStackForm(5), "A", "B", "C",
                'A', new UnificationEntry(OrePrefix.plate, Materials.TungstenSteel),
                'B', Ingredient.fromStacks(OreDictUnifier.get(OrePrefix.plate, Materials.Bronze), OreDictUnifier.get(OrePrefix.plate, Materials.Brass)),
                'C', Ingredient.fromStacks(OreDictUnifier.get(OrePrefix.plate, Materials.Tin), OreDictUnifier.get(OrePrefix.plate, Materials.Zinc),
                        OreDictUnifier.get(OrePrefix.plate, Materials.Aluminium)));
        RecipeMaps.FORMING_PRESS_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, Materials.Nickel)
                .input(OrePrefix.plate, Materials.Brass)
                .input(OrePrefix.plate, Materials.Aluminium)
                .output(TJAMetaItems.MIXED_METAL_INGOT, 2)
                .EUt(8).duration(40)
                .buildAndRegister();
        RecipeMaps.FORMING_PRESS_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, Materials.Steel)
                .input(OrePrefix.plate, Materials.Brass)
                .input(OrePrefix.plate, Materials.Aluminium)
                .output(TJAMetaItems.MIXED_METAL_INGOT, 4)
                .EUt(8).duration(80)
                .buildAndRegister();
        RecipeMaps.FORMING_PRESS_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, Materials.Titanium)
                .input(OrePrefix.plate, Materials.Brass)
                .input(OrePrefix.plate, Materials.Aluminium)
                .output(TJAMetaItems.MIXED_METAL_INGOT, 6)
                .EUt(8).duration(120)
                .buildAndRegister();
        RecipeMaps.FORMING_PRESS_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, Materials.Tungsten)
                .input(OrePrefix.plate, Materials.Brass)
                .input(OrePrefix.plate, Materials.Aluminium)
                .output(TJAMetaItems.MIXED_METAL_INGOT, 8)
                .EUt(8).duration(160)
                .buildAndRegister();
        RecipeMaps.FORMING_PRESS_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, Materials.TungstenSteel)
                .input(OrePrefix.plate, Materials.Brass)
                .input(OrePrefix.plate, Materials.Aluminium)
                .output(TJAMetaItems.MIXED_METAL_INGOT, 10)
                .EUt(8).duration(200)
                .buildAndRegister();
        // advanced alloy plate
        RecipeMaps.COMPRESSOR_RECIPES.recipeBuilder()
                .input(TJAMetaItems.MIXED_METAL_INGOT)
                .output(TJAMetaItems.ADVANCED_ALLOY_PLATE)
                .EUt(2).duration(400)
                .buildAndRegister();
        // tiered reinforced glass
        RecipeMaps.ALLOY_SMELTER_RECIPES.recipeBuilder()
                .input(OrePrefix.dust, Materials.Glass, 3)
                .input(TJAMetaItems.ADVANCED_ALLOY_PLATE)
                .outputs(TJAMetaBlocks.TIERED_GLASS.getItemVariant(BlockTieredGlass.CasingType.ULV, 4))
                .EUt(4).duration(400)
                .buildAndRegister();
        RecipeMaps.ALLOY_SMELTER_RECIPES.recipeBuilder()
                .input("blockGlass", 3)
                .input(TJAMetaItems.ADVANCED_ALLOY_PLATE)
                .outputs(TJAMetaBlocks.TIERED_GLASS.getItemVariant(BlockTieredGlass.CasingType.ULV, 4))
                .EUt(4).duration(400)
                .buildAndRegister();
        final BlockTieredGlass.CasingType[] glassCasingType = BlockTieredGlass.CasingType.values();
        for (int i = 1; i < glassCasingType.length; i++) {
            if (i > 8 && !isGregicalityLoaded) continue;
            RecipeMaps.FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
                    .inputs(TJAMetaBlocks.TIERED_GLASS.getItemVariant(glassCasingType[i - 1]))
                    .fluidInputs(glassCasingType[i].getMaterial().getFluid(144))
                    .outputs(TJAMetaBlocks.TIERED_GLASS.getItemVariant(glassCasingType[i]))
                    .EUt(4L << i * 2).duration(400)
                    .buildAndRegister();
        }
        if (isActuallyAdditionsLoaded) {
            // compressed chest
            ModHandler.addShapedRecipe("compressed_chest", TJAMetaTileEntities.COMPRESSED_CHEST.getStackForm(), "OCO", "PBP", "OCO",
                    'O', new UnificationEntry(OrePrefix.block, Materials.Obsidian),
                    'P', MetaItems.ELECTRIC_PISTON_MV.getStackForm(),
                    'C', TJAItemUtils.getItemStackFromName("actuallyadditions:block_giant_chest_large"),
                    'B', TJAItemUtils.getItemStackFromName("actuallyadditions:item_crate_keeper"));
            // compressed crate
            ModHandler.addShapedRecipe("compressed_crate", TJAMetaTileEntities.COMPRESSED_CRATE.getStackForm(), "OPO", "CBC", "OPO",
                    'O', new UnificationEntry(OrePrefix.block, Materials.Obsidian),
                    'P', MetaItems.ELECTRIC_PISTON_MV.getStackForm(),
                    'C', TJAItemUtils.getItemStackFromName("actuallyadditions:block_giant_chest_large"),
                    'B', TJAItemUtils.getItemStackFromName("actuallyadditions:item_crate_keeper"));
        }
        if (isGregicalityLoaded) {
            // supra solar panel (max)
            RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                    .input(OrePrefix.wireGtQuadruple, GCYLMaterials.MAXSuperconductor, 64)
                    .input(OrePrefix.dust, GCYLMaterials.SiliconCarbide, 64)
                    .input(OrePrefix.bolt, GCYLMaterials.CosmicNeutronium, 16)
                    .input(OrePrefix.plateDense, GCYLMaterials.ChaosAlloy, 7)
                    .inputs(TJAMetaBlocks.BATTERY_CELL.getItemVariant(BlockBatteryCell.CasingType.CELL_MAX))
                    .input(OrePrefix.circuit, MarkerMaterials.Tier.MAX)
                    .input(GCYLCoreItems.EMITTER_MAX)
                    .input(GCYLCoreItems.SENSOR_MAX)
                    .fluidInputs(GCYLMaterials.CosmicMeshPlasma.getFluid(1000), GCYLMaterials.Quantum.getFluid(1296))
                    .output(TJAMetaItems.SUPRA_SOLAR_PANEL)
                    .EUt(983040000).duration(100)
                    .buildAndRegister();
            // infinite fluid drilling plant
            RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                    .input(MetaItems.ELECTRIC_MOTOR_UHV, 64)
                    .input(MetaItems.ELECTRIC_PUMP_UHV, 64)
                    .input(OrePrefix.foil, GCYLMaterials.Pikyonium, 64)
                    .input(OrePrefix.pipeHugeFluid, GCYLMaterials.EnrichedNaquadahAlloy, 64)
                    .input(OrePrefix.wireGtSingle, GCYLMaterials.UHVSuperconductor, 64)
                    .input(OrePrefix.screw, Materials.Duranium, 48)
                    .input(OrePrefix.gearSmall, GCYLMaterials.TitanSteel, 32)
                    .input(OrePrefix.plate, Materials.Seaborgium, 32)
                    .input(MetaTileEntities.ADVANCED_FLUID_DRILLING_RIG, 16)
                    .input(OrePrefix.circuit, MarkerMaterials.Tier.UEV)
                    .input(OrePrefix.stickLong, GCYLMaterials.NaquadriaticTaranium, 16)
                    .input(OrePrefix.gear, GCYLMaterials.Taranium, 16)
                    .input(OrePrefix.frameGt, GCYLMaterials.HDCS)
                    .fluidInputs(Materials.SolderingAlloy.getFluid(4608), GCYLMaterials.Polyetheretherketone.getFluid(9216),
                            Materials.Lubricant.getFluid(64000), Materials.Naquadria.getFluid(2304))
                    .output(TJAMetaTileEntities.INFINITE_FLUID_DRILL)
                    .stationResearch(stationRecipeBuilder -> stationRecipeBuilder
                            .researchStack(MetaTileEntities.ADVANCED_FLUID_DRILLING_RIG)
                            .EUt(GTValues.VAOC[GTValues.UEV])
                            .CWUt(256))
                    .EUt(GTValues.VAOC[GTValues.UEV]).duration(2400)
                    .buildAndRegister();
        }
        if (isAE2Loaded) {
            // ME super interface
            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .input(OrePrefix.screw, Materials.TungstenSteel, 16)
                    .inputs(Api.INSTANCE.definitions().materials().annihilationCore().maybeStack(10).orElse(ItemStack.EMPTY),
                            Api.INSTANCE.definitions().materials().formationCore().maybeStack(10).orElse(ItemStack.EMPTY),
                            Api.INSTANCE.definitions().blocks().quartzVibrantGlass().maybeStack(8).orElse(ItemStack.EMPTY))
                    .input(OrePrefix.plate, Materials.RedSteel, 16)
                    .input(MetaItems.ELECTRIC_PISTON_EV, 4)
                    .inputs(Api.INSTANCE.definitions().blocks().iface().maybeStack(2).orElse(ItemStack.EMPTY))
                    .input(OrePrefix.circuit, MarkerMaterials.Tier.IV, 2)
                    .fluidInputs(Materials.SolderingAlloy.getFluid(1440))
                    .outputs(TJAAE2Blocks.SUPER_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY))
                    .EUt(1920).duration(1000)
                    .buildAndRegister();
            // ME super fluid interface
            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .circuitMeta(3)
                    .input(OrePrefix.screw, Materials.TungstenCarbide, 16)
                    .inputs(Api.INSTANCE.definitions().materials().annihilationCore().maybeStack(10).orElse(ItemStack.EMPTY),
                            Api.INSTANCE.definitions().materials().formationCore().maybeStack(10).orElse(ItemStack.EMPTY),
                            Api.INSTANCE.definitions().blocks().quartzVibrantGlass().maybeStack(8).orElse(ItemStack.EMPTY))
                    .input(OrePrefix.plate, Materials.RedSteel, 16)
                    .input(MetaItems.ELECTRIC_PUMP_EV, 4)
                    .inputs(Api.INSTANCE.definitions().blocks().fluidIface().maybeStack(2).orElse(ItemStack.EMPTY))
                    .input(OrePrefix.circuit, MarkerMaterials.Tier.IV, 2)
                    .fluidInputs(Materials.SolderingAlloy.getFluid(1440))
                    .outputs(TJAAE2Blocks.SUPER_FLUID_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY))
                    .EUt(1920).duration(1000)
                    .buildAndRegister();
            // ME stocking interface
            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .inputs(Api.INSTANCE.definitions().blocks().quartzVibrantGlass().maybeStack(16).orElse(ItemStack.EMPTY))
                    .input(OrePrefix.screw, Materials.NaquadahEnriched, 16)
                    .input(OrePrefix.plateDouble, Materials.RhodiumPlatedPalladium, 8)
                    .inputs(Api.INSTANCE.definitions().blocks().iface().maybeStack(4).orElse(ItemStack.EMPTY))
                    .input(MetaItems.ELECTRIC_PISTON_LUV, 4)
                    .input(OrePrefix.circuit, MarkerMaterials.Tier.ZPM, 2)
                    .input(MetaItems.SENSOR_LuV)
                    .input(MetaItems.EMITTER_LuV)
                    .fluidInputs(Materials.SolderingAlloy.getFluid(1440))
                    .outputs(TJAAE2Blocks.STOCKING_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY))
                    .EUt(GTValues.VAOC[GTValues.LuV]).duration(1000)
                    .buildAndRegister();
            // ME stocking fluid interface
            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .circuitMeta(3)
                    .inputs(Api.INSTANCE.definitions().blocks().quartzVibrantGlass().maybeStack(16).orElse(ItemStack.EMPTY))
                    .input(OrePrefix.screw, Materials.NaquadahAlloy, 16)
                    .input(OrePrefix.plateDouble, Materials.RhodiumPlatedPalladium, 8)
                    .inputs(Api.INSTANCE.definitions().blocks().fluidIface().maybeStack(4).orElse(ItemStack.EMPTY))
                    .input(MetaItems.ELECTRIC_PUMP_LuV, 4)
                    .input(OrePrefix.circuit, MarkerMaterials.Tier.ZPM, 2)
                    .input(MetaItems.SENSOR_LuV)
                    .input(MetaItems.EMITTER_LuV)
                    .fluidInputs(Materials.SolderingAlloy.getFluid(1440))
                    .outputs(TJAAE2Blocks.STOCKING_FLUID_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY))
                    .EUt(GTValues.VAOC[GTValues.LuV]).duration(1000)
                    .buildAndRegister();
            // Super Pattern Multi-Tool
            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .input(OrePrefix.plate, Materials.Titanium, 32)
                    .inputs(Api.INSTANCE.definitions().materials().blankPattern().maybeStack(16).orElse(ItemStack.EMPTY),
                            Api.INSTANCE.definitions().materials().engProcessor().maybeStack(4).orElse(ItemStack.EMPTY),
                            Api.INSTANCE.definitions().materials().calcProcessor().maybeStack(4).orElse(ItemStack.EMPTY))
                    .inputs(new GTRecipeItemInput(isNAE2Loaded ? new ItemStack[]{TJAItemUtils.getItemStackFromName("nae2:pattern_multiplier")} :
                            new ItemStack[]{Api.INSTANCE.definitions().parts().monitor().maybeStack(1).orElse(ItemStack.EMPTY),
                                    Api.INSTANCE.definitions().parts().semiDarkMonitor().maybeStack(1).orElse(ItemStack.EMPTY),
                                    Api.INSTANCE.definitions().parts().darkMonitor().maybeStack(1).orElse(ItemStack.EMPTY)}))
                    .input(OrePrefix.circuit, MarkerMaterials.Tier.IV, 2)
                    .outputs(TJAAE2Items.SUPER_PATTERN_MULTIPLIER.maybeStack(1).orElse(ItemStack.EMPTY))
                    .EUt(GTValues.VAOC[GTValues.EV]).duration(1200)
                    .buildAndRegister();
            if (isGregicalityLoaded) {
                // ME pattern interface
                RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                        .inputs(Api.INSTANCE.definitions().materials().blankPattern().maybeStack(64).orElse(ItemStack.EMPTY),
                                Api.INSTANCE.definitions().materials().blankPattern().maybeStack(64).orElse(ItemStack.EMPTY),
                                Api.INSTANCE.definitions().materials().blankPattern().maybeStack(64).orElse(ItemStack.EMPTY),
                                Api.INSTANCE.definitions().materials().blankPattern().maybeStack(64).orElse(ItemStack.EMPTY),
                                Api.INSTANCE.definitions().materials().blankPattern().maybeStack(64).orElse(ItemStack.EMPTY),
                                Api.INSTANCE.definitions().materials().blankPattern().maybeStack(64).orElse(ItemStack.EMPTY),
                                Api.INSTANCE.definitions().materials().blankPattern().maybeStack(64).orElse(ItemStack.EMPTY),
                                Api.INSTANCE.definitions().materials().blankPattern().maybeStack(64).orElse(ItemStack.EMPTY),
                                Api.INSTANCE.definitions().blocks().quartzVibrantGlass().maybeStack(64).orElse(ItemStack.EMPTY))
                        .input(OrePrefix.screw, Materials.Seaborgium, 64)
                        .inputs(Api.INSTANCE.definitions().materials().annihilationCore().maybeStack(50).orElse(ItemStack.EMPTY),
                                Api.INSTANCE.definitions().materials().formationCore().maybeStack(50).orElse(ItemStack.EMPTY),
                                Api.INSTANCE.definitions().blocks().iface().maybeStack(16).orElse(ItemStack.EMPTY))
                        .input(OrePrefix.plate, GCYLMaterials.HastelloyX78, 16)
                        .input(MetaItems.ELECTRIC_PISTON_UHV, 4)
                        .input(OrePrefix.circuit, MarkerMaterials.Tier.UEV, 2)
                        .fluidInputs(Materials.SolderingAlloy.getFluid(18432), Materials.Lubricant.getFluid(64000))
                        .outputs(TJAAE2Blocks.PATTERN_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY))
                        .stationResearch(stationRecipeBuilder -> stationRecipeBuilder
                                .researchStack(TJAAE2Blocks.SUPER_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY))
                                .EUt(GTValues.VAOC[GTValues.UHV])
                                .CWUt(128))
                        .EUt(GTValues.VAOC[GTValues.UEV]).duration(1000)
                        .buildAndRegister();
                // ME super ultimate interface
                RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                        .inputs(Api.INSTANCE.definitions().materials().annihilationCore().maybeStack(64).orElse(ItemStack.EMPTY),
                                Api.INSTANCE.definitions().materials().formationCore().maybeStack(64).orElse(ItemStack.EMPTY),
                                Api.INSTANCE.definitions().blocks().quartzVibrantGlass().maybeStack(64).orElse(ItemStack.EMPTY))
                        .input(OrePrefix.circuit, MarkerMaterials.Tier.UXV, 8)
                        .inputs(TJAAE2Blocks.PATTERN_INTERFACE.maybeStack(4).orElse(ItemStack.EMPTY),
                                TJAAE2Blocks.STOCKING_INTERFACE.maybeStack(4).orElse(ItemStack.EMPTY),
                                TJAAE2Blocks.STOCKING_FLUID_INTERFACE.maybeStack(4).orElse(ItemStack.EMPTY),
                                TJAAE2Blocks.STOCKING_DUAL_INTERFACE.maybeStack(4).orElse(ItemStack.EMPTY),
                                TJAAE2Blocks.SUPER_INTERFACE.maybeStack(4).orElse(ItemStack.EMPTY),
                                TJAAE2Blocks.SUPER_FLUID_INTERFACE.maybeStack(4).orElse(ItemStack.EMPTY),
                                TJAAE2Blocks.SUPER_DUAL_INTERFACE.maybeStack(4).orElse(ItemStack.EMPTY),
                                TJAItemUtils.getItemStackFromName("ae2fc:dual_interface", 4)) // TODO add replacement dual interface
                        .input(MetaItems.ELECTRIC_PISTON_UXV, 4)
                        .input(MetaItems.EMITTER_UXV, 4)
                        .input(MetaItems.SENSOR_UXV, 4)
                        .input(MetaItems.FIELD_GENERATOR_UXV, 4)
                        .fluidInputs(Materials.SolderingAlloy.getFluid(55296), Materials.Lubricant.getFluid(64000),
                                GCYLMaterials.FullerenePolymerMatrix.getFluid(9216), GCYLMaterials.Periodicium.getFluid(18432))
                        .outputs(TJAAE2Blocks.SUPER_ULTIMATE_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY))
                        .stationResearch(stationRecipeBuilder -> stationRecipeBuilder
                                .researchStack(TJAAE2Blocks.SUPER_DUAL_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY))
                                .EUt(GTValues.VAOC[GTValues.OpV])
                                .CWUt(1024))
                        .EUt(GTValues.VAOC[GTValues.OpV]).duration(1000)
                        .buildAndRegister();
            }
        }
        if (isGregicalityLoaded && isSuperCriticalLoaded) {
            // industrial fusion reactor mk1
            RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                    .input(MetaItems.HIGH_POWER_INTEGRATED_CIRCUIT, 64)
                    .input(OrePrefix.wireGtQuadruple, GCYLMaterials.LuVSuperconductor, 64)
                    .input(MetaItems.EMITTER_LuV, 16)
                    .input(MetaItems.SENSOR_LuV, 16)
                    .input(OrePrefix.plateDense, Materials.Einsteinium, 7)
                    .input(OrePrefix.plateDense, Materials.Rutherfordium, 7)
                    .input(MetaTileEntities.FUSION_REACTOR[0])
                    .input(OrePrefix.circuit, MarkerMaterials.Tier.ZPM, 4)
                    .fluidInputs(Materials.SolderingAlloy.getFluid(5760), Materials.Lubricant.getFluid(16000),
                            Materials.Uranium238.getFluid(1440), SCMaterials.Plutonium244.getFluid(1440))
                    .output(TJAMetaTileEntities.INDUSTRIAL_FUSION_REACTOR_MK1)
                    .EUt(60000).duration(1000)
                    .buildAndRegister();
            // industrial fusion reactor mk2
            RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                    .input(OrePrefix.wireGtOctal, GCYLMaterials.ZPMSuperconductor, 64)
                    .input(MetaItems.HIGH_POWER_INTEGRATED_CIRCUIT, 64)
                    .input(MetaItems.HIGH_POWER_INTEGRATED_CIRCUIT, 32)
                    .input(MetaItems.EMITTER_ZPM, 16)
                    .input(MetaItems.SENSOR_ZPM, 16)
                    .input(OrePrefix.plateDense, Materials.Fermium, 7)
                    .input(OrePrefix.plateDense, Materials.Dubnium, 7)
                    .input(OrePrefix.circuit, MarkerMaterials.Tier.UV, 16)
                    .input(MetaTileEntities.FUSION_REACTOR[1])
                    .fluidInputs(Materials.SolderingAlloy.getFluid(5760), Materials.Lubricant.getFluid(16000),
                            Materials.Polonium.getFluid(2880), Materials.Lutetium.getFluid(2880))
                    .output(TJAMetaTileEntities.INDUSTRIAL_FUSION_REACTOR_MK2)
                    .EUt(120000).duration(1000)
                    .buildAndRegister();
            // industrial fusion reactor mk3
            RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                    .input(MetaItems.ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 64)
                    .input(MetaItems.ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 64)
                    .input(OrePrefix.wireGtHex, GCYLMaterials.UVSuperconductor, 64)
                    .input(MetaItems.SENSOR_UV, 16)
                    .input(MetaItems.EMITTER_UV, 16)
                    .input(OrePrefix.plateDense, Materials.Mendelevium, 7)
                    .input(OrePrefix.plateDense, Materials.Seaborgium, 7)
                    .input(OrePrefix.circuit, MarkerMaterials.Tier.UHV, 64)
                    .input(MetaTileEntities.FUSION_REACTOR[2])
                    .fluidInputs(Materials.SolderingAlloy.getFluid(5760), Materials.Lubricant.getFluid(16000),
                            Materials.Copernicium.getFluid(5760), Materials.Meitnerium.getFluid(5760))
                    .output(TJAMetaTileEntities.INDUSTRIAL_FUSION_REACTOR_MK3)
                    .EUt(180000).duration(1000)
                    .buildAndRegister();
        }
    }
}
