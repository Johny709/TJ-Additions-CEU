package tja.machines.singleblocks;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.Icon;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.DoubleSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widget.scroll.VerticalScrollData;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import com.cleanroommc.modularui.widgets.RichTextWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import gregtech.api.capability.impl.FilteredFluidHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.MetaTileEntityGuiData;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.mui.widget.GTFluidSlot;
import gregtech.core.sound.GTSoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.lwjgl.opengl.GL11;
import tja.capability.TJADataCodes;
import tja.capability.handler.IMinerHandler;
import tja.capability.workables.MinerWorkableHandler;
import tja.event.RenderEventHandler;
import tja.items.handlers.GTLargeItemStackHandler;
import tja.mui.TJAGuiTextures;
import tja.mui.sync.TJAPhantomItemSlotSH;
import tja.mui.widget.TJAPhantomItemSlotWidget;
import tja.rendering.TJARenderUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class MetaTileEntityAdvancedChunkMiner extends TJATieredWorkableMetaTileEntity implements IMinerHandler {

    private final MinerWorkableHandler workableHandler = new MinerWorkableHandler(this);
    private final GTLargeItemStackHandler filterInventory =
            new GTLargeItemStackHandler(this, 27, 1, this, false);
    private final FluidStack drillingFluid;
    private final Set<UUID> playersSeeBoundingBox = new HashSet<>();

    public MetaTileEntityAdvancedChunkMiner(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        this.drillingFluid = Materials.DrillingFluid.getFluid(1 << tier - 1);
        this.workableHandler.initialize(1);
        this.initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityAdvancedChunkMiner(this.metaTileEntityId, this.getTier());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, @Nonnull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("tja.multiblock.advanced_large_miner.description"));
        tooltip.add(I18n.format("tja.multiblock.advanced_large_miner.output"));
        tooltip.add(I18n.format("gregtech.machine.miner.working_area_chunks", this.getDiameter(), this.getDiameter()));
        tooltip.add(I18n.format("tja.multiblock.advanced_chunk_miner.fluid_usage", 1 << this.getTier() - 1,
                this.drillingFluid.getLocalizedName()));
        tooltip.add(I18n.format("tja.multiblock.advanced_chunk_miner.block_per_tick", 1 << this.getTier() - 1));
    }

    @Override
    public void update() {
        super.update();
        if (!this.getWorld().isRemote) {
            this.workableHandler.update();
            this.fillInternalTankFromFluidContainer(this.importFluids);
        }
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new NotifiableItemStackHandler(this, 2, this, false);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new NotifiableItemStackHandler(this, this.getTier() * 3 + 9, this, false);
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        return new FluidTankList(true, new FilteredFluidHandler(64000)
                .setFilter(fluidStack -> Objects.requireNonNull(Materials.DrillingFluid.getFluid(0)).isFluidEqual(fluidStack)));
    }

    @Override
    public @Nonnull ModularPanel buildUI(MetaTileEntityGuiData data, PanelSyncManager syncManager, UISettings settings) {
        syncManager.syncValue("progress", new DoubleSyncValue(() -> this.workableHandler.getProgress() == 0 ? 0 :
                1.0 * this.workableHandler.getProgress() / this.workableHandler.getMaxProgress()));
        final StringSyncValue fluidName = new StringSyncValue(() -> this.importFluids.getTankAt(0).getFluid() != null ?
                this.importFluids.getTankAt(0).getFluid().getLocalizedName() : "");
        syncManager.syncValue("fluid_name", fluidName);
        final BooleanSyncValue renderBox = new BooleanSyncValue(() -> this.playersSeeBoundingBox.contains(data.getPlayer().getUniqueID()),
                bool -> this.addPlayerToSeeBoundingBox(bool, data.getPlayer().getUniqueID()));
        syncManager.syncValue("render_box", renderBox);
        final BooleanSyncValue silktouch = new BooleanSyncValue(this.workableHandler::isSilkTouch, this.workableHandler::setSilkTouch);
        syncManager.syncValue("silktouch", silktouch);
        final BooleanSyncValue itemAutoOutput = new BooleanSyncValue(this::isAutoOutputItems, this::setItemAutoOutput);
        syncManager.syncValue("item_auto_output", itemAutoOutput);
        final BooleanSyncValue isWorking = new BooleanSyncValue(this.workableHandler::isWorkingEnabled, this::setWorkingEnabled);
        syncManager.syncValue("is_working", isWorking);
        syncManager.registerSlotGroup(new SlotGroup("item_input", 1, 1, true));

        final IPanelHandler oreDictPanel = syncManager.syncedPanel("ore_dict.panel", true,
                (panel, handler) -> this.workableHandler.getOreDictFilter().createPopupPanel(panel, "ore_dict.panel"));
        syncManager.syncValue("ore_dict", new BooleanSyncValue(oreDictPanel::isPanelOpen, value -> {
            if (!value && oreDictPanel.isPanelOpen()) {
                oreDictPanel.closePanel();
            } else if (value)
                oreDictPanel.openPanel();
        }));
        final IPanelHandler filterPanel = syncManager.syncedPanel("filter.panel", true, this::createFilterPanel);
        syncManager.syncValue("filter", new BooleanSyncValue(filterPanel::isPanelOpen, value -> {
            if (!value && filterPanel.isPanelOpen()) {
                filterPanel.closePanel();
            } else if (value)
                filterPanel.openPanel();
        }));
        final Icon detail = GTGuiTextures.BUTTON_POWER_DETAIL.asIcon().size(18, 6).marginTop(24);
        return ModularPanel.defaultPanel("advanced_chunk_miner.gui", 176, 186)
                .child(GTGuiTextures.DISPLAY.asWidget()
                        .pos(7, 6)
                        .size(81, 71))
                .child(new ProgressWidget()
                        .pos(90, 33)
                        .size(22)
                        .texture(GTGuiTextures.PROGRESS_BAR_ARROW, 22)
                        .syncHandler("progress"))
                .child(new GTFluidSlot()
                        .pos(69, 58)
                        .syncHandler(this.importFluids.getTankAt(0)))
                .child(new RichTextWidget()
                        .pos(11, -15)
                        .size(80)
                        .autoUpdate(true)
                        .textBuilder(richText -> richText.addLine(IKey.lang("gregtech.fluid.amount",
                                this.importFluids.getTankAt(0).getFluidAmount(), this.importFluids.getTankAt(0).getCapacity()))
                                .addLine(IKey.str(fluidName.getStringValue())
                                        .style(TextFormatting.WHITE))
                                .addLine(IKey.lang(this.getDoneText()))))
                .child(new ItemSlot()
                        .pos(90, 8)
                        .background(GTGuiTextures.SLOT, GTGuiTextures.IN_SLOT_OVERLAY)
                        .slot(new ModularSlot(this.importItems, 0)
                                .slotGroup("item_input")))
                .child(new ItemSlot()
                        .pos(90, 60)
                        .background(GTGuiTextures.SLOT, GTGuiTextures.OUT_SLOT_OVERLAY)
                        .slot(new ModularSlot(this.importItems, 1)
                                .canPut(false)))
                .child(new Grid()
                        .pos(113, 8)
                        .size(58, 72)
                        .scrollable(new VerticalScrollData() {{
                            this.setScrollSize(exportItems.getSlots() * 18 / 3);
                        }})
                        .gridOfSizeWidth(this.exportItems.getSlots(), 3, (x, y, i) -> new ItemSlot()
                                .slot(new ModularSlot(this.exportItems, i)
                                        .canPut(false))))
                .child(new ToggleButton()
                        .pos(7, 82)
                        .overlay(GTGuiTextures.BOX_OVERLAY)
                        .addTooltipLine(IKey.lang("tja.multiblock.advanced_large_miner.mining_area_bounding_box"))
                        .syncHandler("render_box"))
                .child(new ToggleButton()
                        .pos(25, 82)
                        .overlay(TJAGuiTextures.TOGGLE_RESET_BUTTON)
                        .addTooltipLine(IKey.lang("tja.machine.universal.reset"))
                        .syncHandler("reset"))
                .child(new ToggleButton()
                        .pos(43, 82)
                        .overlay(GTGuiTextures.FILTER_SETTINGS_OVERLAY)
                        .syncHandler("filter"))
                .child(new ToggleButton()
                        .pos(61, 82)
                        .overlay(GTGuiTextures.FILTER_SETTINGS_OVERLAY)
                        .syncHandler("ore_dict"))
                .child(new ToggleButton()
                        .pos(79, 82)
                        .overlay(new ItemDrawable(Blocks.WEB))
                        .tooltipDynamic(richTooltip -> richTooltip.addLine(IKey.lang(silktouch.getBoolValue() ?
                                "tja.multiblock.advanced_large_miner.silktouch_true" : "tja.multiblock.advanced_large_miner.silktouch_false"))
                                .addLine(IKey.lang("tja.multiblock.advanced_large_miner.silktouch")
                                        .style(TextFormatting.GRAY)))
                        .syncHandler("silktouch"))
                .child(new ToggleButton()
                        .pos(133, 82)
                        .overlay(GTGuiTextures.BUTTON_ITEM_OUTPUT)
                        .tooltipDynamic(richTooltip -> richTooltip.addLine(IKey.lang(itemAutoOutput.getBoolValue() ?
                                "gregtech.gui.item_auto_input.tooltip.enabled" : "gregtech.gui.item_auto_input.tooltip.disabled"))
                                .addLine(IKey.lang("tja.machine.universal.auto_output")
                                        .style(TextFormatting.GRAY)))
                        .syncHandler("item_auto_output"))
                .child(new ToggleButton()
                        .pos(151, 82)
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

    private ModularPanel createFilterPanel(PanelSyncManager syncManager, IPanelHandler panelHandler) {
        return ModularPanel.defaultPanel("filter.panel", 174, 80)
                .child(new Grid()
                        .pos(4, 22)
                        .size(166, 54)
                        .gridOfSizeWidth(this.filterInventory.size(), 9, (x, y, i) -> new TJAPhantomItemSlotWidget()
                                .background(GTGuiTextures.SLOT, GTGuiTextures.FILTER_SLOT_OVERLAY)
                                .syncHandler(new TJAPhantomItemSlotSH(this.filterInventory, i, this.workableHandler::addItemToFilter,
                                        this.workableHandler::removeItemFromFilter))))
                .child(new ButtonWidget<>()
                        .pos(157, 5)
                        .size(12)
                        .overlay(GuiTextures.CLOSE)
                        .onMousePressed(mouseButton -> {
                            panelHandler.closePanel();
                            return true;
                        }));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.SCREEN.renderSided(EnumFacing.UP, renderState, translation, pipeline);
        for (EnumFacing facing : EnumFacing.HORIZONTALS)
            if (facing != this.getOutputFacing())
                Textures.CHUNK_MINER_OVERLAY.renderSided(facing, renderState, translation, pipeline);
        Textures.PIPE_OUT_OVERLAY.renderSided(this.getOutputFacing(), renderState, translation, pipeline);
        if (this.isAutoOutputItems())
            Textures.ITEM_OUTPUT_OVERLAY.renderSided(this.getOutputFacing(), renderState, translation, pipeline);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("filterInventory", this.filterInventory.serializeNBT());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.filterInventory.deserializeNBT(data.getCompoundTag("filterInventory"));
    }

    private void addPlayerToSeeBoundingBox(boolean value, UUID uuid) {
        if (!value && this.playersSeeBoundingBox.contains(uuid)) {
            this.playersSeeBoundingBox.remove(uuid);
        } else if (value)
            this.playersSeeBoundingBox.add(uuid);
        this.writeCustomData(TJADataCodes.RENDER_BOX, buffer -> buffer.writeUniqueId(uuid)
                .writeBoolean(value));
    }

    @Override
    public void receiveCustomData(int dataId, @Nonnull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == TJADataCodes.RENDER_BOX) {
            final UUID uuid = buf.readUniqueId();
            final boolean value = buf.readBoolean();
            if (!value && this.playersSeeBoundingBox.contains(uuid)) {
                this.playersSeeBoundingBox.remove(uuid);
            } else if (value)
                this.playersSeeBoundingBox.add(uuid);
        }
    }

    @Override
    public void onPlacement(@Nullable EntityLivingBase placer) {
        super.onPlacement(placer);
        if (this.getWorld().isRemote)
            this.initializeMiningAreaBoundingBox();
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        if (this.getWorld().isRemote)
            this.removeMiningAreaBoundingBox();
    }

    @SideOnly(Side.CLIENT)
    private void initializeMiningAreaBoundingBox() {
        final String key = this.getMetaFullName() + "." + this.getPos() + "." + this.getWorld().provider.getDimension();
        RenderEventHandler.addToRenderWorldLastEvent(key, event -> {
            final EntityPlayer player = Minecraft.getMinecraft().player;
            if (player == null) return;
            if (!this.playersSeeBoundingBox.contains(player.getUniqueID())) return;

            GlStateManager.pushMatrix();
            GlStateManager.disableCull();
            GlStateManager.disableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();

            final Chunk chunk = this.getWorld().getChunk(this.getPos());
            final float partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();
            final double playerX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
            final double playerY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
            final double playerZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
            // offset rendering position relative to player camera position
            GlStateManager.translate(-playerX + chunk.x * 16, -playerY, -playerZ + chunk.z * 16);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 0.3f);

            TJARenderUtil.drawCube(2, 0, 256);

            GlStateManager.disableBlend();
            GlStateManager.glLineWidth(4);
            GlStateManager.scale(16f, 16f, 16f);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            final Tessellator tessellator = Tessellator.getInstance();
            final BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);

            final int diameter = this.getDiameter();
            final int reminder = diameter % 2;
            final int start = diameter / 2;
            final int end = start + reminder;
            for (int i = -start; i < end; i++) {
                for (int j = -start; j < end; j++) {
                    final int x = j % diameter;
                    final int z = j / diameter + i;
                    buffer.pos(x, 0, z).endVertex();
                    buffer.pos(x + 1, 0, z).endVertex();

                    buffer.pos(x + 1, 0, z).endVertex();
                    buffer.pos(x + 1, 0, z + 1).endVertex();

                    buffer.pos(x + 1, 0, z + 1).endVertex();
                    buffer.pos(x, 0, z + 1).endVertex();

                    buffer.pos(x, 0, z + 1).endVertex();
                    buffer.pos(x, 0, z).endVertex();

                    buffer.pos(x, 0, z).endVertex();
                    buffer.pos(x, 16, z).endVertex();

                    buffer.pos(x + 1, 0, z).endVertex();
                    buffer.pos(x + 1, 16, z).endVertex();

                    buffer.pos(x + 1, 0, z + 1).endVertex();
                    buffer.pos(x + 1, 16, z + 1).endVertex();

                    buffer.pos(x, 0, z + 1).endVertex();
                    buffer.pos(x, 16, z + 1).endVertex();

                    buffer.pos(x, 16, z).endVertex();
                    buffer.pos(x + 1, 16, z).endVertex();

                    buffer.pos(x + 1, 16, z).endVertex();
                    buffer.pos(x + 1, 16, z + 1).endVertex();

                    buffer.pos(x + 1, 16, z + 1).endVertex();
                    buffer.pos(x, 16, z + 1).endVertex();

                    buffer.pos(x, 16, z + 1).endVertex();
                    buffer.pos(x, 16, z).endVertex();
                }
            }
            tessellator.draw();
            GlStateManager.popMatrix();
        });
    }

    @Override
    public IItemHandlerModifiable getExportItemInventory() {
        final TileEntity tileEntity = this.getWorld().getTileEntity(this.getPos().offset(this.getOutputFacing()));
        final IItemHandler itemHandler = tileEntity != null ?
                tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, this.getOutputFacing()) : null;
        return itemHandler instanceof IItemHandlerModifiable ? (IItemHandlerModifiable) itemHandler : super.getExportItemInventory();
    }

    @SideOnly(Side.CLIENT)
    private void removeMiningAreaBoundingBox() {
        final String key = this.getMetaFullName() + "." + this.getPos() + "." + this.getWorld().provider.getDimension();
        RenderEventHandler.removeFromRenderWorldLastEvent(key);
    }

    private String getDoneText() {
        return this.workableHandler.isDone() ? I18n.format("gregtech.multiblock.large_miner.done") : "";
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.workableHandler.isWorkingEnabled();
    }

    @Override
    public void setWorkingEnabled(boolean b) {
        this.workableHandler.setWorkingEnabled(b);
    }

    @Override
    public EnumFacing getInitialFacing() {
        return this.frontFacing;
    }

    @Override
    public int getFortuneLvl() {
        return 0;
    }

    @Override
    public int getDiameter() {
        return this.getTier();
    }

    @Override
    public FluidStack getDrillingFluid() {
        return this.drillingFluid;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SoundEvent getSound() {
        return GTSoundEvents.MINER;
    }
}
