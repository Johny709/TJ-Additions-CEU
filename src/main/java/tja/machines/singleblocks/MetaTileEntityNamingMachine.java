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
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.MetaTileEntityGuiData;
import gregtech.client.renderer.texture.Textures;
import gregtech.core.sound.GTSoundEvents;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import tja.capability.handler.INameHandler;
import tja.capability.workables.NamingMachineWorkableHandler;
import tja.textures.TJATextures;
import tja.util.EnumFacingHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityNamingMachine extends TJATieredWorkableMetaTileEntity implements INameHandler {

    private final NamingMachineWorkableHandler workableHandler = new NamingMachineWorkableHandler(this);
    private final int parallel;
    @Nonnull
    private String name = "";

    public MetaTileEntityNamingMachine(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        this.parallel = this.getTier();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityNamingMachine(this.metaTileEntityId, this.getTier());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, @Nonnull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("tja.multiblock.parallel", this.parallel));
        tooltip.add(I18n.format("tja.machine.naming_machine.description"));
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new NotifiableItemStackHandler(this, 2, this, false);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new NotifiableItemStackHandler(this, 4, this, true);
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

        return ModularPanel.defaultPanel("architect_workbench.gui", 176, 166)
                .child(IKey.lang(this.getMetaFullName()).asWidget()
                        .pos(7, 5))
                .child(Flow.col()
                        .pos(34, 22)
                        .size(18, 36)
                        .children(this.importItems.getSlots(), i -> new ItemSlot()
                                .slot(new ModularSlot(this.importItems, i)
                                        .slotGroup("item_input"))))
                .child(new Grid()
                        .pos(105, 22)
                        .size(36)
                        .gridOfSizeWidth(this.exportItems.getSlots(), 2, (x, y, i) -> new ItemSlot()
                                .slot(new ModularSlot(this.exportItems, i)
                                        .canPut(false))))
                .child(new TextFieldWidget()
                        .pos(34, 5)
                        .size(107, 18)
                        .autoUpdateOnChange(true)
                        .value(new StringSyncValue(this::getName, this::setName)))
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
        TJATextures.NAME_TAG.renderSided(EnumFacingHelper.getLeftFacingFrom(this.getFrontFacing()), renderState, translation, pipeline);
        TJATextures.NAME_TAG.renderSided(EnumFacingHelper.getRightFacingFrom(this.getFrontFacing()), renderState, translation, pipeline);
        Textures.PIPE_OUT_OVERLAY.renderSided(this.getOutputFacing(), renderState, translation, pipeline);
        if (this.isAutoOutputItems())
            Textures.ITEM_OUTPUT_OVERLAY.renderSided(this.getOutputFacing(), renderState, translation, pipeline);
        if (this.isAutoOutputFluids())
            Textures.FLUID_OUTPUT_OVERLAY.renderSided(this.getOutputFacing(), renderState, translation, pipeline);
    }

    public void setName(@Nonnull String name) {
        this.name = name;
        this.markDirty();
    }

    @Nonnull
    @Override
    public String getName() {
        return this.name;
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
        return GTSoundEvents.ASSEMBLER;
    }
}
