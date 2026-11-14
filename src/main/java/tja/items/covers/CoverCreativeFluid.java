package tja.items.covers;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.Icon;
import com.cleanroommc.modularui.factory.SidedPosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IWorkable;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.cover.CoverBase;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.CoverableView;
import gregtech.api.mui.GTGuiTextures;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import tja.integration.ae2.IFluidConfigSetter;
import tja.integration.ae2.ITIckSetter;
import tja.mui.MUIUtils;
import tja.mui.sync.SelectionWidgetGroupSH;
import tja.mui.sync.TJAPhantomFluidSlotSH;
import tja.mui.widget.SelectionWidgetGroup;
import tja.mui.widget.TJAPhantomFluidSlotWidget;
import tja.textures.TJATextures;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CoverCreativeFluid extends CoverBase implements ITickable, CoverWithUI, IWorkable, ITIckSetter, IFluidConfigSetter {

    private final FluidTankList fluidFilter = new FluidTankList(true, IntStream.range(0, 9)
            .mapToObj(i -> new FluidTank(Integer.MAX_VALUE))
            .collect(Collectors.toList()));
    private final IFluidHandler fluidHandler;
    private boolean isWorking;
    private int speed = 1;

    public CoverCreativeFluid(@Nonnull CoverDefinition definition, @Nonnull CoverableView coverableView, @Nonnull EnumFacing attachedSide) {
        super(definition, coverableView, attachedSide);
        this.fluidHandler = coverableView.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, attachedSide);
    }

    @Override
    public boolean canAttach(@Nonnull CoverableView coverableView, @Nonnull EnumFacing enumFacing) {
        return coverableView.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, enumFacing) != null;
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
        final SelectionWidgetGroupSH selectionWidgetGroupSH = new SelectionWidgetGroupSH();
        syncManager.syncValue("selection", selectionWidgetGroupSH);

        final Icon detail = GTGuiTextures.BUTTON_POWER_DETAIL.asIcon().size(18, 6).marginTop(24);
        final IPanelHandler tickPanel = syncManager.syncedPanel("tick.panel", true,
                (sync, handler) -> MUIUtils.createTicksPanel(sync, handler, this));
        syncManager.syncValue("tick_open", new BooleanSyncValue(tickPanel::isPanelOpen, value -> {
            if (!value && tickPanel.isPanelOpen()) {
                tickPanel.closePanel();
            } else if (value)
                tickPanel.openPanel();
        }));
        final IPanelHandler fluidConfig = syncManager.syncedPanel("fluid_config.panel", true,
                (panelBuilder, subPanel) ->
                        MUIUtils.createFluidConfigPanel(panelBuilder, subPanel, this, selectionWidgetGroupSH));
        selectionWidgetGroupSH.setIndexListener(i -> {
            if (!fluidConfig.isPanelOpen() && syncManager.isClient())
                fluidConfig.openPanel();
        });
        return ModularPanel.defaultPanel("creative_fluid_cover_gui", 200, 187)
                .child(IKey.str(this.getDefinition().getDropItemStack().getDisplayName()).asWidget()
                        .pos(7, 5))
                .child(new SelectionWidgetGroup()
                        .pos(61, 25)
                        .size(54)
                        .gridOfSizeWidth(this.fluidFilter.getTanks(), 3, (x, y, i) -> new TJAPhantomFluidSlotWidget()
                                .syncHandler(new TJAPhantomFluidSlotSH(this.fluidFilter, i)))
                        .syncHandler("selection"))
                .child(new TextFieldWidget()
                        .pos(7, 82)
                        .size(162, 18)
                        .setMaxLength(10)
                        .autoUpdateOnChange(true)
                        .setValidator(MUIUtils::numberValidator)
                        .setPattern(Pattern.compile("\\*?[0-9_]*\\*?"))
                        .addTooltipLine(IKey.lang("tja.machine.universal.ticks.operation"))
                        .value(new StringSyncValue(() -> String.valueOf(this.getTickTime()), this::setTickTime)))
                .child(Flow.col()
                        .pos(174, 82)
                        .size(18, 54)
                        .child(new ToggleButton()
                                .overlay(GTGuiTextures.FILTER_SETTINGS_OVERLAY)
                                .addTooltipLine(IKey.lang("tja.machine.universal.configure.ticks_operation"))
                                .syncHandler("tick_open"))
                        .child(new ToggleButton()
                                .top(80)
                                .disableHoverBackground()
                                .overlay(true, detail, GTGuiTextures.BUTTON_POWER[1])
                                .overlay(false, detail, GTGuiTextures.BUTTON_POWER[0])
                                .tooltipDynamic(richTooltip -> richTooltip.addLine(IKey.lang(isWorking.getBoolValue() ?
                                                "tja.machine.universal.power_on" : "tja.machine.universal.power_off"))
                                        .addLine(IKey.lang("tja.machine.universal.power_toggle")
                                                .style(TextFormatting.GRAY)))
                                .syncHandler("is_working")))
                .child(SlotGroupWidget.playerInventory(false)
                        .left(7).bottom(7));
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
            for (int index = 0; index < 9; index++) {
                final FluidStack fluid = this.fluidFilter.getTankAt(index).getFluid();
                if (fluid != null) {
                    this.fluidHandler.fill(fluid, true);
                }
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
        data.setTag("fluidFilter", this.fluidFilter.serializeNBT());
        data.setInteger("speed", this.speed);
        data.setBoolean("isWorking", this.isWorking);
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound data) {
        super.readFromNBT(data);
        this.fluidFilter.deserializeNBT(data.getCompoundTag("fluidFilter"));
        this.speed = Math.max(1, data.getInteger("speed"));
        this.isWorking = data.getBoolean("isWorking");
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, T defaultValue) {
        if (capability == GregtechTileCapabilities.CAPABILITY_WORKABLE)
            return GregtechTileCapabilities.CAPABILITY_WORKABLE.cast(this);
        return null;
    }

    @Override
    public void setFluidCount(String text, int index) {
        if (index < 0 || index >= this.fluidFilter.getTanks()) return;
        final FluidStack fluidStack = this.fluidFilter.getTankAt(index).getFluid();
        if (fluidStack == null) return;
        fluidStack.amount = (int) Math.min(Integer.MAX_VALUE, Long.parseLong(text));
        this.markAsDirty();
    }

    @Override
    public String getFluidCount(int index) {
        return String.valueOf(index >= 0 && index < this.fluidFilter.getTanks() ? this.fluidFilter.getTankAt(index).getFluidAmount() : 0);
    }

    public void setWorking(boolean isWorking) {
        this.isWorking = isWorking;
        this.markAsDirty();
    }

    public void setSpeed(long speed) {
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

    @Override
    public void setTickTime(String tickTime) {
        if (tickTime == null || tickTime.isEmpty())
            tickTime = String.valueOf(1);
        this.speed = (int) Math.max(1, Math.min(Integer.MAX_VALUE, Long.parseLong(tickTime)));
        this.markAsDirty();
    }

    @Override
    public int getTickTime() {
        return this.speed;
    }
}
