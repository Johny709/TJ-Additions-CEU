package tja.integration.ae2.blocks;

import appeng.api.config.Settings;
import appeng.block.misc.BlockInterface;
import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.factory.GuiFactories;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.IMultiFluidTankHandler;
import com.cleanroommc.modularui.value.sync.*;
import com.cleanroommc.modularui.widgets.*;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.slot.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tja.TJA;
import tja.integration.ae2.ISuperDualInterface;
import tja.integration.ae2.helpers.DualitySuperFluidInterface;
import tja.integration.ae2.helpers.DualitySuperInterface;
import tja.integration.ae2.tile.TileStockingDualInterface;
import tja.mui.MUIUtils;
import tja.mui.TJAGuiTextures;
import tja.mui.slot.TJAModularSlot;
import tja.mui.sync.PagedWidgetSyncHandler;
import tja.mui.sync.SelectionWidgetGroupSH;
import tja.mui.sync.TJAPhantomFluidSlotSH;
import tja.mui.sync.TJAPhantomItemSlotSH;
import tja.mui.widget.SelectionWidgetGroup;
import tja.mui.widget.TJAPhantomFluidSlotWidget;
import tja.mui.widget.TJAPhantomItemSlotWidget;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;


public class BlockStockingDualInterface extends BlockInterface {

    public static final DualitySuperInterface DUALITY_INSTANCE;
    public static final DualitySuperFluidInterface FLUID_DUALITY_INSTANCE;

    static {
        final TileStockingDualInterface dualInterface = new TileStockingDualInterface();
        DUALITY_INSTANCE = (DualitySuperInterface) dualInterface.getInterfaceDuality();
        FLUID_DUALITY_INSTANCE = (DualitySuperFluidInterface) dualInterface.getDualityFluidInterface();
    }

    public BlockStockingDualInterface() {
        this.setTileEntity(TileStockingDualInterface.class);
        GameRegistry.registerTileEntity(TileStockingDualInterface.class, new ResourceLocation(TJA.MOD_ID, "me.stocking_dual_interface"));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack is, World world, List<String> lines, ITooltipFlag advancedItemTooltips) {
        if (DUALITY_INSTANCE != null && FLUID_DUALITY_INSTANCE != null) {
            lines.add(I18n.format("tile.me.super_interface.storage_slots", DUALITY_INSTANCE.getStorage().getSlots()));
            lines.add(I18n.format("tile.me.super_interface.upgrade_slots", DUALITY_INSTANCE.getInventoryByName("upgrades").getSlots()));
            lines.add(I18n.format("tile.me.super_fluid_interface.fluid_tanks", FLUID_DUALITY_INSTANCE.getTanks().getSlots()));
            lines.add(I18n.format("tile.me.super_fluid_interface.upgrade_slots", FLUID_DUALITY_INSTANCE.getInventoryByName("upgrades").getSlots()));
        }
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state,
                                    @Nonnull IBlockAccess world,
                                    @Nonnull BlockPos pos,
                                    @Nonnull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public boolean onActivated(final World world,
                               final BlockPos pos,
                               final EntityPlayer player,
                               final EnumHand hand,
                               final @Nullable ItemStack heldItem,
                               final EnumFacing side,
                               final float hitX,
                               final float hitY,
                               final float hitZ) {
        if (player.isSneaking())
            return false;
        if (!world.isRemote)
            GuiFactories.tileEntity().open(player, pos);
        return true;
    }

    public static ModularPanel createDualInterfaceGUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings, ISuperDualInterface iface) {
        final StringSyncValue interfaceName = new StringSyncValue(iface::getCustomInventoryName);
        syncManager.syncValue("interface_name", interfaceName);
        final BooleanSyncValue itemAutoPullValue = new BooleanSyncValue(() ->
                iface.getInterfaceDuality().getConfigManager().getSetting(Settings.BLOCK).ordinal() == 0, iface::setItemAutoPull);
        syncManager.syncValue("item_auto_pull", itemAutoPullValue);
        final BooleanSyncValue itemAutoPushValue = new BooleanSyncValue(() ->
                iface.getInterfaceDuality().getConfigManager().getSetting(Settings.STICKY_MODE).ordinal() == 0, iface::setItemAutoPush);
        syncManager.syncValue("item_auto_push", itemAutoPushValue);
        final BooleanSyncValue fluidAutoPullValue = new BooleanSyncValue(() ->
                iface.getDualityFluidInterface().getConfigManager().getSetting(Settings.BLOCK).ordinal() == 0, iface::setFluidAutoPull);
        syncManager.syncValue("fluid_auto_pull", fluidAutoPullValue);
        final BooleanSyncValue fluidAutoPushValue = new BooleanSyncValue(() ->
                iface.getDualityFluidInterface().getConfigManager().getSetting(Settings.STICKY_MODE).ordinal() == 0, iface::setFluidAutoPush);
        syncManager.syncValue("fluid_auto_push", fluidAutoPushValue);
        final PagedWidget.Controller controller = new PagedWidget.Controller();
        syncManager.syncValue("controller", new PagedWidgetSyncHandler(controller));
        final SelectionWidgetGroupSH selectionWidgetGroupSH = new SelectionWidgetGroupSH();
        syncManager.syncValue("selection", selectionWidgetGroupSH);
        final SelectionWidgetGroupSH selectionWidgetGroupSH2 = new SelectionWidgetGroupSH();
        syncManager.syncValue("selection_2", selectionWidgetGroupSH2);

        syncManager.registerSlotGroup(new SlotGroup("item_upgrade_inventory", 1, 1, true));
        syncManager.registerSlotGroup(new SlotGroup("fluid_upgrade_inventory", 1, 1, true));
        syncManager.registerSlotGroup(new SlotGroup("storage_inventory", 9, 2, true));

        final Flow itemUpgradeArea = Flow.row();
        settings.getRecipeViewerSettings().addExclusionArea(itemUpgradeArea);
        final Flow fluidUpgradeArea = Flow.row();
        settings.getRecipeViewerSettings().addExclusionArea(fluidUpgradeArea);
        final Flow tabArea = Flow.col();
        settings.getRecipeViewerSettings().addExclusionArea(tabArea);

        final IPanelHandler prioritySettings = syncManager.syncedPanel("me.interface.priority", true,
                (panelBuilder, subPanel) -> MUIUtils.createPriorityPanel(panelBuilder, subPanel, iface));
        final IPanelHandler ticksSettings = syncManager.syncedPanel("me.interface.ticks", true,
                (panelBuilder, subPanel) -> MUIUtils.createTicksPanel(panelBuilder, subPanel, iface));
        final IPanelHandler itemConfig = syncManager.syncedPanel("me.super_interface.item_config", true,
                (panelBuilder, subPanel) -> MUIUtils.createItemConfigPanel(panelBuilder, subPanel, iface, selectionWidgetGroupSH));
        selectionWidgetGroupSH.setIndexListener(i -> {
            if (!itemConfig.isPanelOpen())
                itemConfig.openPanel();
        });
        final IPanelHandler fluidConfig = syncManager.syncedPanel("me.super_interface.fluid_config", true,
                (panelBuilder, subPanel) -> MUIUtils.createFluidConfigPanel(panelBuilder, subPanel, iface, selectionWidgetGroupSH2));
        selectionWidgetGroupSH2.setIndexListener(i -> {
            if (!fluidConfig.isPanelOpen())
                fluidConfig.openPanel();
        });
        return ModularPanel.defaultPanel("me.stocking_dual_interface.gui", 176, 292)
                .child(new RichTextWidget()
                        .pos(7, 2)
                        .size(162, 18)
                        .autoUpdate(true)
                        .textBuilder(richText -> richText.addLine(interfaceName.getStringValue())))
                .child(new ButtonWidget<>()
                        .left(154)
                        .size(22)
                        .background(TJAGuiTextures.INTERFACE_SETTINGS_BASE_EDGE_RIGHT, TJAGuiTextures.CERTUS_QUARTZ_WRENCH)
                        .tooltip(richTooltip -> richTooltip.addLine(IKey.lang("gui.appliedenergistics2.Priority")))
                        .onMousePressed(mouseButton -> {
                            if (prioritySettings.isPanelOpen()) {
                                prioritySettings.closePanel();
                            } else prioritySettings.openPanel();
                            return true;
                        }))
                .child(new ButtonWidget<>()
                        .left(132)
                        .size(22)
                        .background(TJAGuiTextures.INTERFACE_SETTINGS_BASE_LEFT, TJAGuiTextures.CLOCK)
                        .tooltip(richTooltip -> richTooltip.addLine(IKey.lang("tja.machine.universal.ticks.operation")))
                        .onMousePressed(mouseButton -> {
                            if (ticksSettings.isPanelOpen()) {
                                ticksSettings.closePanel();
                            } else ticksSettings.openPanel();
                            return true;
                        }))
                .child(tabArea
                        .right(100).top(3)
                        .child(new PageButton(0, controller)
                                .tab(GuiTextures.TAB_LEFT, 0)
                                .addTooltipLine(IKey.lang("tile.me.stocking_interface.name"))
                                .overlay(iface.getItemTabTexture()))
                        .child(new PageButton(1, controller)
                                .tab(GuiTextures.TAB_LEFT, 0)
                                .addTooltipLine(IKey.lang("tile.me.stocking_fluid_interface.name"))
                                .overlay(iface.getFluidTabTexture())))
                .child(new PagedWidget<>()
                        .controller(controller)
                        .addPage(Flow.row()
                                .child(IKey.lang("gui.appliedenergistics2.Config").asWidget()
                                        .pos(7, 23)
                                        .size(162, 8))
                                .child(IKey.lang("gui.appliedenergistics2.StoredItems").asWidget()
                                        .pos(7, 109)
                                        .size(162, 8))
                                .child(IKey.lang("container.inventory").asWidget()
                                        .pos(7, 198)
                                        .size(162, 8))
                                .child(new SelectionWidgetGroup()
                                        .pos(7, 34)
                                        .size(162, iface.getInterfaceDuality().getConfig().getSlots() * 18 / 9)
                                        .gridOfSizeWidth(iface.getInterfaceDuality().getConfig().getSlots(), 9, (x, y, i) -> new TJAPhantomItemSlotWidget()
                                                .background(TJAGuiTextures.SLOT_DOWN)
                                                .syncHandler(new TJAPhantomItemSlotSH(iface.getInterfaceDuality().getConfig(), i)))
                                        .syncHandler("selection"))
                                .child(new Grid()
                                        .pos(7,121)
                                        .size(162, iface.getInterfaceDuality().getStorage().getSlots() * 18 / 9)
                                        .gridOfSizeWidth(iface.getInterfaceDuality().getStorage().getSlots(), 9, (x, y, i) -> new ItemSlot()
                                                .slot(new TJAModularSlot(iface.getInterfaceDuality().getStorage(), i)
                                                        .slotGroup("storage_inventory"))))
                                .child(itemUpgradeArea
                                        .left(179)
                                        .size(32, 194)
                                        .background(GuiTextures.MC_BACKGROUND)
                                        .children(10, i -> new ItemSlot()
                                                .pos(7, 7 + (18 * i))
                                                .background(GuiTextures.SLOT_ITEM, TJAGuiTextures.UPGRADE_OVERLAY)
                                                .slot(new TJAModularSlot(iface.getInterfaceDuality().getInventoryByName("upgrades"), i)
                                                        .slotGroup("item_uprade_inventory"))))
                                .child(new ToggleButton()
                                        .pos(-18, 60)
                                        .size(16)
                                        .stateBackground(TJAGuiTextures.TOGGLE_AUTO_PULL)
                                        .syncHandler("item_auto_pull")
                                        .tooltipDynamic(richTooltip -> {
                                            richTooltip.addLine(IKey.lang("tile.me.stocking_interface.auto_pull"));
                                            richTooltip.addLine(IKey.lang("tile.me.stocking_interface.auto_pull.description")
                                                    .style(TextFormatting.GRAY));
                                        }))
                                .child(new ToggleButton()
                                        .pos(-18, 78)
                                        .size(16)
                                        .stateBackground(TJAGuiTextures.TOGGLE_BLOCKING_MODE)
                                        .syncHandler("item_auto_push")
                                        .tooltipDynamic(richTooltip -> {
                                            richTooltip.addLine(IKey.lang("tile.me.stocking_interface.auto_push"));
                                            richTooltip.addLine(IKey.lang("tile.me.stocking_interface.auto_push.description")
                                                    .style(TextFormatting.GRAY));
                                        })))
                        .addPage(Flow.row()
                                .child(IKey.lang("gui.appliedenergistics2.Config").asWidget()
                                        .pos(7, 23)
                                        .size(162, 8))
                                .child(IKey.lang("gui.appliedenergistics2.StoredFluids").asWidget()
                                        .pos(7, 109)
                                        .size(162, 8))
                                .child(IKey.lang("container.inventory").asWidget()
                                        .pos(7, 198)
                                        .size(162, 8))
                                .child(new SelectionWidgetGroup()
                                        .pos(7, 34)
                                        .size(162, iface.getDualityFluidInterface().getConfig().getSlots() * 18 / 9)
                                        .gridOfSizeWidth(iface.getDualityFluidInterface().getConfig().getSlots(), 9, (x, y, i) -> new TJAPhantomFluidSlotWidget()
                                                .background(TJAGuiTextures.SLOT_DOWN)
                                                .syncHandler(new TJAPhantomFluidSlotSH((IMultiFluidTankHandler) iface.getDualityFluidInterface().getConfig(), i)))
                                        .syncHandler("selection_2"))
                                .child(new Grid()
                                        .pos(7,85)
                                        .size(162, iface.getDualityFluidInterface().getTanks().getSlots() * 54 / 9)
                                        .gridOfSizeWidth(iface.getDualityFluidInterface().getTanks().getSlots(), 9, (x, y, i) -> new FluidSlot()
                                                .size(18, 54)
                                                .background(GuiTextures.SLOT_ITEM)
                                                .syncHandler((IMultiFluidTankHandler) iface.getDualityFluidInterface().getTanks(), i)))
                                .child(fluidUpgradeArea
                                        .left(179)
                                        .size(32, 86)
                                        .background(GuiTextures.MC_BACKGROUND)
                                        .children(4, i -> new ItemSlot()
                                                .pos(7, 7 + (18 * i))
                                                .background(GuiTextures.SLOT_ITEM, TJAGuiTextures.UPGRADE_OVERLAY)
                                                .slot(new TJAModularSlot(iface.getDualityFluidInterface().getInventoryByName("upgrades"), i)
                                                        .slotGroup("fluid_upgrade_inventory"))))
                                .child(new ToggleButton()
                                        .pos(-18, 60)
                                        .size(16)
                                        .stateBackground(TJAGuiTextures.TOGGLE_AUTO_PULL)
                                        .syncHandler("fluid_auto_pull")
                                        .tooltipDynamic(richTooltip -> {
                                            richTooltip.addLine(IKey.lang("tile.me.stocking_fluid_interface.auto_pull"));
                                            richTooltip.addLine(IKey.lang("tile.me.stocking_fluid_interface.auto_pull.description")
                                                    .style(TextFormatting.GRAY));
                                        }))
                                .child(new ToggleButton()
                                        .pos(-18, 78)
                                        .size(16)
                                        .stateBackground(TJAGuiTextures.TOGGLE_BLOCKING_MODE)
                                        .syncHandler("fluid_auto_push")
                                        .tooltipDynamic(richTooltip -> {
                                            richTooltip.addLine(IKey.lang("tile.me.stocking_fluid_interface.auto_push"));
                                            richTooltip.addLine(IKey.lang("tile.me.stocking_fluid_interface.auto_push.description")
                                                    .style(TextFormatting.GRAY));
                                        }))))
                .bindPlayerInventory();
    }
}
