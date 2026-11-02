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
import com.cleanroommc.modularui.widgets.ProgressWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import gregtech.api.capability.impl.GhostCircuitItemStackHandler;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.MetaTileEntityGuiData;
import gregtech.api.mui.widget.GhostCircuitSlotWidget;
import gregtech.client.renderer.texture.Textures;
import gregtech.core.sound.GTSoundEvents;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import tja.capability.workables.ChiselWorkbenchWorkableHandler;
import tja.textures.TJATextures;
import tja.util.EnumFacingHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class MetaTileEntityChiselWorkbench extends TJATieredWorkableMetaTileEntity {

    private final ChiselWorkbenchWorkableHandler workableHandler = new ChiselWorkbenchWorkableHandler(this);
    private final GhostCircuitItemStackHandler circuitSlot = new GhostCircuitItemStackHandler(this);
    private final GhostCircuitItemStackHandler circuitSlot1 = new GhostCircuitItemStackHandler(this);
    private final IItemHandlerModifiable combinedInventory;
    private final int parallel;

    public MetaTileEntityChiselWorkbench(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        this.parallel = this.getTier();
        this.combinedInventory = new ItemHandlerList(Arrays.asList(this.circuitSlot, this.circuitSlot1, this.importItems));
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityChiselWorkbench(this.metaTileEntityId, this.getTier());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, @Nonnull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("tja.multiblock.parallel", this.parallel));
        tooltip.add(I18n.format("tja.multiblock.large_chisel_workbench.description"));
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new NotifiableItemStackHandler(this, 3, this, false);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new NotifiableItemStackHandler(this,4, this, true);
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
        syncManager.syncValue("item_input", new BooleanSyncValue(this::isAutoOutputItems, this::setItemAutoOutput));
        syncManager.registerSlotGroup(new SlotGroup("item_input", 2, 1, true));

        return ModularPanel.defaultPanel("chisel_workbench.gui", 176, 166)
                .child(IKey.lang(this.getMetaFullName()).asWidget()
                        .pos(7, 5))
                .child(Flow.row()
                        .pos(16, 22)
                        .size(54, 18)
                        .children(this.importItems.getSlots(), i -> new ItemSlot()
                                .background(GTGuiTextures.SLOT, GTGuiTextures.MOLD_OVERLAY)
                                .slot(new ModularSlot(this.importItems, i)
                                        .slotGroup("item_input"))))
                .child(new Grid()
                        .pos(105, 22)
                        .size(36)
                        .gridOfSizeWidth(this.exportItems.getSlots(), 2, (x, y, i) -> new ItemSlot()
                                .slot(new ModularSlot(this.exportItems, i)
                                        .canPut(false))))
                .child(new GhostCircuitSlotWidget()
                        .pos(133, 62)
                        .background(GTGuiTextures.SLOT, GTGuiTextures.INT_CIRCUIT_OVERLAY)
                        .slot(this.circuitSlot, 0))
                .child(new GhostCircuitSlotWidget()
                        .pos(151, 62)
                        .background(GTGuiTextures.SLOT, GTGuiTextures.INT_CIRCUIT_OVERLAY)
                        .slot(this.circuitSlot1, 0))
                .child(new ToggleButton()
                        .pos(7, 62)
                        .background(GTGuiTextures.BUTTON_ITEM_OUTPUT)
                        .syncHandler("item_input"))
                .child(new ProgressWidget()
                        .leftRel(0.5f)
                        .topRelAnchor(0.25f, 0.5f)
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
        TJATextures.CHISEL.renderSided(this.frontFacing.getOpposite(), renderState, translation, pipeline);
        TJATextures.CHISEL.renderSided(EnumFacingHelper.getLeftFacingFrom(this.frontFacing), renderState, translation, pipeline);
        TJATextures.CHISEL.renderSided(EnumFacingHelper.getRightFacingFrom(this.frontFacing), renderState, translation, pipeline);
        Textures.PIPE_OUT_OVERLAY.renderSided(this.getOutputFacing(), renderState, translation, pipeline);
        if (this.isAutoOutputItems())
            Textures.ITEM_OUTPUT_OVERLAY.renderSided(this.getOutputFacing(), renderState, translation, pipeline);
        if (this.isAutoOutputFluids())
            Textures.FLUID_OUTPUT_OVERLAY.renderSided(this.getOutputFacing(), renderState, translation, pipeline);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        final NBTTagCompound compound = new NBTTagCompound();
        final NBTTagCompound compound1 = new NBTTagCompound();
        this.circuitSlot.write(compound);
        this.circuitSlot1.write(compound1);
        data.setTag("ghostCircuit", compound);
        data.setTag("ghostCircuit1", compound1);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.circuitSlot.read(data.getCompoundTag("ghostCircuit"));
        this.circuitSlot1.read(data.getCompoundTag("ghostCircuit1"));
    }

    @Override
    public IItemHandlerModifiable getImportItemInventory() {
        return this.combinedInventory;
    }

    @Override
    public int getParallel() {
        return this.parallel;
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
        return GTSoundEvents.CUT;
    }
}
