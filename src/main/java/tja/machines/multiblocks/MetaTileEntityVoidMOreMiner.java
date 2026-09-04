package tja.machines.multiblocks;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.value.sync.DoubleSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.fulltrix.gcyl.materials.GCYLMaterials;
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
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.util.KeyUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tja.TJAValues;
import tja.blocks.BlockSolidCasings;
import tja.blocks.TJAMetaBlocks;
import tja.capability.workables.VoidMOreMinerWorkableHandler;
import tja.machines.controllers.TJMultiblockControllerBase;
import tja.mui.MUIUtils;
import tja.textures.TJATextures;
import tja.util.TJAFluidUtils;
import tja.util.TJAUtility;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import static tja.machines.multiblocks.MetaTileEntityInfiniteFluidDrill.DRILLING_MUD;

public class MetaTileEntityVoidMOreMiner extends TJMultiblockControllerBase implements ProgressBarMultiblock {

    public static final FluidStack USED_DRILLING_MUD = GCYLMaterials.UsedDrillingMud.getFluid(1);
    public static final FluidStack PYROTHEUM = GCYLMaterials.Pyrotheum.getFluid(1);
    public static final FluidStack CRYOTHEUM = GCYLMaterials.Cryotheum.getFluid(1);
    private final VoidMOreMinerWorkableHandler workableHandler = new VoidMOreMinerWorkableHandler(this);

    public MetaTileEntityVoidMOreMiner(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityVoidMOreMiner(this.metaTileEntityId);
    }

    @Override
    protected void updateFormedValid() {
        if (((this.getMaintenanceProblems() >> 5) & 1) != 0)
            this.workableHandler.update();
    }

    @Override
    protected @Nonnull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("CCCCCCCCC", "CCCCCCCCC", "C#######C", "C#######C", "C#######C", "CCCCCCCCC", "CFFFFFFFC", "CFFFFFFFC", "CfffffffC", "C#######C")
                .aisle("C#######C", "C#######C", "#########", "#########", "#########", "C###D###C", "F##DDD##F", "F##DDD##F", "f##DDD##f", "#########")
                .aisle("C#######C", "C#######C", "#########", "####D####", "###DDD###", "C##DDD##C", "F#DD#DD#F", "F#D###D#F", "f#D###D#f", "#########")
                .aisle("C###D###C", "C###D###C", "###DDD###", "###D#D###", "##DD#DD##", "C#D###D#C", "FDD###DDF", "FD#####DF", "fD#####Df", "#########")
                .aisle("C##DfD##C", "C##DfD##C", "###DfD###", "##D###D##", "##D###D##", "CDD###DDC", "FD#####DF", "FD#####DF", "fD#####Df", "#########")
                .aisle("C###D###C", "C###D###C", "###DDD###", "###D#D###", "##DD#DD##", "C#D###D#C", "FDD###DDF", "FD#####DF", "fD#####Df", "#########")
                .aisle("C#######C", "C#######C", "#########", "####D####", "###DDD###", "C##DDD##C", "F#DD#DD#F", "F#D###D#F", "f#D###D#f", "#########")
                .aisle("C#######C", "C#######C", "#########", "#########", "#########", "C###D###C", "F##DDD##F", "F##DDD##F", "f##DDD##f", "#########")
                .aisle("CCCCCCCCC", "CCCCSCCCC", "C#######C", "C#######C", "C#######C", "CCCCCCCCC", "CFFFFFFFC", "CFFFFFFFC", "CfffffffC", "C#######C")
                .where('S', this.selfPredicate())
                .where('C', states(TJAMetaBlocks.SOLID_CASINGS.getState(BlockSolidCasings.SolidCasingType.HEAVY_QUARK_DEGENERATE_MATTER))
                        .setMinGlobalLimited(100)
                        .or(abilities(MultiblockAbility.IMPORT_ITEMS, MultiblockAbility.EXPORT_ITEMS, MultiblockAbility.IMPORT_FLUIDS,
                                MultiblockAbility.EXPORT_FLUIDS))
                        .or(abilities(MultiblockAbility.INPUT_ENERGY, MultiblockAbility.INPUT_LASER).setExactLimit(1))
                        .or(autoAbilities(true, false)))
                .where('D', states(TJAMetaBlocks.SOLID_CASINGS.getState(BlockSolidCasings.SolidCasingType.PERIODICIUM)))
                .where('F', frames(GCYLMaterials.QCDMatter))
                .where('f', tieredHatchPredicate())
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
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.MULTIBLOCK_WORKABLE_OVERLAY.renderOrientedState(renderState, translation, pipeline,
                this.frontFacing, this.workableHandler.isActive(), this.workableHandler.isWorkingEnabled());
    }

    @Override
    protected void configureDisplayText(MultiblockUIBuilder builder) {
        builder.addEnergyUsageLine(this.inputEnergyContainer)
                .addEnergyTierLine(this.tier)
                .addCustom((key, syncer) -> {
                    final long energyPerTick = syncer.syncLong(this.workableHandler.getEnergyPerTick());
                    key.add(KeyUtil.lang(TextFormatting.GRAY, "tja.machine.universal.eut", TJAValues.thousandFormat.format(energyPerTick),
                            GTValues.VOCNF[TJAUtility.getTierByVoltage(energyPerTick)]));
                    final long heat = syncer.syncLong(this.workableHandler.heat());
                    final long maxHeat = syncer.syncLong(this.workableHandler.maxHeat());
                    key.add(KeyUtil.lang(TextFormatting.GRAY, "tj.machine.universal.temperature", TJAValues.thousandFormat.format(heat),
                            TJAValues.thousandFormat.format(maxHeat)));
                })
                .addProgressLine(this.workableHandler.getProgress(), this.workableHandler.getMaxProgress())
                .addRunningPerfectlyLine(this.workableHandler.isActive())
                .setWorkingStatus(this.workableHandler.isWorkingEnabled(), this.workableHandler.isActive())
                .addCustom((key, syncer) -> MUIUtils.addRecipeInputOutputLine(key, syncer, this.workableHandler, this.getWorld()));
    }

    @Override
    public void registerBars(List<UnaryOperator<TemplateBarBuilder>> bars, PanelSyncManager panelSyncManager) {
        final DoubleSyncValue drillingMudAmount = new DoubleSyncValue(() -> TJAFluidUtils.getFluidAmountFromTanks(DRILLING_MUD, this.importFluidTank));
        final DoubleSyncValue drillingMudCapacity = new DoubleSyncValue(() -> TJAFluidUtils.getFluidCapacityFromTanks(DRILLING_MUD, this.importFluidTank));
        final DoubleSyncValue pyrotheumAmount = new DoubleSyncValue(() -> TJAFluidUtils.getFluidAmountFromTanks(PYROTHEUM, this.importFluidTank));
        final DoubleSyncValue pyrotheumCapacity = new DoubleSyncValue(() -> TJAFluidUtils.getFluidCapacityFromTanks(PYROTHEUM, this.importFluidTank));
        final DoubleSyncValue cryotheumAmount = new DoubleSyncValue(() -> TJAFluidUtils.getFluidAmountFromTanks(CRYOTHEUM, this.importFluidTank));
        final DoubleSyncValue cryotheumCapacity = new DoubleSyncValue(() -> TJAFluidUtils.getFluidCapacityFromTanks(CRYOTHEUM, this.importFluidTank));
        panelSyncManager.syncValue("drilling_mud_amount", drillingMudAmount);
        panelSyncManager.syncValue("drilling_mud_capacity", drillingMudCapacity);
        panelSyncManager.syncValue("pyrotheum_amount", pyrotheumAmount);
        panelSyncManager.syncValue("pyrotheum_capacity", pyrotheumCapacity);
        panelSyncManager.syncValue("cryotheum_amount", cryotheumAmount);
        panelSyncManager.syncValue("cryotheum_capacity", cryotheumCapacity);
        bars.add(bar -> bar.progress(() -> drillingMudAmount.getDoubleValue()== 0 ? 0
                        : drillingMudAmount.getDoubleValue() / drillingMudCapacity.getDoubleValue())
                .texture(GTGuiTextures.PROGRESS_BAR_LCE_FUEL)
                .tooltipBuilder(tooltip -> {
                    if (this.isStructureFormed()) {
                        if (drillingMudCapacity.getDoubleValue() != 0)
                            tooltip.add(KeyUtil.lang("tja.multiblock.bars.fluid", DRILLING_MUD.getLocalizedName(),
                                    drillingMudAmount.getDoubleValue(), drillingMudCapacity.getDoubleValue(),
                                    drillingMudAmount.getDoubleValue() / drillingMudCapacity.getDoubleValue()));
                    } else tooltip.add(KeyUtil.lang("gregtech.multiblock.invalid_structure"));
                }));
        bars.add(bar -> bar.progress(() -> pyrotheumCapacity.getDoubleValue() == 0 ? 0
                        : pyrotheumAmount.getDoubleValue() / pyrotheumCapacity.getDoubleValue())
                .texture(GTGuiTextures.PROGRESS_BAR_FUSION_HEAT)
                .tooltipBuilder(tooltip -> {
                    if (this.isStructureFormed()) {
                        if (pyrotheumCapacity.getDoubleValue() != 0)
                            tooltip.add(KeyUtil.lang("tja.multiblock.bars.fluid", PYROTHEUM.getLocalizedName(),
                                    pyrotheumAmount.getDoubleValue(), pyrotheumCapacity.getDoubleValue(),
                                    pyrotheumAmount.getDoubleValue() / pyrotheumCapacity.getDoubleValue()));
                    } else tooltip.add(KeyUtil.lang("gregtech.multiblock.invalid_structure"));
                }));
        bars.add(bar -> bar.progress(() -> cryotheumCapacity.getDoubleValue()== 0 ? 0
                        : cryotheumAmount.getDoubleValue() / cryotheumCapacity.getDoubleValue())
                .texture(GTGuiTextures.PROGRESS_BAR_LCE_OXYGEN)
                .tooltipBuilder(tooltip -> {
                    if (this.isStructureFormed()) {
                        if (cryotheumCapacity.getDoubleValue() != 0)
                            tooltip.add(KeyUtil.lang("tja.multiblock.bars.fluid", CRYOTHEUM.getLocalizedName(),
                                    cryotheumAmount.getDoubleValue(), cryotheumCapacity.getDoubleValue(),
                                    cryotheumAmount.getDoubleValue() / cryotheumCapacity.getDoubleValue()));
                    } else tooltip.add(KeyUtil.lang("gregtech.multiblock.invalid_structure"));
                }));
    }

    @Override
    public int getProgressBarCount() {
        return 3;
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return TJATextures.HEAVY_QUARK_DEGENERATE_MATTER;
    }

    @Override
    public int getVoidingModeInt() {
        return this.getVoidingMode();
    }
}
