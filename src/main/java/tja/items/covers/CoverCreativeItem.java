package tja.items.covers;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.Icon;
import com.cleanroommc.modularui.factory.SidedPosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.*;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.RichTextWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.PhantomItemSlot;
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
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import tja.TJA;
import tja.items.handlers.LargeItemStackHandler;
import tja.textures.TJATextures;
import tja.util.TJAItemUtils;

import javax.annotation.Nonnull;

public class CoverCreativeItem extends CoverBase implements ITickable, CoverWithUI {

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
    public ModularPanel buildUI(SidedPosGuiData guiData, PanelSyncManager guiSyncManager, UISettings settings) {
        guiSyncManager.syncValue("is_working", new BooleanSyncValue(() -> this.isWorking, this::setWorking));
        guiSyncManager.syncValue("speed_multiply", new InteractionSyncHandler()
                .setOnMousePressed(mouseData -> this.setSpeed(this.speed * 2L)));
        guiSyncManager.syncValue("speed_divide", new InteractionSyncHandler()
                .setOnMousePressed(mouseData -> this.setSpeed(this.speed / 2)));

        final Icon detail = GTGuiTextures.BUTTON_POWER_DETAIL.asIcon().size(18, 6).marginTop(24);
        return ModularPanel.defaultPanel("creative_item_cover_gui", 176, 187)
                .child(new Grid()
                        .pos(61, 25)
                        .size(54)
                        .gridOfSizeWidth(this.itemFilter.getSlots(), 3, (x, y, i) -> new PhantomItemSlot()
                                .syncHandler(new PhantomItemSlotSH(new ModularSlot(this.itemFilter, i)
                                        .ignoreMaxStackSize(true)))))
                .child(new RichTextWidget()
                        .size(54, 18)
                        .pos(63, 80)
                        .autoUpdate(true)
                        .textBuilder(richText -> richText.addLine(KeyUtil.lang(TextFormatting.WHITE, "tja.machine.universal.ticks", this.speed)))
                        .background(GTGuiTextures.DISPLAY))
                .child(new ButtonWidget<>()
                        .size(18)
                        .pos(43, 80)
                        .overlay(KeyUtil.string("-"))
                        .background(GuiTextures.MC_BUTTON)
                        .hoverBackground(GuiTextures.MC_BUTTON_HOVERED)
                        .syncHandler("speed_divide"))
                .child(new ButtonWidget<>()
                        .size(18)
                        .pos(116, 80)
                        .overlay(KeyUtil.string("+"))
                        .background(GuiTextures.MC_BUTTON)
                        .hoverBackground(GuiTextures.MC_BUTTON_HOVERED)
                        .syncHandler("speed_multiply"))
                .child(new ToggleButton()
                        .size(18)
                        .pos(152, 80)
                        .disableHoverBackground()
                        .overlay(true, detail, GTGuiTextures.BUTTON_POWER[1])
                        .overlay(false, detail, GTGuiTextures.BUTTON_POWER[0])
                        .syncHandler("is_working"))
                .bindPlayerInventory();
    }

    @Override
    public void renderCover(@Nonnull CCRenderState ccRenderState, @Nonnull Matrix4 matrix4, @Nonnull IVertexOperation[] iVertexOperations, @Nonnull Cuboid6 cuboid6, @Nonnull BlockRenderLayer blockRenderLayer) {
        TJATextures.CREATIVE_FLUID_COVER_OVERLAY.renderSided(this.getAttachedSide(), ccRenderState, matrix4, iVertexOperations);
    }

    @Override
    public void update() {
        if (this.isWorking && this.getOffsetTimer() % this.speed == 0) {
            for (int i = 0; i < 9; i++) {
                final ItemStack filterStack = this.itemFilter.getStackInSlot(i).copy();
                TJAItemUtils.insertIntoItemHandler(this.itemHandler, filterStack, false);
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

    private void setWorking(boolean isWorking) {
        this.isWorking = isWorking;
        this.markAsDirty();
    }

    private void setSpeed(long speed) {
        this.speed = (int) Math.max(1, Math.min(Integer.MAX_VALUE, speed));
        this.markAsDirty();
    }
}
