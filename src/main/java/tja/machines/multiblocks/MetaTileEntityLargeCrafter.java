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
import gregtech.core.sound.GTSoundEvents;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Triple;
import tja.TJAConfig;
import tja.TJAValues;
import tja.blocks.BlockSolidCasings2;
import tja.blocks.TJAMetaBlocks;
import tja.capability.handler.IRecipeMapProvider;
import tja.capability.workables.CrafterRecipeLogic;
import tja.machines.controllers.TJAMultiblockControllerBase;
import tja.mui.MUIUtils;
import tja.recipes.CountableIngredient;
import tja.textures.TJATextures;
import tja.util.Color;
import tja.util.EnumFacingHelper;
import tja.util.TJAUtility;
import tja.util.TooltipHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.List;

import static gregtech.api.util.RelativeDirection.*;

public class MetaTileEntityLargeCrafter extends TJAMultiblockControllerBase implements IRecipeMapProvider {

    private final CrafterRecipeLogic recipeLogic = new CrafterRecipeLogic(this);

    public MetaTileEntityLargeCrafter(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityLargeCrafter(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tja.multiblock.large_crafter.description"));
        tooltip.add(I18n.format("tja.multiblock.large_crafter.description.1"));
        tooltip.add(TooltipHelper.blinkingText(Color.YELLOW,  20, "tja.multiblock.large_crafter.warning"));
    }

    @Override
    protected void updateFormedValid() {
        if (((this.getMaintenanceProblems() >> 5) & 1) != 0)
            this.recipeLogic.update();
    }

    @Override
    protected @Nonnull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(RIGHT, UP, BACK)
                .aisle("XXXXX", "FXXXF", "FXXXF", "FXXXF", " XXX ")
                .aisle("XXXXX", "G#C#G", "GV#VG", "F#V#F", " XXX ")
                .aisle("XXXXX", "G#C#G", "GV#VG", "F#V#F", " XXX ")
                .aisle("XXXXX", "G#C#G", "GV#VG", "F#V#F", " XXX ")
                .aisle("XXXXX", "FXXXF", "FXSXF", "FXXXF", " XXX ")
                .where('S', this.selfPredicate())
                .where('C', states(this.getCasingState()))
                .where('X', states(this.getCasingState()).setMinGlobalLimited(25)
                        .or(abilities(MultiblockAbility.IMPORT_ITEMS, MultiblockAbility.EXPORT_ITEMS))
                        .or(abilities(MultiblockAbility.INPUT_ENERGY, MultiblockAbility.INPUT_LASER).setMaxGlobalLimited(1))
                        .or(abilities(MultiblockAbility.MAINTENANCE_HATCH).setExactLimit(1)))
                .where('V', tieredHatchPredicate())
                .where('F', frames(Materials.Osmiridium))
                .where('G', glassPredicate())
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
        this.recipeLogic.initialize(this.getAbilities(MultiblockAbility.IMPORT_ITEMS).size());
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.recipeLogic.invalidate();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.MULTIBLOCK_WORKABLE_OVERLAY.renderOrientedState(renderState, translation, pipeline, this.frontFacing, this.recipeLogic.isActive(),
                this.recipeLogic.isWorkingEnabled());
        TJATextures.CRAFTER.renderSided(EnumFacingHelper.getTopFacingFrom(this.getFrontFacing()), renderState, translation, pipeline);
    }

    @Override
    protected void configureDisplayText(MultiblockUIBuilder builder) {
        builder.addEnergyUsageLine(this.inputEnergyContainer)
                .addEnergyTierLine(this.tier)
                .addParallelsLine(this.getParallel())
                .addCustom((key, syncer) -> {
                    if (!this.isStructureFormed()) return;
                    final long energyPerTick = syncer.syncLong(this.recipeLogic.getEnergyPerTick());
                    if (energyPerTick < 1) return;
                    key.add(KeyUtil.lang("tja.machine.universal.eut", TJAValues.thousandFormat.format(energyPerTick),
                            GTValues.VOCNF[TJAUtility.getTierFromVoltage(energyPerTick)]));
                })
                .addProgressLine(this.recipeLogic.getProgress(), this.recipeLogic.getMaxProgress())
                .addRunningPerfectlyLine(this.recipeLogic.isActive())
                .setWorkingStatus(this.recipeLogic.isWorkingEnabled(), this.recipeLogic.isActive())
                .addCustom((key, syncer) -> MUIUtils.addRecipeInputOutputLine(key, syncer, this.recipeLogic, this.getWorld()));
    }

    private IBlockState getCasingState() {
        return TJAMetaBlocks.SOLID_CASINGS_2.getState(BlockSolidCasings2.SolidCasingType.ASSEMBLING_CASING);
    }

    @Override
    public void clearRecipeCache() {
        this.recipeLogic.clearCache();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return TJATextures.ASSEMBLING_CASING;
    }

    @Override
    public int getParallel() {
        if (TJAValues.isModLoaded(TJAValues.GCYM_MOD_ID)) {
            final List<IParallelHatch> parallelHatches = this.getAbilities(GCYMMultiblockAbility.PARALLEL_HATCH);
            return parallelHatches != null && !parallelHatches.isEmpty() ? parallelHatches.get(0).getCurrentParallel() : 1;
        } else return TJAConfig.largeCrafter.stack * tier;
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
        return this.recipeLogic.isDistinct();
    }

    @Override
    public void setDistinct(boolean b) {
        this.recipeLogic.setDistinct(b);
    }

    @Override
    public boolean isActive() {
        return this.recipeLogic.isActive();
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.recipeLogic.isWorkingEnabled();
    }

    @Override
    public void setWorkingEnabled(boolean b) {
        this.recipeLogic.setWorkingEnabled(b);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SoundEvent getSound() {
        return GTSoundEvents.ASSEMBLER;
    }

    @Override
    public Int2ObjectMap<Triple<IRecipe, NonNullList<CountableIngredient>, NonNullList<ItemStack>>> getCrafterRecipeMap() {
        return null;
    }
}
