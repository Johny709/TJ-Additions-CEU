package tja.multiblockpart;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.scroll.VerticalScrollData;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.google.common.collect.ImmutableList;
import gregtech.api.GTValues;
import gregtech.api.capability.*;
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
import gregtech.api.mui.sync.GTFluidSyncHandler;
import gregtech.api.mui.widget.GhostCircuitSlotWidget;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockNotifiablePart;
import gregtech.common.mui.widget.GTFluidSlot;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tja.TJAValues;
import tja.textures.TJATextures;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MetaTileEntitySuperFluidHatch extends MetaTileEntityMultiblockNotifiablePart implements IMetaTileEntityGuiHolder,
        IMultiblockAbilityPart<IFluidTank>,
        IGhostSlotConfigurable,
        IControllable {

    private final GhostCircuitItemStackHandler circuitSlot = new GhostCircuitItemStackHandler(this);
    private boolean workingEnabled;

    public MetaTileEntitySuperFluidHatch(ResourceLocation metaTileEntityId, int tier, boolean isExportHatch) {
        super(metaTileEntityId, tier, isExportHatch);
        this.initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntitySuperFluidHatch(this.metaTileEntityId, this.getTier(), this.isExportHatch);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, @Nonnull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format(this.isExportHatch ? "gregtech.machine.fluid_hatch.export.tooltip" : "gregtech.machine.fluid_hatch.import.tooltip"));
        tooltip.add(I18n.format("gregtech.universal.tooltip.fluid_storage_capacity_mult", this.getTierGrid(this.getTier()),
                this.getCapacity(this.getTier())));
        tooltip.add(I18n.format("gregtech.universal.enabled"));
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        return this.isExportHatch ? super.createImportFluidHandler() :
                new FluidTankList(true, IntStream.range(0, this.getTierGrid(this.getTier()))
                .mapToObj(i -> new NotifiableFluidTank(this.getCapacity(this.getTier()), this, false))
                .collect(Collectors.toList()));
    }

    @Override
    protected FluidTankList createExportFluidHandler() {
        return !this.isExportHatch ? super.createExportFluidHandler() :
                new FluidTankList(true, IntStream.range(0, this.getTierGrid(this.getTier()))
                .mapToObj(i -> new NotifiableFluidTank(this.getCapacity(this.getTier()), this, false))
                .collect(Collectors.toList()));
    }

    @Override
    public void update() {
        super.update();
        if (!this.getWorld().isRemote) {
            if (this.workingEnabled) {
                if (this.isExportHatch) {
                    pushFluidsIntoNearbyHandlers(this.getFrontFacing());
                } else {
                    pullFluidsFromNearbyHandlers(this.getFrontFacing());
                }
            }
        }
    }

    @Override
    public @Nonnull ModularPanel buildUI(MetaTileEntityGuiData data, PanelSyncManager syncManager, UISettings settings) {
        final BooleanSyncValue workingEnabled = new BooleanSyncValue(this::isWorkingEnabled, this::setWorkingEnabled);
        syncManager.syncValue("working_enabled", workingEnabled);

        final IMultipleTankHandler tanks = this.isExportHatch ? this.exportFluids : this.importFluids;
        final int slotLength = (int) Math.sqrt(tanks.getTanks());
        final int offsetX = Math.max(7, 88 - (9 * this.getTier()));
        final int offsetY = Math.min(9, this.getTier()) * 18;
        return ModularPanel.defaultPanel("super_fluid_hatch.gui", 200, 133 + offsetY)
                .child(IKey.lang(this.getMetaFullName()).asWidget()
                        .pos(7, 5))
                .child(new Grid()
                        .pos(offsetX, 18)
                        .size(Math.min(184, 4 + 18 * (this.getTier() + 1)), Math.min(180, 18 * (this.getTier() + 1)))
                        .scrollable(new VerticalScrollData() {{
                            this.setScrollSize((int) Math.ceil((double) tanks.getTanks() / Math.min(7, slotLength)) * 18);
                        }})
                        .gridOfSizeWidth(tanks.getTanks(), slotLength, (x, y, i) -> new GTFluidSlot()
                                .syncHandler(new GTFluidSyncHandler(tanks.getTankAt(i))
                                        .accessibility(true, !this.isExportHatch))))
                .child(Flow.col()
                        .pos(174, 50 + offsetY)
                        .size(18, 54)
                        .childIf(this.isExportHatch, () -> new Widget<>()
                                .background(GTGuiTextures.SLOT, GTGuiTextures.BUTTON_X)
                                .addTooltipLine(IKey.lang("gregtech.gui.configurator_slot.unavailable.tooltip")))
                        .childIf(!this.isExportHatch, () -> new GhostCircuitSlotWidget()
                                .background(GTGuiTextures.SLOT, GTGuiTextures.INT_CIRCUIT_OVERLAY)
                                .slot(this.circuitSlot, 0))
                        .child(new ToggleButton()
                                .top(36)
                                .overlay(GTGuiTextures.BUTTON_FLUID_OUTPUT)
                                .tooltipDynamic(richTooltip -> richTooltip.addLine(IKey.lang(this.isExportHatch ?
                                        workingEnabled.getBoolValue() ? "gregtech.gui.fluid_auto_output.tooltip.enabled" :
                                                "gregtech.gui.fluid_auto_output.tooltip.disabled" :
                                        workingEnabled.getBoolValue() ? "gregtech.gui.fluid_auto_input.tooltip.enabled" :
                                                "gregtech.gui.fluid_auto_input.tooltip.disabled"
                                )))
                                .syncHandler("working_enabled"))
                        .child(GTGuiTextures.GREGTECH_LOGO.asWidget()
                                .top(58)))
                .child(SlotGroupWidget.playerInventory(false)
                        .left(7).bottom(7));
    }

    @Override
    public void registerAbilities(@Nonnull AbilityInstances abilityInstances) {
        if (abilityInstances.isKey(MultiblockAbility.IMPORT_ITEMS))
            abilityInstances.add(this.circuitSlot);
        if (abilityInstances.isKey(MultiblockAbility.IMPORT_FLUIDS))
            abilityInstances.add(this.importFluids);
        if (abilityInstances.isKey(MultiblockAbility.EXPORT_FLUIDS))
            abilityInstances.add(this.exportFluids);
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
            Textures.FLUID_HATCH_OUTPUT_OVERLAY.renderSided(this.getFrontFacing(), renderState, translation, pipeline);
        } else {
            Textures.PIPE_IN_OVERLAY.renderSided(this.getFrontFacing(), renderState, translation, pipeline);
            Textures.FLUID_HATCH_INPUT_OVERLAY.renderSided(this.getFrontFacing(), renderState, translation, pipeline);
        }
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.WORKING_ENABLED) {
            this.workingEnabled = buf.readBoolean();
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(this.workingEnabled);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.workingEnabled = buf.readBoolean();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        this.circuitSlot.write(data);
        data.setBoolean("workingEnabled", this.workingEnabled);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.circuitSlot.read(data);
        this.workingEnabled = data.getBoolean("workingEnabled");
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE)
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        return super.getCapability(capability, side);
    }

    @Nonnull
    @Override
    public List<MultiblockAbility<?>> getAbilities() {
        final ImmutableList.Builder<MultiblockAbility<?>> abilities = ImmutableList.builder();
        if (!this.isExportHatch)
            abilities.add(MultiblockAbility.IMPORT_ITEMS);
        abilities.add(this.isExportHatch ? MultiblockAbility.EXPORT_FLUIDS : MultiblockAbility.IMPORT_FLUIDS);
        return abilities.build();
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

    public int getTierGrid(int tier) {
        tier++;
        return tier * tier;
    }

    public int getCapacity(int tier) {
        return (int) Math.min(Integer.MAX_VALUE, (8000L << tier) * (64L << Math.max(0, tier - GTValues.UHV)));
    }
}
