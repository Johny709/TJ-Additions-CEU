package tja.mixin.gregtech;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.metatileentity.multiblock.ui.MultiblockUIBuilder;
import gregtech.api.metatileentity.multiblock.ui.Operation;
import gregtech.api.mui.GTByteBufAdapters;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.util.KeyUtil;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import tja.util.Counter;
import tja.util.map.Strategies;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Mixin(value = MultiblockUIBuilder.class, remap = false)
public abstract class MixinMultiblockUIBuilder {

    @Shadow
    abstract MultiblockUIBuilder.InternalSyncer getSyncer();

    @Shadow
    protected abstract boolean isServer();

    @Shadow
    protected abstract void addKey(IDrawable key, Function<IDrawable, Operation> function);

    @Shadow
    public abstract MultiblockUIBuilder addEmptyLine();

    @Shadow
    protected abstract void addItemOutputLine(@Nonnull ItemStack stack, long count, int recipeLength);

    @Shadow
    protected abstract void addFluidOutputLine(@Nonnull FluidStack stack, long count, int recipeLength);

    @Inject(method = "addRecipeOutputLine(Lgregtech/api/capability/impl/AbstractRecipeLogic;I)Lgregtech/api/metatileentity/multiblock/ui/MultiblockUIBuilder;",
            at = @At(value = "INVOKE", target = "Ljava/util/ArrayList;<init>()V", ordinal = 0),
            locals = LocalCapture.CAPTURE_FAILSOFT)
    private void injectAddRecipeOutputLine(AbstractRecipeLogic arl, int maxLines, CallbackInfoReturnable<MultiblockUIBuilder> cir,
                                           Recipe recipe, RecipeMap<?> map, Recipe trimmed, int p, long eut, long maxVoltage, int maxProgress) {
        List<GTRecipeInput> itemInputs = new ArrayList<>();
        List<GTRecipeInput> fluidInputs = new ArrayList<>();

        if (this.isServer()) {
            // recipe searching has to be done server only
            itemInputs.addAll(trimmed.getInputs());
            fluidInputs.addAll(trimmed.getFluidInputs());
        }

        itemInputs = this.getSyncer().syncCollection(itemInputs,
                GTByteBufAdapters.makeAdapter(buffer -> GTRecipeInput.readFromNBT(buffer.readCompoundTag()),
                        (buffer, value) -> buffer.writeCompoundTag(GTRecipeInput.writeToNBT(value))));
        fluidInputs = this.getSyncer().syncCollection(fluidInputs,
                GTByteBufAdapters.makeAdapter(buffer -> GTRecipeInput.readFromNBT(buffer.readCompoundTag()),
                        (buffer, value) -> buffer.writeCompoundTag(GTRecipeInput.writeToNBT(value))));

        // map identical items and fluids together to get total amount.
        final Object2ObjectMap<ItemStack[], Counter> itemInputMap = new Object2ObjectLinkedOpenCustomHashMap<>(Strategies.INGREDIENT_STRATEGY);
        final Object2ObjectMap<FluidStack, Counter> fluidInputMap = new Object2ObjectLinkedOpenHashMap<>();
        for (GTRecipeInput itemInput : itemInputs) {
            if (itemInput == null) continue;
            itemInputMap.computeIfAbsent(itemInput.getInputStacks(), k -> new Counter(0))
                    .increment(itemInput.getAmount());
        }
        for (GTRecipeInput fluidInput : fluidInputs) {
            if (fluidInput == null) continue;
            fluidInputMap.computeIfAbsent(fluidInput.getInputFluidStack(), k -> new Counter(0))
                    .increment(fluidInput.getAmount());
        }
        this.addKey(KeyUtil.lang(TextFormatting.GRAY, "tja.machine.universal.consuming"), Operation::addLine);
        for (Object2ObjectMap.Entry<ItemStack[], Counter> entry : itemInputMap.object2ObjectEntrySet()) {
            this.addItemOutputLine(this.tJ_Additions_CEU$getItemStackOreDict(entry.getKey()), entry.getValue().getValue() * p, maxProgress);
        }
        for (Object2ObjectMap.Entry<FluidStack, Counter> entry : fluidInputMap.object2ObjectEntrySet()) {
            this.addFluidOutputLine(entry.getKey(), entry.getValue().getValue() * p, maxProgress);
        }
        this.addEmptyLine();
    }

    @Unique
    private ItemStack tJ_Additions_CEU$getItemStackOreDict(ItemStack[] itemStacks) {
        final long ticks = this.isServer() ? 0 : Minecraft.getMinecraft().world.getTotalWorldTime();
        final int index = (int) Math.min(itemStacks.length - 1, ticks % (itemStacks.length * 20L) / 20);
        return itemStacks[index];
    }
}
