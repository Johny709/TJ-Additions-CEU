package tja.blocks;

import com.fulltrix.gcyl.materials.GCYLMaterials;
import gregtech.api.block.VariantBlock;
import gregtech.api.unification.material.Materials;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tja.TJAValues;

import javax.annotation.Nonnull;

public class BlockTieredGlass extends VariantBlock<BlockTieredGlass.CasingType> {

    public BlockTieredGlass() {
        super(Material.IRON);
        this.setRegistryName("tiered_glass");
        this.setTranslationKey("tiered_glass");
        this.setHardness(5.0F);
        this.setResistance(10.0F);
        this.setSoundType(SoundType.GLASS);
        this.setHarvestLevel("wrench", 2);
        this.setDefaultState(this.getState(CasingType.ULV));
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Nonnull
    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public boolean isOpaqueCube(@Nonnull IBlockState state) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(@Nonnull IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, @Nonnull EnumFacing side) {
        IBlockState iblockstate = blockAccess.getBlockState(pos.offset(side));
        Block block = iblockstate.getBlock();

        return block != this && super.shouldSideBeRendered(blockState, blockAccess, pos, side);
    }

    public enum CasingType implements IStringSerializable {
        ULV("ulv", null),
        LV("lv", Materials.BorosilicateGlass),
        MV("mv", Materials.Nickel),
        HV("hv", Materials.Chrome),
        EV("ev", Materials.Tungsten),
        IV("iv", Materials.Iridium),
        LUV("luv", Materials.Osmium),
        ZPM("zpm", Materials.Duranium),
        UV("uv", Materials.Tritanium),
        UHV("uhv", Materials.Seaborgium),
        UEV("uev", Materials.Bohrium),
        UIV("uiv", getGregicalityMaterial(12)),
        UXV("uxv", getGregicalityMaterial(13)),
        OPV("opv", Materials.Neutronium),
        MAX("max", getGregicalityMaterial(14));

        private final String name;
        private final gregtech.api.unification.material.Material material;

        CasingType(String name, gregtech.api.unification.material.Material material) {
            this.name = name;
            this.material = material;
        }

        @Nonnull
        @Override
        public String getName() {
            return this.name;
        }

        public gregtech.api.unification.material.Material getMaterial() {
            return this.material;
        }

        private static gregtech.api.unification.material.Material getGregicalityMaterial(int ordinal) {
            if (!TJAValues.isModLoaded(TJAValues.GCYL_MOD_ID))
                return null;
            switch (ordinal) {
                case 11 : return GCYLMaterials.HeavyQuarkDegenerateMatter;
                case 12: return GCYLMaterials.QCDMatter;
                default: return GCYLMaterials.CosmicNeutronium;
            }
        }
    }
}
