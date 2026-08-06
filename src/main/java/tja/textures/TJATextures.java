package tja.textures;

import codechicken.lib.render.BlockRenderer;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.texture.TextureUtils;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.TransformationList;
import codechicken.lib.vec.uv.IconTransformation;
import codechicken.lib.vec.uv.UVTransformationList;
import gregtech.client.renderer.cclop.UVMirror;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import tja.TJA;

import java.util.ArrayList;
import java.util.List;

public class TJATextures {

    private static final ThreadLocal<BlockRenderer.BlockFace> BLOCK_FACES = ThreadLocal.withInitial(BlockRenderer.BlockFace::new);
    public static final List<TextureUtils.IIconRegister> ICON_REGISTERS = new ArrayList<>();

    public static final TJASimpleOverlayRenderer SUPRA_SOLAR_PANEL_OVERLAY = new TJASimpleOverlayRenderer(TJA.MOD_ID, "blocks/cover/overlay_solar_panel_supra");
    public static final TJASimpleOverlayRenderer CREATIVE_ENERGY_COVER_OVERLAY = new TJASimpleOverlayRenderer(TJA.MOD_ID, "blocks/cover/creative_energy_cover_overlay");
    public static final TJASimpleOverlayRenderer CREATIVE_FLUID_COVER_OVERLAY = new TJASimpleOverlayRenderer(TJA.MOD_ID, "blocks/cover/creative_fluid_cover_overlay");

    @SideOnly(Side.CLIENT)
    public static void register(TextureMap textureMap) {
        TJA.LOGGER.info("Loading TJA meta tile entity texture sprites...");
        for (TextureUtils.IIconRegister iconRegister : ICON_REGISTERS) {
            iconRegister.registerIcons(textureMap);
        }
    }

    @SideOnly(Side.CLIENT)
    public static void renderFace(CCRenderState renderState, Matrix4 translation, IVertexOperation[] ops, EnumFacing face, Cuboid6 bounds, TextureAtlasSprite sprite) {
        BlockRenderer.BlockFace blockFace = BLOCK_FACES.get();
        blockFace.loadCuboidFace(bounds, face.getIndex());
        UVTransformationList uvList = new UVTransformationList(new IconTransformation(sprite));
        if (face.getIndex() == 0) {
            uvList.prepend(new UVMirror(0, 0, bounds.min.z, bounds.max.z));
        }
        renderState.setPipeline(blockFace, 0, blockFace.verts.length,
                ArrayUtils.addAll(ops, new TransformationList(translation), uvList));
        renderState.render();
    }
}
