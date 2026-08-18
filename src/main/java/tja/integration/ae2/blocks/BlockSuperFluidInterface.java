package tja.integration.ae2.blocks;

import appeng.fluids.block.BlockFluidInterface;
import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.factory.GuiFactories;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.IMultiFluidTankHandler;
import com.cleanroommc.modularui.value.sync.FluidSlotSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.RichTextWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.slot.FluidSlot;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tja.integration.ae2.ISuperFluidInterface;
import tja.integration.ae2.helpers.DualitySuperFluidInterface;
import tja.integration.ae2.tile.TileSuperFluidInterface;
import tja.mui.MUIUtils;
import tja.mui.TJAGuiTextures;
import tja.util.Color;
import tja.util.TooltipHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;


public class BlockSuperFluidInterface extends BlockFluidInterface {

    public static final DualitySuperFluidInterface FLUID_DUALITY_INSTANCE = (DualitySuperFluidInterface) new TileSuperFluidInterface().getDualityFluidInterface();

    public BlockSuperFluidInterface() {
        this.setTileEntity(TileSuperFluidInterface.class);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack is, World world, List<String> lines, ITooltipFlag advancedItemTooltips) {
        lines.add(TooltipHelper.blinkingText(Color.YELLOW, 20, "tile.me.super_interface.description"));
        if (FLUID_DUALITY_INSTANCE != null) {
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

    public static ModularPanel createFluidInterfaceGUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings, ISuperFluidInterface superFluidInterface) {
        final StringSyncValue interfaceName = new StringSyncValue(superFluidInterface::getCustomInventoryName);
        syncManager.syncValue("interface_name", interfaceName);

        syncManager.registerSlotGroup(new SlotGroup("upgrade_inventory", 1, 1, true));

        final Flow upgradeArea = Flow.row();
        settings.getRecipeViewerSettings().addExclusionArea(upgradeArea);

        final IPanelHandler prioritySettings = syncManager.syncedPanel("me.fluid_interface.priority", true, (panelBuilder, subPanel) -> MUIUtils.createFluidPriorityPanel(panelBuilder, subPanel, superFluidInterface));
        return ModularPanel.defaultPanel("me.super_fluid_interface.gui", 176, 292)
                .child(new RichTextWidget()
                        .pos(7, 2)
                        .size(162, 18)
                        .autoUpdate(true)
                        .textBuilder(richText -> richText.addLine(interfaceName.getStringValue())))
                .child(new TextWidget<>(IKey.lang("gui.appliedenergistics2.Config"))
                        .pos(7, 23))
                .child(new TextWidget<>(IKey.lang("gui.appliedenergistics2.StoredFluids"))
                        .pos(7, 181))
                .child(new TextWidget<>(IKey.lang("container.inventory"))
                        .pos(7, 198))
                .child(SlotGroupWidget.builder()
                        .row("CCCCCCCCC")
                        .row("TTTTTTTTT")
                        .row("         ")
                        .row("         ")
                        .row("CCCCCCCCC")
                        .row("TTTTTTTTT")
                        .key('C', i -> new FluidSlot()
                                .background(GuiTextures.SLOT_ITEM, TJAGuiTextures.SLOW_DOWN)
                                .syncHandler(new FluidSlotSyncHandler((IMultiFluidTankHandler) superFluidInterface.getDualityFluidInterface().getConfig(), i)
                                        .phantom(true)))
                        .key('T', i -> new FluidSlot()
                                .size(18, 54)
                                .background(GuiTextures.SLOT_ITEM)
                                .syncHandler((IMultiFluidTankHandler) superFluidInterface.getDualityFluidInterface().getTanks(), i))
                        .build().pos(7, 34))
                .child(upgradeArea
                        .left(179)
                        .size(32, 86)
                        .background(GuiTextures.MC_BACKGROUND)
                        .children(4, i -> new ItemSlot()
                                .pos(7, 7 + (18 * i))
                                .background(GuiTextures.SLOT_ITEM, TJAGuiTextures.UPGRADE_OVERLAY)
                                .slot(new ModularSlot(superFluidInterface.getDualityFluidInterface().getInventoryByName("upgrades"), i)
                                        .slotGroup("upgrade_inventory"))))
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
                .bindPlayerInventory();
    }
}
