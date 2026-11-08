package tja.machines.singleblocks;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.DoubleSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.scroll.VerticalScrollData;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.slot.FluidSlot;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import com.fulltrix.gcyl.materials.GCYLMaterials;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.capability.impl.NotifiableFluidTank;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.MetaTileEntityGuiData;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.client.renderer.texture.Textures;
import gregtech.core.sound.GTSoundEvents;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import tja.TJAValues;
import tja.capability.handler.IFarmerHandler;
import tja.capability.workables.FarmingStationWorkableHandler;
import tja.event.RenderEventHandler;
import tja.items.handlers.GTLargeItemStackHandler;
import tja.items.handlers.NotifiableFilteredItemStackHandler;
import tja.mui.TJAGuiTextures;
import tja.rendering.TJARenderUtil;
import tja.util.EnumFacingHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class MetaTileEntityFarmingStation extends TJATieredWorkableMetaTileEntity implements IFarmerHandler {

    private final FarmingStationWorkableHandler workableHandler = new FarmingStationWorkableHandler(this);

    private final IItemHandlerModifiable seedInventory = new NotifiableFilteredItemStackHandler(this, 6,
            this.getTier() >= GTValues.ZPM ? 256 : this.getTier() >= GTValues.EV ? 128 : 64, this, false)
            .setItemStackPredicate((slot, stack) -> stack.getItem() instanceof IPlantable ||
                    Block.getBlockFromItem(stack.getItem()) instanceof IPlantable);

    private final IItemHandlerModifiable fertilizerInventory = new NotifiableFilteredItemStackHandler(this, 2,
            this.getTier() >= GTValues.ZPM ? 256 : this.getTier() >= GTValues.EV ? 128 : 64, this, false)
            .setItemStackPredicate((slot, stack) -> ((stack.getItem() instanceof ItemDye && stack.getItem().getMetadata(stack) == 15) ||
                    TJAValues.isModLoaded(TJAValues.GCYL_MOD_ID)) &&
                    stack.isItemEqual(OreDictUnifier.get(OrePrefix.dust, GCYLMaterials.OrganicFertilizer)));

    private final IItemHandlerModifiable toolInventory = new NotifiableFilteredItemStackHandler(this, 3, 1, this, false)
            .setItemStackPredicate((slot, stack) -> {
                final Set<String> toolClasses = stack.getItem().getToolClasses(stack);
                switch (slot) {
                    case 0: return stack.getItem() instanceof ItemHoe || toolClasses.contains(ToolClasses.HOE);
                    case 1: return toolClasses.contains(ToolClasses.AXE) || toolClasses.contains(ToolClasses.SAW);
                    case 2: return stack.getItem() instanceof ItemShears || toolClasses.contains(ToolClasses.SHEARS);
                    default: return false;
                }
            });

    private final IFluidTank waterTank = new NotifiableFluidTank(this.getTier() >= GTValues.ZPM ? 256000 : this.getTier() >= GTValues.EV ? 128000 : 64000,
            this, false);
    private final Set<UUID> playersSeeBoundingBox = new HashSet<>();

    public MetaTileEntityFarmingStation(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        final int range = (9 + (2 * tier)) * (9 + (2 * tier));
        this.workableHandler.initialize(range);
        this.initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityFarmingStation(this.metaTileEntityId, this.getTier());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, @Nonnull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("tja.machine.universal.linked.entity.radius", TJAValues.thousandFormat.format((9 + (2L * this.getTier())) / 2),
                TJAValues.thousandFormat.format((9 + (2L * this.getTier())) / 2)));
        tooltip.add(I18n.format("tja.machine.farming_station.description", this.getTier() >= GTValues.ZPM ? 4 :
                this.getTier() >= GTValues.EV ? 2 : 1));
        tooltip.add(I18n.format("tja.machine.farming_station.fertilizer.tooltip", this.getTier() * 10));
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return this.seedInventory == null ? super.createImportItemHandler() :
                new ItemHandlerList(Arrays.asList(this.seedInventory, this.toolInventory, this.fertilizerInventory));
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new GTLargeItemStackHandler(this, 6 + (3 * this.getTier()),
                this.getTier() >= GTValues.ZPM ? 256 : this.getTier() >= GTValues.EV ? 128 : 64, this, false);
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        return this.waterTank == null ? super.createImportFluidHandler() : new FluidTankList(true, this.waterTank);
    }

    @Override
    public void update() {
        super.update();
        if (!this.getWorld().isRemote)
            this.workableHandler.update();
    }

    @Override
    public @Nonnull ModularPanel buildUI(MetaTileEntityGuiData data, PanelSyncManager syncManager, UISettings settings) {
        syncManager.syncValue("progress", new DoubleSyncValue(() -> this.workableHandler.getProgress() == 0 ? 0 :
                1.0 * this.workableHandler.getProgress() / this.workableHandler.getMaxProgress()));
        final BooleanSyncValue itemAutoOutput = new BooleanSyncValue(this::isAutoOutputItems, this::setItemAutoOutput);
        syncManager.syncValue("item_auto_output", itemAutoOutput);
        syncManager.syncValue("render_box", new BooleanSyncValue(() -> this.playersSeeBoundingBox.contains(data.getPlayer().getUniqueID()),
                bool -> this.addPlayerToSeeBoundingBox(data.getPlayer().getUniqueID())));
        syncManager.registerSlotGroup(new SlotGroup("tool_input", 1, 1, true));
        syncManager.registerSlotGroup(new SlotGroup("fertilizer_input", 2, 2, true));
        syncManager.registerSlotGroup(new SlotGroup("seed_input", 2, 3, true));

        return ModularPanel.defaultPanel("farming_station.gui", 176, 182)
                .child(IKey.lang(this.getMetaFullName()).asWidget()
                        .pos(7, 5))
                .child(new Grid()
                        .pos(7, 22)
                        .size(36, 54)
                        .gridOfSizeWidth(this.seedInventory.getSlots(), 2, (x, y, i) -> new ItemSlot()
                                .slot(new ModularSlot(this.seedInventory, i)
                                        .slotGroup("seed_input"))))
                .child(new Grid()
                        .pos(105, 22)
                        .size(64, 54)
                        .scrollable(new VerticalScrollData(false, 10) {{
                            this.setScrollSize(exportItems.getSlots() * 18 / 3);
                        }})
                        .gridOfSizeWidth(this.exportItems.getSlots(), 3, (x, y, i) -> new ItemSlot()
                                .slot(new ModularSlot(this.exportItems, i)
                                        .canPut(false))))
                .child(Flow.col()
                        .pos(52, 22)
                        .size(18, 54)
                        .children(this.toolInventory.getSlots(), i -> new ItemSlot()
                                .slot(new ModularSlot(this.toolInventory, i)
                                        .slotGroup("tool_input"))))
                .child(Flow.row()
                        .pos(34, 69)
                        .size(18, 36)
                        .children(this.fertilizerInventory.getSlots(), i -> new ItemSlot()
                                .slot(new ModularSlot(this.fertilizerInventory, i)
                                        .slotGroup("fertilizer_input"))))
                .child(new FluidSlot()
                        .pos(105, 78)
                        .tank(this.waterTank))
                .child(new ToggleButton()
                        .pos(7, 78)
                        .overlay(GTGuiTextures.BUTTON_ITEM_OUTPUT)
                        .tooltipDynamic(richTooltip -> richTooltip.addLine(IKey.lang(itemAutoOutput.getBoolValue() ?
                                "gregtech.gui.item_auto_input.tooltip.enabled" : "gregtech.gui.item_auto_input.tooltip.disabled"))
                                .addLine(IKey.lang("tja.machine.universal.auto_output")
                                        .style(TextFormatting.GRAY)))
                        .syncHandler("item_auto_output"))
                .child(new ToggleButton()
                        .pos(133, 78)
                        .stateBackground(TJAGuiTextures.TOGGLE_BOX)
                        .syncHandler("render_box"))
                .child(new ProgressWidget()
                        .pos(77, 39)
                        .size(22)
                        .texture(GTGuiTextures.PROGRESS_BAR_ARROW, 22)
                        .syncHandler("progress"))
                .bindPlayerInventory();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.MULTIBLOCK_WORKABLE_OVERLAY.renderOrientedState(renderState, translation, pipeline, EnumFacingHelper.getTopFacingFrom(this.getFrontFacing()),
                this.workableHandler.isActive(), this.workableHandler.isWorkingEnabled());
        Textures.PIPE_OUT_OVERLAY.renderSided(this.getOutputFacing(), renderState, translation, pipeline);
        if (this.isAutoOutputItems())
            Textures.ITEM_OUTPUT_OVERLAY.renderSided(this.getOutputFacing(), renderState, translation, pipeline);
        if (this.isAutoOutputFluids())
            Textures.FLUID_OUTPUT_OVERLAY.renderSided(this.getOutputFacing(), renderState, translation, pipeline);
    }

    private void addPlayerToSeeBoundingBox(UUID uuid) {
        if (this.playersSeeBoundingBox.contains(uuid)) {
            this.playersSeeBoundingBox.remove(uuid);
        } else this.playersSeeBoundingBox.add(uuid);
        this.writeCustomData(20, buffer -> buffer.writeUniqueId(uuid));
    }

    @Override
    public void receiveCustomData(int dataId, @Nonnull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == 20) {
            final UUID uuid = buf.readUniqueId();
            if (this.playersSeeBoundingBox.contains(uuid)) {
                this.playersSeeBoundingBox.remove(uuid);
            } else this.playersSeeBoundingBox.add(uuid);
        }
    }

    @Override
    public void onPlacement(@Nullable EntityLivingBase placer) {
        super.onPlacement(placer);
        if (this.getWorld().isRemote)
            this.initializeHarvestRangeBox();
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        if (this.getWorld().isRemote)
            this.removeHarvestRangeBox();
    }

    @SideOnly(Side.CLIENT)
    private void initializeHarvestRangeBox() {
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

            final BlockPos pos = this.getPos();
            final float partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();
            final double playerX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
            final double playerY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
            final double playerZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
            // offset rendering position relative to player camera position
            GlStateManager.translate(-playerX + pos.getX(), -playerY + pos.getY(), -playerZ + pos.getZ());
            GlStateManager.color(0, 0, 1.0f, 0.3f);

            final int radius = (int) (9 + (2L * this.getTier())) / 2;
            TJARenderUtil.drawCube(radius, -1, radius + 1);
            GlStateManager.popMatrix();
        });
    }

    @SideOnly(Side.CLIENT)
    private void removeHarvestRangeBox() {
        final String key = this.getMetaFullName() + "." + this.getPos() + "." + this.getWorld().provider.getDimension();
        RenderEventHandler.removeFromRenderWorldLastEvent(key);
    }

    @Override
    public IItemHandlerModifiable getToolInventory() {
        return this.toolInventory;
    }

    @Override
    public IItemHandlerModifiable getFertilizerInventory() {
        return this.fertilizerInventory;
    }

    @Override
    public boolean isActive() {
        return this.workableHandler.isActive();
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
    @SideOnly(Side.CLIENT)
    public SoundEvent getSound() {
        return GTSoundEvents.BATH;
    }
}
