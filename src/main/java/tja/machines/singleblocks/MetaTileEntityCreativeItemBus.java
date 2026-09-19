package tja.machines.singleblocks;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.PhantomItemSlotSH;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.PhantomItemSlot;
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
import tja.textures.TJATextures;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class MetaTileEntityCreativeItemBus extends MetaTileEntityMultiblockNotifiablePart implements IMultiblockAbilityPart<IItemHandlerModifiable>, IMetaTileEntityGuiHolder {

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
        return new GTLargeItemStackHandler(this, this.getController(), false, 16, Integer.MAX_VALUE) {
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
    public @Nonnull ModularPanel buildUI(MetaTileEntityGuiData metaTileEntityGuiData, PanelSyncManager panelSyncManager, UISettings uiSettings) {
        return ModularPanel.defaultPanel("creative_item_bus.gui", 196, 184)
                .child(new TextWidget<>(IKey.lang(this.getMetaFullName()))
                        .pos(7, 5))
                .child(new Grid()
                        .pos(43, 24)
                        .size(72, 72)
                        .gridOfSizeWidth(this.importItems.getSlots(), 4, (x, y, i) -> new PhantomItemSlot()
                                .syncHandler(new PhantomItemSlotSH(new ModularSlot(this.importItems, i)
                                        .ignoreMaxStackSize(true)))))
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

            renderState.baseColour = TJAValues.VC[this.getTier() - 2] << 8; // TODO get better MAX color overlay. use UMV color overlay for the time being
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

    @Override
    public @Nullable MultiblockAbility<IItemHandlerModifiable> getAbility() {
        return MultiblockAbility.IMPORT_ITEMS;
    }
}
