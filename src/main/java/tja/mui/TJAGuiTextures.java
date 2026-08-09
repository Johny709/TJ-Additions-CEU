package tja.mui;

import com.cleanroommc.modularui.drawable.UITexture;
import net.minecraft.util.ResourceLocation;
import tja.TJA;

public final class TJAGuiTextures {

    public static final UITexture SLOW_DOWN = UITexture.fullImage(resource("textures/gui/widgets/slot_down"));

    public static final UITexture TOGGLE_BLOCKING_MODE = UITexture.fullImage(resource("textures/gui/widgets/block_mode_toggle"));
    public static final UITexture TOGGLE_INTERFACE_TERMINAL = UITexture.fullImage(resource("textures/gui/widgets/interface_terminal_toggle"));
    public static final UITexture TOGGLE_SEND_FLUID = UITexture.fullImage(resource( "textures/gui/widgets/send_fluid_toggle"));
    public static final UITexture TOGGLE_SPLITTING_ITEMS_FLUIDS = UITexture.fullImage(resource("textures/gui/widgets/splitting_items_fluids_toggle"));
    public static final UITexture CYCLE_LOCK_CRAFTING = UITexture.fullImage(resource("textures/gui/widgets/lock_crafting_cycle"));
    public static final UITexture CYCLE_BLOCKING_MODE_EX = UITexture.fullImage(resource("textures/gui/widgets/blocking_mode_ex_cycle"));
    public static final UITexture AE2_MULTIPLY2_BUTTON = UITexture.fullImage(resource("textures/gui/widgets/ae2_multiply2_button"));
    public static final UITexture AE2_DIVIDE2_BUTTON = UITexture.fullImage(resource("textures/gui/widgets/ae2_divide2_button"));
    public static final UITexture AE2_MULTIPLY3_BUTTON = UITexture.fullImage(resource("textures/gui/widgets/ae2_multiply3_button"));
    public static final UITexture AE2_DIVIDE3_BUTTON = UITexture.fullImage(resource("textures/gui/widgets/ae2_divide3_button"));
    public static final UITexture INTERFACE_SETTINGS_BASE_EDGE_RIGHT = UITexture.fullImage(resource("textures/gui/widgets/interface_settings_base_edge_right"));

    public static final UITexture UPGRADE_OVERLAY = UITexture.fullImage(resource("textures/gui/overlay/me.upgrade_overlay"));
    public static final UITexture PATTERN_OVERLAY = UITexture.fullImage(resource("textures/gui/overlay/me.pattern_overlay"));

    private static ResourceLocation resource(String path) {
        return new ResourceLocation(TJA.MOD_ID, path);
    }
}
