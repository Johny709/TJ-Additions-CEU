package tja.machines.controllers;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.RecipeMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import tja.capability.TJADataCodes;

public abstract class TJExtendableMultiblockController extends RecipeMapMultiblockController {

    protected int slices = 1;

    public TJExtendableMultiblockController(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap) {
        super(metaTileEntityId, recipeMap);
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        int slices = this.slices;
        slices = MathHelper.clamp(slices + (playerIn.isSneaking() ? 1 : -1), 1, this.getMaxSlices());
        if (slices != this.slices) {
            if (!this.getWorld().isRemote) {
                this.slices = slices;
                this.resetStructure();
                this.writeCustomData(TJADataCodes.MULTIBLOCK_SLICES, buffer -> buffer.writeInt(this.slices));
            } else playerIn.sendMessage(new TextComponentTranslation((playerIn.isSneaking() ? "tja.multiblock.parallel.layer.increment" : "tja.multiblock.parallel.layer.decrement"), this.slices));
        } else if (this.getWorld().isRemote)
            playerIn.sendMessage(new TextComponentTranslation((playerIn.isSneaking() ? "tja.multiblock.parallel.layer.increment.fail" : "tja.multiblock.parallel.layer.decrement.fail"), this.slices));
        return true;
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == TJADataCodes.MULTIBLOCK_SLICES)
            this.slices = buf.readInt();
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(this.slices);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.slices = buf.readInt();
        this.resetStructure();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("slices", this.slices);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.slices = data.getInteger("slices");
        this.resetStructure();
    }

    @Override
    public void reinitializeStructurePattern() {
        this.slices = 1;
        super.reinitializeStructurePattern();
    }

    private void resetStructure() {
        if (this.isStructureFormed())
            this.invalidateStructure();
        this.structurePattern = this.createStructurePattern();
    }

    public int getSlices() {
        return this.slices;
    }

    public int getMaxSlices() {
        return 64;
    }
}
