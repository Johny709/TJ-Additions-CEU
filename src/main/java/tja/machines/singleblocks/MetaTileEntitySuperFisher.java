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
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.MetaTileEntityGuiData;
import gregtech.client.renderer.texture.Textures;
import gregtech.core.sound.GTSoundEvents;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import tja.capability.handler.IPosHandler;
import tja.capability.workables.FisherWorkableHandler;
import tja.textures.TJATextures;
import tja.util.EnumFacingHelper;
import tja.util.TJAItemUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntitySuperFisher extends TJATieredWorkableMetaTileEntity implements IPosHandler {

    public static final ItemStack STRING = TJAItemUtils.getItemStackFromName("minecraft:string");
    private final FisherWorkableHandler workableHandler = new FisherWorkableHandler(this);
    private final int parallel;

    public MetaTileEntitySuperFisher(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        this.parallel = this.getTier();
        this.initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntitySuperFisher(this.metaTileEntityId, this.getTier());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, @Nonnull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("tja.multiblock.parallel", this.parallel));
        tooltip.add(I18n.format("gregtech.machine.fisher.speed", Math.max(1, 3200 / (2 << this.getTier()))));
        tooltip.add(I18n.format("gregtech.machine.fisher.requirement", 5, 5));
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new NotifiableItemStackHandler(this, 1, this, false);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new NotifiableItemStackHandler(this, this.getSlotGrid(this.getTier()), this, true);
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
        syncManager.registerSlotGroup(new SlotGroup("item_input", 2, 1, true));

        final int slotLength = (int) Math.sqrt(this.exportItems.getSlots());
        final int offsetY = Math.min(7, slotLength) * 18;
        return ModularPanel.defaultPanel("architect_workbench.gui", 176, 133 + offsetY)
                .child(IKey.lang(this.getMetaFullName()).asWidget()
                        .pos(7, 5))
                .child(new ItemSlot()
                        .pos(7, 7)
                        .slot(new ModularSlot(this.importItems, 0)
                                .slotGroup("item_input")))
                .child(new Grid()
                        .pos(43, 7)
                        .size(offsetY + 4, offsetY)
                        .scrollable(new VerticalScrollData() {{
                            this.setScrollSize((int) Math.ceil((double) importItems.getSlots() / Math.min(7, slotLength)) * 18);
                        }})
                        .gridOfSizeWidth(this.exportItems.getSlots(), slotLength, (x, y, i) -> new ItemSlot()
                                .slot(new ModularSlot(this.exportItems, i)
                                        .canPut(false))))
                .child(new ToggleButton()
                        .pos(7, 29 + offsetY)
                        .overlay(GTGuiTextures.BUTTON_ITEM_OUTPUT)
                        .tooltipDynamic(richTooltip -> richTooltip.addLine(IKey.lang(itemAutoOutput.getBoolValue() ?
                                "gregtech.gui.item_auto_input.tooltip.enabled" : "gregtech.gui.item_auto_input.tooltip.disabled"))
                                .addLine(IKey.lang("tja.machine.universal.auto_output")
                                        .style(TextFormatting.GRAY)))
                        .syncHandler("item_auto_output"))
                .child(new ProgressWidget()
                        .pos(25, 7)
                        .size(22)
                        .texture(GTGuiTextures.PROGRESS_BAR_ARROW, 22)
                        .syncHandler("progress"))
                .bindPlayerInventory();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.MULTIBLOCK_WORKABLE_OVERLAY.renderOrientedState(renderState, translation, pipeline, this.frontFacing, this.workableHandler.isActive(),
                this.workableHandler.isWorkingEnabled());
        TJATextures.FISHING_ROD.renderSided(EnumFacingHelper.getLeftFacingFrom(this.getFrontFacing()), renderState, translation, pipeline);
        TJATextures.FISHING_ROD.renderSided(EnumFacingHelper.getRightFacingFrom(this.getFrontFacing()), renderState, translation, pipeline);
        Textures.PIPE_OUT_OVERLAY.renderSided(this.getOutputFacing(), renderState, translation, pipeline);
        if (this.isAutoOutputItems())
            Textures.ITEM_OUTPUT_OVERLAY.renderSided(this.getOutputFacing(), renderState, translation, pipeline);
        if (this.isAutoOutputFluids())
            Textures.FLUID_OUTPUT_OVERLAY.renderSided(this.getOutputFacing(), renderState, translation, pipeline);
    }

    private int getSlotGrid(int tier) {
        tier++;
        return tier * tier;
    }

    @Override
    public int getParallel() {
        return this.parallel;
    }

    @Override
    public BlockPos getBlockPos() {
        return this.getPos().offset(EnumFacing.DOWN);
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
    public boolean isActive() {
        return this.workableHandler.isActive();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SoundEvent getSound() {
        return GTSoundEvents.BATH;
    }
}
