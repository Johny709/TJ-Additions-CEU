package tja.machines.singleblocks;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.FluidSlotSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.slot.FluidSlot;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.AbilityInstances;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.mui.IMetaTileEntityGuiHolder;
import gregtech.api.mui.MetaTileEntityGuiData;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockNotifiablePart;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tja.TJAValues;
import tja.textures.TJATextures;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MetaTileEntityCreativeFluidHatch extends MetaTileEntityMultiblockNotifiablePart implements IMetaTileEntityGuiHolder, IMultiblockAbilityPart<IFluidTank> {

    public MetaTileEntityCreativeFluidHatch(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GTValues.MAX, false);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityCreativeFluidHatch(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("metaitem.creative_cover.tooltip.1"));
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        return new FluidTankList(true, IntStream.range(0, 16)
                .mapToObj(i -> new FluidTank(Integer.MAX_VALUE) {
                    @Override
                    public FluidStack drain(FluidStack resource, boolean doDrain) {
                        FluidStack fluidStack = this.getFluid();
                        if (fluidStack == null || !fluidStack.isFluidEqual(resource)) return null;
                        fluidStack = fluidStack.copy();
                        fluidStack.amount = Math.min(fluidStack.amount, resource.amount);
                        return fluidStack;
                    }

                    @Override
                    public FluidStack drain(int maxDrain, boolean doDrain) {
                        if (maxDrain == Integer.MIN_VALUE)
                            super.drain(Integer.MAX_VALUE, doDrain);
                        FluidStack fluidStack = this.getFluid();
                        if (fluidStack == null) return null;
                        fluidStack = fluidStack.copy();
                        fluidStack.amount = Math.min(fluidStack.amount, maxDrain);
                        return fluidStack;
                    }
                }).collect(Collectors.toList()));
    }

    @Override
    public @Nonnull ModularPanel buildUI(MetaTileEntityGuiData metaTileEntityGuiData, PanelSyncManager panelSyncManager, UISettings uiSettings) {
        return ModularPanel.defaultPanel("creative_fluid_hatch.gui", 196, 184)
                .child(new TextWidget<>(IKey.lang(this.getMetaFullName()))
                        .pos(7, 5))
                .child(new Grid()
                        .pos(43, 24)
                        .size(72, 72)
                        .gridOfSizeWidth(this.importFluids.getTanks(), 4, (x, y, i) -> new FluidSlot()
                                .syncHandler(new FluidSlotSyncHandler(this.importFluids.getTankAt(i))
                                        .phantom(true))))
                .bindPlayerInventory();
    }

    @Override
    public void registerAbilities(@Nonnull AbilityInstances abilityInstances) {
        abilityInstances.add(this.importFluids);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (this.getController() == null) {
            final int oldBaseColor = renderState.baseColour;
            final int oldAlphaOverride = renderState.alphaOverride;

            renderState.baseColour = TJAValues.VC[this.getTier() - 2] << 8; // TODO get better MAX color overlay. use UMV color overlay for the time being
            renderState.alphaOverride = 0xFF;

            for (EnumFacing facing : EnumFacing.VALUES)
                TJATextures.SUPER_HATCH_OVERLAY.renderSided(facing, renderState, translation, pipeline);

            renderState.baseColour = oldBaseColor;
            renderState.alphaOverride = oldAlphaOverride;
        }
        Textures.PIPE_IN_OVERLAY.renderSided(this.getFrontFacing(), renderState, translation, pipeline);
        Textures.FLUID_HATCH_INPUT_OVERLAY.renderSided(this.getFrontFacing(), renderState, translation, pipeline);
    }

    @Override
    public @Nullable MultiblockAbility<IFluidTank> getAbility() {
        return MultiblockAbility.IMPORT_FLUIDS;
    }
}
