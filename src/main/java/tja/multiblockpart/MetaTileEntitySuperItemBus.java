package tja.multiblockpart;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.scroll.VerticalScrollData;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.IGhostSlotConfigurable;
import gregtech.api.capability.impl.GhostCircuitItemStackHandler;
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
import gregtech.api.util.GTHashMaps;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockNotifiablePart;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import tja.TJAValues;
import tja.integration.ae2.ITIckSetter;
import tja.items.handlers.NotifiableLargeItemStackHandler;
import tja.mui.MUIUtils;
import tja.textures.TJATextures;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MetaTileEntitySuperItemBus extends MetaTileEntityMultiblockNotifiablePart implements IMetaTileEntityGuiHolder,
        IMultiblockAbilityPart<IItemHandlerModifiable>,
        IGhostSlotConfigurable,
        IControllable,
        ITIckSetter {

    private final GhostCircuitItemStackHandler circuitSlot = new GhostCircuitItemStackHandler(this);
    private boolean workingEnabled = true;
    private boolean autoCollapse;
    private int tickTime = 5;

    public MetaTileEntitySuperItemBus(ResourceLocation metaTileEntityId, int tier, boolean isExportHatch) {
        super(metaTileEntityId, tier, isExportHatch);
        this.initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntitySuperItemBus(this.metaTileEntityId, this.getTier(), this.isExportHatch);
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
    public void update() {
        super.update();
        if (!this.getWorld().isRemote && this.getOffsetTimer() % this.tickTime == 0) {
            if (this.workingEnabled) {
                if (this.isExportHatch) {
                    this.pushItemsIntoNearbyHandlers(this.getFrontFacing());
                } else {
                    this.pullItemsFromNearbyHandlers(this.getFrontFacing());
                }
            }
            // Only attempt to auto collapse the inventory contents once the bus has been notified
            if (this.isAutoCollapse()) {
                // Exclude the ghost circuit inventory from the auto collapse, so it does not extract any ghost circuits
                // from the slot
                IItemHandlerModifiable inventory = (this.isExportHatch ? this.getExportItems() : super.getImportItems());
                if (!this.isAttachedToMultiBlock() || (this.isExportHatch ? this.getNotifiedItemOutputList().contains(inventory) :
                        this.getNotifiedItemInputList().contains(inventory))) {
                    collapseInventorySlotContents(inventory);
                }
            }
        }
    }

    @Override
    public @Nonnull ModularPanel buildUI(MetaTileEntityGuiData data, PanelSyncManager syncManager, UISettings settings) {
        final BooleanSyncValue autoCollapse = new BooleanSyncValue(this::isAutoCollapse, this::setAutoCollapse);
        syncManager.syncValue("auto_collapse", autoCollapse);
        final BooleanSyncValue workingEnabled = new BooleanSyncValue(this::isWorkingEnabled, this::setWorkingEnabled);
        syncManager.syncValue("working_enabled", workingEnabled);
        final IItemHandlerModifiable itemHandler = this.isExportHatch ? this.exportItems : this.importItems;
        final int slotLength = (int) Math.sqrt(itemHandler.getSlots());
        syncManager.registerSlotGroup(new SlotGroup("item_input", slotLength, 1, true));

        final int offsetX = Math.max(7, 88 - (9 * this.getTier()));
        final int offsetY = Math.min(9, this.getTier()) * 18;
        final IPanelHandler tickPanel = syncManager.syncedPanel("tick.panel", true,
                (sync, handler) -> MUIUtils.createTicksPanel(sync, handler, this));
        syncManager.syncValue("tick_open", new BooleanSyncValue(tickPanel::isPanelOpen, value -> {
            if (!value && tickPanel.isPanelOpen()) {
                tickPanel.closePanel();
            } else if (value)
                tickPanel.openPanel();
        }));
        return ModularPanel.defaultPanel("super_bus.gui", 200, 145 + offsetY)
                .child(IKey.lang(this.getMetaFullName()).asWidget()
                        .pos(7, 5))
                .child(new Grid()
                        .pos(offsetX, 18)
                        .size(Math.min(184, 4 + 18 * (this.getTier() + 1)), Math.min(180, 18 * (this.getTier() + 1)))
                        .scrollable(new VerticalScrollData() {{
                            this.setScrollSize((int) Math.ceil((double) itemHandler.getSlots() / Math.min(7, slotLength)) * 18);
                        }})
                        .gridOfSizeWidth(itemHandler.getSlots(), slotLength, (x, y, i) -> new ItemSlot()
                                .slot(new ModularSlot(itemHandler, i)
                                        .ignoreMaxStackSize(true)
                                        .canPut(!this.isExportHatch)
                                        .slotGroup("item_input"))))
                .child(new TextFieldWidget()
                        .pos(7, 40 + offsetY)
                        .size(162, 18)
                        .setMaxLength(10)
                        .autoUpdateOnChange(true)
                        .setValidator(MUIUtils::numberValidator)
                        .setPattern(Pattern.compile("\\*?[0-9_]*\\*?"))
                        .addTooltipLine(IKey.lang("tja.machine.universal.ticks.operation"))
                        .value(new StringSyncValue(() -> String.valueOf(this.getTickTime()), this::setTickTime)))
                .child(Flow.col()
                        .pos(174, 40 + offsetY)
                        .size(18, 54)
                        .child(new ToggleButton()
                                .overlay(GTGuiTextures.FILTER_SETTINGS_OVERLAY)
                                .addTooltipLine(IKey.lang("tja.machine.universal.configure.ticks_operation"))
                                .syncHandler("tick_open"))
                        .childIf(this.isExportHatch, () -> new Widget<>()
                                .top(22)
                                .background(GTGuiTextures.SLOT, GTGuiTextures.BUTTON_X)
                                .addTooltipLine(IKey.lang("gregtech.gui.configurator_slot.unavailable.tooltip")))
                        .childIf(!this.isExportHatch, () -> new GhostCircuitSlotWidget()
                                .top(22)
                                .background(GTGuiTextures.SLOT, GTGuiTextures.INT_CIRCUIT_OVERLAY)
                                .slot(this.circuitSlot, 0))
                        .child(new ToggleButton()
                                .top(40)
                                .overlay(GTGuiTextures.BUTTON_AUTO_COLLAPSE)
                                .tooltipDynamic(richTooltip -> richTooltip.addLine(IKey.lang(autoCollapse.getBoolValue() ?
                                        "gregtech.gui.item_auto_collapse.tooltip.enabled" : "gregtech.gui.item_auto_collapse.tooltip.disabled")))
                                .syncHandler("auto_collapse"))
                        .child(new ToggleButton()
                                .top(58)
                                .overlay(GTGuiTextures.BUTTON_ITEM_OUTPUT)
                                .tooltipDynamic(richTooltip -> richTooltip.addLine(IKey.lang(this.isExportHatch ?
                                        workingEnabled.getBoolValue() ? "gregtech.gui.item_auto_output.tooltip.enabled" :
                                                "gregtech.gui.item_auto_output.tooltip.disabled" :
                                        workingEnabled.getBoolValue() ? "gregtech.gui.item_auto_input.tooltip.enabled" :
                                                "gregtech.gui.item_auto_input.tooltip.disabled"
                                )))
                                .syncHandler("working_enabled"))
                        .child(GTGuiTextures.GREGTECH_LOGO.asWidget()
                                .top(80)))
                .child(SlotGroupWidget.playerInventory(false)
                        .left(7).bottom(7));
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
        if (this.isExportHatch) {
            Textures.PIPE_OUT_OVERLAY.renderSided(this.getFrontFacing(), renderState, translation, pipeline);
            Textures.ITEM_HATCH_OUTPUT_OVERLAY.renderSided(this.getFrontFacing(), renderState, translation, pipeline);
        } else {
            Textures.PIPE_IN_OVERLAY.renderSided(this.getFrontFacing(), renderState, translation, pipeline);
            Textures.ITEM_HATCH_INPUT_OVERLAY.renderSided(this.getFrontFacing(), renderState, translation, pipeline);
        }
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        this.setAutoCollapse(!this.autoCollapse);
        if (!this.getWorld().isRemote) {
            if (this.autoCollapse) {
                playerIn.sendStatusMessage(new TextComponentTranslation("gregtech.bus.collapse_true"), true);
            } else {
                playerIn.sendStatusMessage(new TextComponentTranslation("gregtech.bus.collapse_false"), true);
            }
        }
        return true;
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.TOGGLE_COLLAPSE_ITEMS) {
            this.autoCollapse = buf.readBoolean();
        } else if (dataId == GregtechDataCodes.WORKING_ENABLED) {
            this.workingEnabled = buf.readBoolean();
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(this.workingEnabled);
        buf.writeBoolean(this.autoCollapse);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.workingEnabled = buf.readBoolean();
        this.autoCollapse = buf.readBoolean();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        this.circuitSlot.write(data);
        data.setBoolean("autoCollapse", this.autoCollapse);
        data.setBoolean("workingEnabled", this.workingEnabled);
        data.setInteger("tickTime", this.tickTime);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.circuitSlot.read(data);
        this.autoCollapse = data.getBoolean("autoCollapse");
        this.workingEnabled = data.getBoolean("workingEnabled");
        this.tickTime = Math.max(1, data.getInteger("tickTime"));
    }

    @Override
    public void registerAbilities(@Nonnull AbilityInstances abilityInstances) {
        if (abilityInstances.isKey(MultiblockAbility.EXPORT_ITEMS))
            abilityInstances.add(this.exportItems);
        if (abilityInstances.isKey(MultiblockAbility.IMPORT_ITEMS)) {
            abilityInstances.add(this.circuitSlot);
            abilityInstances.add(this.importItems);
        }
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
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE)
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        return super.getCapability(capability, side);
    }

    @Override
    public @Nullable MultiblockAbility<IItemHandlerModifiable> getAbility() {
        return this.isExportHatch ? MultiblockAbility.EXPORT_ITEMS : MultiblockAbility.IMPORT_ITEMS;
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        this.workingEnabled = workingEnabled;
        World world = getWorld();
        if (world != null && !world.isRemote) {
            writeCustomData(GregtechDataCodes.WORKING_ENABLED, buf -> buf.writeBoolean(workingEnabled));
        }
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.workingEnabled;
    }

    public boolean isAutoCollapse() {
        return this.autoCollapse;
    }

    public void setAutoCollapse(boolean inverted) {
        this.autoCollapse = inverted;
        if (!this.getWorld().isRemote) {
            if (this.autoCollapse) {
                if (this.isExportHatch) {
                    this.addNotifiedOutput(this.getExportItems());
                } else {
                    this.addNotifiedInput(super.getImportItems());
                }
            }
            this.writeCustomData(GregtechDataCodes.TOGGLE_COLLAPSE_ITEMS,
                    packetBuffer -> packetBuffer.writeBoolean(this.autoCollapse));
            this.notifyBlockUpdate();
            this.markDirty();
        }
    }

    @Override
    public boolean hasGhostCircuitInventory() {
        return !this.isExportHatch;
    }

    @Override
    public void setGhostCircuitConfig(int config) {
        if (this.circuitSlot.getCircuitValue() == config) {
            return;
        }
        this.circuitSlot.setCircuitValue(config);
        if (!this.getWorld().isRemote) {
            this.markDirty();
        }
    }

    @Override
    public void setTickTime(String tickTime) {
        if (tickTime == null || tickTime.isEmpty())
            tickTime = String.valueOf(1);
        this.tickTime = (int) Math.max(1, Math.min(Integer.MAX_VALUE, Long.parseLong(tickTime)));
        this.markDirty();
    }

    @Override
    public int getTickTime() {
        return this.tickTime;
    }

    public int getTierGrid(int tier) {
        tier++;
        return tier * tier;
    }

    public int getCapacity(int tier) {
        if (tier >= GTValues.MAX)
            return Integer.MAX_VALUE;
        return 64 * (64 << Math.max(0, tier - GTValues.UHV));
    }

    private static void collapseInventorySlotContents(IItemHandlerModifiable inventory) {
        // Gather a snapshot of the provided inventory
        Object2IntMap<ItemStack> inventoryContents = GTHashMaps.fromItemHandler(inventory, true);

        List<ItemStack> inventoryItemContents = new ArrayList<>();

        // Populate the list of item stacks in the inventory with apportioned item stacks, for easy replacement
        for (Object2IntMap.Entry<ItemStack> e : inventoryContents.object2IntEntrySet()) {
            ItemStack stack = e.getKey();
            int count = e.getIntValue();
            int maxStackSize = stack.getMaxStackSize();
            while (count >= maxStackSize) {
                ItemStack copy = stack.copy();
                copy.setCount(maxStackSize);
                inventoryItemContents.add(copy);
                count -= maxStackSize;
            }
            if (count > 0) {
                ItemStack copy = stack.copy();
                copy.setCount(count);
                inventoryItemContents.add(copy);
            }
        }

        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stackToMove;
            // Ensure that we are not exceeding the List size when attempting to populate items
            if (i >= inventoryItemContents.size()) {
                stackToMove = ItemStack.EMPTY;
            } else {
                stackToMove = inventoryItemContents.get(i);
            }

            // Populate the slots
            inventory.setStackInSlot(i, stackToMove);
        }
    }
}
