package tja.recipes;

import appeng.core.Api;
import com.fulltrix.gcyl.item.GCYLCoreItems;
import com.fulltrix.gcyl.materials.GCYLMaterials;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeMaps;
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
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.registries.IForgeRegistry;
import supercritical.api.unification.material.SCMaterials;
import tja.TJAValues;
import tja.blocks.BlockBatteryCell;
import tja.blocks.BlockTieredGlass;
import tja.blocks.TJAAE2Blocks;
import tja.blocks.TJAMetaBlocks;
import tja.items.TJAMetaItems;
import tja.machines.TJAMetaTileEntities;
import tja.util.TJAItemUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GTRecipes {

    public static void init(IForgeRegistry<IRecipe> recipes) {
        final boolean isAE2Loaded = TJAValues.isModLoaded(TJAValues.AE2_MOD_ID);
        final boolean isGregicalityLoaded = TJAValues.isModLoaded(TJAValues.GCYL_MOD_ID);
        final boolean isSuperCriticalLoaded = TJAValues.isModLoaded(TJAValues.SUPERCRITICAL_MOD_ID);
        final boolean isActuallyAdditionsLoaded = TJAValues.isModLoaded(TJAValues.ACTUALLY_ADDITIONS_MOD_ID);

        final List<Material> materials = new ArrayList<>(Arrays.asList(Materials.WroughtIron, Materials.Steel, Materials.Aluminium, Materials.StainlessSteel,
                Materials.Titanium, Materials.TungstenSteel, Materials.RhodiumPlatedPalladium, Materials.Duranium, Materials.Tritanium));
        final List<Material> cableMaterials = new ArrayList<>(Arrays.asList(Materials.RedAlloy, Materials.Tin, Materials.Copper, Materials.Gold,
                Materials.Aluminium, Materials.Platinum, Materials.Niobium, Materials.Naquadah, Materials.NaquadahAlloy));
        final Material[] circuitTiers = new Material[]{MarkerMaterials.Tier.ULV, MarkerMaterials.Tier.LV, MarkerMaterials.Tier.MV, MarkerMaterials.Tier.HV,
                MarkerMaterials.Tier.EV, MarkerMaterials.Tier.IV, MarkerMaterials.Tier.LuV, MarkerMaterials.Tier.ZPM, MarkerMaterials.Tier.UV,
                MarkerMaterials.Tier.UHV, MarkerMaterials.Tier.UEV, MarkerMaterials.Tier.UIV, MarkerMaterials.Tier.UXV, MarkerMaterials.Tier.OpV,
                MarkerMaterials.Tier.MAX};
        if (isGregicalityLoaded) {
            materials.addAll(Arrays.asList(Materials.Seaborgium, Materials.Bohrium, GCYLMaterials.Quantum, GCYLMaterials.BlackTitanium,
                    GCYLMaterials.HeavyQuarkDegenerateMatter, Materials.Neutronium));
            cableMaterials.addAll(Arrays.asList(GCYLMaterials.AbyssalAlloy, GCYLMaterials.TitanSteel, GCYLMaterials.BlackTitanium,
                    GCYLMaterials.NaquadriaticTaranium, Materials.Neutronium, GCYLMaterials.CosmicNeutronium));
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
            if (i > 8 && !isGregicalityLoaded) continue;
            ModHandler.addShapedRecipe("fluid_sampler." + GTValues.VN[i], TJAMetaTileEntities.FLUID_SAMPLERS.get(i).getStackForm(),
                    "GGG", "PHP", "CDC",
                    'G', TJAItemUtils.getItemStackFromName("minecraft:glass"),
                    'P', new UnificationEntry(OrePrefix.plate, materials.get(i)),
                    'H', MetaTileEntities.HULL[i].getStackForm(),
                    'C', new UnificationEntry(OrePrefix.cableGtSingle, cableMaterials.get(i)),
                    'D', new UnificationEntry(OrePrefix.toolHeadDrill, materials.get(i)));
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
            // ME stocking fluid inteface
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
