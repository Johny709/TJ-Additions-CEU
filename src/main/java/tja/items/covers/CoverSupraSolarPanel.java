package tja.items.covers;

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
import net.minecraft.init.Blocks;
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

public class CoverSupraSolarPanel extends CoverBase implements ITickable {

    private final BlockPos.MutableBlockPos skyPos = new BlockPos.MutableBlockPos();
    private IEnergyContainer energyContainer;
    private boolean canSeeSky;
    private long amps;

    public CoverSupraSolarPanel(@Nonnull CoverDefinition definition, @Nonnull CoverableView coverableView, @Nonnull EnumFacing attachedSide) {
        super(definition, coverableView, attachedSide);
    }

    @Override
    public boolean canAttach(@Nonnull CoverableView coverableView, @Nonnull EnumFacing enumFacing) {
        final BlockPos pos = coverableView.getPos();
        final World world = coverableView.getWorld();
        final MetaTileEntity metaTileEntity = MetaTileEntityUtils.getMetaTileEntityAt(world, pos);
        return enumFacing == EnumFacing.UP && (metaTileEntity instanceof IEnergyContainerStorage ||
                (metaTileEntity instanceof IQuantumStorage<?> &&
                ((IQuantumStorage<?>) metaTileEntity).getTypeValue() instanceof IEnergyContainer));
    }

    @Override
    public void onAttachment(@Nonnull CoverableView coverableView, @Nonnull EnumFacing side, @Nullable EntityPlayer player, @Nonnull ItemStack itemStack) {
        final BlockPos pos = coverableView.getPos();
        final World world = coverableView.getWorld();
        this.findEnergyContainer(MetaTileEntityUtils.getMetaTileEntityAt(world, pos));
    }

    @Override
    public void renderCover(@Nonnull CCRenderState ccRenderState, @Nonnull Matrix4 matrix4, @Nonnull IVertexOperation[] iVertexOperations, @Nonnull Cuboid6 cuboid6, @Nonnull BlockRenderLayer blockRenderLayer) {
        TJATextures.SUPRA_SOLAR_PANEL_OVERLAY.renderSided(this.getAttachedSide(), ccRenderState, matrix4, iVertexOperations);
    }

    @Override
    public void update() {
        if (this.canSeeSky) {
            if (this.energyContainer == null) {
                this.findEnergyContainer(MetaTileEntityUtils.getMetaTileEntityAt(this.getWorld(), this.getPos()));
            } else this.energyContainer.acceptEnergyFromNetwork(this.getAttachedSide(), Integer.MAX_VALUE, this.amps);
        }
        if (this.getOffsetTimer() % 50 == 0) {
            this.canSeeSky = true;
            final int x = this.getPos().getX();
            final int y = this.getPos().getY();
            final int z = this.getPos().getZ();
            final World world = this.getWorld();
            for (int i = 1; i < this.getWorld().getHeight(); i++) {
                this.skyPos.setPos(x, y + i, z);
                if (world.getBlockState(this.skyPos).getBlock() != Blocks.AIR) {
                    this.canSeeSky = false;
                    break;
                }
            }
        }
    }

    private void findEnergyContainer(MetaTileEntity metaTileEntity) {
        if (metaTileEntity instanceof IEnergyContainerStorage) {
            this.energyContainer = ((IEnergyContainerStorage) metaTileEntity).getEnergyContainer();
            this.amps = Math.max(this.energyContainer.getInputAmperage(), this.energyContainer.getOutputAmperage());
        } else if (metaTileEntity instanceof IQuantumStorage<?>) {
            this.energyContainer = (IEnergyContainer) ((IQuantumStorage<?>) metaTileEntity).getTypeValue();
            this.amps = Math.max(this.energyContainer.getInputAmperage(), this.energyContainer.getOutputAmperage());
        }
    }
}
