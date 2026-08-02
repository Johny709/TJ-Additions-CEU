package tja.recipes;

import com.fulltrix.gcyl.item.GCYLCoreItems;
import com.fulltrix.gcyl.materials.GCYLMaterials;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import tja.TJAValues;
import tja.blocks.BlockBatteryCell;
import tja.blocks.TJAMetaBlocks;
import tja.items.TJAMetaItems;
import tja.machines.TJAMetaTileEntities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TJARecipes {

    public static void init() {
        if (TJAValues.isModLoaded(TJAValues.GREGTECH_MOD_ID)) {
            final boolean isGregicalityLoaded = TJAValues.isModLoaded(TJAValues.GCYL_MOD_ID);
            // mega coke oven
            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .input(MetaTileEntities.COKE_OVEN, 64)
                    .input(MetaTileEntities.COKE_OVEN, 64)
                    .input(MetaTileEntities.COKE_OVEN, 64)
                    .input(MetaTileEntities.COKE_OVEN, 64)
                    .output(TJAMetaTileEntities.MEGA_COKE_OVEN)
                    .EUt(30).duration(1200)
                    .buildAndRegister();
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
            }
        }
    }
}
