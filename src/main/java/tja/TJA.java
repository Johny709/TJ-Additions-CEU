package tja;

import codechicken.lib.texture.TextureUtils;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import tja.blocks.TJAAE2Blocks;
import tja.capability.TJASimpleCapabilityManager;
import tja.integration.theoneprobe.TheOneProbeModule;
import tja.items.TJACoverBehaviors;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tja.items.TJAMetaItems;
import tja.machines.TJAMetaTileEntities;
import tja.textures.TJATextures;


@Mod(modid = TJA.MOD_ID, name = TJA.MOD_NAME, version = TJA.VERSION, dependencies = "required-after:modularui@[3.1.6,);" +
    "after:gregtech@[2.9.0-beta,)")
public class TJA {
    public static final String MOD_ID = "tja";
    public static final String MOD_NAME = "TJ Additions CEU";
    public static final String VERSION = "0.1";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    /**
     * <a href="https://cleanroommc.com/wiki/forge-mod-development/event#overview">
     *     Take a look at how many FMLStateEvents you can listen to via the @Mod.EventHandler annotation here
     * </a>
     */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("Hello From {}!", MOD_NAME);
        for (String modid : TJAConfig.modids) {
            if (!TJAValues.isModLoaded(modid))
                throw new IllegalStateException(String.format("Mod %s is not loaded", modid));
        }
        if (TJAValues.isModLoaded(TJAValues.GREGTECH_MOD_ID)) {
            TJAMetaTileEntities.init();
            TJAMetaItems.init();
            TJASimpleCapabilityManager.init();
            if (event.getSide() == Side.CLIENT)
                TextureUtils.addIconRegister(TJATextures::register);
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        if (TJAValues.isModLoaded(TJAValues.GREGTECH_MOD_ID))
            TJACoverBehaviors.init();
        if (TJAValues.isModLoaded(TJAValues.THEONEPROBE_MOD_ID)) {
            TheOneProbeModule.registerElements();
            TheOneProbeModule.init();
        }
        if (TJAValues.isModLoaded(TJAValues.AE2_MOD_ID))
            TJAAE2Blocks.registerUpgrades();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        if (TJAValues.isModLoaded(TJAValues.AE2_MOD_ID)) {
            if (event.getSide() == Side.CLIENT)
                TJAAE2Blocks.registerItemModels();
        }
    }
}
