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
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.slot.FluidSlot;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;
import tja.integration.ae2.ISuperFluidInterface;
import tja.integration.ae2.tile.TileSuperFluidInterface;
import tja.mui.MUIUtils;
import tja.mui.TJAGuiTextures;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public class BlockSuperFluidInterface extends BlockFluidInterface {

    public BlockSuperFluidInterface() {
        this.setTileEntity(TileSuperFluidInterface.class);
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
        final IPanelHandler prioritySettings = syncManager.syncedPanel("me.fluid_interface.priority", true, (panelBuilder, subPanel) -> MUIUtils.createFluidPriorityPanel(panelBuilder, subPanel, superFluidInterface));
        return ModularPanel.defaultPanel("me.super_fluid_interface.gui", 176, 292)
                .child(new RichTextWidget()
                        .pos(7, 2)
                        .size(162, 18)
                        .autoUpdate(true)
                        .textBuilder(richText -> richText.addLine(interfaceName.getStringValue())))
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
                .child(Flow.row()
                        .left(179)
                        .size(32, 194)
                        .background(GuiTextures.MC_BACKGROUND)
                        .children(4, i -> new ItemSlot()
                                .pos(7, 7 + (18 * i))
                                .background(GuiTextures.SLOT_ITEM, TJAGuiTextures.PATTERN_OVERLAY)
                                .slot((IItemHandlerModifiable) superFluidInterface.getDualityFluidInterface().getInventoryByName("upgrades"), i)))
                .child(new ButtonWidget<>()
                        .left(154)
                        .size(22)
                        .background(TJAGuiTextures.INTERFACE_SETTINGS_BASE_EDGE_RIGHT)
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
