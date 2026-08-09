package tja.materials;

import com.fulltrix.gcyl.materials.GCYLMaterials;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.info.MaterialFlags;
import tja.TJAValues;

public class TJAMaterials {

    public static void init() {
        Materials.Iron.addFlags(MaterialFlags.GENERATE_DOUBLE_PLATE);
        Materials.Tritanium.addFlags(MaterialFlags.GENERATE_DOUBLE_PLATE);
        Materials.Seaborgium.addFlags(MaterialFlags.GENERATE_DOUBLE_PLATE);
        Materials.Bohrium.addFlags(MaterialFlags.GENERATE_DOUBLE_PLATE);
        Materials.Einsteinium.addFlags(MaterialFlags.GENERATE_DENSE);
        Materials.Fermium.addFlags(MaterialFlags.GENERATE_DENSE);
        Materials.Mendelevium.addFlags(MaterialFlags.GENERATE_DENSE);
        if (TJAValues.isModLoaded(TJAValues.GCYL_MOD_ID)) {
            GCYLMaterials.Adamantium.addFlags(MaterialFlags.GENERATE_DOUBLE_PLATE);
            GCYLMaterials.Vibranium.addFlags(MaterialFlags.GENERATE_DOUBLE_PLATE);
            GCYLMaterials.HeavyQuarkDegenerateMatter.addFlags(MaterialFlags.GENERATE_DOUBLE_PLATE);
            GCYLMaterials.ChaosAlloy.addFlags(MaterialFlags.GENERATE_DOUBLE_PLATE);
        }
    }
}
