package tja.machines.multiblocks;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import gregicality.multiblocks.common.metatileentities.multiblockpart.MetaTileEntityTieredHatch;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.ui.MultiblockUIBuilder;
import gregtech.api.metatileentity.multiblock.ui.MultiblockUIFactory;
import gregtech.api.mui.GTGuiTextures;
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
import tja.capability.handler.INameHandler;
import tja.capability.workables.NamingMachineWorkableHandler;
import tja.machines.controllers.TJAMultiblockControllerBase;
import tja.mui.MUIUtils;
import tja.textures.TJATextures;
import tja.util.EnumFacingHelper;
import tja.util.TJAUtility;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MetaTileEntityLargeNamingMachine extends TJAMultiblockControllerBase implements INameHandler {

    private final NamingMachineWorkableHandler workableHandler = new NamingMachineWorkableHandler(this);
    private int parallel;
    @Nonnull
    private String name = "";

    public MetaTileEntityLargeNamingMachine(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityLargeNamingMachine(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, @Nonnull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("tja.multiblock.parallel_tier", TJAConfig.largeNamingMachine.stack));
        tooltip.add(I18n.format("tja.machine.naming_machine.description"));
    }

    @Override
    protected void updateFormedValid() {
        if (((this.getMaintenanceProblems() >> 5) & 1) != 0)
            this.workableHandler.update();
    }

    @Override
    protected @Nonnull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle(" XXX ", "XXXXX", " XXX ", "  C  ", "     ")
                .aisle("XXXXX", "X###X", "X###X", " CCC ", "  C  ")
                .aisle("XXXXX", "X###X", "X###X", "CC#CC", " CVC ")
                .aisle("XXXXX", "X###X", "X###X", " CCC ", "  C  ")
                .aisle(" XXX ", "XXSXX", " XXX ", "  C  ", "     ")
                .where('S', this.selfPredicate())
                .where('C', states(this.getCasingState()))
                .where('X', states(this.getCasingState()).or(abilities(MultiblockAbility.IMPORT_ITEMS, MultiblockAbility.EXPORT_ITEMS))
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
        TJATextures.NAME_TAG.renderSided(EnumFacingHelper.getLeftFacingFrom(this.getFrontFacing()), renderState, translation, pipeline);
        TJATextures.NAME_TAG.renderSided(EnumFacingHelper.getRightFacingFrom(this.getFrontFacing()), renderState, translation, pipeline);
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

    @Override
    protected MultiblockUIFactory createUIFactory() {
        return super.createUIFactory()
                .createFlexButton((guiData, syncManager) -> {
                    final IPanelHandler throttle = syncManager.syncedPanel("name_panel", true, this::makeNamePanel);

                    return new ButtonWidget<>()
                            .size(18)
                            .overlay(GTGuiTextures.FILTER_SETTINGS_OVERLAY.asIcon().size(16))
                            .onMousePressed(i -> {
                                if (throttle.isPanelOpen()) {
                                    throttle.closePanel();
                                } else {
                                    throttle.openPanel();
                                }
                                return true;
                            });
                });
    }

    private @Nonnull ModularPanel makeNamePanel(@Nonnull PanelSyncManager syncManager, @Nonnull IPanelHandler panelHandler) {
        return ModularPanel.defaultPanel("naming_machine_name", 112, 24)
                .child(new TextFieldWidget()
                        .pos(3, 3)
                        .size(107, 18)
                        .autoUpdateOnChange(true)
                        .value(new StringSyncValue(this::getName, this::setName)));
    }
    
    private IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Override
    public int getParallel() {
        return this.parallel;
    }

    public void setName(@Nonnull String name) {
        this.name = name;
        this.markDirty();
    }

    @Nonnull
    @Override
    public String getName() {
        return this.name;
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
        return GTSoundEvents.ASSEMBLER;
    }
}
