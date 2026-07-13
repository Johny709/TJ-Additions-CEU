package tja.mixin.ae2;

import appeng.api.parts.IPart;
import appeng.tile.networking.TileCableBus;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.SidedPosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = TileCableBus.class, remap = false)
public abstract class MixinTileCableBus implements IGuiHolder<SidedPosGuiData> {

    @Shadow
    public abstract IPart getPart(EnumFacing side);

    @Override
    public ModularPanel buildUI(SidedPosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        final IPart part = this.getPart(data.getSide());
        return part instanceof IGuiHolder<?> ? ((IGuiHolder<SidedPosGuiData>) part).buildUI(data, syncManager, settings) : null;
    }
}
