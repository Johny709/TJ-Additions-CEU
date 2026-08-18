package tja.events;

import gregtech.api.GregTechAPI;
import gregtech.api.metatileentity.registry.MTEManager;
import gregtech.api.unification.material.event.MaterialEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import tja.TJA;
import tja.materials.TJAMaterials;

public class GTEventHandler {

    @SubscribeEvent
    public void registerMTERegistry(MTEManager.MTERegistryEvent event) {
        GregTechAPI.mteManager.createRegistry(TJA.MOD_ID);
    }

    @SubscribeEvent
    public void registerGTMaterials(MaterialEvent event) {
        TJAMaterials.init();
    }
}
