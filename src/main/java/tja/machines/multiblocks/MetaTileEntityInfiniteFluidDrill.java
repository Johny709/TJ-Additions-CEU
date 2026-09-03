package tja.machines.multiblocks;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import gregicality.multiblocks.common.metatileentities.multiblockpart.MetaTileEntityTieredHatch;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.ProgressBarMultiblock;
import gregtech.api.metatileentity.multiblock.ui.MultiblockUIBuilder;
import gregtech.api.metatileentity.multiblock.ui.TemplateBarBuilder;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.sync.FixedIntArraySyncValue;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.KeyUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tja.blocks.BlockSolidCasings;
import tja.blocks.TJAMetaBlocks;
import tja.capability.workables.InfiniteFluidDrillWorkableHandler;
import tja.machines.controllers.TJMultiblockControllerBase;
import tja.mui.MUIUtils;
import tja.textures.TJATextures;
import tja.util.TJAUtility;

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import static com.fulltrix.gcyl.materials.GCYLMaterials.DrillingMud;

public class MetaTileEntityInfiniteFluidDrill extends TJMultiblockControllerBase implements ProgressBarMultiblock {

    public static final FluidStack DRILLING_MUD = DrillingMud.getFluid(1);
    private final InfiniteFluidDrillWorkableHandler workableHandler = new InfiniteFluidDrillWorkableHandler(this);
    private long maxVoltage;
    private int tier;

    public MetaTileEntityInfiniteFluidDrill(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityInfiniteFluidDrill(this.metaTileEntityId);
    }

    @Override
    protected void updateFormedValid() {
        if ( ((this.getMaintenanceProblems() >> 5) & 1) != 0)
            this.workableHandler.update();
    }

    @Override
    protected @Nonnull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("CF FC", "CF FC", "CCCCC", " XXX ", "  C  ", "  C  ", "  C  ", "     ", "     ", "     ", "     ", "     ", "     ", "     ")
                .aisle("F   F", "F   F", "CCMCC", "X###X", " C#C ", " C#C ", " C#C ", " FCF ", " FFF ", " FFF ", "  F  ", "     ", "     ", "     ")
                .aisle("     ", "     ", "CMMMC", "X#T#X", "C#T#C", "C#T#C", "C#T#C", " CCC ", " FCF ", " FFF ", " FFF ", "  F  ", "  F  ", "  F  ")
                .aisle("F   F", "F   F", "CCMCC", "X###X", " C#C ", " C#C " ," C#C ", " FCF ", " FFF ", " FFF ", "  F  ", "     ", "     ", "     ")
                .aisle("CF FC", "CF FC", "CCCCC", " XSX ", "  C  ", "  C  ", "  C  ", "     ", "     ", "     ", "     ", "     ", "     ", "     ")
                .where('S', this.selfPredicate())
                .where('C', states(TJAMetaBlocks.SOLID_CASINGS.getState(BlockSolidCasings.SolidCasingType.SEABORGIUM_CASING)))
                .where('X', states(TJAMetaBlocks.SOLID_CASINGS.getState(BlockSolidCasings.SolidCasingType.SEABORGIUM_CASING))
                        .or(abilities(MultiblockAbility.IMPORT_FLUIDS, MultiblockAbility.EXPORT_FLUIDS))
                        .or(abilities(MultiblockAbility.INPUT_ENERGY, MultiblockAbility.INPUT_LASER).setExactLimit(1))
                        .or(autoAbilities(true, false)))
                .where('F', states(MetaBlocks.FRAMES.get(Materials.Seaborgium).getStateFromMeta(1)))
                .where('T', states(MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.TUNGSTENSTEEL_PIPE)))
                .where('M', tieredHatchPredicate())
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
        this.workableHandler.initialize(this.tier);
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.maxVoltage = 0;
        this.tier = 0;
    }

    @Override
    protected void configureDisplayText(MultiblockUIBuilder builder) {
        builder.addEnergyUsageLine(this.inputEnergyContainer)
                .addEnergyTierLine(this.tier)
                .addProgressLine(this.workableHandler.getProgress(), this.workableHandler.getMaxProgress())
                .addRunningPerfectlyLine(this.workableHandler.isActive())
                .setWorkingStatus(this.workableHandler.isWorkingEnabled(), this.workableHandler.isActive())
                .addCustom((key, syncer) -> MUIUtils.addRecipeInputOutputLine(key, syncer, this.workableHandler, this.getWorld()));
    }

    @Override
    public void registerBars(List<UnaryOperator<TemplateBarBuilder>> bars, PanelSyncManager panelSyncManager) {
        final FixedIntArraySyncValue drillingMudValue = new FixedIntArraySyncValue(() ->
                this.getTotalFluidAmount(DRILLING_MUD, this.importFluidTank), null);
        bars.add(bar -> bar.progress(() -> drillingMudValue.getValue(1) == 0 ? 0
                        : 1.0 * drillingMudValue.getValue(0) / drillingMudValue.getValue(1))
                .texture(GTGuiTextures.PROGRESS_BAR_FLUID_RIG_DEPLETION)
                .tooltipBuilder(tooltip -> {
                    if (this.isStructureFormed()) {
                        if (drillingMudValue.getValue(1) != 0)
                            tooltip.add(KeyUtil.lang("tja.multiblock.bars.fluid", DRILLING_MUD.getLocalizedName(),
                                    drillingMudValue.getValue(0), drillingMudValue.getValue(1),
                                    100 * drillingMudValue.getValue(0) / drillingMudValue.getValue(1)));
                    } else tooltip.add(KeyUtil.lang("gregtech.multiblock.invalid_structure"));
                }));
    }

    @Override
    public int getProgressBarCount() {
        return 1;
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return TJATextures.SEABORGIUM;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.MULTIBLOCK_WORKABLE_OVERLAY.renderOrientedState(renderState, translation, pipeline,
                this.frontFacing, this.workableHandler.isActive(), this.workableHandler.isWorkingEnabled());
    }

    @Override
    public long getMaxVoltage() {
        return this.maxVoltage;
    }

    @Override
    public int getTier() {
        return this.tier;
    }
}
