package tja.machines.multiblocks;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.utils.serialization.ByteBufAdapters;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.ProgressBarMultiblock;
import gregtech.api.metatileentity.multiblock.ui.MultiblockUIBuilder;
import gregtech.api.metatileentity.multiblock.ui.TemplateBarBuilder;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuiTheme;
import gregtech.api.mui.sync.FixedIntArraySyncValue;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.KeyUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.metatileentities.multi.BoilerType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tja.capability.handler.IBoilerHandler;
import tja.capability.workables.MegaBoilerRecipeLogic;
import tja.machines.controllers.TJMultiblockControllerBase;
import tja.mui.MUIUtils;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.UnaryOperator;

public class MetaTileEntityMegaBoiler extends TJMultiblockControllerBase implements IBoilerHandler, ProgressBarMultiblock {

    private final MegaBoilerRecipeLogic recipeLogic = new MegaBoilerRecipeLogic(this);
    private final BoilerType boilerType;

    public MetaTileEntityMegaBoiler(ResourceLocation metaTileEntityId, BoilerType boilerType) {
        super(metaTileEntityId);
        this.boilerType = boilerType;
        this.reinitializeStructurePattern();
        this.recipeLogic.setActiveConsumer(this::setLastActive);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityMegaBoiler(this.metaTileEntityId, this.boilerType);
    }

    @Override
    protected void updateFormedValid() {
        if (this.getOffsetTimer() > 40 && ((this.getMaintenanceProblems() >> 5) & 1) != 0)
            this.recipeLogic.update();
    }

    @Nonnull
    @Override
    protected BlockPattern createStructurePattern() {
        return this.boilerType == null ? FactoryBlockPattern.start().build() : FactoryBlockPattern.start()
                .aisle("FFFFFFFFFFFFFFF", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC")
                .aisle("FFFFFFFFFFFFFFF", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CCCCCCCCCCCCCCC")
                .aisle("FFFFFFFFFFFFFFF", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CCCCCCCCCCCCCCC")
                .aisle("FFFFFFFFFFFFFFF", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CCCCCCCCCCCCCCC")
                .aisle("FFFFFFFFFFFFFFF", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CCCCCCCCCCCCCCC")
                .aisle("FFFFFFFFFFFFFFF", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CCCCCCCCCCCCCCC")
                .aisle("FFFFFFFFFFFFFFF", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CCCCCCCCCCCCCCC")
                .aisle("FFFFFFFFFFFFFFF", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CCCCCCCCCCCCCCC")
                .aisle("FFFFFFFFFFFFFFF", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CCCCCCCCCCCCCCC")
                .aisle("FFFFFFFFFFFFFFF", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CCCCCCCCCCCCCCC")
                .aisle("FFFFFFFFFFFFFFF", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CCCCCCCCCCCCCCC")
                .aisle("FFFFFFFFFFFFFFF", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CCCCCCCCCCCCCCC")
                .aisle("FFFFFFFFFFFFFFF", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CCCCCCCCCCCCCCC")
                .aisle("FFFFFFFFFFFFFFF", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CCCCCCCCCCCCCCC")
                .aisle("FFFFFFFFFFFFFFF", "CCCCCCCCCCCCCCC", "CCCCCCCSCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC")
                .where('S', this.selfPredicate())
                .where('C', states(this.boilerType.casingState).setMinGlobalLimited(200)
                        .or(abilities(MultiblockAbility.EXPORT_FLUIDS)))
                .where('F', states(this.boilerType.fireboxState).setMinGlobalLimited(200)
                        .or(abilities(MultiblockAbility.IMPORT_ITEMS, MultiblockAbility.IMPORT_FLUIDS, MultiblockAbility.MAINTENANCE_HATCH,
                                MultiblockAbility.EXPORT_ITEMS))
                        .or(abilities(MultiblockAbility.MUFFLER_HATCH).setExactLimit(1)))
                .where('P', states(this.boilerType.pipeState))
                .build();
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.recipeLogic.initialize(this.getAbilities(MultiblockAbility.IMPORT_ITEMS).size());
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.recipeLogic.invalidate();
    }

    @Override
    protected void configureDisplayText(MultiblockUIBuilder builder) {
        builder.addCustom((key, syncer) -> {
                    if (!syncer.syncBoolean(this.isStructureFormed())) return;
                    key.add(KeyUtil.lang(syncer.syncString("gregtech.multiblock.hpca.temperature"), syncer.syncLong(this.recipeLogic.heat())));
                    key.add(KeyUtil.lang(syncer.syncString("gregtech.multiblock.large_boiler.steam_output"), syncer.syncInt(this.recipeLogic.getProduction())));
                })
                .addProgressLine(this.recipeLogic.getProgress(), this.recipeLogic.getMaxProgress())
                .setWorkingStatus(this.recipeLogic.isWorkingEnabled(), this.recipeLogic.isActive())
                .addCustom((key, syncer) -> {
                    if (!syncer.syncBoolean(this.isStructureFormed())) return;
                    final List<FluidStack> fluidInputs = syncer.syncCollection(this.recipeLogic.getFluidInputs(), ByteBufAdapters.FLUID_STACK);
                    final List<ItemStack> itemInputs = syncer.syncCollection(this.recipeLogic.getItemInputs(), ByteBufAdapters.ITEM_STACK);
                    final List<FluidStack> fluidOutputs = syncer.syncCollection(this.recipeLogic.getFluidOutputs(), ByteBufAdapters.FLUID_STACK);
                    final List<ItemStack> itemOutputs = syncer.syncCollection(this.recipeLogic.getItemOutputs(), ByteBufAdapters.ITEM_STACK);
                    final int maxProgress = syncer.syncInt(this.recipeLogic.getMaxProgress());
                    if (!fluidInputs.isEmpty() || !itemInputs.isEmpty())
                        key.add(KeyUtil.lang(syncer.syncString("machine.universal.consuming")));
                    for (FluidStack fluidStack : fluidInputs) {
                        MUIUtils.addFluidOutputLine(key, fluidStack, fluidStack.amount, maxProgress);
                    }
                    for (ItemStack stack : itemInputs) {
                        MUIUtils.addItemOutputLine(key, stack, stack.getCount(), maxProgress);
                    }
                    if (!fluidOutputs.isEmpty() || !itemOutputs.isEmpty()) {
                        key.add(KeyUtil.lang("")); // new line
                        key.add(KeyUtil.lang(syncer.syncString("gregtech.gui.multiblock.recipe_producing")));
                    }
                    for (FluidStack fluidStack : fluidOutputs) {
                        MUIUtils.addFluidOutputLine(key, fluidStack, fluidStack.amount, maxProgress);
                    }
                    for (ItemStack stack : itemOutputs) {
                        MUIUtils.addItemOutputLine(key, stack, stack.getCount(), maxProgress);
                    }
                });
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.MULTIBLOCK_WORKABLE_OVERLAY.renderOrientedState(renderState, translation, pipeline, this.frontFacing, this.recipeLogic.isActive(), this.recipeLogic.isWorkingEnabled());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return this.boilerType.casingRenderer;
    }

    @Override
    public double getHeatEfficiencyMultiplier() {
        final int efficiency = this.boilerType == BoilerType.TUNGSTENSTEEL ? 32 : this.boilerType == BoilerType.TITANIUM ? 31 : this.boilerType == BoilerType.STEEL ? 30 : 28;
        final double temperature = this.recipeLogic.heat() / (this.getMaxTemperature() * 1.0);
        return 1.0 + Math.round(efficiency * temperature) / 100.0;
    }

    @Override
    public double getFuelConsumptionMultiplier() {
        return this.boilerType == BoilerType.TUNGSTENSTEEL ? 5.4 : this.boilerType == BoilerType.TITANIUM ? 3.0 : this.boilerType == BoilerType.STEEL ? 1.6 : 1.0;
    }

    @Override
    public int getBaseSteamOutput() {
        return this.boilerType.steamPerTick();
    }

    @Override
    public int getMaxTemperature() {
        return this.boilerType == BoilerType.TUNGSTENSTEEL ? 7800 : this.boilerType == BoilerType.TITANIUM ? 3700 : this.boilerType == BoilerType.STEEL ? 1600 : 900;
    }

    @Override
    public int getParallel() {
        return 512;
    }

    @Override
    public int getProgressBarCount() {
        return 3;
    }

    @Override
    public GTGuiTheme getUITheme() {
        return this.boilerType == BoilerType.BRONZE ? GTGuiTheme.BRONZE : this.boilerType == BoilerType.STEEL ? GTGuiTheme.STEEL : super.getUITheme();
    }

    @Override
    public void registerBars(List<UnaryOperator<TemplateBarBuilder>> bars, PanelSyncManager panelSyncManager) {
        final FluidStack water = Materials.Water.getFluid(Integer.MAX_VALUE);
        final FixedIntArraySyncValue heatSyncValue = new FixedIntArraySyncValue(() -> new int[]{(int) this.recipeLogic.heat(), (int) this.recipeLogic.maxHeat()}, null);
        panelSyncManager.syncValue("boiler_heat_bar", heatSyncValue);
        final FixedIntArraySyncValue waterSyncValue = new FixedIntArraySyncValue(() -> this.getTotalFluidAmount(water, this.importFluidTank), null);
        panelSyncManager.syncValue("boiler_water_bar", waterSyncValue);
        final FixedIntArraySyncValue fuelSyncValue = new FixedIntArraySyncValue(() -> this.getTotalFluidAmount(this.recipeLogic.getLastBurnFluid(), this.importFluidTank), null);
        panelSyncManager.syncValue("boiler_fuel_bar", fuelSyncValue);
        final String lastBurnFluid = this.recipeLogic.getLastBurnFluid() != null ? this.recipeLogic.getLastBurnFluid().getLocalizedName() : "";
        bars.add(bar -> bar.progress(() -> heatSyncValue.getValue(1) == 0 ? 0 : 1.0 * heatSyncValue.getValue(0) / heatSyncValue.getValue(1))
                .texture(GTGuiTextures.PROGRESS_BAR_BOILER_HEAT)
                .tooltipBuilder(tooltip -> {
                    if (this.isStructureFormed()) {
                        if (heatSyncValue.getValue(1) != 0)
                            tooltip.add(KeyUtil.lang("tj.multiblock.bars.heat", heatSyncValue.getValue(0), heatSyncValue.getValue(1), 100 * heatSyncValue.getValue(0) / heatSyncValue.getValue(1)));
                    } else tooltip.add(KeyUtil.lang("gregtech.multiblock.invalid_structure"));
                }));
        bars.add(bar -> bar.progress(() -> waterSyncValue.getValue(1) == 0 ? 0 : 1.0 * waterSyncValue.getValue(0) / waterSyncValue.getValue(1))
                .texture(GTGuiTextures.PROGRESS_BAR_LCE_OXYGEN)
                .tooltipBuilder(tooltip -> {
                    if (this.isStructureFormed()) {
                        if (waterSyncValue.getValue(1) != 0)
                            tooltip.add(KeyUtil.lang("tj.multiblock.bars.fluid", water.getLocalizedName(), waterSyncValue.getValue(0), waterSyncValue.getValue(1), 100 * waterSyncValue.getValue(0) / waterSyncValue.getValue(1)));
                    } else tooltip.add(KeyUtil.lang("gregtech.multiblock.invalid_structure"));
                }));
        bars.add(bar -> bar.progress(() -> fuelSyncValue.getValue(1) == 0 ? 0 : 1.0 * fuelSyncValue.getValue(0) / fuelSyncValue.getValue(1))
                .texture(GTGuiTextures.PROGRESS_BAR_LCE_FUEL)
                .tooltipBuilder(tooltip -> {
                    if (this.isStructureFormed()) {
                        if (fuelSyncValue.getValue(1) != 0)
                            tooltip.add(KeyUtil.lang("tj.multiblocks.bars.fuel", lastBurnFluid, fuelSyncValue.getValue(0), fuelSyncValue.getValue(1), 100 * fuelSyncValue.getValue(0) / fuelSyncValue.getValue(1)));
                    } else tooltip.add(KeyUtil.lang("gregtech.multiblock.invalid_structure"));
                }));
    }
}
