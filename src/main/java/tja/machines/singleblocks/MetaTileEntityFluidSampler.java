package tja.machines.singleblocks;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.DoubleSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.MetaTileEntityGuiData;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import tja.capability.workables.FluidSamplerWorkableHandler;
import tja.textures.TJATextures;

import javax.annotation.Nonnull;

public class MetaTileEntityFluidSampler extends TJATieredMetaTileEntity {

    private final FluidSamplerWorkableHandler workableHandler = new FluidSamplerWorkableHandler(this);

    public MetaTileEntityFluidSampler(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityFluidSampler(this.metaTileEntityId, this.getTier());
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new ItemStackHandler(1);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new ItemStackHandler(1);
    }

    @Override
    public void update() {
        super.update();
        if (!this.getWorld().isRemote) {
            this.workableHandler.update();
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        TJATextures.PRINTER_OVERLAY.render(renderState, translation, pipeline, this.frontFacing, this.workableHandler.isActive());
    }

    @Override
    public @Nonnull ModularPanel buildUI(MetaTileEntityGuiData metaTileEntityGuiData, PanelSyncManager panelSyncManager, UISettings uiSettings) {
        panelSyncManager.syncValue("progress", new DoubleSyncValue(() -> this.workableHandler.getProgress() == 0 ? 0 :
                1.0 * this.workableHandler.getProgress() / this.workableHandler.getMaxProgress()));
        panelSyncManager.registerSlotGroup(new SlotGroup("item_input", 1, 1, true));

        return ModularPanel.defaultPanel("mte.fluid_sampler")
                .child(new TextWidget<>(IKey.lang(this.getMetaFullName()))
                        .pos(7, 5))
                .child(new ProgressWidget()
                        .size(22)
                        .leftRel(0.5f)
                        .topRelAnchor(0.25f, 0.5f)
                        .texture(GTGuiTextures.PROGRESS_BAR_ARROW, 22)
                        .syncHandler("progress"))
                .child(new ItemSlot()
                        .leftRel(0.3f)
                        .topRelAnchor(0.25f, 0.5f)
                        .slot(new ModularSlot(this.importItems, 0)
                                .slotGroup("item_input")))
                .child(new ItemSlot()
                        .leftRel(0.7f)
                        .topRelAnchor(0.25f, 0.5f)
                        .slot(new ModularSlot(this.exportItems, 0)
                                .canPut(false)))
                .bindPlayerInventory();
    }

    @Override
    public boolean isActive() {
        return this.workableHandler.isActive();
    }
}
