package tja.multiblockpart;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.scroll.VerticalScrollData;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.GhostCircuitItemStackHandler;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.AbilityInstances;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.mui.IMetaTileEntityGuiHolder;
import gregtech.api.mui.MetaTileEntityGuiData;
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
import tja.items.handlers.NotifiableLargeItemStackHandler;
import tja.textures.TJATextures;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class MetaTileEntitySuperBus extends MetaTileEntityMultiblockNotifiablePart implements IMetaTileEntityGuiHolder,
        IMultiblockAbilityPart<IItemHandlerModifiable> {

    private final GhostCircuitItemStackHandler circuitSlot = new GhostCircuitItemStackHandler(this);
    private final IItemHandlerModifiable combinedInventory;

    public MetaTileEntitySuperBus(ResourceLocation metaTileEntityId, int tier, boolean isExportHatch) {
        super(metaTileEntityId, tier, isExportHatch);
        this.initializeInventory();
        this.combinedInventory = new ItemHandlerList(Arrays.asList(this.circuitSlot, this.importItems));
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntitySuperBus(this.metaTileEntityId, this.getTier(), this.isExportHatch);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, @Nonnull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format(this.isExportHatch ? "gregtech.machine.item_bus.export.tooltip" : "gregtech.machine.item_bus.import.tooltip"));
        tooltip.add(I18n.format("tja.machine.universal.stack", TJAValues.thousandFormat.format(this.getCapacity(this.getTier()))));
        tooltip.add(I18n.format("tja.machine.universal.slots", TJAValues.thousandFormat.format(this.getTierGrid(this.getTier()))));
        tooltip.add(I18n.format("gregtech.universal.enabled"));
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        final int tier = this.getTier();
        return this.isExportHatch ? super.createImportItemHandler() :
                new NotifiableLargeItemStackHandler(this, this.getTierGrid(tier), this.getCapacity(tier), null, false);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        final int tier = this.getTier();
        return !this.isExportHatch ? super.createExportItemHandler() :
                new NotifiableLargeItemStackHandler(this, this.getTierGrid(tier), this.getCapacity(tier), null, true);
    }

    @Override
    public @Nonnull ModularPanel buildUI(MetaTileEntityGuiData data, PanelSyncManager syncManager, UISettings settings) {
        final IItemHandlerModifiable itemHandler = this.isExportHatch ? this.exportItems : this.importItems;
        final int slotLength = (int) Math.sqrt(itemHandler.getSlots());
        syncManager.registerSlotGroup(new SlotGroup("item_input", slotLength, 1, true));

        final int offsetY = Math.min(9, this.getTier()) * 18;
        final int offsetX = Math.max(7, 88 - (9 * this.getTier()));
        return ModularPanel.defaultPanel("super_bus.gui", 194, 133 + offsetY)
                .child(IKey.lang(this.getMetaFullName()).asWidget()
                        .pos(7, 5))
                .child(new Grid()
                        .pos(offsetX, 18)
                        .size(Math.min(180, 4 + 18 * (this.getTier() + 1)))
                        .scrollable(new VerticalScrollData() {{
                            this.setScrollSize((int) Math.ceil((double) itemHandler.getSlots() / Math.min(7, slotLength)) * 18);
                        }})
                        .gridOfSizeWidth(itemHandler.getSlots(), slotLength, (x, y, i) -> new ItemSlot()
                                .slot(new ModularSlot(itemHandler, i)
                                        .ignoreMaxStackSize(true)
                                        .slotGroup("item_input"))))
                .bindPlayerInventory();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (this.getController() == null) {
            final int oldBaseColor = renderState.baseColour;
            final int oldAlphaOverride = renderState.alphaOverride;

            // TODO get better MAX color overlay. use UMV color overlay for the time being
            renderState.baseColour = TJAValues.VC[this.getTier()] << 8;
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
    public void registerAbilities(@Nonnull AbilityInstances abilityInstances) {
        abilityInstances.add(this.combinedInventory);
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
    public @Nullable MultiblockAbility<IItemHandlerModifiable> getAbility() {
        return this.isExportHatch ? MultiblockAbility.EXPORT_ITEMS : MultiblockAbility.IMPORT_ITEMS;
    }

    public int getTierGrid(int tier) {
        tier++;
        return tier * tier;
    }

    public int getCapacity(int tier) {
        return 64 * (64 << Math.max(0, tier - GTValues.UHV));
    }
}
