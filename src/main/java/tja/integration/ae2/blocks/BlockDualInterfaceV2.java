package tja.integration.ae2.blocks;

import appeng.api.config.CondenserOutput;
import appeng.api.config.LockCraftingMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.block.misc.BlockInterface;
import com.circulation.random_complement.client.RCSettings;
import com.circulation.random_complement.common.interfaces.RCIConfigurableObject;
import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.factory.GuiFactories;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.IMultiFluidTankHandler;
import com.cleanroommc.modularui.value.sync.*;
import com.cleanroommc.modularui.widget.scroll.VerticalScrollData;
import com.cleanroommc.modularui.widgets.*;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.slot.FluidSlot;
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
import org.apache.commons.lang3.tuple.Pair;
import tja.TJA;
import tja.TJAValues;
import tja.integration.ae2.ISuperDualInterface;
import tja.integration.ae2.helpers.DualitySuperFluidInterface;
import tja.integration.ae2.helpers.DualitySuperInterface;
import tja.integration.ae2.tile.TileDualInterfaceV2;
import tja.mui.MUIUtils;
import tja.mui.TJAGuiTextures;
import tja.mui.slot.TJAModularSlot;
import tja.mui.sync.PagedWidgetSyncHandler;
import tja.util.TJAUtility;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;


public class BlockDualInterfaceV2 extends BlockInterface {

    public static final DualitySuperInterface DUALITY_INSTANCE;
    public static final DualitySuperFluidInterface FLUID_DUALITY_INSTANCE;

    static {
        final TileDualInterfaceV2 dualInterface = new TileDualInterfaceV2();
        DUALITY_INSTANCE = (DualitySuperInterface) dualInterface.getInterfaceDuality();
        FLUID_DUALITY_INSTANCE = (DualitySuperFluidInterface) dualInterface.getDualityFluidInterface();
    }

    public BlockDualInterfaceV2() {
        this.setTileEntity(TileDualInterfaceV2.class);
        GameRegistry.registerTileEntity(TileDualInterfaceV2.class, new ResourceLocation(TJA.MOD_ID, "me.dual_interface_v2"));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack is, World world, List<String> lines, ITooltipFlag advancedItemTooltips) {
        if (!TJAValues.isModLoaded(TJAValues.AE2FC_MOD_ID))
            lines.add(I18n.format("tile.me.dual_interface_v2.description"));
        if (DUALITY_INSTANCE != null && FLUID_DUALITY_INSTANCE != null) {
            lines.add(I18n.format("tile.me.super_interface.pattern_slots", DUALITY_INSTANCE.getPatterns().getSlots()));
            lines.add(I18n.format("tile.me.super_interface.storage_slots", DUALITY_INSTANCE.getStorage().getSlots()));
            lines.add(I18n.format("tile.me.super_interface.upgrade_slots", DUALITY_INSTANCE.getInventoryByName("upgrades").getSlots()));
            lines.add(I18n.format("tile.me.super_fluid_interface.fluid_tanks", FLUID_DUALITY_INSTANCE.getTanks().getSlots()));
            lines.add(I18n.format("tile.me.super_fluid_interface.upgrade_slots", FLUID_DUALITY_INSTANCE.getInventoryByName("upgrades").getSlots()));
        }
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

    public static ModularPanel createDualInterfaceGUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings, ISuperDualInterface superDualInterface) {
        final StringSyncValue interfaceName = new StringSyncValue(superDualInterface::getCustomInventoryName);
        syncManager.syncValue("interface_name", interfaceName);
        final BooleanSyncValue blockingMode = new BooleanSyncValue(() -> superDualInterface.getInterfaceDuality().getConfigManager().getSetting(Settings.BLOCK).ordinal() == 0, superDualInterface::setBlockingMode);
        syncManager.syncValue("blocking_mode", blockingMode);
        final IntSyncValue lockCrafting = new IntSyncValue(() -> superDualInterface.getInterfaceDuality().getConfigManager().getSetting(Settings.UNLOCK).ordinal(), i -> superDualInterface.setLockCrafting(LockCraftingMode.values()[i]));
        syncManager.syncValue("lock_crafting", lockCrafting);
        final BooleanSyncValue interfaceTerminal = new BooleanSyncValue(() -> superDualInterface.getInterfaceDuality().getConfigManager().getSetting(Settings.INTERFACE_TERMINAL).ordinal() != 0, superDualInterface::setInterfaceTerminal);
        syncManager.syncValue("interface_terminal", interfaceTerminal);
        final BooleanSyncValue fluidPacket = new BooleanSyncValue(() -> superDualInterface.getInterfaceDuality().getConfigManager().getSetting(Settings.OPERATION_MODE).ordinal() == 0, superDualInterface::setFluidPacket);
        syncManager.syncValue("fluid_packet", fluidPacket);
        final BooleanSyncValue splittingItemsFluids = new BooleanSyncValue(() -> superDualInterface.getInterfaceDuality().getConfigManager().getSetting(Settings.LEVEL_TYPE).ordinal() == 0, superDualInterface::setSplittingItemsFluids);
        syncManager.syncValue("splitting_items_fluids", splittingItemsFluids);
        final IntSyncValue blockingModeEx = new IntSyncValue(() -> superDualInterface.getInterfaceDuality().getConfigManager().getSetting(Settings.CONDENSER_OUTPUT).ordinal(), i -> superDualInterface.setBlockModeEx(CondenserOutput.values()[i]));
        syncManager.syncValue("blocking_mode_ex", blockingModeEx);
        final IntSyncValue patternUpgrades = new IntSyncValue(() -> ((DualitySuperInterface.DualityUpgradeInventory) superDualInterface.getInterfaceDuality().getInventoryByName("upgrades")).getInstalledUpgrades(Upgrades.PATTERN_EXPANSION));
        syncManager.syncValue("pattern_upgrades", patternUpgrades);

        final BooleanSyncValue intelligentBlocking;
        if (TJAValues.isModLoaded(TJAValues.RANDOM_COMPLEMENT_MOD_ID)) {
            intelligentBlocking = new BooleanSyncValue(() -> ((RCIConfigurableObject) superDualInterface.getInterfaceDuality()).r$getConfigManager().getSetting(RCSettings.IntelligentBlocking).ordinal() == 0, superDualInterface::setIntelligentBlocking);
            syncManager.syncValue("intelligent_blocking", intelligentBlocking);
        } else intelligentBlocking = null;

        final PagedWidget.Controller controller = new PagedWidget.Controller();
        syncManager.syncValue("controller", new PagedWidgetSyncHandler(controller));

        syncManager.registerSlotGroup(new SlotGroup("pattern_inventory", 9, 0, true));
        syncManager.registerSlotGroup(new SlotGroup("item_upgrade_inventory", 1, 1, true));
        syncManager.registerSlotGroup(new SlotGroup("fluid_upgrade_inventory", 1, 1, true));
        syncManager.registerSlotGroup(new SlotGroup("storage_inventory", 9, 2, true));

        final Flow itemUpgradeArea = Flow.row();
        settings.getRecipeViewerSettings().addExclusionArea(itemUpgradeArea);
        final Flow fluidUpgradeArea = Flow.row();
        settings.getRecipeViewerSettings().addExclusionArea(fluidUpgradeArea);
        final Flow tabArea = Flow.col();
        settings.getRecipeViewerSettings().addExclusionArea(tabArea);
        final Flow buttonArea = Flow.col();
        settings.getRecipeViewerSettings().addExclusionArea(buttonArea);

        final Pair<ItemStack, Integer> patternMultiTool = MUIUtils.getPatternMultiTool(data);
        final IPanelHandler prioritySettings = syncManager.syncedPanel("me.interface.priority", true, (panelBuilder, subPanel) -> MUIUtils.createPriorityPanel(panelBuilder, subPanel, superDualInterface));
        return ModularPanel.defaultPanel("me.dual_interface.gui", 176, 292)
                .childIf(TJAValues.isModLoaded(TJAValues.NAE2_MOD_ID), () -> MUIUtils.createPatternMultiToolWidget(syncManager, settings, patternMultiTool.getKey()))
                .child(new RichTextWidget()
                        .pos(7, 2)
                        .size(162, 18)
                        .autoUpdate(true)
                        .textBuilder(richText -> richText.addLine(interfaceName.getStringValue())))
                .child(tabArea
                        .right(100).top(3)
                        .child(new PageButton(0, controller)
                                .tab(GuiTextures.TAB_LEFT, 0)
                                .addTooltipLine(IKey.lang("tile.me.super_interface.name"))
                                .overlay(superDualInterface.getItemTabTexture()))
                        .child(new PageButton(1, controller)
                                .tab(GuiTextures.TAB_LEFT, 0)
                                .addTooltipLine(IKey.lang("tile.me.super_fluid_interface.name"))
                                .overlay(superDualInterface.getFluidTabTexture())))
                .child(new PagedWidget<>()
                        .controller(controller)
                        .addPage(Flow.row()
                                .child(new TextWidget<>(IKey.lang("gui.appliedenergistics2.Config"))
                                        .size(162, 8)
                                        .pos(7, 23))
                                .child(new TextWidget<>(IKey.lang("gui.appliedenergistics2.StoredItems"))
                                        .size(162, 8)
                                        .pos(7, 109))
                                .child(new TextWidget<>(IKey.lang("gui.appliedenergistics2.Patterns"))
                                        .size(162, 8)
                                        .pos(7, 123))
                                .child(SlotGroupWidget.builder()
                                        .row("CCCCCCCCC")
                                        .row("SSSSSSSSS")
                                        .key('C', i -> new PhantomItemSlot()
                                                .background(TJAGuiTextures.SLOW_DOWN)
                                                .syncHandler(new PhantomItemSlotSH(new TJAModularSlot(superDualInterface.getInterfaceDuality().getConfig(), i)
                                                        .ignoreMaxStackSize(true))))
                                        .key('S', i -> new ItemSlot()
                                                .slot(new TJAModularSlot(superDualInterface.getInterfaceDuality().getStorage(), i)
                                                        .slotGroup("storage_inventory")))
                                        .build().pos(7, 34))
                                .child(itemUpgradeArea
                                        .left(179)
                                        .size(32, 194)
                                        .background(GuiTextures.MC_BACKGROUND)
                                        .children(superDualInterface.getInterfaceDuality().getInventoryByName("upgrades").getSlots(), i -> new ItemSlot()
                                                .pos(7, 7 + (18 * i))
                                                .background(GuiTextures.SLOT_ITEM, TJAGuiTextures.UPGRADE_OVERLAY)
                                                .slot(new TJAModularSlot(superDualInterface.getInterfaceDuality().getInventoryByName("upgrades"), i)
                                                        .slotGroup("item_upgrade_inventory"))))
                                .child(new Grid()
                                        .pos(7, 133)
                                        .size(166, 72)
                                        .scrollable(new VerticalScrollData() {{
                                            this.setScrollSize(superDualInterface.getInterfaceDuality().getPatterns().getSlots() / 9 * 18);
                                        }})
                                        .gridOfSizeWidth(superDualInterface.getInterfaceDuality().getPatterns().getSlots(), 9, (x, y, i) -> new ItemSlot()
                                                .background(GuiTextures.SLOT_ITEM, TJAGuiTextures.PATTERN_OVERLAY)
                                                .setEnabledIf(itemSlot -> i / 9 <= patternUpgrades.getIntValue())
                                                .slot(new TJAModularSlot(superDualInterface.getInterfaceDuality().getPatterns(), i)
                                                        .slotGroup("pattern_inventory"))))
                                .child(buttonArea
                                        .pos(-18, 60)
                                        .size(18, 198)
                                        .child(new ToggleButton()
                                                .size(16)
                                                .syncHandler("blocking_mode")
                                                .stateBackground(TJAGuiTextures.TOGGLE_BLOCKING_MODE)
                                                .tooltipDynamic(richTooltip -> {
                                                    richTooltip.addLine(IKey.lang("gui.tooltips.appliedenergistics2.InterfaceBlockingMode"));
                                                    richTooltip.addLine(IKey.lang(blockingMode.getBoolValue() ? "gui.tooltips.appliedenergistics2.Blocking" : "gui.tooltips.appliedenergistics2.NonBlocking")
                                                            .style(TextFormatting.GRAY));
                                                }))
                                        .child(new CycleButtonWidget()
                                                .top(18)
                                                .size(16)
                                                .length(5)
                                                .syncHandler("lock_crafting")
                                                .stateBackground(TJAGuiTextures.CYCLE_LOCK_CRAFTING)
                                                .tooltipDynamic(richTooltip -> {
                                                    richTooltip.addLine(IKey.lang(TJAValues.LOCKING_MODE_TOOLTIP_TITLE[lockCrafting.getIntValue()]));
                                                    richTooltip.addLine(IKey.lang(TJAValues.LOCKING_MODE_TOOLTIP_DESCRIPTION[lockCrafting.getIntValue()])
                                                            .style(TextFormatting.GRAY));
                                                }))
                                        .child(new ToggleButton()
                                                .top(36)
                                                .size(16)
                                                .syncHandler("interface_terminal")
                                                .stateBackground(TJAGuiTextures.TOGGLE_INTERFACE_TERMINAL)
                                                .tooltipDynamic(richTooltip -> {
                                                    richTooltip.addLine(IKey.lang("item.appliedenergistics2.multi_part.interface_terminal.name"));
                                                    richTooltip.addLine(IKey.lang("gui.appliedenergistics2.InterfaceTerminalHint")
                                                            .style(TextFormatting.GRAY));
                                                }))
                                        .childIf(TJAValues.isModLoaded(TJAValues.AE2FC_MOD_ID), () -> new ToggleButton()
                                                .top(54)
                                                .size(16)
                                                .syncHandler("fluid_packet")
                                                .stateBackground(TJAGuiTextures.TOGGLE_SEND_FLUID)
                                                .tooltipDynamic(richTooltip -> {
                                                    richTooltip.addLine(IKey.lang(fluidPacket.getBoolValue() ? "ae2fc.tooltip.fake_packet" : "ae2fc.tooltip.real_fluid"));
                                                    richTooltip.addLine(IKey.lang(fluidPacket.getBoolValue() ? "ae2fc.tooltip.fake_packet.hint" : "ae2fc.tooltip.real_fluid.hint")
                                                            .style(TextFormatting.GRAY));
                                                }))
                                        .childIf(TJAValues.isModLoaded(TJAValues.AE2FC_MOD_ID), () -> new ToggleButton()
                                                .top(72)
                                                .size(16)
                                                .syncHandler("splitting_items_fluids")
                                                .stateBackground(TJAGuiTextures.TOGGLE_SPLITTING_ITEMS_FLUIDS)
                                                .tooltipDynamic(richTooltip -> {
                                                    richTooltip.addLine(IKey.lang("ae2fc.tooltip.allow_splitting"));
                                                    richTooltip.addLine(IKey.lang(splittingItemsFluids.getBoolValue() ? "ae2fc.tooltip.prevent_splitting.hint" : "ae2fc.tooltip.allow_splitting.hint")
                                                            .style(TextFormatting.GRAY));
                                                }))
                                        .childIf(TJAValues.isModLoaded(TJAValues.AE2FC_MOD_ID), () -> new CycleButtonWidget()
                                                .top(90)
                                                .size(16)
                                                .length(3)
                                                .syncHandler("blocking_mode_ex")
                                                .stateBackground(TJAGuiTextures.CYCLE_BLOCKING_MODE_EX)
                                                .tooltipDynamic(richTooltip -> {
                                                    richTooltip.addLine(IKey.lang(blockingModeEx.getIntValue() == 0 ? "ae2fc.tooltip.block_all" : blockingModeEx.getIntValue() == 1 ? "ae2fc.tooltip.block_item" : "ae2fc.tooltip.block_fluid"));
                                                    richTooltip.addLine(IKey.lang(blockingModeEx.getIntValue() == 0 ? "ae2fc.tooltip.block_all.hint" : blockingModeEx.getIntValue() == 1 ? "ae2fc.tooltip.block_item.hint" : "ae2fc.tooltip.block_fluid.hint")
                                                            .style(TextFormatting.GRAY));
                                                }))
                                        .childIf(TJAValues.isModLoaded(TJAValues.RANDOM_COMPLEMENT_MOD_ID), () -> new ToggleButton()
                                                .top(108)
                                                .size(16)
                                                .syncHandler("intelligent_blocking")
                                                .stateBackground(TJAGuiTextures.TOGGLE_BLOCKING_MODE)
                                                .tooltipDynamic(richTooltip -> {
                                                    richTooltip.addLine(IKey.lang("gui.intelligent_blocking.name"));
                                                    richTooltip.addLine(IKey.lang(intelligentBlocking.getBoolValue() ? "gui.intelligent_blocking.OPEN.text" : "gui.intelligent_blocking.CLOSE.text")
                                                            .style(TextFormatting.GRAY));
                                                }))
                                        .childIf(TJAValues.isModLoaded(TJAValues.RANDOM_COMPLEMENT_MOD_ID), () -> new ButtonWidget<>()
                                                .top(126)
                                                .size(16)
                                                .background(TJAGuiTextures.AE2_MULTIPLY2_BUTTON)
                                                .onMousePressed(mouseButton -> TJAUtility.changeInterfacePatternAmount(superDualInterface.getInterfaceDuality().getPatterns(), m -> m * 2, () -> TJAUtility.updatePatterns(superDualInterface.getInterfaceDuality().getPatterns())))
                                                .tooltip(richTooltip -> {
                                                    richTooltip.addLine(IKey.lang("gui.action.MULTIPLY_2.name"));
                                                    richTooltip.addLine(IKey.lang("gui.pattern_term.auto_fill_pattern.MULTIPLY_2.text")
                                                            .style(TextFormatting.GRAY));
                                                }))
                                        .childIf(TJAValues.isModLoaded(TJAValues.RANDOM_COMPLEMENT_MOD_ID), () -> new ButtonWidget<>()
                                                .top(144)
                                                .size(16)
                                                .background(TJAGuiTextures.AE2_DIVIDE2_BUTTON)
                                                .onMousePressed(mouseButton -> TJAUtility.changeInterfacePatternAmount(superDualInterface.getInterfaceDuality().getPatterns(), m -> m / 2, () -> TJAUtility.updatePatterns(superDualInterface.getInterfaceDuality().getPatterns())))
                                                .tooltip(richTooltip -> {
                                                    richTooltip.addLine(IKey.lang("gui.action.DIVIDE_2.name"));
                                                    richTooltip.addLine(IKey.lang("gui.pattern_term.auto_fill_pattern.DIVIDE_2.text")
                                                            .style(TextFormatting.GRAY));
                                                }))
                                        .childIf(TJAValues.isModLoaded(TJAValues.RANDOM_COMPLEMENT_MOD_ID), () -> new ButtonWidget<>()
                                                .top(162)
                                                .size(16)
                                                .background(TJAGuiTextures.AE2_MULTIPLY3_BUTTON)
                                                .onMousePressed(mouseButton -> TJAUtility.changeInterfacePatternAmount(superDualInterface.getInterfaceDuality().getPatterns(), m -> m * 3, () -> TJAUtility.updatePatterns(superDualInterface.getInterfaceDuality().getPatterns())))
                                                .tooltip(richTooltip -> {
                                                    richTooltip.addLine(IKey.lang("gui.action.MULTIPLY_3.name"));
                                                    richTooltip.addLine(IKey.lang("gui.pattern_term.auto_fill_pattern.MULTIPLY_3.text")
                                                            .style(TextFormatting.GRAY));
                                                }))
                                        .childIf(TJAValues.isModLoaded(TJAValues.RANDOM_COMPLEMENT_MOD_ID), () -> new ButtonWidget<>()
                                                .top(180)
                                                .size(16)
                                                .background(TJAGuiTextures.AE2_DIVIDE3_BUTTON)
                                                .onMousePressed(mouseButton -> TJAUtility.changeInterfacePatternAmount(superDualInterface.getInterfaceDuality().getPatterns(), m -> m / 3, () -> TJAUtility.updatePatterns(superDualInterface.getInterfaceDuality().getPatterns())))
                                                .tooltip(richTooltip -> {
                                                    richTooltip.addLine(IKey.lang("gui.action.DIVIDE_3.name"));
                                                    richTooltip.addLine(IKey.lang("gui.pattern_term.auto_fill_pattern.DIVIDE_3.text")
                                                            .style(TextFormatting.GRAY));
                                                }))))
                        .addPage(Flow.row()
                                .child(new TextWidget<>(IKey.lang("gui.appliedenergistics2.Config"))
                                        .pos(7, 23)
                                        .size(162, 8))
                                .child(new TextWidget<>(IKey.lang("gui.appliedenergistics2.StoredFluids"))
                                        .pos(7, 181)
                                        .size(162, 8))
                                .child(new TextWidget<>(IKey.lang("container.inventory"))
                                        .pos(7, 198)
                                        .size(162, 8))
                                .child(SlotGroupWidget.builder()
                                        .row("CCCCCCCCC")
                                        .row("TTTTTTTTT")
                                        .key('C', i -> new FluidSlot()
                                                .background(GuiTextures.SLOT_ITEM, TJAGuiTextures.SLOW_DOWN)
                                                .syncHandler(new FluidSlotSyncHandler((IMultiFluidTankHandler) superDualInterface.getDualityFluidInterface().getConfig(), i)
                                                        .phantom(true)))
                                        .key('T', i -> new FluidSlot()
                                                .size(18, 54)
                                                .background(GuiTextures.SLOT_ITEM)
                                                .syncHandler((IMultiFluidTankHandler) superDualInterface.getDualityFluidInterface().getTanks(), i))
                                        .build().pos(7, 34))
                                .child(fluidUpgradeArea
                                        .left(179)
                                        .size(32, 86)
                                        .background(GuiTextures.MC_BACKGROUND)
                                        .children(superDualInterface.getDualityFluidInterface().getInventoryByName("upgrades").getSlots(), i -> new ItemSlot()
                                                .pos(7, 7 + (18 * i))
                                                .background(GuiTextures.SLOT_ITEM, TJAGuiTextures.UPGRADE_OVERLAY)
                                                .slot(new TJAModularSlot(superDualInterface.getDualityFluidInterface().getInventoryByName("upgrades"), i)
                                                        .slotGroup("fluid_upgrade_inventory"))))))
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
                .child(SlotGroupWidget.playerInventory(7, true, (i, slot) -> {
                    if (i == patternMultiTool.getValue())
                        slot.setEnabled(false);
                    return slot;
                }));
    }
}
