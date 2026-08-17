package tja.textures;

import net.minecraft.util.EnumFacing;

public enum TJAOverlayFace {
    FRONT, BACK, TOP, BOTTOM, SIDE;

    public static TJAOverlayFace bySide(EnumFacing side, EnumFacing frontFacing) {
        if (side == frontFacing) {
            return FRONT;
        } else if (side.getOpposite() == frontFacing) {
            return BACK;
        } else if (side == EnumFacing.UP) {
            return TOP;
        } else if (side == EnumFacing.DOWN) {
            return BOTTOM;
        } else return SIDE;
    }
}
