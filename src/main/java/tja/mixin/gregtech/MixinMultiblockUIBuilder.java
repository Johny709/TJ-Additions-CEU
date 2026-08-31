package tja.mixin.gregtech;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.network.NetworkUtils;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.metatileentity.multiblock.ui.MultiblockUIBuilder;
import gregtech.api.metatileentity.multiblock.ui.Operation;
import gregtech.api.mui.GTByteBufAdapters;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.chance.output.impl.ChancedFluidOutput;
import gregtech.api.recipes.chance.output.impl.ChancedItemOutput;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.util.GTUtility;
import gregtech.api.util.KeyUtil;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import tja.util.Counter;
import tja.util.TJAItemUtils;
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

    @Shadow
    protected abstract void addChancedItemOutputLine(@Nonnull ChancedItemOutput output, int count, int chance, int recipeLength);

    @Shadow
    protected abstract void addChancedFluidOutputLine(ChancedFluidOutput output, int count, int chance, int recipeLength);

    @Inject(method = "addRecipeOutputLine(Lgregtech/api/capability/impl/AbstractRecipeLogic;I)Lgregtech/api/metatileentity/multiblock/ui/MultiblockUIBuilder;",
            at = @At(value = "INVOKE", target = "Ljava/util/ArrayList;<init>()V", ordinal = 0),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            cancellable = true)
    private void injectAddRecipeOutputLine(AbstractRecipeLogic arl, int maxLines, CallbackInfoReturnable<MultiblockUIBuilder> cir,
                                           Recipe recipe, RecipeMap<?> map, Recipe trimmed, int p, long eut, long maxVoltage, int maxProgress) {
        List<GTRecipeInput> itemInputs = new ArrayList<>();
        List<GTRecipeInput> fluidInputs = new ArrayList<>();
        List<ItemStack> itemOutputs = new ArrayList<>();
        List<FluidStack> fluidOutputs = new ArrayList<>();
        List<ChancedItemOutput> chancedItemOutputs = new ArrayList<>();
        List<ChancedFluidOutput> chancedFluidOutputs = new ArrayList<>();

        if (this.isServer()) {
            // recipe searching has to be done server only
            itemInputs.addAll(trimmed.getInputs());
            fluidInputs.addAll(trimmed.getFluidInputs());
            itemOutputs.addAll(trimmed.getOutputs());
            fluidOutputs.addAll(trimmed.getFluidOutputs());
            chancedItemOutputs.addAll(trimmed.getChancedOutputs().getChancedEntries());
            chancedFluidOutputs.addAll(trimmed.getChancedFluidOutputs().getChancedEntries());
        }

        itemInputs = this.getSyncer().syncCollection(itemInputs,
                GTByteBufAdapters.makeAdapter(buffer -> GTRecipeInput.readFromNBT(buffer.readCompoundTag()),
                        (buffer, value) -> buffer.writeCompoundTag(GTRecipeInput.writeToNBT(value))));
        fluidInputs = this.getSyncer().syncCollection(fluidInputs,
                GTByteBufAdapters.makeAdapter(buffer -> GTRecipeInput.readFromNBT(buffer.readCompoundTag()),
                        (buffer, value) -> buffer.writeCompoundTag(GTRecipeInput.writeToNBT(value))));
        itemOutputs = this.getSyncer().syncCollection(itemOutputs,
                GTByteBufAdapters.makeAdapter(NetworkUtils::readItemStack, NetworkUtils::writeItemStack));
        fluidOutputs = this.getSyncer().syncCollection(fluidOutputs,
                GTByteBufAdapters.makeAdapter(NetworkUtils::readFluidStack, NetworkUtils::writeFluidStack));
        chancedItemOutputs = this.getSyncer().syncCollection(chancedItemOutputs, GTByteBufAdapters.CHANCED_ITEM_OUTPUT);
        chancedFluidOutputs = this.getSyncer().syncCollection(chancedFluidOutputs, GTByteBufAdapters.CHANCED_FLUID_OUTPUT);

        // map identical items and fluids together to get total amount.
        final Object2ObjectMap<ItemStack[], Counter> itemInputMap = new Object2ObjectLinkedOpenCustomHashMap<>(Strategies.INGREDIENT_STRATEGY);
        final Object2ObjectMap<FluidStack, Counter> fluidInputMap = new Object2ObjectLinkedOpenHashMap<>();
        final Object2ObjectMap<ItemStack, Counter> itemOutputMap = new Object2ObjectLinkedOpenCustomHashMap<>(Strategies.ITEMSTACK_STRATEGY);
        final Object2ObjectMap<FluidStack, Counter> fluidOutputMap = new Object2ObjectLinkedOpenHashMap<>();
        final Object2ObjectMap<ChancedItemOutput, Counter> chancedItemOutputMap =
                new Object2ObjectLinkedOpenCustomHashMap<>(Strategies.CHANCED_ITEM_STRATEGY);
        final Object2ObjectMap<ChancedFluidOutput, Counter> chancedFluidOutputMap =
                new Object2ObjectLinkedOpenCustomHashMap<>(Strategies.CHANCED_FLUID_STRATEGY);

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
        for (ItemStack itemOutput : itemOutputs) {
            if (itemOutput == null) continue;
            itemOutputMap.computeIfAbsent(itemOutput, k -> new Counter(0))
                    .increment(itemOutput.getCount());
        }
        for (FluidStack fluidOutput : fluidOutputs) {
            if (fluidOutput == null) continue;
            fluidOutputMap.computeIfAbsent(fluidOutput, k -> new Counter(0))
                    .increment(fluidOutput.amount);
        }
        for (ChancedItemOutput itemOutput : chancedItemOutputs) {
            if (itemOutput == null) continue;
            chancedItemOutputMap.computeIfAbsent(itemOutput, k -> new Counter(0))
                    .increment(itemOutput.getIngredient().getCount());
        }
        for (ChancedFluidOutput fluidOutput : chancedFluidOutputs) {
            if (fluidOutput == null) continue;
            chancedFluidOutputMap.computeIfAbsent(fluidOutput, k -> new Counter(0))
                    .increment(fluidOutput.getIngredient().amount);
        }

        // consuming line
        this.addKey(KeyUtil.lang(TextFormatting.GRAY, "tja.machine.universal.consuming"), Operation::addLine);

        for (Object2ObjectMap.Entry<ItemStack[], Counter> entry : itemInputMap.object2ObjectEntrySet()) {
            this.addItemOutputLine(TJAItemUtils.getItemStackOreDict(entry.getKey(), !this.isServer()), entry.getValue().getValue() * p, maxProgress);
        }
        for (Object2ObjectMap.Entry<FluidStack, Counter> entry : fluidInputMap.object2ObjectEntrySet()) {
            this.addFluidOutputLine(entry.getKey(), entry.getValue().getValue() * p, maxProgress);
        }

        // producing line
        this.addEmptyLine();
        this.addKey(KeyUtil.lang(TextFormatting.GRAY, "gregtech.gui.multiblock.recipe_producing"), Operation::addLine);

        for (Object2ObjectMap.Entry<ItemStack, Counter> entry : itemOutputMap.object2ObjectEntrySet()) {
            this.addItemOutputLine(entry.getKey(), entry.getValue().getValue() * p, maxProgress);
        }
        for (Object2ObjectMap.Entry<FluidStack, Counter> entry : fluidOutputMap.object2ObjectEntrySet()) {
            this.addFluidOutputLine(entry.getKey(), entry.getValue().getValue() * p, maxProgress);
        }

        final int recipeTier = GTUtility.getTierByVoltage(eut);
        final int machineTier = GTUtility.getOCTierByVoltage(maxVoltage);

        for (Object2ObjectMap.Entry<ChancedItemOutput, Counter> entry : chancedItemOutputMap.object2ObjectEntrySet()) {
            final int chance = this.getSyncer().syncInt(() -> map.chanceFunction.getBoostedChance(entry.getKey(), recipeTier, machineTier));
            this.addChancedItemOutputLine(entry.getKey(), (int) entry.getValue().getValue() * p, chance, maxProgress);
        }
        for (Object2ObjectMap.Entry<ChancedFluidOutput, Counter> entry : chancedFluidOutputMap.object2ObjectEntrySet()) {
            final int chance = this.getSyncer().syncInt(() -> map.chanceFunction.getBoostedChance(entry.getKey(), recipeTier, machineTier));
            this.addChancedFluidOutputLine(entry.getKey(), (int) entry.getValue().getValue() * p, chance, maxLines);
        }

        cir.setReturnValue((MultiblockUIBuilder) (Object) this);
    }
}
