package tja.items.covers;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.Icon;
import com.cleanroommc.modularui.factory.SidedPosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.*;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.RichTextWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IWorkable;
import gregtech.api.cover.CoverBase;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.CoverableView;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.util.KeyUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import tja.items.handlers.LargeItemStackHandler;
import tja.mui.MUIUtils;
import tja.mui.sync.SelectionWidgetGroupSH;
import tja.mui.sync.TJAPhantomItemSlotSH;
import tja.mui.widget.SelectionWidgetGroup;
import tja.mui.widget.TJAPhantomItemSlotWidget;
import tja.textures.TJATextures;
import tja.util.TJAItemUtils;

import javax.annotation.Nonnull;

import java.util.regex.Pattern;

import static net.minecraft.util.text.TextFormatting.WHITE;

public class CoverCreativeItem extends CoverBase implements ITickable, CoverWithUI, IWorkable {

    private final LargeItemStackHandler itemFilter = new LargeItemStackHandler(9, Integer.MAX_VALUE);
    private final IItemHandler itemHandler;
    private boolean isWorking;
    private int speed = 1;

    public CoverCreativeItem(@Nonnull CoverDefinition definition, @Nonnull CoverableView coverableView, @Nonnull EnumFacing attachedSide) {
        super(definition, coverableView, attachedSide);
        this.itemHandler = coverableView.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, attachedSide);
    }

    @Override
    public boolean canAttach(@Nonnull CoverableView coverableView, @Nonnull EnumFacing enumFacing) {
        return coverableView.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, enumFacing) != null;
    }

    @Nonnull
    @Override
    public EnumActionResult onScrewdriverClick(@Nonnull EntityPlayer player, @Nonnull EnumHand hand, @Nonnull CuboidRayTraceResult hitResult) {
        if (!this.getWorld().isRemote)
            this.openUI((EntityPlayerMP) player);
        return EnumActionResult.SUCCESS;
    }

    @Override
    public ModularPanel buildUI(SidedPosGuiData guiData, PanelSyncManager syncManager, UISettings settings) {
        final BooleanSyncValue isWorking = new BooleanSyncValue(() -> this.isWorking, this::setWorking);
        syncManager.syncValue("is_working", isWorking);
        syncManager.syncValue("speed_multiply", new InteractionSyncHandler()
                .setOnMousePressed(mouseData -> this.setSpeed(this.speed * 2L)));
        syncManager.syncValue("speed_divide", new InteractionSyncHandler()
                .setOnMousePressed(mouseData -> this.setSpeed(this.speed / 2)));
        final SelectionWidgetGroupSH selectionWidgetGroupSH = new SelectionWidgetGroupSH();
        syncManager.syncValue("selection", selectionWidgetGroupSH);
        syncManager.syncValue("count_multiply", new InteractionSyncHandler()
                .setOnMousePressed(mouseData ->
                        this.setItemCount(String.valueOf(Long.parseLong(this.getItemCount(selectionWidgetGroupSH.getSelectedIndex())) * 2),
                                selectionWidgetGroupSH.getSelectedIndex())));
        syncManager.syncValue("count_divide", new InteractionSyncHandler()
                .setOnMousePressed(mouseData ->
                        this.setItemCount(String.valueOf(Long.parseLong(this.getItemCount(selectionWidgetGroupSH.getSelectedIndex())) / 2),
                                selectionWidgetGroupSH.getSelectedIndex())));

        final Icon detail = GTGuiTextures.BUTTON_POWER_DETAIL.asIcon().size(18, 6).marginTop(24);
        return ModularPanel.defaultPanel("creative_item_cover_gui", 176, 187)
                .child(new SelectionWidgetGroup()
                        .pos(61, 25)
                        .size(54)
                        .gridOfSizeWidth(this.itemFilter.getSlots(), 3, (x, y, i) -> new TJAPhantomItemSlotWidget()
                                .syncHandler(new TJAPhantomItemSlotSH(this.itemFilter, i)))
                        .syncHandler("selection"))
                .child(new TextFieldWidget()
                        .pos(26, 5)
                        .size(124, 18)
                        .setMaxLength(10)
                        .autoUpdateOnChange(true)
                        .setValidator(MUIUtils::numberValidator)
                        .setPattern(Pattern.compile("\\*?[0-9_]*\\*?"))
                        .setEnabledIf(textFieldWidget -> selectionWidgetGroupSH.getSelectedIndex() >= 0)
                        .value(new StringSyncValue(() -> this.getItemCount(selectionWidgetGroupSH.getSelectedIndex()),
                                string -> this.setItemCount(string, selectionWidgetGroupSH.getSelectedIndex()))))
                .child(new ButtonWidget<>()
                        .pos(7, 5)
                        .overlay(KeyUtil.string("*2"))
                        .setEnabledIf(w -> selectionWidgetGroupSH.getSelectedIndex() >= 0)
                        .syncHandler("count_multiply"))
                .child(new ButtonWidget<>()
                        .pos(151, 5)
                        .overlay(KeyUtil.string("/2"))
                        .setEnabledIf(w -> selectionWidgetGroupSH.getSelectedIndex() >= 0)
                        .syncHandler("count_divide"))
                .child(new RichTextWidget()
                        .size(54, 18)
                        .pos(63, 80)
                        .autoUpdate(true)
                        .textBuilder(richText -> richText.addLine(KeyUtil.lang(WHITE, "tja.machine.universal.ticks", this.speed)))
                        .background(GTGuiTextures.DISPLAY))
                .child(new ButtonWidget<>()
                        .pos(43, 80)
                        .overlay(KeyUtil.string("+"))
                        .syncHandler("speed_multiply"))
                .child(new ButtonWidget<>()
                        .pos(116, 80)
                        .overlay(KeyUtil.string("-"))
                        .syncHandler("speed_divide"))
                .child(new ToggleButton()
                        .pos(152, 80)
                        .disableHoverBackground()
                        .overlay(true, detail, GTGuiTextures.BUTTON_POWER[1])
                        .overlay(false, detail, GTGuiTextures.BUTTON_POWER[0])
                        .tooltipDynamic(richTooltip -> richTooltip.addLine(IKey.lang(isWorking.getBoolValue() ?
                                "tja.machine.universal.power_on" : "tja.machine.universal.power_off"))
                                .addLine(IKey.lang("tja.machine.universal.power_toggle")
                                        .style(TextFormatting.GRAY)))
                        .syncHandler("is_working"))
                .bindPlayerInventory();
    }

    @Override
    public void renderCover(@Nonnull CCRenderState ccRenderState,
                            @Nonnull Matrix4 matrix4,
                            @Nonnull IVertexOperation[] iVertexOperations,
                            @Nonnull Cuboid6 cuboid6,
                            @Nonnull BlockRenderLayer blockRenderLayer) {
        TJATextures.CREATIVE_FLUID_COVER_OVERLAY.renderSided(this.getAttachedSide(), ccRenderState, matrix4, iVertexOperations);
    }

    @Override
    public void update() {
        if (this.isWorking && this.getOffsetTimer() % this.speed == 0) {
            for (int i = 0; i < 9; i++) {
                final ItemStack filterStack = this.itemFilter.getStackInSlot(i).copy();
                TJAItemUtils.insertIntoItemHandlerNotifiable(this.itemHandler, filterStack, false);
            }
        }
    }

    @Override
    public void writeInitialSyncData(@Nonnull PacketBuffer packetBuffer) {
        packetBuffer.writeBoolean(this.isWorking);
        packetBuffer.writeInt(this.speed);
    }

    @Override
    public void readInitialSyncData(@Nonnull PacketBuffer packetBuffer) {
        this.isWorking = packetBuffer.readBoolean();
        this.speed = packetBuffer.readInt();
    }

    @Override
    public void writeToNBT(@Nonnull NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("itemFilter", this.itemFilter.serializeNBT());
        data.setInteger("speed", this.speed);
        data.setBoolean("isWorking", this.isWorking);
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound data) {
        super.readFromNBT(data);
        this.itemFilter.deserializeNBT(data.getCompoundTag("itemFilter"));
        this.speed = Math.max(1, data.getInteger("speed"));
        this.isWorking = data.getBoolean("isWorking");
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, T defaultValue) {
        if (capability == GregtechTileCapabilities.CAPABILITY_WORKABLE)
            return GregtechTileCapabilities.CAPABILITY_WORKABLE.cast(this);
        return null;
    }

    private void setItemCount(String text, int index) {
        if (index < 0 || index >= this.itemFilter.getSlots()) return;
        final ItemStack stack = this.itemFilter.getStackInSlot(index);
        if (stack.isEmpty()) return;
        stack.setCount((int) Math.min(Integer.MAX_VALUE, Long.parseLong(text)));
        this.markAsDirty();
    }

    private String getItemCount(int index) {
        return String.valueOf(index >= 0 && index < this.itemFilter.getSlots() ? this.itemFilter.getStackInSlot(index).getCount() : 0);
    }

    private void setWorking(boolean isWorking) {
        this.isWorking = isWorking;
        this.markAsDirty();
    }

    private void setSpeed(long speed) {
        this.speed = (int) Math.max(1, Math.min(Integer.MAX_VALUE, speed));
        this.markAsDirty();
    }

    @Override
    public int getProgress() {
        return (int) this.getOffsetTimer() % this.speed;
    }

    @Override
    public int getMaxProgress() {
        return this.speed;
    }

    @Override
    public boolean isActive() {
        return this.isWorking;
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.isWorking;
    }

    @Override
    public void setWorkingEnabled(boolean b) {}
}
