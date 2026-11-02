package tja.machines.multiblocks;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregicality.multiblocks.common.metatileentities.multiblockpart.MetaTileEntityTieredHatch;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.ui.MultiblockUIBuilder;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.util.KeyUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.core.sound.GTSoundEvents;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tja.TJAConfig;
import tja.TJAValues;
import tja.capability.workables.ChiselWorkbenchWorkableHandler;
import tja.machines.controllers.ExtendableMultiblockController;
import tja.mui.MUIUtils;
import tja.textures.TJATextures;
import tja.util.EnumFacingHelper;
import tja.util.TJAUtility;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static gregtech.api.util.RelativeDirection.*;

public class MetaTileEntityLargeChiselWorkbench extends ExtendableMultiblockController {

    private final ChiselWorkbenchWorkableHandler workableHandler = new ChiselWorkbenchWorkableHandler(this);
    private int parallel;

    public MetaTileEntityLargeChiselWorkbench(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityLargeChiselWorkbench(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, @Nonnull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("tja.multiblock.parallel_tier", TJAConfig.largeChiselWorkbench.stack));
        tooltip.add(I18n.format("tja.multiblock.large_chisel_workbench.description"));
    }

    @Override
    protected void updateFormedValid() {
        if (((this.getMaintenanceProblems() >> 5) & 1) != 0)
            this.workableHandler.update();
    }

    @Override
    protected @Nonnull BlockPattern createStructurePattern() {
        final FactoryBlockPattern pattern = FactoryBlockPattern.start(RIGHT, UP, BACK);
        for (int layer = 0; layer < this.parallelLayer; layer++) {
            pattern.aisle("XXX", "XXX", "C C", "C C");
            pattern.aisle("XXX", "XVX", "###", "CVC");
        }
        return pattern.aisle("XXX", "XSX", "C C", "C C")
                .where('S', this.selfPredicate())
                .where('C', states(this.getCasingState()))
                .where('X', states(this.getCasingState()).setMinGlobalLimited(12)
                        .or(abilities(MultiblockAbility.IMPORT_ITEMS, MultiblockAbility.EXPORT_ITEMS))
                        .or(abilities(MultiblockAbility.INPUT_ENERGY, MultiblockAbility.INPUT_LASER).setMaxGlobalLimited(1))
                        .or(abilities(MultiblockAbility.MAINTENANCE_HATCH).setExactLimit(1)))
                .where('V', tieredHatchPredicate())
                .where('#', air())
                .build();
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        final int tier = context.getOrDefault("tiered_hatches", new ArrayList<MetaTileEntityTieredHatch>()).get(0).getTier();
        if (tier >= GTValues.MAX) {
            this.maxVoltage = this.inputEnergyContainer.getInputVoltage();
            this.maxVoltage += this.maxVoltage / Integer.MAX_VALUE;
        } else this.maxVoltage = 8L << tier * 2;
        this.tier = TJAUtility.getTierByVoltage(this.maxVoltage);
        this.parallel = TJAConfig.largeArchitectWorkbench.stack * tier;
        this.workableHandler.initialize(this.getAbilities(MultiblockAbility.IMPORT_ITEMS).size());
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.workableHandler.invalidate();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.MULTIBLOCK_WORKABLE_OVERLAY.renderOrientedState(renderState, translation, pipeline,
                this.frontFacing, this.workableHandler.isActive(), this.workableHandler.isWorkingEnabled());
        TJATextures.CHISEL.renderSided(EnumFacingHelper.getLeftFacingFrom(this.frontFacing), renderState, translation, pipeline);
        TJATextures.CHISEL.renderSided(EnumFacingHelper.getRightFacingFrom(this.frontFacing), renderState, translation, pipeline);
    }

    @Override
    protected void configureDisplayText(MultiblockUIBuilder builder) {
        builder.addEnergyUsageLine(this.inputEnergyContainer)
                .addEnergyTierLine(this.tier)
                .addCustom((key, syncer) -> {
                    if (!this.isStructureFormed()) return;
                    final long energyPerTick = syncer.syncLong(this.workableHandler.getEnergyPerTick());
                    if (energyPerTick < 1) return;
                    key.add(KeyUtil.lang("tja.machine.universal.eut", TJAValues.thousandFormat.format(energyPerTick),
                            GTValues.VOCNF[TJAUtility.getTierFromVoltage(energyPerTick)]));
                })
                .addProgressLine(this.workableHandler.getProgress(), this.workableHandler.getMaxProgress())
                .addRunningPerfectlyLine(this.workableHandler.isActive())
                .setWorkingStatus(this.workableHandler.isWorkingEnabled(), this.workableHandler.isActive())
                .addCustom((key, syncer) -> MUIUtils.addRecipeInputOutputLine(key, syncer, this.workableHandler, this.getWorld()));
    }

    private IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STAINLESS_CLEAN);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.CLEAN_STAINLESS_STEEL_CASING;
    }

    @Override
    public int getParallel() {
        return this.parallel;
    }

    @Override
    public int getMaxParallel() {
        return TJAConfig.largeChiselWorkbench.maximumSlices;
    }

    @Override
    public int getVoidingModeInt() {
        return this.getVoidingMode();
    }

    @Override
    public boolean canBeDistinct() {
        return true;
    }

    @Override
    public boolean isDistinct() {
        return this.workableHandler.isDistinct();
    }

    @Override
    public void setDistinct(boolean b) {
        this.workableHandler.setDistinct(b);
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
        return this.workableHandler.isActive() && super.isActive();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SoundEvent getSound() {
        return GTSoundEvents.CUT;
    }
}
