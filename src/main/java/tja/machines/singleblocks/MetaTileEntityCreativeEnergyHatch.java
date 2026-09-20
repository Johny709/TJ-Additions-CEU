package tja.machines.singleblocks;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.DoubleSyncValue;
import com.cleanroommc.modularui.value.sync.InteractionSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.EnergyContainerHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.AbilityInstances;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.IMetaTileEntityGuiHolder;
import gregtech.api.mui.MetaTileEntityGuiData;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tja.TJAValues;
import tja.mui.MUIUtils;
import tja.textures.TJATextures;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.regex.Pattern;

public class MetaTileEntityCreativeEnergyHatch extends MetaTileEntityMultiblockPart implements IMultiblockAbilityPart<IEnergyContainer>, IMetaTileEntityGuiHolder {

    private final EnergyContainerHandler energyContainer = new EnergyContainerHandler(this, Long.MAX_VALUE,
            0, 0, 0, 0) {

        @Override
        public long changeEnergy(long energyToAdd) {
            return energyToAdd;
        }

        @Override
        public long getEnergyStored() {
            return MetaTileEntityCreativeEnergyHatch.this.energyStored;
        }

        @Override
        public long getInputVoltage() {
            return inputVoltage;
        }

        @Override
        public long getInputAmperage() {
            return inputAmps;
        }
    };

    private long energyStored = Long.MAX_VALUE;
    private long inputVoltage = Integer.MAX_VALUE;
    private long inputAmps = 2;

    public MetaTileEntityCreativeEnergyHatch(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GTValues.MAX);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityCreativeEnergyHatch(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("metaitem.creative_cover.tooltip.1"));
    }

    @Override
    public @Nonnull ModularPanel buildUI(MetaTileEntityGuiData metaTileEntityGuiData, PanelSyncManager panelSyncManager, UISettings uiSettings) {
        panelSyncManager.syncValue("energy_bar", new DoubleSyncValue(() -> this.energyContainer.getEnergyCapacity() == 0 ? 0 :
                1.0 * this.energyContainer.getEnergyStored() / this.energyContainer.getEnergyCapacity()));
        panelSyncManager.syncValue("energy_stored", new StringSyncValue(() -> String.valueOf(this.energyStored), this::setEnergyStored));
        panelSyncManager.syncValue("input_voltage", new StringSyncValue(() -> String.valueOf(this.inputVoltage), this::setInputVoltage));
        panelSyncManager.syncValue("input_amps", new StringSyncValue(() -> String.valueOf(this.inputAmps), this::setInputAmps));
        panelSyncManager.syncValue("divide_2_energy", new InteractionSyncHandler()
                .setOnMousePressed(mouseData -> this.setEnergyStored(String.valueOf((double) this.energyStored / 2))));
        panelSyncManager.syncValue("multiply_2_energy", new InteractionSyncHandler()
                .setOnMousePressed(mouseData -> this.setEnergyStored(String.valueOf((double) this.energyStored * 2))));
        panelSyncManager.syncValue("divide_2_voltage", new InteractionSyncHandler()
                .setOnMousePressed(mouseData -> this.setInputVoltage(String.valueOf(this.inputVoltage / 2))));
        panelSyncManager.syncValue("multiply_2_voltage", new InteractionSyncHandler()
                .setOnMousePressed(mouseData -> this.setInputVoltage(String.valueOf(this.inputVoltage * 2))));
        panelSyncManager.syncValue("divide_2_amps", new InteractionSyncHandler()
                .setOnMousePressed(mouseData -> this.setInputAmps(String.valueOf(this.inputAmps / 2))));
        panelSyncManager.syncValue("multiply_2_amps", new InteractionSyncHandler()
                .setOnMousePressed(mouseData -> this.setInputAmps(String.valueOf(this.inputAmps * 2))));

        return ModularPanel.defaultPanel("creative_energy_hatch.gui", 196, 180)
                .child(new ProgressWidget()
                        .pos(7, 7)
                        .size(180, 19)
                        .texture(GTGuiTextures.PROGRESS_BAR_MULTI_ENERGY_YELLOW, 180)
                        .syncHandler("energy_bar"))
                .child(new TextFieldWidget()
                        .pos(26, 27)
                        .size(124, 18)
                        .setMaxLength(18)
                        .autoUpdateOnChange(true)
                        .setValidator(MUIUtils::numberValidator)
                        .setPattern(Pattern.compile("\\*?[0-9_]*\\*?"))
                        .addTooltipLine(IKey.lang("metaitem.creative_energy_cover.set.energy_rate"))
                        .syncHandler("energy_stored"))
                .child(new TextFieldWidget()
                        .pos(26, 47)
                        .size(124, 18)
                        .setMaxLength(10)
                        .autoUpdateOnChange(true)
                        .setValidator(MUIUtils::numberValidator)
                        .setPattern(Pattern.compile("\\*?[0-9_]*\\*?"))
                        .addTooltipLine(IKey.lang("metaitem.creative_energy_cover.set.voltage"))
                        .syncHandler("input_voltage"))
                .child(new TextFieldWidget()
                        .pos(26, 67)
                        .size(124, 18)
                        .setMaxLength(10)
                        .autoUpdateOnChange(true)
                        .setValidator(MUIUtils::numberValidator)
                        .setPattern(Pattern.compile("\\*?[0-9_]*\\*?"))
                        .addTooltipLine(IKey.lang("metaitem.creative_energy_cover.set.amps"))
                        .syncHandler("input_amps"))
                .child(new ButtonWidget<>()
                        .pos(7, 27)
                        .size(18)
                        .overlay(IKey.str("/2"))
                        .background(GuiTextures.MC_BUTTON)
                        .hoverBackground(GuiTextures.MC_BUTTON_HOVERED)
                        .syncHandler("divide_2_energy"))
                .child(new ButtonWidget<>()
                        .pos(151, 27)
                        .size(18)
                        .overlay(IKey.str("*2"))
                        .background(GuiTextures.MC_BUTTON)
                        .hoverBackground(GuiTextures.MC_BUTTON_HOVERED)
                        .syncHandler("multiply_2_energy"))
                .child(new ButtonWidget<>()
                        .pos(7, 47)
                        .size(18)
                        .overlay(IKey.str("/2"))
                        .background(GuiTextures.MC_BUTTON)
                        .hoverBackground(GuiTextures.MC_BUTTON_HOVERED)
                        .syncHandler("divide_2_voltage"))
                .child(new ButtonWidget<>()
                        .pos(151, 47)
                        .size(18)
                        .overlay(IKey.str("*2"))
                        .background(GuiTextures.MC_BUTTON)
                        .hoverBackground(GuiTextures.MC_BUTTON_HOVERED)
                        .syncHandler("multiply_2_voltage"))
                .child(new ButtonWidget<>()
                        .pos(7, 67)
                        .size(18)
                        .overlay(IKey.str("/2"))
                        .background(GuiTextures.MC_BUTTON)
                        .hoverBackground(GuiTextures.MC_BUTTON_HOVERED)
                        .syncHandler("divide_2_amps"))
                .child(new ButtonWidget<>()
                        .pos(151, 67)
                        .size(18)
                        .overlay(IKey.str("*2"))
                        .background(GuiTextures.MC_BUTTON)
                        .hoverBackground(GuiTextures.MC_BUTTON_HOVERED)
                        .syncHandler("multiply_2_amps"))
                .bindPlayerInventory();
    }

    @Override
    public void registerAbilities(@Nonnull AbilityInstances abilityInstances) {
        abilityInstances.add(this.energyContainer);
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
        Textures.ENERGY_IN_MULTI.renderSided(this.getFrontFacing(), renderState, translation, pipeline);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setLong("energyStored", this.energyStored);
        data.setLong("inputVoltage", this.inputVoltage);
        data.setLong("inputAmps", this.inputAmps);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.energyStored = data.getLong("energyStored");
        this.inputVoltage = data.getLong("inputVoltage");
        this.inputAmps = data.getLong("inputAmps");
    }

    public void setEnergyStored(String amount) {
        if (amount == null || amount.isEmpty())
            amount = String.valueOf(0);
        this.energyStored = (long) Math.max(0, Math.min(Long.MAX_VALUE, Double.parseDouble(amount)));
        this.markDirty();
    }

    public void setInputVoltage(String amount) {
        if (amount == null || amount.isEmpty())
            amount = String.valueOf(0);
        this.inputVoltage = Math.max(0, Math.min(2147483648L, Long.parseLong(amount)));
        this.markDirty();
    }

    public void setInputAmps(String amount) {
        if (amount == null || amount.isEmpty())
            amount = String.valueOf(0);
        this.inputAmps = Math.max(0, Math.min(4294967295L, Long.parseLong(amount)));
        this.markDirty();
    }

    @Override
    public @Nullable MultiblockAbility<IEnergyContainer> getAbility() {
        return MultiblockAbility.INPUT_ENERGY;
    }
}
