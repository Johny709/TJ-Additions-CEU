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
import gregtech.api.capability.impl.GhostCircuitItemStackHandler;
import gregtech.api.capability.impl.ItemHandlerList;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import tja.TJAValues;
import tja.items.handlers.GTLargeItemStackHandler;
import tja.mui.MUIUtils;
import tja.mui.sync.SelectionWidgetGroupSH;
import tja.mui.sync.TJAPhantomItemSlotSH;
import tja.mui.widget.SelectionWidgetGroup;
import tja.mui.widget.TJAPhantomItemSlotWidget;
import tja.textures.TJATextures;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class MetaTileEntityCreativeItemBus extends MetaTileEntityMultiblockNotifiablePart implements IMetaTileEntityGuiHolder,
        IMultiblockAbilityPart<IItemHandlerModifiable> {

    private final GhostCircuitItemStackHandler circuitSlot = new GhostCircuitItemStackHandler(this);
    private final IItemHandlerModifiable combinedInventory;

    public MetaTileEntityCreativeItemBus(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GTValues.MAX, false);
        this.combinedInventory = new ItemHandlerList(Arrays.asList(this.circuitSlot, this.importItems));
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityCreativeItemBus(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("metaitem.creative_cover.tooltip.1"));
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new GTLargeItemStackHandler(this, 16, Integer.MAX_VALUE,  this.getController(), false) {
            @Nonnull
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (amount == Integer.MIN_VALUE)
                    return super.extractItem(slot, amount, simulate);
                final ItemStack stack = this.getStackInSlot(slot).copy();
                stack.setCount(Math.min(stack.getCount(), amount));
                return stack;
            }
        };
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
                        this.setItemCount(String.valueOf(Long.parseLong(this.getItemCount(selectionWidgetGroupSH.getSelectedIndex())) * 2),
                                selectionWidgetGroupSH.getSelectedIndex())));
        syncManager.syncValue("count_divide", new InteractionSyncHandler()
                .setOnMousePressed(mouseData ->
                        this.setItemCount(String.valueOf(Long.parseLong(this.getItemCount(selectionWidgetGroupSH.getSelectedIndex())) / 2),
                                selectionWidgetGroupSH.getSelectedIndex())));

        return ModularPanel.defaultPanel("creative_item_bus.gui", 196, 184)
                .child(new TextWidget<>(IKey.lang(this.getMetaFullName()))
                        .pos(7, 5))
                .child(new SelectionWidgetGroup()
                        .pos(43, 24)
                        .size(72, 72)
                        .gridOfSizeWidth(this.importItems.getSlots(), 4, (x, y, i) -> new TJAPhantomItemSlotWidget()
                                .syncHandler(new TJAPhantomItemSlotSH(this.importItems, i)))
                        .syncHandler("selection"))
                .child(new TextFieldWidget()
                        .pos(26, 5)
                        .size(124, 18)
                        .setMaxLength(10)
                        .autoUpdateOnChange(true)
                        .setValidator(MUIUtils::numberValidator)
                        .setPattern(Pattern.compile("\\*?[0-9_]*\\*?"))
                        .setEnabledIf(textFieldWidget -> selectionWidgetGroupSH.getSelectedIndex() >= 0)
                        .value(new StringSyncValue(() -> this.getItemCount(selectionWidgetGroupSH.getSelectedIndex()),
                                string -> this.setItemCount(string, selectionWidgetGroupSH.getSelectedIndex()))))
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
        abilityInstances.add(this.combinedInventory);
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
        Textures.ITEM_HATCH_INPUT_OVERLAY.renderSided(this.getFrontFacing(), renderState, translation, pipeline);
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

    private void setItemCount(String text, int index) {
        if (index < 0 || index >= this.importItems.getSlots()) return;
        final ItemStack stack = this.importItems.getStackInSlot(index);
        if (stack.isEmpty()) return;
        stack.setCount((int) Math.min(Integer.MAX_VALUE, Long.parseLong(text)));
        this.markDirty();
    }

    private String getItemCount(int index) {
        return String.valueOf(index >= 0 && index < this.importItems.getSlots() ? this.importItems.getStackInSlot(index).getCount() : 0);
    }

    @Override
    public @Nullable MultiblockAbility<IItemHandlerModifiable> getAbility() {
        return MultiblockAbility.IMPORT_ITEMS;
    }
}
