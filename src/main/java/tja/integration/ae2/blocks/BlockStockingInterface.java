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
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.PhantomItemSlotSH;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widgets.*;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.PhantomItemSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
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
import tja.integration.ae2.ISuperInterface;
import tja.integration.ae2.helpers.DualitySuperInterface;
import tja.integration.ae2.tile.TileStockingInterface;
import tja.mui.MUIUtils;
import tja.mui.TJAGuiTextures;
import tja.mui.slot.TJAModularSlot;
import tja.mui.sync.SelectionWidgetGroupSH;
import tja.mui.sync.TJAPhantomItemSlotSH;
import tja.mui.widget.SelectionWidgetGroup;
import tja.mui.widget.TJAPhantomItemSlotWidget;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;


public class BlockStockingInterface extends BlockInterface {

    public static final DualitySuperInterface DUALITY_INSTANCE = (DualitySuperInterface) new TileStockingInterface().getInterfaceDuality();

    public BlockStockingInterface() {
        this.setTileEntity(TileStockingInterface.class);
        GameRegistry.registerTileEntity(TileStockingInterface.class, new ResourceLocation(TJA.MOD_ID, "me.stocking_interface"));
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state,
                                    @Nonnull IBlockAccess world,
                                    @Nonnull BlockPos pos,
                                    @Nonnull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack is, World world, List<String> lines, ITooltipFlag advancedItemTooltips) {
        if (DUALITY_INSTANCE != null) {
            lines.add(I18n.format("tile.me.super_interface.storage_slots", DUALITY_INSTANCE.getStorage().getSlots()));
            lines.add(I18n.format("tile.me.super_interface.upgrade_slots", DUALITY_INSTANCE.getInventoryByName("upgrades").getSlots()));
        }
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

    public static ModularPanel createInterfaceGUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings, ISuperInterface iface) {
        final StringSyncValue interfaceName = new StringSyncValue(iface::getCustomInventoryName);
        syncManager.syncValue("interface_name", interfaceName);
        final BooleanSyncValue autoPullValue = new BooleanSyncValue(() -> iface.getInterfaceDuality().getConfigManager()
                .getSetting(Settings.BLOCK).ordinal() == 0, iface::setItemAutoPull);
        syncManager.syncValue("auto_pull", autoPullValue);
        final BooleanSyncValue autoPushValue = new BooleanSyncValue(() -> iface.getInterfaceDuality().getConfigManager()
                .getSetting(Settings.STICKY_MODE).ordinal() == 0, iface::setItemAutoPush);
        syncManager.syncValue("auto_push", autoPushValue);
        final SelectionWidgetGroupSH selectionWidgetGroupSH = new SelectionWidgetGroupSH();
        syncManager.syncValue("selection", selectionWidgetGroupSH);

        syncManager.registerSlotGroup(new SlotGroup("upgrade_inventory", 1, 1, true));
        syncManager.registerSlotGroup(new SlotGroup("storage_inventory", 9, 2, true));

        final Flow upgradeArea = Flow.row();
        settings.getRecipeViewerSettings().addExclusionArea(upgradeArea);
        final Flow buttonArea = Flow.col();
        settings.getRecipeViewerSettings().addExclusionArea(buttonArea);

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
        return ModularPanel.defaultPanel("me.stocking_interface.gui", 176, 292)
                .child(new RichTextWidget()
                        .pos(7, 2)
                        .size(162, 18)
                        .autoUpdate(true)
                        .textBuilder(richText -> richText.addLine(interfaceName.getStringValue())))
                .child(IKey.lang("gui.appliedenergistics2.Config").asWidget()
                        .pos(7, 23))
                .child(IKey.lang("gui.appliedenergistics2.StoredItems").asWidget()
                        .pos(7, 109))
                .child(IKey.lang("container.inventory").asWidget()
                        .pos(7, 198))
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
                .child(upgradeArea
                        .left(179)
                        .size(32, 194)
                        .background(GuiTextures.MC_BACKGROUND)
                        .children(10, i -> new ItemSlot()
                                .pos(7, 7 + (18 * i))
                                .background(GuiTextures.SLOT_ITEM, TJAGuiTextures.UPGRADE_OVERLAY)
                                .slot(new TJAModularSlot(iface.getInterfaceDuality().getInventoryByName("upgrades"), i)
                                        .slotGroup("upgrade_inventory"))))
                .child(buttonArea
                        .pos(-18, 8)
                        .size(18, 36)
                        .child(new ToggleButton()
                                .size(16)
                                .stateBackground(TJAGuiTextures.TOGGLE_AUTO_PULL)
                                .syncHandler("auto_pull")
                                .tooltipDynamic(richTooltip -> {
                                    richTooltip.addLine(IKey.lang("tile.me.stocking_interface.auto_pull"));
                                    richTooltip.addLine(IKey.lang("tile.me.stocking_interface.auto_pull.description")
                                            .style(TextFormatting.GRAY));
                                }))
                        .child(new ToggleButton()
                                .top(18)
                                .size(16)
                                .stateBackground(TJAGuiTextures.TOGGLE_BLOCKING_MODE)
                                .syncHandler("auto_push")
                                .tooltipDynamic(richTooltip -> {
                                    richTooltip.addLine(IKey.lang("tile.me.stocking_interface.auto_push"));
                                    richTooltip.addLine(IKey.lang("tile.me.stocking_interface.auto_push.description")
                                            .style(TextFormatting.GRAY));
                                })))
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
                .bindPlayerInventory();
    }
}
