package tja.mixin.gregtech;

import gregtech.common.items.behaviors.DataItemBehavior;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nonnull;
import java.util.List;

@Mixin(value = DataItemBehavior.class, remap = false)
public abstract class MixinDataItemBehavior {

    @Inject(method = "addInformation", at = @At("HEAD"))
    private void injectAddInformation(@Nonnull ItemStack itemStack, List<String> lines, CallbackInfo ci) {
        final NBTTagCompound compound = itemStack.getTagCompound();
        if (compound != null) {
            final NBTTagCompound fluidCompound = compound.getCompoundTag("fluidInChunk");
            final boolean hasFluid = fluidCompound.hasKey("name");
            lines.add(I18n.format(hasFluid ? fluidCompound.getString("name") : "metaitem.fluid_cell.universal.empty"));
            if (hasFluid) {
                lines.add(I18n.format("tja.machine.fluid_sampler.yield", fluidCompound.getInteger("yield")));
                lines.add(I18n.format("gregtech.jei.fluid.depleted_rate", fluidCompound.getInteger("depletedYield")));
            }
        }
    }
}
