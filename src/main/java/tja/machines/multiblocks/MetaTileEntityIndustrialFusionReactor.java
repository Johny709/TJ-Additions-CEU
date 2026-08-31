package tja.machines.multiblocks;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.value.sync.DoubleSyncValue;
import com.cleanroommc.modularui.value.sync.LongSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.google.common.collect.Lists;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.EnergyContainerHandler;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.IFastRenderMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.ProgressBarMultiblock;
import gregtech.api.metatileentity.multiblock.ui.MultiblockUIBuilder;
import gregtech.api.metatileentity.multiblock.ui.MultiblockUIFactory;
import gregtech.api.metatileentity.multiblock.ui.TemplateBarBuilder;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.BlockWorldState;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.logic.OCParams;
import gregtech.api.recipes.properties.RecipePropertyStorage;
import gregtech.api.recipes.properties.impl.FusionEUToStartProperty;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.KeyUtil;
import gregtech.api.util.RelativeDirection;
import gregtech.api.util.interpolate.Eases;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.IRenderSetup;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.shader.postprocessing.BloomEffect;
import gregtech.client.shader.postprocessing.BloomType;
import gregtech.client.utils.*;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockFusionCasing;
import gregtech.common.blocks.BlockGlassCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import tja.machines.controllers.TJExtendableMultiblockController;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

import static gregtech.api.metatileentity.multiblock.MultiblockAbility.INPUT_ENERGY;
import static gregtech.api.util.RelativeDirection.*;

public class MetaTileEntityIndustrialFusionReactor extends TJExtendableMultiblockController implements IFastRenderMetaTileEntity, IBloomEffect, ProgressBarMultiblock {

    protected static final int NO_COLOR = 0;

    private final long energyToStart;
    private final int tier;
    private EnergyContainerList inputEnergyContainers;
    private long heat = 0; // defined in TileEntityFusionReactor but serialized in FusionRecipeLogic
    private int fusionRingColor = NO_COLOR;

    @SideOnly(Side.CLIENT)
    private boolean registeredBloomRenderTicket;

    public MetaTileEntityIndustrialFusionReactor(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, RecipeMaps.FUSION_RECIPES);
        this.recipeMapWorkable = new IndustrialFusionRecipeLogic(this);
        this.energyToStart = 160_000_000L << tier - 6;
        this.tier = tier;
        this.reinitializeStructurePattern();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityIndustrialFusionReactor(this.metaTileEntityId, this.tier);
    }

    @Override
    protected @Nonnull BlockPattern createStructurePattern() {
        final FactoryBlockPattern factoryPattern = FactoryBlockPattern.start(LEFT, FRONT, DOWN);
        for (int count = 1; count < this.slices; count++) {
            factoryPattern.aisle("               ", "      ICI      ", "    CC   CC    ", "   C       C   ", "  C         C  ", "  C         C  ", " I           I ", " C           C ", " I           I ", "  C         C  ", "  C         C  ", "   C       C   ", "    CC   CC    ", "      ICI      ", "               ");
            factoryPattern.aisle("      OGO      ", "    GG###GG    ", "   E##OGO##E   ", "  EcEG   GEcE  ", " G#E       E#G ", " G#G       G#G ", "O#O         O#O", "G#G         G#G", "O#O         O#O", " G#G       G#G ", " G#E       E#G ", "  EcEG   GEcE  ", "   E##OGO##E   ", "    GG###GG    ", "      OGO      ");
        }
        return factoryPattern
                .aisle("               ", "      ICI      ", "    CC   CC    ", "   C       C   ", "  C         C  ", "  C         C  ", " I           I ", " C           C ", " I           I ", "  C         C  ", "  C         C  ", "   C       C   ", "    CC   CC    ", "      ICI      ", "               ")
                .aisle("      OSO      ", "    GG###GG    ", "   E##OGO##E   ", "  EcEG   GEcE  ", " G#E       E#G ", " G#G       G#G ", "O#O         O#O", "G#G         G#G", "O#O         O#O", " G#G       G#G ", " G#E       E#G ", "  EcEG   GEcE  ", "   E##OGO##E   ", "    GG###GG    ", "      OGO      ")
                .aisle("               ", "      ICI      ", "    CC   CC    ", "   C       C   ", "  C         C  ", "  C         C  ", " I           I ", " C           C ", " I           I ", "  C         C  ", "  C         C  ", "   C       C   ", "    CC   CC    ", "      ICI      ", "               ")
                .where('S', this.selfPredicate())
                .where('C', states(this.getCasingState())
                        .or(abilities(MultiblockAbility.MAINTENANCE_HATCH).setMaxGlobalLimited(1)))
                .where('G', states(this.getCasingState(), this.getGlassState()))
                .where('c', states(this.getCoilState()))
                .where('O', states(this.getCasingState(), this.getGlassState())
                        .or(abilities(MultiblockAbility.EXPORT_FLUIDS).setMaxGlobalLimited(16 * this.slices)))
                .where('E', states(this.getCasingState(), this.getGlassState())
                        .or(tilePredicate(energyHatchPredicate(this.tier),
                                () -> new BlockInfo[]{new BlockInfo(MetaTileEntities.ENERGY_INPUT_HATCH[this.tier].getBlock())})
                                .setMaxGlobalLimited(16 * this.slices)))
                .where('I', states(this.getCasingState())
                        .or(abilities(MultiblockAbility.IMPORT_FLUIDS).setMaxGlobalLimited(16 * this.slices)))
                .where('#', air())
                .build();
    }

    public static BiFunction<BlockWorldState, MetaTileEntity, Boolean> energyHatchPredicate(int tier) {
        return (state, tile) -> {
            if (tile instanceof MetaTileEntityMultiblockPart) {
                final MetaTileEntityMultiblockPart multiblockPart = (MetaTileEntityMultiblockPart) tile;
                if (multiblockPart instanceof IMultiblockAbilityPart<?>) {
                    final IMultiblockAbilityPart<?> abilityPart = (IMultiblockAbilityPart<?>) multiblockPart;
                    return abilityPart.getAbility() == INPUT_ENERGY && multiblockPart.getTier() >= tier;
                }
            }
            return false;
        };
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        final List<IEnergyContainer> energyInputs = getAbilities(MultiblockAbility.INPUT_ENERGY);
        this.inputEnergyContainers = new EnergyContainerList(energyInputs);
        final long euCapacity = energyInputs.size() * (long) Math.pow(2, tier - 6) * 10000000L;
        this.energyContainer = new EnergyContainerHandler(this, euCapacity, GTValues.V[tier], 0, 0, 0) {

            @Nonnull
            @Override
            public String getName() {
                return GregtechDataCodes.FUSION_REACTOR_ENERGY_CONTAINER_TRAIT;
            }
        };
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.energyContainer = new EnergyContainerHandler(this, 0, 0, 0, 0, 0) {

            @Nonnull
            @Override
            public String getName() {
                return GregtechDataCodes.FUSION_REACTOR_ENERGY_CONTAINER_TRAIT;
            }
        };
        this.inputEnergyContainers = new EnergyContainerList(Lists.newArrayList());
        this.heat = 0;
        this.setFusionRingColor(NO_COLOR);
    }

    @Override
    protected void updateFormedValid() {
        if (this.inputEnergyContainers.getEnergyStored() > 0) {
            final long energyAdded = this.energyContainer.addEnergy(this.inputEnergyContainers.getEnergyStored());
            if (energyAdded > 0) this.inputEnergyContainers.removeEnergy(energyAdded);
        }
        super.updateFormedValid();
        if (this.recipeMapWorkable.isWorking() && this.fusionRingColor == NO_COLOR) {
            if (this.recipeMapWorkable.getPreviousRecipe() != null &&
                    !this.recipeMapWorkable.getPreviousRecipe().getFluidOutputs().isEmpty()) {
                this.setFusionRingColor(0xFF000000 |
                        this.recipeMapWorkable.getPreviousRecipe().getFluidOutputs().get(0).getFluid().getColor());
            }
        } else if (!this.recipeMapWorkable.isWorking() && this.isStructureFormed()) {
            this.setFusionRingColor(NO_COLOR);
        }
    }

    @Override
    protected MultiblockUIFactory createUIFactory() {
        IDrawable title;
        if (tier == GTValues.LuV) {
            // MK1
            title = GTGuiTextures.FUSION_REACTOR_MK1_TITLE;
        } else if (tier == GTValues.ZPM) {
            // MK2
            title = GTGuiTextures.FUSION_REACTOR_MK2_TITLE;
        } else {
            // MK3
            title = GTGuiTextures.FUSION_REACTOR_MK3_TITLE;
        }

        DoubleSyncValue progress = new DoubleSyncValue(recipeMapWorkable::getProgressPercent);
        return new MultiblockUIFactory(this)
                .setScreenHeight(138)
                .disableDisplayText()
                .addScreenChildren((parent, syncManager) -> {
                    MultiblockUIBuilder status = MultiblockUIFactory.builder("status", syncManager);
                    status.setAction(b -> b.structureFormed(true)
                            .setWorkingStatus(recipeMapWorkable.isWorkingEnabled(), recipeMapWorkable.isActive())
                            .addWorkingStatusLine());
                    parent.child(new Column()
                            .padding(4)
                            .expanded()
                            .child(title.asWidget()
                                    .marginBottom(8)
                                    .size(69, 12))
                            .child(new ProgressWidget()
                                    .size(77, 77)
                                    .tooltipAutoUpdate(true)
                                    .tooltipBuilder(status::build)
                                    .background(GTGuiTextures.FUSION_DIAGRAM.asIcon()
                                            .size(89, 101)
                                            .marginTop(11))
                                    .direction(ProgressWidget.Direction.CIRCULAR_CW)
                                    .value(progress)
                                    .texture(null, GTGuiTextures.FUSION_PROGRESS, 77))
                            .child(GTGuiTextures.FUSION_LEGEND.asWidget()
                                    .left(4)
                                    .bottom(4)
                                    .size(108, 41)));
                });
    }

    @Override
    public int getProgressBarCount() {
        return 2;
    }

    @Override
    public void registerBars(List<UnaryOperator<TemplateBarBuilder>> bars, PanelSyncManager syncManager) {
        LongSyncValue capacity = new LongSyncValue(energyContainer::getEnergyCapacity);
        syncManager.syncValue("capacity", capacity);
        LongSyncValue stored = new LongSyncValue(energyContainer::getEnergyStored);
        syncManager.syncValue("stored", stored);
        LongSyncValue heat = new LongSyncValue(this::getHeat);
        syncManager.syncValue("heat", heat);

        bars.add(barTest -> barTest
                .progress(() -> capacity.getLongValue() > 0 ?
                        1.0 * stored.getLongValue() / capacity.getLongValue() : 0)
                .texture(GTGuiTextures.PROGRESS_BAR_FUSION_ENERGY)
                .tooltipBuilder(tooltip -> tooltip
                        .add(KeyUtil.lang(TextFormatting.GRAY,
                                "gregtech.multiblock.energy_stored",
                                stored.getLongValue(), capacity.getLongValue()))));

        bars.add(barTest -> barTest
                .texture(GTGuiTextures.PROGRESS_BAR_FUSION_HEAT)
                .tooltipBuilder(tooltip -> {
                    IKey heatInfo = KeyUtil.string(TextFormatting.AQUA,
                            "%,d / %,d EU",
                            heat.getLongValue(), capacity.getLongValue());
                    tooltip.add(KeyUtil.lang(TextFormatting.GRAY,
                            "gregtech.multiblock.fusion_reactor.heat",
                            heatInfo));
                })
                .progress(() -> capacity.getLongValue() > 0 ?
                        1.0 * heat.getLongValue() / capacity.getLongValue() : 0));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(double x, double y, double z, float partialTicks) {
        if (this.hasFusionRingColor() && !this.registeredBloomRenderTicket) {
            this.registeredBloomRenderTicket = true;
            BloomEffectUtil.registerBloomRender(IndustrialFusionBloomSetup.INSTANCE, getBloomType(), this, this);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderBloomEffect(@Nonnull BufferBuilder buffer, @Nonnull EffectRenderContext context) {
        if (!this.hasFusionRingColor()) return;
        int color = RenderUtil.interpolateColor(this.fusionRingColor, -1, Eases.QUAD_IN.getInterpolation(
                Math.abs((Math.abs(getOffsetTimer() % 50) + context.partialTicks()) - 25) / 25));
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        EnumFacing relativeBack = RelativeDirection.BACK.getRelativeFacing(getFrontFacing(), getUpwardsFacing(),
                isFlipped());
        EnumFacing.Axis axis = RelativeDirection.UP.getRelativeFacing(getFrontFacing(), getUpwardsFacing(), isFlipped())
                .getAxis();

        for (int i = 0; i < this.slices; i++) {
            buffer.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION_COLOR);
            RenderBufferHelper.renderRing(buffer,
                    getPos().getX() - context.cameraX() + relativeBack.getXOffset() * 7 + 0.5,
                    getPos().getY() + (i * 2) - context.cameraY() + relativeBack.getYOffset() * 7 + 0.5,
                    getPos().getZ() - context.cameraZ() + relativeBack.getZOffset() * 7 + 0.5,
                    6, 0.2, 10, 20,
                    r, g, b, a, axis);
            Tessellator.getInstance().draw();
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldRenderBloomEffect(@Nonnull EffectRenderContext context) {
        return this.hasFusionRingColor() && context.camera().isBoundingBoxInFrustum(getRenderBoundingBox());
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.UPDATE_COLOR)
            this.fusionRingColor = buf.readVarInt();
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        EnumFacing relativeRight = RelativeDirection.RIGHT.getRelativeFacing(getFrontFacing(), getUpwardsFacing(),
                isFlipped());
        EnumFacing relativeBack = RelativeDirection.BACK.getRelativeFacing(getFrontFacing(), getUpwardsFacing(),
                isFlipped());

        return new AxisAlignedBB(
                this.getPos().offset(relativeBack).offset(relativeRight, 6),
                this.getPos().offset(relativeBack, 13).offset(relativeRight.getOpposite(), 6));
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 0;
    }

    @Override
    public boolean isGlobalRenderer() {
        return true;
    }

    protected boolean hasFusionRingColor() {
        return this.fusionRingColor != NO_COLOR;
    }

    private static BloomType getBloomType() {
        ConfigHolder.FusionBloom fusionBloom = ConfigHolder.client.shader.fusionBloom;
        return BloomType.fromValue(fusionBloom.useShader ? fusionBloom.bloomStyle : -1);
    }

    protected void setFusionRingColor(int fusionRingColor) {
        if (this.fusionRingColor != fusionRingColor) {
            this.fusionRingColor = fusionRingColor;
            writeCustomData(GregtechDataCodes.UPDATE_COLOR, buf -> buf.writeVarInt(fusionRingColor));
        }
    }

    private IBlockState getGlassState() {
        return MetaBlocks.TRANSPARENT_CASING.getState(BlockGlassCasing.CasingType.FUSION_GLASS);
    }

    private IBlockState getCasingState() {
        if (this.tier == GTValues.LuV)
            return MetaBlocks.FUSION_CASING.getState(BlockFusionCasing.CasingType.FUSION_CASING);
        if (this.tier == GTValues.ZPM)
            return MetaBlocks.FUSION_CASING.getState(BlockFusionCasing.CasingType.FUSION_CASING_MK2);

        return MetaBlocks.FUSION_CASING.getState(BlockFusionCasing.CasingType.FUSION_CASING_MK3);
    }

    private IBlockState getCoilState() {
        if (this.tier == GTValues.LuV)
            return MetaBlocks.FUSION_CASING.getState(BlockFusionCasing.CasingType.SUPERCONDUCTOR_COIL);

        return MetaBlocks.FUSION_CASING.getState(BlockFusionCasing.CasingType.FUSION_COIL);
    }

    @Nonnull
    @Override
    @SideOnly(Side.CLIENT)
    protected ICubeRenderer getFrontOverlay() {
        return Textures.FUSION_REACTOR_OVERLAY;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        if (this.recipeMapWorkable.isActive()) {
            return Textures.ACTIVE_FUSION_TEXTURE;
        } else {
            return Textures.FUSION_TEXTURE;
        }
    }

    public long getHeat() {
        return this.heat;
    }

    public class IndustrialFusionRecipeLogic extends MultiblockRecipeLogic {

        public IndustrialFusionRecipeLogic(MetaTileEntityIndustrialFusionReactor tileEntity) {
            super(tileEntity);
            this.setAllowOverclocking(false);
        }

        @Override
        public void updateWorkable() {
            super.updateWorkable();
            // Drain heat when the reactor is not active, is paused via soft mallet, or does not have enough energy and
            // has fully wiped recipe progress
            // Don't drain heat when there is not enough energy and there is still some recipe progress, as that makes
            // it doubly hard to complete the recipe
            // (Will have to recover heat and recipe progress)
            if (heat > 0) {
                if (!this.isActive || !this.workingEnabled || (this.hasNotEnoughEnergy && this.progressTime == 0)) {
                    heat = heat <= 10000L * slices ? 0 : (heat - 10000L * slices);
                }
            }
        }

        @Override
        public boolean checkRecipe(@Nonnull Recipe recipe) {
            if (!super.checkRecipe(recipe))
                return false;

            // if the reactor is not able to hold enough energy for it, do not run the recipe
            if (recipe.getProperty(FusionEUToStartProperty.getInstance(), 0L) * slices > energyContainer.getEnergyCapacity())
                return false;

            final long heatDiff = recipe.getProperty(FusionEUToStartProperty.getInstance(), 0L) * slices - heat;
            // if the stored heat is >= required energy, recipe is okay to run
            if (heatDiff <= 0)
                return true;

            // if the remaining energy needed is more than stored, do not run
            if (energyContainer.getEnergyStored() < heatDiff)
                return false;

            // remove the energy needed
            energyContainer.removeEnergy(heatDiff);
            // increase the stored heat
            heat += heatDiff;
            return true;
        }

        @Override
        protected void modifyOverclockPre(@Nonnull OCParams ocParams, @Nonnull RecipePropertyStorage storage) {
            super.modifyOverclockPre(ocParams, storage);
            long recipeEnergy = Math.max(160_000_000, storage.get(FusionEUToStartProperty.getInstance(), 0L));
            final long recipeEnergyOld = recipeEnergy;
            float ocMultiplier = 1;
            while (recipeEnergy <= energyToStart) {
                if (recipeEnergy != recipeEnergyOld)
                    ocMultiplier *= recipeEnergy > 640_000_000 ? 3 : 2.0F;
                recipeEnergy *= 2;
            }
            ocParams.setEut((long) (ocParams.eut() * ocMultiplier));
            ocParams.setDuration((int) (ocParams.duration() / ocMultiplier));
        }

        @Nonnull
        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound tag = super.serializeNBT();
            tag.setLong("Heat", heat);
            return tag;
        }

        @Override
        public void deserializeNBT(@Nonnull NBTTagCompound compound) {
            super.deserializeNBT(compound);
            heat = compound.getLong("Heat");
        }

        @Override
        public long getMaxVoltage() {
            return Math.min(GTValues.V[tier], super.getMaxVoltage());
        }

        @Override
        protected long getMaxParallelVoltage() {
            return this.getMaxVoltage() * this.getParallelLimit();
        }

        @Override
        public int getParallelLimit() {
            return getSlices();
        }

        @Override
        public <T> T getCapability(Capability<T> capability) {
            System.out.println("getting capability");
            return super.getCapability(capability);
        }
    }

    @SideOnly(Side.CLIENT)
    private static final class IndustrialFusionBloomSetup implements IRenderSetup {

        private static final IndustrialFusionBloomSetup INSTANCE = new IndustrialFusionBloomSetup();

        float lastBrightnessX;
        float lastBrightnessY;

        @Override
        public void preDraw(@Nonnull BufferBuilder buffer) {
            BloomEffect.strength = (float) ConfigHolder.client.shader.fusionBloom.strength;
            BloomEffect.baseBrightness = (float) ConfigHolder.client.shader.fusionBloom.baseBrightness;
            BloomEffect.highBrightnessThreshold = (float) ConfigHolder.client.shader.fusionBloom.highBrightnessThreshold;
            BloomEffect.lowBrightnessThreshold = (float) ConfigHolder.client.shader.fusionBloom.lowBrightnessThreshold;
            BloomEffect.step = 1;

            this.lastBrightnessX = OpenGlHelper.lastBrightnessX;
            this.lastBrightnessY = OpenGlHelper.lastBrightnessY;
            GlStateManager.color(1, 1, 1, 1);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
            GlStateManager.disableTexture2D();
        }

        @Override
        public void postDraw(@Nonnull BufferBuilder buffer) {
            GlStateManager.enableTexture2D();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, this.lastBrightnessX, this.lastBrightnessY);
        }
    }
}
