package tja.mixin.ae2;

import appeng.api.parts.IPart;
import appeng.tile.networking.TileCableBus;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.SidedPosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tja.TJA;

@Mixin(value = TileCableBus.class, remap = false)
public abstract class MixinTileCableBus implements IGuiHolder<SidedPosGuiData> {

    @Shadow
    public abstract IPart getPart(EnumFacing side);

    @Override
    @SideOnly(Side.CLIENT)
    public ModularScreen createScreen(SidedPosGuiData data, ModularPanel mainPanel) {
        final IPart part = this.getPart(data.getSide());
        return part instanceof IGuiHolder<?> ? ((IGuiHolder<SidedPosGuiData>) part).createScreen(data, mainPanel) :
                new ModularScreen(TJA.MOD_ID, mainPanel);
    }

    @Override
    public ModularPanel buildUI(SidedPosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        final IPart part = this.getPart(data.getSide());
        return part instanceof IGuiHolder<?> ? ((IGuiHolder<SidedPosGuiData>) part).buildUI(data, syncManager, settings) : null;
    }
}
