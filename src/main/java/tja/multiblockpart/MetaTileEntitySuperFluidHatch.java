package tja.multiblockpart;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.scroll.VerticalScrollData;
import com.cleanroommc.modularui.widgets.layout.Grid;
import gregtech.api.GTValues;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.NotifiableFluidTank;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.AbilityInstances;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.mui.IMetaTileEntityGuiHolder;
import gregtech.api.mui.MetaTileEntityGuiData;
import gregtech.api.mui.sync.GTFluidSyncHandler;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockNotifiablePart;
import gregtech.common.mui.widget.GTFluidSlot;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
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

public class MetaTileEntitySuperFluidHatch extends MetaTileEntityMultiblockNotifiablePart implements IMetaTileEntityGuiHolder,
        IMultiblockAbilityPart<IFluidTank> {

    public MetaTileEntitySuperFluidHatch(ResourceLocation metaTileEntityId, int tier, boolean isExportHatch) {
        super(metaTileEntityId, tier, isExportHatch);
        this.initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntitySuperFluidHatch(this.metaTileEntityId, this.getTier(), this.isExportHatch);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, @Nonnull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format(this.isExportHatch ? "gregtech.machine.fluid_hatch.export.tooltip" : "gregtech.machine.fluid_hatch.import.tooltip"));
        tooltip.add(I18n.format("gregtech.universal.tooltip.fluid_storage_capacity_mult", this.getTierGrid(this.getTier()),
                this.getCapacity(this.getTier())));
        tooltip.add(I18n.format("gregtech.universal.enabled"));
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        return this.isExportHatch ? super.createImportFluidHandler() :
                new FluidTankList(true, IntStream.range(0, this.getTierGrid(this.getTier()))
                .mapToObj(i -> new NotifiableFluidTank(this.getCapacity(this.getTier()), this, false))
                .collect(Collectors.toList()));
    }

    @Override
    protected FluidTankList createExportFluidHandler() {
        return !this.isExportHatch ? super.createExportFluidHandler() :
                new FluidTankList(true, IntStream.range(0, this.getTierGrid(this.getTier()))
                .mapToObj(i -> new NotifiableFluidTank(this.getCapacity(this.getTier()), this, false))
                .collect(Collectors.toList()));
    }

    @Override
    public @Nonnull ModularPanel buildUI(MetaTileEntityGuiData data, PanelSyncManager syncManager, UISettings settings) {
        final IMultipleTankHandler tanks = this.isExportHatch ? this.exportFluids : this.importFluids;
        final int slotLength = (int) Math.sqrt(tanks.getTanks());
        final int offsetY = Math.min(9, this.getTier()) * 18;
        final int offsetX = Math.max(7, 88 - (9 * this.getTier()));
        return ModularPanel.defaultPanel("super_fluid_hatch.gui", 194, 133 + offsetY)
                .child(IKey.lang(this.getMetaFullName()).asWidget()
                        .pos(7, 5))
                .child(new Grid()
                        .pos(offsetX, 18)
                        .size(Math.min(184, 4 + 18 * (this.getTier() + 1)), Math.min(180, 18 * (this.getTier() + 1)))
                        .scrollable(new VerticalScrollData() {{
                            this.setScrollSize((int) Math.ceil((double) tanks.getTanks() / Math.min(7, slotLength)) * 18);
                        }})
                        .gridOfSizeWidth(tanks.getTanks(), slotLength, (x, y, i) -> new GTFluidSlot()
                                .syncHandler(new GTFluidSyncHandler(tanks.getTankAt(i))
                                        .accessibility(!this.isExportHatch, true))))
                .bindPlayerInventory();
    }

    @Override
    public void registerAbilities(@Nonnull AbilityInstances abilityInstances) {
        if (abilityInstances.isKey(MultiblockAbility.IMPORT_FLUIDS))
            abilityInstances.add(this.importFluids);
        if (abilityInstances.isKey(MultiblockAbility.EXPORT_FLUIDS))
            abilityInstances.add(this.exportFluids);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (this.getController() == null) {
            final int oldBaseColor = renderState.baseColour;
            final int oldAlphaOverride = renderState.alphaOverride;

            // TODO get better MAX color overlay. use UMV color overlay for the time being
            renderState.baseColour = TJAValues.VC[this.getTier()] << 8;
            renderState.alphaOverride = 0xFF;

            for (EnumFacing facing : EnumFacing.VALUES)
                TJATextures.SUPER_HATCH_OVERLAY.renderSided(facing, renderState, translation, pipeline);

            renderState.baseColour = oldBaseColor;
            renderState.alphaOverride = oldAlphaOverride;
        }
        Textures.PIPE_IN_OVERLAY.renderSided(this.getFrontFacing(), renderState, translation, pipeline);
        Textures.ITEM_HATCH_INPUT_OVERLAY.renderSided(this.getFrontFacing(), renderState, translation, pipeline);
    }

    @Override
    public @Nullable MultiblockAbility<IFluidTank> getAbility() {
        return this.isExportHatch ? MultiblockAbility.EXPORT_FLUIDS : MultiblockAbility.IMPORT_FLUIDS;
    }

    public int getTierGrid(int tier) {
        tier++;
        return tier * tier;
    }

    public int getCapacity(int tier) {
        return (int) Math.min(Integer.MAX_VALUE, (8000L << tier) * (64L << Math.max(0, tier - GTValues.UHV)));
    }
}
