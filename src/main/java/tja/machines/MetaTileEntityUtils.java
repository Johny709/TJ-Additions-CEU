package tja.machines;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public final class MetaTileEntityUtils {

    @Nullable
    public static MetaTileEntity getMetaTileEntityAt(World world, BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof MetaTileEntityHolder) {
            return ((MetaTileEntityHolder) tileEntity).getMetaTileEntity();
        }
        return null;
    }

    @Nullable
    public static IMultiblockAbilityPart<?> getMultiblockAbilityPart(World world, BlockPos pos) {
        MetaTileEntity metaTileEntity = getMetaTileEntityAt(world, pos);
        return metaTileEntity instanceof IMultiblockAbilityPart<?> ? ((IMultiblockAbilityPart<?>) metaTileEntity) : null;
    }
}
