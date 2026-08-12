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
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.PhantomItemSlot;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;
import tja.integration.ae2.ISuperInterface;
import tja.integration.ae2.tile.TileStockingInterface;
import tja.mui.MUIUtils;
import tja.mui.TJAGuiTextures;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public class BlockStockingInterface extends BlockInterface {

    public BlockStockingInterface() {
        this.setTileEntity(TileStockingInterface.class);
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public boolean onActivated(final World world, final BlockPos pos, final EntityPlayer player, final EnumHand hand, final @Nullable ItemStack heldItem, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        if (player.isSneaking())
            return false;
        if (!world.isRemote)
            GuiFactories.tileEntity().open(player, pos);
        return true;
    }

    public static ModularPanel createInterfaceGUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings, ISuperInterface superInterface) {
        final StringSyncValue interfaceName = new StringSyncValue(superInterface::getCustomInventoryName);
        syncManager.syncValue("interface_name", interfaceName);
        final BooleanSyncValue autoPullValue = new BooleanSyncValue(() -> superInterface.getInterfaceDuality().getConfigManager().getSetting(Settings.BLOCK).ordinal() == 0, superInterface::setItemAutoPull);
        syncManager.syncValue("auto_pull", autoPullValue);
        final BooleanSyncValue autoPushValue = new BooleanSyncValue(() -> superInterface.getInterfaceDuality().getConfigManager().getSetting(Settings.STICKY_MODE).ordinal() == 0, superInterface::setItemAutoPush);
        syncManager.syncValue("auto_push", autoPushValue);

        final Flow upgradeArea = Flow.row();
        settings.getRecipeViewerSettings().addExclusionArea(upgradeArea);

        final IPanelHandler prioritySettings = syncManager.syncedPanel("me.interface.priority", true, (panelBuilder, subPanel) -> MUIUtils.createPriorityPanel(panelBuilder, subPanel, superInterface));
        final IPanelHandler ticksSettings = syncManager.syncedPanel("me.interface.ticks", true, (panelBuilder, subPanel) -> MUIUtils.createTicksPanel(panelBuilder, subPanel, superInterface));
        return ModularPanel.defaultPanel("me.stocking_interface.gui", 176, 292)
                .child(new RichTextWidget()
                        .pos(7, 2)
                        .size(162, 18)
                        .autoUpdate(true)
                        .textBuilder(richText -> richText.addLine(interfaceName.getStringValue())))
                .child(new TextWidget<>(IKey.lang("gui.appliedenergistics2.Config"))
                        .pos(7, 23))
                .child(new TextWidget<>(IKey.lang("gui.appliedenergistics2.StoredItems"))
                        .pos(7, 181))
                .child(SlotGroupWidget.builder()
                        .row("CCCCCCCCC")
                        .row("SSSSSSSSS")
                        .row("CCCCCCCCC")
                        .row("SSSSSSSSS")
                        .row("CCCCCCCCC")
                        .row("SSSSSSSSS")
                        .row("CCCCCCCCC")
                        .row("SSSSSSSSS")
                        .key('C', i -> new PhantomItemSlot()
                                .background(TJAGuiTextures.SLOW_DOWN)
                                .syncHandler(new PhantomItemSlotSH(new ModularSlot(superInterface.getInterfaceDuality().getConfig(), i)
                                        .ignoreMaxStackSize(true))))
                        .key('S', i -> new ItemSlot()
                                .slot((IItemHandlerModifiable) superInterface.getInterfaceDuality().getStorage(), i))
                        .build().pos(7, 34))
                .child(upgradeArea
                        .left(179)
                        .size(32, 194)
                        .background(GuiTextures.MC_BACKGROUND)
                        .children(10, i -> new ItemSlot()
                                .pos(7, 7 + (18 * i))
                                .background(GuiTextures.SLOT_ITEM, TJAGuiTextures.UPGRADE_OVERLAY)
                                .slot((IItemHandlerModifiable) superInterface.getInterfaceDuality().getInventoryByName("upgrades"), i)))
                .child(new ToggleButton()
                        .pos(-18, 35)
                        .size(16)
                        .stateBackground(TJAGuiTextures.TOGGLE_AUTO_PULL)
                        .syncHandler("auto_pull")
                        .tooltipDynamic(richTooltip -> {
                            richTooltip.addLine(IKey.lang("tile.me.stocking_interface.auto_pull"));
                            richTooltip.addLine(IKey.lang("tile.me.stocking_interface.auto_pull.description")
                                    .style(TextFormatting.GRAY));
                        }))
                .child(new ToggleButton()
                        .pos(-18, 53)
                        .size(16)
                        .stateBackground(TJAGuiTextures.TOGGLE_BLOCKING_MODE)
                        .syncHandler("auto_push")
                        .tooltipDynamic(richTooltip -> {
                            richTooltip.addLine(IKey.lang("tile.me.stocking_interface.auto_push"));
                            richTooltip.addLine(IKey.lang("tile.me.stocking_interface.auto_push.description")
                                    .style(TextFormatting.GRAY));
                        }))
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
