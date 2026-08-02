package tja.blocks;

import gregtech.api.block.VariantBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;

public class BlockBatteryCell extends VariantBlock<BlockBatteryCell.CasingType> {

    public BlockBatteryCell() {
        super(Material.IRON);
        this.setRegistryName("cell_battery");
        this.setTranslationKey("cell_battery");
        this.setHardness(5.0F);
        this.setResistance(10.0F);
        this.setDefaultState(this.getState(CasingType.CELL_LV));
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    public enum CasingType implements IStringSerializable {
        CELL_LV("lv"),
        CELL_MV("mv"),
        CELL_HV("hv"),
        CELL_EV("ev"),
        CELL_IV("iv"),
        CELL_LUV("luv"),
        CELL_ZPM("zpm"),
        CELL_UV("uv"),
        CELL_UHV("uhv"),
        CELL_UEV("uev"),
        CELL_UIV("uiv"),
        CELL_UXV("uxv"),
        CELL_OPV("opv"),
        CELL_MAX("max");

        private final String name;

        CasingType(String name) {
            this.name = name;
        }

        @Nonnull
        @Override
        public String getName() {
            return this.name;
        }
    }
}
