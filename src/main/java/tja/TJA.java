package tja;

import appeng.api.config.Upgrades;
import appeng.api.definitions.IItems;
import appeng.core.Api;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tja.integration.ae2.IApiItems;


@Mod(modid = TJA.MOD_ID, name = TJA.MOD_NAME, version = TJA.VERSION)
public class TJA {
    public static final String MOD_ID = "tja";
    public static final String MOD_NAME = "TJ Additions CEU";
    public static final String VERSION = "1.0";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    /**
     * <a href="https://cleanroommc.com/wiki/forge-mod-development/event#overview">
     *     Take a look at how many FMLStateEvents you can listen to via the @Mod.EventHandler annotation here
     * </a>
     */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("Hello From {}!", MOD_NAME);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        final IApiItems items = ((IApiItems) (IItems) Api.INSTANCE.definitions().items());
        // Item Storage Cells
        Upgrades.FUZZY.registerItem(items.getCell65m(), 1);
        Upgrades.INVERTER.registerItem(items.getCell65m(), 1);
        Upgrades.STICKY.registerItem(items.getCell65m(), 1);

        Upgrades.FUZZY.registerItem(items.getCell262m(), 1);
        Upgrades.INVERTER.registerItem(items.getCell262m(), 1);
        Upgrades.STICKY.registerItem(items.getCell262m(), 1);

        Upgrades.FUZZY.registerItem(items.getCell1048m(), 1);
        Upgrades.INVERTER.registerItem(items.getCell1048m(), 1);
        Upgrades.STICKY.registerItem(items.getCell1048m(), 1);

        Upgrades.FUZZY.registerItem(items.getCellDigitalSingularity(), 1);
        Upgrades.INVERTER.registerItem(items.getCellDigitalSingularity(), 1);
        Upgrades.STICKY.registerItem(items.getCellDigitalSingularity(), 1);

        // Fluid Storage Cells
        Upgrades.INVERTER.registerItem(items.getFluidCell65m(), 1);
        Upgrades.STICKY.registerItem(items.getFluidCell65m(), 1);

        Upgrades.INVERTER.registerItem(items.getFluidCell262m(), 1);
        Upgrades.STICKY.registerItem(items.getFluidCell262m(), 1);

        Upgrades.INVERTER.registerItem(items.getFluidCell1048m(), 1);
        Upgrades.STICKY.registerItem(items.getFluidCell1048m(), 1);

        Upgrades.INVERTER.registerItem(items.getFluidCellDigitalSingularity(), 1);
        Upgrades.STICKY.registerItem(items.getFluidCellDigitalSingularity(), 1);
    }
}
