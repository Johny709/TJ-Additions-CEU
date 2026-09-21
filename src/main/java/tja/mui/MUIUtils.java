package tja.mui;

import appeng.core.Api;
import baubles.api.BaublesApi;
import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.serialization.ByteBufAdapters;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.InteractionSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.scroll.HorizontalScrollData;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import gregtech.api.metatileentity.multiblock.ui.KeyManager;
import gregtech.api.metatileentity.multiblock.ui.Operation;
import gregtech.api.metatileentity.multiblock.ui.UISyncer;
import gregtech.api.mui.drawable.GTObjectDrawable;
import gregtech.api.util.KeyUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.tuple.Pair;
import tja.TJAValues;
import tja.capability.IRecipeInfo;
import tja.integration.ae2.IPrioritySetter;
import tja.integration.ae2.ITIckSetter;
import tja.items.TJAAE2Items;
import tja.items.handlers.FilteredItemStackHandler;
import tja.mui.slot.TJAModularSlot;
import tja.util.Counter;
import tja.util.TJAItemUtils;
import tja.util.TJAUtility;
import tja.util.map.Strategies;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public final class MUIUtils {
    
    public static void addRecipeInputOutputLine(KeyManager key, UISyncer syncer, IRecipeInfo recipeInfo, World world) {
        List<FluidStack> fluidInputs = new ArrayList<>();
        List<ItemStack> itemInputs = new ArrayList<>();
        List<FluidStack> fluidOutputs = new ArrayList<>();
        List<ItemStack> itemOutputs = new ArrayList<>();
        if (!world.isRemote) {
            fluidInputs.addAll(recipeInfo.getFluidInputs());
            itemInputs.addAll(recipeInfo.getItemInputs());
            fluidOutputs.addAll(recipeInfo.getFluidOutputs());
            itemOutputs.addAll(recipeInfo.getItemOutputs());
        }
        fluidInputs = syncer.syncCollection(fluidInputs, ByteBufAdapters.FLUID_STACK);
        itemInputs = syncer.syncCollection(itemInputs, ByteBufAdapters.ITEM_STACK);
        fluidOutputs = syncer.syncCollection(fluidOutputs, ByteBufAdapters.FLUID_STACK);
        itemOutputs = syncer.syncCollection(itemOutputs, ByteBufAdapters.ITEM_STACK);

        final Object2ObjectMap<ItemStack, Counter> itemInputMap = new Object2ObjectLinkedOpenCustomHashMap<>(Strategies.ITEMSTACK_STRATEGY);
        final Object2ObjectMap<ItemStack, Counter> itemOutputMap = new Object2ObjectLinkedOpenCustomHashMap<>(Strategies.ITEMSTACK_STRATEGY);
        final Object2ObjectMap<FluidStack, Counter> fluidInputMap = new Object2ObjectLinkedOpenHashMap<>();
        final Object2ObjectMap<FluidStack, Counter> fluidOutputMap = new Object2ObjectLinkedOpenHashMap<>();
        final int maxProgress = syncer.syncInt(recipeInfo.getMaxProgress());

        if (!fluidInputs.isEmpty() || !itemInputs.isEmpty()) {
            key.add(KeyUtil.lang(TextFormatting.GRAY, "tja.machine.universal.consuming"));

            for (ItemStack stack : itemInputs)
                itemInputMap.computeIfAbsent(stack, item -> new Counter(0))
                        .increment(stack.getCount());
            for (FluidStack fluidStack : fluidInputs)
                fluidInputMap.computeIfAbsent(fluidStack, fluid -> new Counter(0))
                        .increment(fluidStack.amount);
            for (Object2ObjectMap.Entry<ItemStack, Counter> entry : itemInputMap.object2ObjectEntrySet())
                MUIUtils.addItemOutputLine(key, entry.getKey(), entry.getValue().getValue(), maxProgress);
            for (Object2ObjectMap.Entry<FluidStack, Counter> entry : fluidInputMap.object2ObjectEntrySet())
                MUIUtils.addFluidOutputLine(key, entry.getKey(), entry.getValue().getValue(), maxProgress);
        }
        if (!fluidOutputs.isEmpty() || !itemOutputs.isEmpty()) {
            key.add(KeyUtil.lang("")); // new line
            key.add(KeyUtil.lang(TextFormatting.GRAY, "gregtech.gui.multiblock.recipe_producing"));

            for (ItemStack stack : itemOutputs)
                itemOutputMap.computeIfAbsent(stack, item -> new Counter(0))
                        .increment(stack.getCount());
            for (FluidStack fluidStack : fluidOutputs)
                fluidOutputMap.computeIfAbsent(fluidStack, fluid -> new Counter(0))
                        .increment(fluidStack.amount);
            for (Object2ObjectMap.Entry<ItemStack, Counter> entry : itemOutputMap.object2ObjectEntrySet())
                MUIUtils.addItemOutputLine(key, entry.getKey(), entry.getValue().getValue(), maxProgress);
            for (Object2ObjectMap.Entry<FluidStack, Counter> entry : fluidOutputMap.object2ObjectEntrySet())
                MUIUtils.addFluidOutputLine(key, entry.getKey(), entry.getValue().getValue(), maxProgress);
        }
    }

    /**
     * Add an item output of a recipe to the display.
     *
     * @param stack        the {@link ItemStack} to display.
     * @param recipeLength the recipe length, in ticks.
     */
    public static void addItemOutputLine(KeyManager keyManager, @Nonnull ItemStack stack, long count, int recipeLength) {
        IKey name = KeyUtil.string(TextFormatting.AQUA, stack.getDisplayName());
        IKey amount = KeyUtil.number(TextFormatting.GOLD, count);
        IKey rate = KeyUtil.string(TextFormatting.WHITE,
                formateRecipeRate(recipeLength, count));

        keyManager.add(Operation.add(new GTObjectDrawable(stack, count)
                .asIcon()
                .asHoverable()
                .addTooltipLine(formateRecipeData(name, amount, rate))));
    }

    /**
     * Add the fluid outputs of a recipe to the display.
     *
     * @param stack        a {@link FluidStack}s to display.
     * @param recipeLength the recipe length, in ticks.
     */
    public static void addFluidOutputLine(KeyManager keyManager, @Nonnull FluidStack stack, long count, int recipeLength) {
        IKey name = KeyUtil.fluid(TextFormatting.AQUA, stack);
        IKey amount = KeyUtil.number(TextFormatting.GOLD, count);
        IKey rate = KeyUtil.string(TextFormatting.WHITE,
                formateRecipeRate(recipeLength, count));

        keyManager.add(Operation.add(new GTObjectDrawable(stack, count)
                .asIcon()
                .asHoverable()
                .addTooltipLine(formateRecipeData(name, amount, rate))));
    }

    public static String formateRecipeRate(int recipeLength, long amount) {
        float perSecond = ((float) amount / recipeLength) * 20f;

        String rate;
        if (perSecond > 1) {
            rate = "(" + String.format("%,.2f", perSecond).replaceAll("\\.?0+$", "") + "/s)";
        } else {
            rate = "(" + String.format("%,.2f", 1 / (perSecond)).replaceAll("\\.?0+$", "") + "s/ea)";
        }

        return rate;
    }

    public static IKey formateRecipeData(IKey name, IKey amount, IKey rate) {
        return IKey.comp(name, KeyUtil.string(TextFormatting.WHITE, " x "), amount, IKey.SPACE, rate);
    }

    public static ModularPanel createPriorityPanel(PanelSyncManager syncManager, IPanelHandler panelHandler, IPrioritySetter superInterface) {
        syncManager.syncValue("priority_add_1", new InteractionSyncHandler()
                .setOnMousePressed(mouseData -> superInterface.setPriority(String.valueOf((long) superInterface.getPriority() + 1))));
        syncManager.syncValue("priority_add_10", new InteractionSyncHandler()
                .setOnMousePressed(mouseData -> superInterface.setPriority(String.valueOf((long) superInterface.getPriority() + 10))));
        syncManager.syncValue("priority_add_100", new InteractionSyncHandler()
                .setOnMousePressed(mouseData -> superInterface.setPriority(String.valueOf((long) superInterface.getPriority() + 100))));
        syncManager.syncValue("priority_add_1000", new InteractionSyncHandler()
                .setOnMousePressed(mouseData -> superInterface.setPriority(String.valueOf((long) superInterface.getPriority() + 1000))));
        syncManager.syncValue("priority_sub_1", new InteractionSyncHandler()
                .setOnMousePressed(mouseData -> superInterface.setPriority(String.valueOf((long) superInterface.getPriority() - 1))));
        syncManager.syncValue("priority_sub_10", new InteractionSyncHandler()
                .setOnMousePressed(mouseData -> superInterface.setPriority(String.valueOf((long) superInterface.getPriority() - 10))));
        syncManager.syncValue("priority_sub_100", new InteractionSyncHandler()
                .setOnMousePressed(mouseData -> superInterface.setPriority(String.valueOf((long) superInterface.getPriority() - 100))));
        syncManager.syncValue("priority_sub_1000", new InteractionSyncHandler()
                .setOnMousePressed(mouseData -> superInterface.setPriority(String.valueOf((long) superInterface.getPriority() - 1000))));

        return ModularPanel.defaultPanel("me.interface.priority.panel", 162, 100)
                .child(new TextWidget<>(IKey.lang("gui.appliedenergistics2.Priority"))
                        .pos(7, 7))
                .child(new TextFieldWidget()
                        .pos(7, 46)
                        .size(148, 18)
                        .setMaxLength(10)
                        .autoUpdateOnChange(true)
                        .setValidator(MUIUtils::numberValidator)
                        .setPattern(Pattern.compile("\\*?[0-9_]*\\*?"))
                        .value(new StringSyncValue(() -> String.valueOf(superInterface.getPriority()), superInterface::setPriority)))
                .child(new ButtonWidget<>()
                        .pos(140, 4)
                        .size(12)
                        .overlay(GuiTextures.CLOSE)
                        .background(GuiTextures.MC_BUTTON)
                        .hoverBackground(GuiTextures.MC_BUTTON_HOVERED)
                        .onMousePressed(mouseButton -> {
                            panelHandler.closePanel();
                            return true;
                        }))
                .child(new ButtonWidget<>()
                        .pos(7, 20)
                        .size(25, 20)
                        .overlay(IKey.str("+1"))
                        .background(GuiTextures.MC_BUTTON)
                        .hoverBackground(GuiTextures.MC_BUTTON_HOVERED)
                        .syncHandler("priority_add_1"))
                .child(new ButtonWidget<>()
                        .pos(37, 20)
                        .size(30, 20)
                        .overlay(IKey.str("+10"))
                        .background(GuiTextures.MC_BUTTON)
                        .hoverBackground(GuiTextures.MC_BUTTON_HOVERED)
                        .syncHandler("priority_add_10"))
                .child(new ButtonWidget<>()
                        .pos(72, 20)
                        .size(35, 20)
                        .overlay(IKey.str("+100"))
                        .background(GuiTextures.MC_BUTTON)
                        .hoverBackground(GuiTextures.MC_BUTTON_HOVERED)
                        .syncHandler("priority_add_100"))
                .child(new ButtonWidget<>()
                        .pos(112, 20)
                        .size(40, 20)
                        .overlay(IKey.str("+1000"))
                        .background(GuiTextures.MC_BUTTON)
                        .hoverBackground(GuiTextures.MC_BUTTON_HOVERED)
                        .syncHandler("priority_add_1000"))
                .child(new ButtonWidget<>()
                        .pos(7, 70)
                        .size(25, 20)
                        .overlay(IKey.str("-1"))
                        .background(GuiTextures.MC_BUTTON)
                        .hoverBackground(GuiTextures.MC_BUTTON_HOVERED)
                        .syncHandler("priority_sub_1"))
                .child(new ButtonWidget<>()
                        .pos(37, 70)
                        .size(30, 20)
                        .overlay(IKey.str("-10"))
                        .background(GuiTextures.MC_BUTTON)
                        .hoverBackground(GuiTextures.MC_BUTTON_HOVERED)
                        .syncHandler("priority_sub_10"))
                .child(new ButtonWidget<>()
                        .pos(72, 70)
                        .size(35, 20)
                        .overlay(IKey.str("-100"))
                        .background(GuiTextures.MC_BUTTON)
                        .hoverBackground(GuiTextures.MC_BUTTON_HOVERED)
                        .syncHandler("priority_sub_100"))
                .child(new ButtonWidget<>()
                        .pos(112, 70)
                        .size(40, 20)
                        .overlay(IKey.str("-1000"))
                        .background(GuiTextures.MC_BUTTON)
                        .hoverBackground(GuiTextures.MC_BUTTON_HOVERED)
                        .syncHandler("priority_sub_1000"));
    }

    public static ModularPanel createTicksPanel(PanelSyncManager syncManager, IPanelHandler panelHandler, ITIckSetter superInterface) {
        syncManager.syncValue("tick_add_1", new InteractionSyncHandler()
                .setOnMousePressed(mouseData -> superInterface.setTickTime(String.valueOf((long) superInterface.getTickTime() + 1))));
        syncManager.syncValue("tick_add_10", new InteractionSyncHandler()
                .setOnMousePressed(mouseData -> superInterface.setTickTime(String.valueOf((long) superInterface.getTickTime() + 10))));
        syncManager.syncValue("tick_add_100", new InteractionSyncHandler()
                .setOnMousePressed(mouseData -> superInterface.setTickTime(String.valueOf((long) superInterface.getTickTime() + 100))));
        syncManager.syncValue("tick_add_1000", new InteractionSyncHandler()
                .setOnMousePressed(mouseData -> superInterface.setTickTime(String.valueOf((long) superInterface.getTickTime() + 1000))));
        syncManager.syncValue("tick_sub_1", new InteractionSyncHandler()
                .setOnMousePressed(mouseData -> superInterface.setTickTime(String.valueOf((long) superInterface.getTickTime() - 1))));
        syncManager.syncValue("tick_sub_10", new InteractionSyncHandler()
                .setOnMousePressed(mouseData -> superInterface.setTickTime(String.valueOf((long) superInterface.getTickTime() - 10))));
        syncManager.syncValue("tick_sub_100", new InteractionSyncHandler()
                .setOnMousePressed(mouseData -> superInterface.setTickTime(String.valueOf((long) superInterface.getTickTime() - 100))));
        syncManager.syncValue("tick_sub_1000", new InteractionSyncHandler()
                .setOnMousePressed(mouseData -> superInterface.setTickTime(String.valueOf((long) superInterface.getTickTime() - 1000))));

        return ModularPanel.defaultPanel("me.interface.tick.panel", 162, 100)
                .child(new TextWidget<>(IKey.lang("tja.machine.universal.ticks.operation"))
                        .pos(7, 7))
                .child(new TextFieldWidget()
                        .pos(7, 46)
                        .size(148, 18)
                        .setMaxLength(10)
                        .autoUpdateOnChange(true)
                        .setValidator(MUIUtils::numberValidator)
                        .setPattern(Pattern.compile("\\*?[0-9_]*\\*?"))
                        .value(new StringSyncValue(() -> String.valueOf(superInterface.getTickTime()), superInterface::setTickTime)))
                .child(new ButtonWidget<>()
                        .pos(140, 4)
                        .size(12)
                        .overlay(GuiTextures.CLOSE)
                        .background(GuiTextures.MC_BUTTON)
                        .hoverBackground(GuiTextures.MC_BUTTON_HOVERED)
                        .onMousePressed(mouseButton -> {
                            panelHandler.closePanel();
                            return true;
                        }))
                .child(new ButtonWidget<>()
                        .pos(7, 20)
                        .size(25, 20)
                        .overlay(IKey.str("+1"))
                        .background(GuiTextures.MC_BUTTON)
                        .hoverBackground(GuiTextures.MC_BUTTON_HOVERED)
                        .syncHandler("tick_add_1"))
                .child(new ButtonWidget<>()
                        .pos(37, 20)
                        .size(30, 20)
                        .overlay(IKey.str("+10"))
                        .background(GuiTextures.MC_BUTTON)
                        .hoverBackground(GuiTextures.MC_BUTTON_HOVERED)
                        .syncHandler("tick_add_10"))
                .child(new ButtonWidget<>()
                        .pos(72, 20)
                        .size(35, 20)
                        .overlay(IKey.str("+100"))
                        .background(GuiTextures.MC_BUTTON)
                        .hoverBackground(GuiTextures.MC_BUTTON_HOVERED)
                        .syncHandler("tick_add_100"))
                .child(new ButtonWidget<>()
                        .pos(112, 20)
                        .size(40, 20)
                        .overlay(IKey.str("+1000"))
                        .background(GuiTextures.MC_BUTTON)
                        .hoverBackground(GuiTextures.MC_BUTTON_HOVERED)
                        .syncHandler("tick_add_1000"))
                .child(new ButtonWidget<>()
                        .pos(7, 70)
                        .size(25, 20)
                        .overlay(IKey.str("-1"))
                        .background(GuiTextures.MC_BUTTON)
                        .hoverBackground(GuiTextures.MC_BUTTON_HOVERED)
                        .syncHandler("tick_sub_1"))
                .child(new ButtonWidget<>()
                        .pos(37, 70)
                        .size(30, 20)
                        .overlay(IKey.str("-10"))
                        .background(GuiTextures.MC_BUTTON)
                        .hoverBackground(GuiTextures.MC_BUTTON_HOVERED)
                        .syncHandler("tick_sub_10"))
                .child(new ButtonWidget<>()
                        .pos(72, 70)
                        .size(35, 20)
                        .overlay(IKey.str("-100"))
                        .background(GuiTextures.MC_BUTTON)
                        .hoverBackground(GuiTextures.MC_BUTTON_HOVERED)
                        .syncHandler("tick_sub_100"))
                .child(new ButtonWidget<>()
                        .pos(112, 70)
                        .size(40, 20)
                        .overlay(IKey.str("-1000"))
                        .background(GuiTextures.MC_BUTTON)
                        .hoverBackground(GuiTextures.MC_BUTTON_HOVERED)
                        .syncHandler("tick_sub_1000"));
    }

    public static Widget<?> createPatternMultiToolWidget(PanelSyncManager syncManager, UISettings settings, ItemStack patternMultiTool) {
        final boolean isUpgraded = patternMultiTool.isItemEqual(TJAAE2Items.SUPER_PATTERN_MULTIPLIER.maybeStack(1).orElse(ItemStack.EMPTY));
        final NBTTagCompound compound = TJAItemUtils.getCompoundFromStack(patternMultiTool);
        final NBTTagCompound invTag = compound.getCompoundTag("inv");
        final NBTTagCompound upgradeTag = compound.getCompoundTag("upgrades");
        final FilteredItemStackHandler multiPatternSlots = new FilteredItemStackHandler(isUpgraded ? 72 : 36, 64)
                .setItemStackPredicate((slot, itemStack) -> itemStack.isItemEqual(Api.INSTANCE.definitions().materials().blankPattern().maybeStack(1).orElse(ItemStack.EMPTY)) ||
                        itemStack.isItemEqual(Api.INSTANCE.definitions().items().encodedPattern().maybeStack(1).orElse(ItemStack.EMPTY)) || itemStack.isItemEqual(TJAItemUtils.getItemStackFromName("ae2fc:dense_encoded_pattern")));
        multiPatternSlots.setOnContentsChangedPost((slot, itemStack) -> writePatternMultiToolToNBT(multiPatternSlots, invTag));
        final FilteredItemStackHandler multiUpgradeSlots = new FilteredItemStackHandler(isUpgraded ? 7 : 3, 1)
                .setItemStackPredicate((slot, itemStack) -> itemStack.isItemEqual(Api.INSTANCE.definitions().materials().cardCapacity().maybeStack(1).orElse(ItemStack.EMPTY)));
        multiUpgradeSlots.setOnContentsChangedPost((slot, itemStack) -> writePatternMultiToolToNBT(multiUpgradeSlots, upgradeTag));

        syncManager.syncValue("pattern_multiply_2", new InteractionSyncHandler()
                .setOnMousePressed(mouseData -> TJAUtility.changeInterfacePatternAmount(multiPatternSlots, m -> m * 2,
                        () -> writePatternMultiToolToNBT(multiPatternSlots, invTag))));
        syncManager.syncValue("pattern_multiply_3", new InteractionSyncHandler()
                .setOnMousePressed(mouseData -> TJAUtility.changeInterfacePatternAmount(multiPatternSlots, m -> m * 3,
                        () -> writePatternMultiToolToNBT(multiPatternSlots, invTag))));
        syncManager.syncValue("pattern_add_1", new InteractionSyncHandler()
                .setOnMousePressed(mouseData -> TJAUtility.changeInterfacePatternAmount(multiPatternSlots, m -> m + 1,
                        () -> writePatternMultiToolToNBT(multiPatternSlots, invTag))));
        syncManager.syncValue("pattern_divide_2", new InteractionSyncHandler()
                .setOnMousePressed(mouseData -> TJAUtility.changeInterfacePatternAmount(multiPatternSlots, m -> m / 2,
                        () -> writePatternMultiToolToNBT(multiPatternSlots, invTag))));
        syncManager.syncValue("pattern_divide_3", new InteractionSyncHandler()
                .setOnMousePressed(mouseData -> TJAUtility.changeInterfacePatternAmount(multiPatternSlots, m -> m / 3,
                        () -> writePatternMultiToolToNBT(multiPatternSlots, invTag))));
        syncManager.syncValue("pattern_sub_1", new InteractionSyncHandler()
                .setOnMousePressed(mouseData -> TJAUtility.changeInterfacePatternAmount(multiPatternSlots, m -> m - 1,
                        () -> writePatternMultiToolToNBT(multiPatternSlots, invTag))));
        syncManager.syncValue("pattern_clear", new InteractionSyncHandler()
                .setOnMousePressed(mouseData -> TJAUtility.clearPatterns(multiPatternSlots,
                        () -> writePatternMultiToolToNBT(multiPatternSlots, invTag))));
        final IntSyncValue multiPatternUpgrades = new IntSyncValue(multiUpgradeSlots::getSlotsFilled);
        syncManager.syncValue("multi_pattern_upgrades", multiPatternUpgrades);

        syncManager.registerSlotGroup(new SlotGroup("multi_tool_inventory", 4, 0, true));
        syncManager.registerSlotGroup(new SlotGroup("multi_tool_upgrade_inventory", 1, 1, true));

        final Flow flow = Flow.col();
        settings.getRecipeViewerSettings().addExclusionArea(flow);

        syncManager.addOpenListener(player -> {
            if (!patternMultiTool.isEmpty()) {
                readPatternMultiToolNBT(multiPatternSlots, invTag.getTagList("Items", 10));
                readPatternMultiToolNBT(multiUpgradeSlots, upgradeTag.getTagList("Items", 10));
                if (patternMultiTool.getTagCompound() == null || patternMultiTool.getTagCompound().isEmpty()) {
                    compound.setTag("inv", invTag);
                    compound.setTag("upgrades", upgradeTag);
                    patternMultiTool.setTagCompound(compound);
                }
            }
        });

        return patternMultiTool.isEmpty() ? new Widget<>() :
                flow.rightRel(1.12f)
                        .size(105, 222)
                        .background(GuiTextures.MC_BACKGROUND)
                        .child(new TextWidget<>(IKey.str(patternMultiTool.getDisplayName()))
                                .pos(7, 4))
                        .child(new Grid()
                                .pos(7, 14)
                                .size(72, 166)
                                .scrollable(new HorizontalScrollData() {{
                                    this.setScrollSize(multiPatternSlots.getSlots() * 18 / 9);
                                }})
                                .gridOfSizeHeight(multiPatternSlots.getSlots(), 9, (x, y, i) -> new ItemSlot()
                                        .setEnabledIf(slot -> x <= multiPatternUpgrades.getIntValue())
                                        .background(GuiTextures.SLOT_ITEM, TJAGuiTextures.PATTERN_OVERLAY)
                                        .slot(new TJAModularSlot(multiPatternSlots, i)
                                                .slotGroup("multi_tool_inventory"))))
                        .child(Flow.col()
                                .pos(79, 14)
                                .size(18, multiUpgradeSlots.getSlots() * 18)
                                .children(multiUpgradeSlots.getSlots(), i -> new ItemSlot()
                                        .background(GuiTextures.SLOT_ITEM, TJAGuiTextures.UPGRADE_OVERLAY)
                                        .slot(new TJAModularSlot(multiUpgradeSlots, i)
                                                .slotGroup("multi_tool_upgrade_inventory"))))
                        .child(new ButtonWidget<>()
                                .pos(7, 180)
                                .size(18)
                                .overlay(IKey.str("*2"))
                                .hoverBackground(GuiTextures.MC_BUTTON_HOVERED)
                                .addTooltipLine(IKey.lang("gui.action.MULTIPLY_2.name"))
                                .addTooltipLine(IKey.lang("gui.pattern_term.auto_fill_pattern.MULTIPLY_2.text")
                                        .style(TextFormatting.GRAY))
                                .syncHandler("pattern_multiply_2"))
                        .child(new ButtonWidget<>()
                                .pos(25, 180)
                                .size(18)
                                .overlay(IKey.str("*3"))
                                .hoverBackground(GuiTextures.MC_BUTTON_HOVERED)
                                .addTooltipLine(IKey.lang("gui.action.MULTIPLY_3.name"))
                                .addTooltipLine(IKey.lang("gui.pattern_term.auto_fill_pattern.MULTIPLY_3.text")
                                        .style(TextFormatting.GRAY))
                                .syncHandler("pattern_multiply_3"))
                        .child(new ButtonWidget<>()
                                .pos(43, 180)
                                .size(18)
                                .overlay(IKey.str("+1"))
                                .hoverBackground(GuiTextures.MC_BUTTON_HOVERED)
                                .addTooltipLine(IKey.lang("gui.tooltips.appliedenergistics2.IncreaseByOne"))
                                .addTooltipLine(IKey.lang("gui.tooltips.appliedenergistics2.IncreaseByOneDesc")
                                        .style(TextFormatting.GRAY))
                                .syncHandler("pattern_add_1"))
                        .child(new ButtonWidget<>()
                                .pos(7, 198)
                                .size(18)
                                .overlay(IKey.str("/2"))
                                .hoverBackground(GuiTextures.MC_BUTTON_HOVERED)
                                .addTooltipLine(IKey.lang("gui.action.DIVIDE_2.name"))
                                .addTooltipLine(IKey.lang("gui.pattern_term.auto_fill_pattern.DIVIDE_2.text")
                                        .style(TextFormatting.GRAY))
                                .syncHandler("pattern_divide_2"))
                        .child(new ButtonWidget<>()
                                .pos(25, 198)
                                .size(18)
                                .overlay(IKey.str("/3"))
                                .hoverBackground(GuiTextures.MC_BUTTON_HOVERED)
                                .addTooltipLine(IKey.lang("gui.action.DIVIDE_3.name"))
                                .addTooltipLine(IKey.lang("gui.pattern_term.auto_fill_pattern.DIVIDE_3.text")
                                        .style(TextFormatting.GRAY))
                                .syncHandler("pattern_divide_3"))
                        .child(new ButtonWidget<>()
                                .pos(43, 198)
                                .size(18)
                                .overlay(IKey.str("-1"))
                                .hoverBackground(GuiTextures.MC_BUTTON_HOVERED)
                                .addTooltipLine(IKey.lang("gui.tooltips.appliedenergistics2.DecreaseByOne"))
                                .addTooltipLine(IKey.lang("gui.tooltips.appliedenergistics2.DecreaseByOneDesc")
                                        .style(TextFormatting.GRAY))
                                .syncHandler("pattern_sub_1"))
                        .child(new ButtonWidget<>()
                                .pos(61, 180)
                                .size(36)
                                .overlay(IKey.str("X"))
                                .hoverBackground(GuiTextures.MC_BUTTON_HOVERED)
                                .addTooltipLine(IKey.lang("nae2.pattern_multiplier.unencode"))
                                .addTooltipLine(IKey.lang("nae2.pattern_multiplier.unencode.desc")
                                        .style(TextFormatting.GRAY))
                                .syncHandler("pattern_clear"));
    }

    public static Pair<ItemStack, Integer> getPatternMultiTool(PosGuiData data) {
        return Optional.of(data.getPlayer().inventory.mainInventory)
                .map(inventory -> {
                    final ItemStack multiPattern = TJAItemUtils.getItemStackFromName("nae2:pattern_multiplier");
                    final ItemStack multiPattern2 = TJAAE2Items.SUPER_PATTERN_MULTIPLIER.maybeStack(1).orElse(ItemStack.EMPTY);
                    for (int i = 0; i < inventory.size(); i++) {
                        final ItemStack stack = inventory.get(i);
                        if ((TJAValues.isModLoaded(TJAValues.NAE2_MOD_ID) && stack.isItemEqual(multiPattern)) || stack.isItemEqual(multiPattern2))
                            return Pair.of(stack, i);
                    }
                    if (TJAValues.isModLoaded(TJAValues.BAUBLES_MOD_ID)) {
                        final IItemHandlerModifiable baubleSlots = BaublesApi.getBaublesHandler(data.getPlayer());
                        for (int i = 0; i < baubleSlots.getSlots(); i++) {
                            final ItemStack stack = baubleSlots.getStackInSlot(i);
                            if ((TJAValues.isModLoaded(TJAValues.NAE2_MOD_ID) && stack.isItemEqual(multiPattern)) || stack.isItemEqual(multiPattern2))
                                return Pair.of(baubleSlots.getStackInSlot(i), Integer.MIN_VALUE);
                        }
                    }
                    return Pair.of(ItemStack.EMPTY, -1);
                }).get();
    }

    public static void writePatternMultiToolToNBT(IItemHandler itemHandler, NBTTagCompound compound) {
        final NBTTagList tagList = new NBTTagList();
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            final ItemStack stack = itemHandler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                final NBTTagCompound tagCompound = stack.serializeNBT();
                tagCompound.setInteger("Slot", i);
                tagList.appendTag(tagCompound);
            }
        }
        compound.setTag("Items", tagList);
    }

    public static void readPatternMultiToolNBT(IItemHandlerModifiable itemHandler, NBTTagList tagList) {
        for (int i = 0; i < tagList.tagCount(); i++) {
            final NBTTagCompound compound = tagList.getCompoundTagAt(i);
            if (compound.hasKey("Slot")) {
                final ItemStack patternStack = TJAItemUtils.getItemStackFromName(compound.getString("id"), compound.getInteger("Count"), compound.getShort("Damage"));
                if (compound.hasKey("tag", Constants.NBT.TAG_COMPOUND))
                    patternStack.setTagCompound(compound.getCompoundTag("tag"));
                itemHandler.setStackInSlot(compound.getInteger("Slot"), patternStack);
            }
        }
    }

    public static String numberValidator(String s) {
        return s.startsWith("0") && s.length() > 1 ? String.valueOf(0) : s;
    }
}
