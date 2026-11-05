package tja.machines.singleblocks;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import gregtech.api.capability.impl.FilteredFluidHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.MetaTileEntityGuiData;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.texture.Textures;
import gregtech.core.sound.GTSoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import tja.capability.handler.IMinerHandler;
import tja.capability.workables.MinerWorkableHandler;
import tja.items.handlers.GTLargeItemStackHandler;

import javax.annotation.Nonnull;
import java.util.Objects;

public class MetaTileEntityAdvancedChunkMiner extends TJATieredWorkableMetaTileEntity implements IMinerHandler {

    private final MinerWorkableHandler workableHandler = new MinerWorkableHandler(this);
    private final GTLargeItemStackHandler filterInventory = new GTLargeItemStackHandler(this, 27, 1, this, false);
    private final FluidStack drillingFluid;

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
    protected FluidTankList createExportFluidHandler() {
        return new FluidTankList(true, new FilteredFluidHandler(64000)
                .setFilter(fluidStack -> Objects.requireNonNull(Materials.DrillingFluid.getFluid(0)).isFluidEqual(fluidStack)));
    }

    @Override
    public @Nonnull ModularPanel buildUI(MetaTileEntityGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return ModularPanel.defaultPanel("advanced_chunk_miner.gui", 176, 166)
                .bindPlayerInventory();
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
