package tja.integration.ae2.part;

import appeng.api.parts.IPartModel;
import appeng.fluids.parts.PartFluidInterface;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import appeng.tile.networking.TileCableBus;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.GuiFactories;
import com.cleanroommc.modularui.factory.SidedPosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import tja.TJA;
import tja.integration.ae2.ISuperFluidInterface;
import tja.integration.ae2.blocks.BlockSuperFluidInterface;
import tja.integration.ae2.helpers.DualitySuperFluidInterface;
import tja.items.TJAItems;

import javax.annotation.Nonnull;


public class PartSuperFluidInterface extends PartFluidInterface implements IGuiHolder<SidedPosGuiData>, ISuperFluidInterface {

    public static final ResourceLocation MODEL_BASE = new ResourceLocation(TJA.MOD_ID, "part/me.part.super_fluid_interface_base");

    @PartModels
    public static final PartModel MODELS_OFF = new PartModel(MODEL_BASE, new ResourceLocation(TJA.MOD_ID, "part/me.part.super_fluid_interface_off"));

    @PartModels
    public static final PartModel MODELS_ON = new PartModel(MODEL_BASE, new ResourceLocation(TJA.MOD_ID, "part/me.part.super_fluid_interface_on"));

    @PartModels
    public static final PartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, new ResourceLocation(TJA.MOD_ID, "part/me.part.super_fluid_interface_has_channel"));

    public PartSuperFluidInterface(ItemStack is) {
        super(is);
        ObfuscationReflectionHelper.setPrivateValue(PartFluidInterface.class, this, new DualitySuperFluidInterface(this.getProxy(), this, 18), "duality");
    }

    @Override
    public boolean onPartActivate(EntityPlayer player, EnumHand hand, Vec3d pos) {
        final TileCableBus tileCableBus = (TileCableBus) this.getTile();
        if (tileCableBus != null && !player.getEntityWorld().isRemote) {
            GuiFactories.sidedTileEntity().open(player, tileCableBus.getPos(), this.getSide().getFacing());
        }
        return true;
    }

    @Override
    public ModularPanel buildUI(SidedPosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return BlockSuperFluidInterface.createFluidInterfaceGUI(data, syncManager, settings, this);
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return TJAItems.PART_SUPER_FLUID_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY);
    }

    @Nonnull
    @Override
    public IPartModel getStaticModels() {
        if (this.isActive() && this.isPowered()) {
            return MODELS_HAS_CHANNEL;
        } else if (this.isPowered()) {
            return MODELS_ON;
        } else {
            return MODELS_OFF;
        }
    }

    @Override
    public void setPriority(String priority) {
        this.getDualityFluidInterface().setPriority((int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, Long.parseLong(priority))));
        this.getTile().markDirty();
    }

    @Override
    public void setFluidAutoPull(boolean autoPull) {
        // No such feature
    }
}
