package tja.machines.controllers;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;

import static tja.capability.TJADataCodes.PARALLEL_LAYER;

public abstract class ExtendableMultiblockController extends TJAMultiblockControllerBase {

    protected int parallelLayer = 1;

    public ExtendableMultiblockController(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (!this.getWorld().isRemote) {
            final int lastParallelLayer = this.parallelLayer;
            this.parallelLayer = MathHelper.clamp(playerIn.isSneaking() ? this.parallelLayer - 1 : this.parallelLayer + 1, 1, this.getMaxParallel());
            if (this.parallelLayer != lastParallelLayer) {
                playerIn.sendMessage(new TextComponentTranslation(playerIn.isSneaking() ? "tja.multiblock.parallel.layer.decrement.success" : "tja.multiblock.parallel.layer.increment.success", this.parallelLayer));
            } else playerIn.sendMessage(new TextComponentTranslation(playerIn.isSneaking() ? "tja.multiblock.parallel.layer.decrement.fail" : "tja.multiblock.parallel.layer.increment.fail", this.parallelLayer));
            this.resetStructure();
            this.writeCustomData(PARALLEL_LAYER, buf -> buf.writeInt(this.parallelLayer));
            this.markDirty();
        }
        return true;
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == PARALLEL_LAYER) {
            this.parallelLayer = buf.readInt();
            this.structurePattern = this.createStructurePattern();
            this.scheduleRenderUpdate();
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(this.parallelLayer);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.parallelLayer = buf.readInt();
        this.structurePattern = this.createStructurePattern();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        final NBTTagCompound tagCompound = super.writeToNBT(data);
        tagCompound.setInteger("Parallel", this.parallelLayer);
        return tagCompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.parallelLayer = data.getInteger("Parallel");
        if (data.hasKey("Parallel"))
            this.structurePattern = createStructurePattern();
    }

    @Override
    public void reinitializeStructurePattern() {
        this.parallelLayer = 1;
        super.reinitializeStructurePattern();
    }

    public int getMaxParallel() {
        return 1;
    }

    private void resetStructure() {
        if (this.isStructureFormed())
            this.invalidateStructure();
        this.structurePattern = this.createStructurePattern();
    }
}
