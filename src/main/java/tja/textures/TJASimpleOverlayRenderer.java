package tja.textures;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.texture.TextureUtils;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TJASimpleOverlayRenderer implements TextureUtils.IIconRegister {

    private final String basePath;
    private final String modID;

    @SideOnly(Side.CLIENT)
    private TextureAtlasSprite sprite;

    public TJASimpleOverlayRenderer(String modID, String basePath) {
        this.modID = modID;
        this.basePath = basePath;
        TJATextures.ICON_REGISTERS.add(this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(TextureMap textureMap) {
        this.sprite = textureMap.registerSprite(new ResourceLocation(this.modID, this.basePath));
    }

    @SideOnly(Side.CLIENT)
    public void renderSided(EnumFacing side, Cuboid6 bounds, CCRenderState renderState, IVertexOperation[] pipeline, Matrix4 translation) {
        TJATextures.renderFace(renderState, translation, pipeline, side, bounds, this.sprite);
    }

    @SideOnly(Side.CLIENT)
    public void renderSided(EnumFacing side, CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        this.renderSided(side, Cuboid6.full, renderState, pipeline, translation);
    }
}
