package tja.multiblockpart;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.InteractionSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.GhostCircuitItemStackHandler;
import gregtech.api.capability.impl.NotifiableFluidTank;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.AbilityInstances;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.IMetaTileEntityGuiHolder;
import gregtech.api.mui.MetaTileEntityGuiData;
import gregtech.api.mui.widget.GhostCircuitSlotWidget;
import gregtech.api.util.KeyUtil;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockNotifiablePart;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tja.TJAValues;
import tja.mui.MUIUtils;
import tja.mui.sync.SelectionWidgetGroupSH;
import tja.mui.sync.TJAPhantomFluidSlotSH;
import tja.mui.widget.SelectionWidgetGroup;
import tja.mui.widget.TJAPhantomFluidSlotWidget;
import tja.textures.TJATextures;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MetaTileEntityCreativeFluidHatch extends MetaTileEntityMultiblockNotifiablePart implements IMetaTileEntityGuiHolder,
        IMultiblockAbilityPart<IFluidTank> {

    private final GhostCircuitItemStackHandler circuitSlot = new GhostCircuitItemStackHandler(this);

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
                .mapToObj(i -> new NotifiableFluidTank(Integer.MAX_VALUE, this.getController(), false) {

                    @Override
                    public int fill(FluidStack resource, boolean doFill) {
                        if (doFill)
                            this.onContentsChanged();
                        return super.fill(resource, doFill);
                    }

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
    public void addToMultiBlock(MultiblockControllerBase controllerBase) {
        super.addToMultiBlock(controllerBase);
        this.circuitSlot.addNotifiableMetaTileEntity(controllerBase);
    }

    @Override
    public void removeFromMultiBlock(MultiblockControllerBase controllerBase) {
        super.removeFromMultiBlock(controllerBase);
        this.circuitSlot.removeNotifiableMetaTileEntity(controllerBase);
    }

    @Override
    public @Nonnull ModularPanel buildUI(MetaTileEntityGuiData data, PanelSyncManager syncManager, UISettings settings) {
        final SelectionWidgetGroupSH selectionWidgetGroupSH = new SelectionWidgetGroupSH();
        syncManager.syncValue("selection", selectionWidgetGroupSH);
        syncManager.syncValue("count_multiply", new InteractionSyncHandler()
                .setOnMousePressed(mouseData ->
                        this.setFluidCount(String.valueOf(Long.parseLong(this.getFluidCount(selectionWidgetGroupSH.getSelectedIndex())) * 2),
                                selectionWidgetGroupSH.getSelectedIndex())));
        syncManager.syncValue("count_divide", new InteractionSyncHandler()
                .setOnMousePressed(mouseData ->
                        this.setFluidCount(String.valueOf(Long.parseLong(this.getFluidCount(selectionWidgetGroupSH.getSelectedIndex())) / 2),
                                selectionWidgetGroupSH.getSelectedIndex())));

        return ModularPanel.defaultPanel("creative_fluid_hatch.gui", 196, 184)
                .child(new TextWidget<>(IKey.lang(this.getMetaFullName()))
                        .pos(7, 5))
                .child(new SelectionWidgetGroup()
                        .pos(43, 24)
                        .size(72, 72)
                        .gridOfSizeWidth(this.importFluids.getTanks(), 4, (x, y, i) -> new TJAPhantomFluidSlotWidget()
                                .syncHandler(new TJAPhantomFluidSlotSH(this.importFluids, i)
                                        .setSpecialDrainingMode(true)))
                        .syncHandler("selection"))
                .child(new TextFieldWidget()
                        .pos(26, 5)
                        .size(124, 18)
                        .setMaxLength(10)
                        .autoUpdateOnChange(true)
                        .setValidator(MUIUtils::numberValidator)
                        .setPattern(Pattern.compile("\\*?[0-9_]*\\*?"))
                        .setEnabledIf(textFieldWidget -> selectionWidgetGroupSH.getSelectedIndex() >= 0)
                        .value(new StringSyncValue(() -> this.getFluidCount(selectionWidgetGroupSH.getSelectedIndex()),
                                string -> this.setFluidCount(string, selectionWidgetGroupSH.getSelectedIndex()))))
                .child(new ButtonWidget<>()
                        .pos(7, 5)
                        .overlay(KeyUtil.string("*2"))
                        .setEnabledIf(w -> selectionWidgetGroupSH.getSelectedIndex() >= 0)
                        .syncHandler("count_multiply"))
                .child(new ButtonWidget<>()
                        .pos(151, 5)
                        .overlay(KeyUtil.string("/2"))
                        .setEnabledIf(w -> selectionWidgetGroupSH.getSelectedIndex() >= 0)
                        .syncHandler("count_divide"))
                .child(new GhostCircuitSlotWidget()
                        .pos(162, 78)
                        .background(GuiTextures.SLOT_ITEM, GTGuiTextures.INT_CIRCUIT_OVERLAY)
                        .slot(this.circuitSlot, 0))
                .bindPlayerInventory();
    }

    @Override
    public void registerAbilities(@Nonnull AbilityInstances abilityInstances) {
        if (abilityInstances.isKey(MultiblockAbility.IMPORT_ITEMS))
            abilityInstances.add(this.circuitSlot);
        if (abilityInstances.isKey(MultiblockAbility.IMPORT_FLUIDS))
            abilityInstances.add(this.importFluids);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (this.getController() == null) {
            final int oldBaseColor = renderState.baseColour;
            final int oldAlphaOverride = renderState.alphaOverride;

            // TODO get better MAX color overlay. use UMV color overlay for the time being
            renderState.baseColour = TJAValues.VC[this.getTier() - 2] << 8;
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
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        this.circuitSlot.write(data);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.circuitSlot.read(data);
    }

    private void setFluidCount(String text, int index) {
        if (index < 0 || index >= this.importFluids.getTanks()) return;
        final FluidStack fluidStack = this.importFluids.getTankAt(index).getFluid();
        if (fluidStack == null) return;
        fluidStack.amount = (int) Math.min(Integer.MAX_VALUE, Long.parseLong(text));
        final FluidStack testStack = fluidStack.copy();
        testStack.amount = 0;
        // to notify MTEs that inventory contents have changed
        this.importFluids.getTankAt(index).fill(testStack, true);
        this.markDirty();
    }

    private String getFluidCount(int index) {
        return String.valueOf(index >= 0 && index < this.importFluids.getTanks() ? this.importFluids.getTankAt(index).getFluidAmount() : 0);
    }

    @Nonnull
    @Override
    public List<MultiblockAbility<?>> getAbilities() {
        return Arrays.asList(MultiblockAbility.IMPORT_ITEMS, MultiblockAbility.IMPORT_FLUIDS);
    }
}
