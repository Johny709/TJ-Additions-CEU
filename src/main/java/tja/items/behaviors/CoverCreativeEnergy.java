package tja.items.behaviors;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IQuantumStorage;
import gregtech.api.cover.CoverBase;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverableView;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import tja.capability.IEnergyContainerStorage;
import tja.machines.MetaTileEntityUtils;
import tja.textures.TJATextures;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CoverCreativeEnergy extends CoverBase implements ITickable {

    private IEnergyContainer energyContainer;

    public CoverCreativeEnergy(@Nonnull CoverDefinition definition, @Nonnull CoverableView coverableView, @Nonnull EnumFacing attachedSide) {
        super(definition, coverableView, attachedSide);
    }

    @Override
    public boolean canAttach(@Nonnull CoverableView coverableView, @Nonnull EnumFacing enumFacing) {
        final BlockPos pos = coverableView.getPos();
        final World world = coverableView.getWorld();
        final MetaTileEntity metaTileEntity = MetaTileEntityUtils.getMetaTileEntityAt(world, pos);
        return metaTileEntity instanceof IEnergyContainerStorage ||
                (metaTileEntity instanceof IQuantumStorage<?> &&
                        ((IQuantumStorage<?>) metaTileEntity).getTypeValue() instanceof IEnergyContainer);
    }

    @Override
    public void onAttachment(@Nonnull CoverableView coverableView, @Nonnull EnumFacing side, @Nullable EntityPlayer player, @Nonnull ItemStack itemStack) {
        final BlockPos pos = coverableView.getPos();
        final World world = coverableView.getWorld();
        this.findEnergyContainer(MetaTileEntityUtils.getMetaTileEntityAt(world, pos));
    }

    @Override
    public void renderCover(@Nonnull CCRenderState ccRenderState, @Nonnull Matrix4 matrix4, @Nonnull IVertexOperation[] iVertexOperations, @Nonnull Cuboid6 cuboid6, @Nonnull BlockRenderLayer blockRenderLayer) {
        TJATextures.CREATIVE_ENERGY_COVER_OVERLAY.renderSided(this.getAttachedSide(), ccRenderState, matrix4, iVertexOperations);
    }

    @Override
    public void update() {
        if (this.energyContainer == null) {
            this.findEnergyContainer(MetaTileEntityUtils.getMetaTileEntityAt(this.getWorld(), this.getPos()));
        } else this.energyContainer.addEnergy(Long.MAX_VALUE);
    }

    private void findEnergyContainer(MetaTileEntity metaTileEntity) {
        if (metaTileEntity instanceof IEnergyContainerStorage) {
            this.energyContainer = ((IEnergyContainerStorage) metaTileEntity).getEnergyContainer();
        } else if (metaTileEntity instanceof IQuantumStorage<?>) {
            this.energyContainer = (IEnergyContainer) ((IQuantumStorage<?>) metaTileEntity).getTypeValue();
        }
    }
}
