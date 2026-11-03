package tja.machines.multiblocks;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregicality.multiblocks.api.capability.IParallelHatch;
import gregicality.multiblocks.api.metatileentity.GCYMMultiblockAbility;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.ui.MultiblockUIBuilder;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.KeyUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.core.sound.GTSoundEvents;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tja.TJAConfig;
import tja.TJAValues;
import tja.capability.workables.EnchanterWorkableHandler;
import tja.machines.controllers.TJAMultiblockControllerBase;
import tja.mui.MUIUtils;
import tja.textures.TJATextures;
import tja.util.EnumFacingHelper;
import tja.util.TJAUtility;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static gregtech.api.util.RelativeDirection.*;

public class MetaTileEntityLargeEnchanter extends TJAMultiblockControllerBase {

    private final EnchanterWorkableHandler workableHandler = new EnchanterWorkableHandler(this);

    public MetaTileEntityLargeEnchanter(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityLargeEnchanter(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, @Nonnull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("tja.multiblock.large_enchanter.level.max", TJAValues.thousandFormat.format(this.getTier())));
        if (TJAValues.isModLoaded(TJAValues.GCYM_MOD_ID)) {
            tooltip.add(I18n.format("tja.multiblock.parallel_hatch.eut"));
        } else tooltip.add(I18n.format("tja.multiblock.parallel_tier", TJAConfig.largeEnchanter.stack));
    }

    @Override
    protected void updateFormedValid() {
        if (((this.getMaintenanceProblems() >> 5) & 1) != 0)
            this.workableHandler.update();
    }

    @Override
    protected @Nonnull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(RIGHT, FRONT, DOWN)
                .aisle("       ", "       ", "   C   ", "  CCC  ", "   C   ", "       ", "       ")
                .aisle("       ", "       ", "  CCC  ", "  CVC  ", "  CCC  ", "       ", "       ")
                .aisle("       ", " C   C ", "  CGC  ", "  G#G  ", "  CGC  ", " C   C ", "       ")
                .aisle("       ", " CFFFC ", " FCCCF ", " FCCCF ", " FCCCF ", " CFFFC ", "       ")
                .aisle("       ", " CXGXC ", " X###X ", " G###G ", " X###X ", " CXGXC ", "       ")
                .aisle("       ", " CGGGC ", " GBBBG ", " GB#BG ", " GBBBG ", " CGGGC ", "       ")
                .aisle("       ", " CGGGC ", " GBBBG ", " GB#BG ", " GBBBG ", " CGGGC ", "       ")
                .aisle("       ", " CGGGC ", " GBBBG ", " GB#BG ", " GBBBG ", " CGGGC ", "       ")
                .aisle("       ", " XXSXX ", " XOOOX ", " XOOOX ", " XOOOX ", " XXXXX ", "       ")
                .aisle(" XXXXX ", "XXXXXXX", "XXXXXXX", "XXXXXXX", "XXXXXXX", "XXXXXXX", " XXXXX ")
                .where('S', this.selfPredicate())
                .where('C', states(this.getCasingState()))
                .where('X', states(this.getCasingState()).setMinGlobalLimited(64)
                        .or(abilities(MultiblockAbility.IMPORT_ITEMS, MultiblockAbility.EXPORT_ITEMS, MultiblockAbility.IMPORT_FLUIDS))
                        .or(abilities(MultiblockAbility.INPUT_ENERGY, MultiblockAbility.INPUT_LASER).setMaxGlobalLimited(1))
                        .or(abilities(MultiblockAbility.MAINTENANCE_HATCH).setExactLimit(1))
                        .or(parallelHatchPredicate()))
                .where('G', glassPredicate())
                .where('V', tieredHatchPredicate())
                .where('O', blocks(Blocks.OBSIDIAN))
                .where('B', blocks(Blocks.BOOKSHELF))
                .where('F', frames(Materials.Titanium))
                .where('#', air())
                .build();
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        final int tier = context.getOrDefault("hatch_tier", new IntArrayList()).getInt(0);
        if (tier >= GTValues.MAX) {
            this.maxVoltage = this.inputEnergyContainer.getInputVoltage();
            this.maxVoltage += this.maxVoltage / Integer.MAX_VALUE;
        } else this.maxVoltage = 8L << tier * 2;
        this.tier = TJAUtility.getTierByVoltage(this.maxVoltage);
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
        TJATextures.ENCHANTED_BOOK.renderSided(this.frontFacing.getOpposite(), renderState, translation, pipeline);
        TJATextures.ENCHANTED_BOOK.renderSided(EnumFacingHelper.getLeftFacingFrom(this.frontFacing), renderState, translation, pipeline);
        TJATextures.ENCHANTED_BOOK.renderSided(EnumFacingHelper.getRightFacingFrom(this.frontFacing), renderState, translation, pipeline);
        TJATextures.ENCHANTING_TABLE.renderSided(EnumFacingHelper.getTopFacingFrom(this.frontFacing), renderState, translation, pipeline);
    }

    @Override
    protected void configureDisplayText(MultiblockUIBuilder builder) {
        builder.addEnergyUsageLine(this.inputEnergyContainer)
                .addEnergyTierLine(this.tier)
                .addParallelsLine(this.getParallel())
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
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.TITANIUM_STABLE);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.ROBUST_TUNGSTENSTEEL_CASING;
    }

    @Override
    public int getParallel() {
        if (TJAValues.isModLoaded(TJAValues.GCYM_MOD_ID)) {
            final List<IParallelHatch> parallelHatches = this.getAbilities(GCYMMultiblockAbility.PARALLEL_HATCH);
            return parallelHatches != null && !parallelHatches.isEmpty() ? parallelHatches.get(0).getCurrentParallel() : 1;
        } else return TJAConfig.largeArchitectWorkbench.stack * tier;
    }

    @Override
    public boolean hasFreeParallels() {
        return false;
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
        return GTSoundEvents.ARC;
    }
}
