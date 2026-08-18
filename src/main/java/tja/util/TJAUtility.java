package tja.util;

import appeng.core.Api;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandler;

import java.util.function.LongUnaryOperator;
import java.util.function.Predicate;

public final class TJAUtility {

    private TJAUtility() {}

    /**
     * EU gets rounded down by tier minimum EU.
     * e.g. 32 = tier 1 (LV), 128 = tier 2 (MV). 64 or 120 is still tier 1 (LV).
     * @param voltage EU
     * @return tier
     */
    public static byte getTierByVoltage(long voltage) {
        long eut = 8;
        for (byte i = 0; eut > 0; i++) {
            if ((eut *= 4) > voltage)
                return i;
        }
        return 0;
    }

    /**
     * EU gets rounded up to next tier EU.
     * e.g. 32 = tier 1 (LV), 128 = tier 2 (MV). 64 or 120 becomes tier 2 (MV).
     * @param voltage EU
     * @return tier
     */
    public static byte getTierFromVoltage(long voltage) {
        long eut = 2;
        for (byte i = 0; eut > 0; i++) {
            if ((eut *= 4) >= voltage)
                return i;
        }
        return 0;
    }

    @SafeVarargs
    public static <T> void addToArray(T[] array, T... elements) {
        for (int i = 0; i < elements.length; i++) {
            array[i] = elements[i];
        }
    }

    public static boolean changeInterfacePatternAmount(IItemHandler patternSlots, LongUnaryOperator multiplier, Runnable callback) {
        for (int i = 0; i < patternSlots.getSlots(); i++) {
            final ItemStack stack = patternSlots.getStackInSlot(i);
            final NBTTagCompound compound = stack.getTagCompound();
            if (stack.isEmpty() || compound == null) continue;
            final ResourceLocation resourcelocation = Item.REGISTRY.getNameForObject(stack.getItem());
            final String id = resourcelocation != null ? resourcelocation.toString() : "minecraft:air";
            final NBTTagList inputList = compound.getTagList(id.equals("ae2fc:dense_encoded_pattern") ? "Inputs" : "in", 10);
            final NBTTagList outputList = compound.getTagList(id.equals("ae2fc:dense_encoded_pattern") ? "Outputs" : "out", 10);
            final NBTTagList newInputList = new NBTTagList(), newOutputList = new NBTTagList();
            final Predicate<Boolean> setPatternInputs = simulate -> {
                for (int j = 0; j < inputList.tagCount(); j++) {
                    final NBTTagCompound patternCompound = inputList.getCompoundTagAt(j);
                    final long amount = patternCompound.hasKey("Cnt") ? patternCompound.getLong("Cnt") : patternCompound.getInteger("Count");
                    final long newAmount = multiplier.applyAsLong(amount);
                    if (patternCompound.isEmpty()) {
                        if (!simulate)
                            newInputList.appendTag(patternCompound);
                        continue;
                    }
                    if (newAmount > 0 && newAmount <= Integer.MAX_VALUE) {
                        if (!simulate) {
                            if (id.equals("ae2fc:dense_encoded_pattern")) {
                                patternCompound.setLong("Cnt", newAmount);
                            } else patternCompound.setInteger("Count", (int) newAmount);
                            newInputList.appendTag(patternCompound);
                        }
                    } else return false;
                }
                for (int j = 0; j < outputList.tagCount(); j++) {
                    final NBTTagCompound patternCompound = outputList.getCompoundTagAt(j);
                    final long amount = patternCompound.hasKey("Cnt") ? patternCompound.getLong("Cnt") : patternCompound.getInteger("Count");
                    final long newAmount = multiplier.applyAsLong(amount);
                    if (patternCompound.isEmpty()) {
                        if (!simulate)
                            newOutputList.appendTag(patternCompound);
                        continue;
                    }
                    if (newAmount > 0 && newAmount <= Integer.MAX_VALUE) {
                        if (!simulate) {
                            if (id.equals("ae2fc:dense_encoded_pattern")) {
                                patternCompound.setLong("Cnt", newAmount);
                            } else patternCompound.setInteger("Count", (int) newAmount);
                            newOutputList.appendTag(patternCompound);
                        }
                    } else return false;
                }
                if (!simulate) {
                    compound.setTag("in", newInputList);
                    compound.setTag("out", newOutputList);
                    if (id.equals("ae2fc:dense_encoded_pattern")) {
                        compound.setTag("Inputs", newInputList);
                        compound.setTag("Outputs", newOutputList);
                    }
                }
                return true;
            };
            if (setPatternInputs.test(true))
                setPatternInputs.test(false);
        }
        callback.run();
        return true;
    }

    public static void updatePatterns(IItemHandler patternSlots) {
        final NonNullList<ItemStack> itemStacks = NonNullList.create();
        for (int i = 0; i < patternSlots.getSlots(); i++)
            itemStacks.add(patternSlots.extractItem(i, Integer.MAX_VALUE, false));
        for (int i = 0; i < patternSlots.getSlots(); i++)
            patternSlots.insertItem(i, itemStacks.get(i), false);
    }

    public static void clearPatterns(IItemHandler patternSlots, Runnable callback) {
        for (int i = 0; i < patternSlots.getSlots(); i++) {
            ItemStack pattern = patternSlots.extractItem(i, Integer.MAX_VALUE, false);
            pattern = Api.INSTANCE.definitions().materials().blankPattern().maybeStack(pattern.getCount()).orElse(ItemStack.EMPTY);
            patternSlots.insertItem(i, pattern, false);
        }
        callback.run();
    }
}
