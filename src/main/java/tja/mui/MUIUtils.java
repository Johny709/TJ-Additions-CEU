package tja.mui;

import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.serialization.ByteBufAdapters;
import com.cleanroommc.modularui.value.sync.InteractionSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import gregtech.api.metatileentity.multiblock.ui.KeyManager;
import gregtech.api.metatileentity.multiblock.ui.Operation;
import gregtech.api.metatileentity.multiblock.ui.UISyncer;
import gregtech.api.mui.drawable.GTObjectDrawable;
import gregtech.api.util.KeyUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import tja.capability.IRecipeInfo;
import tja.integration.ae2.ISuperFluidInterface;
import tja.integration.ae2.ISuperInterface;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

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
        final int maxProgress = syncer.syncInt(recipeInfo.getMaxProgress());
        if (!fluidInputs.isEmpty() || !itemInputs.isEmpty())
            key.add(KeyUtil.lang(TextFormatting.GRAY, "tja.machine.universal.consuming"));
        for (FluidStack fluidStack : fluidInputs)
            MUIUtils.addFluidOutputLine(key, fluidStack, fluidStack.amount, maxProgress);
        for (ItemStack stack : itemInputs)
            MUIUtils.addItemOutputLine(key, stack, stack.getCount(), maxProgress);
        if (!fluidOutputs.isEmpty() || !itemOutputs.isEmpty()) {
            key.add(KeyUtil.lang("")); // new line
            key.add(KeyUtil.lang(TextFormatting.GRAY, "gregtech.gui.multiblock.recipe_producing"));
        }
        for (FluidStack fluidStack : fluidOutputs)
            MUIUtils.addFluidOutputLine(key, fluidStack, fluidStack.amount, maxProgress);
        for (ItemStack stack : itemOutputs)
            MUIUtils.addItemOutputLine(key, stack, stack.getCount(), maxProgress);
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

    public static ModularPanel createPriorityPanel(PanelSyncManager syncManager, IPanelHandler panelHandler, ISuperInterface superInterface) {
        syncManager.syncValue("priority", new StringSyncValue(() -> String.valueOf(superInterface.getPriority()), superInterface::setPriority));
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
                        .setMaxLength(11)
                        .autoUpdateOnChange(true)
                        .syncHandler("priority"))
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

    public static ModularPanel createFluidPriorityPanel(PanelSyncManager syncManager, IPanelHandler panelHandler, ISuperFluidInterface superInterface) {
        syncManager.syncValue("priority", new StringSyncValue(() -> String.valueOf(superInterface.getPriority()), superInterface::setPriority));
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

        return ModularPanel.defaultPanel("me.fluid_interface.priority.panel", 162, 100)
                .child(new TextWidget<>(IKey.lang("gui.appliedenergistics2.Priority"))
                        .pos(7, 7))
                .child(new TextFieldWidget()
                        .pos(7, 46)
                        .size(148, 18)
                        .setMaxLength(11)
                        .autoUpdateOnChange(true)
                        .syncHandler("priority"))
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

    public static ModularPanel createTicksPanel(PanelSyncManager syncManager, IPanelHandler panelHandler, ISuperInterface superInterface) {
        syncManager.syncValue("tick", new StringSyncValue(() -> String.valueOf(superInterface.getTickTime()), superInterface::setTickTime));
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
                        .setMaxLength(11)
                        .autoUpdateOnChange(true)
                        .syncHandler("tick"))
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

    public static ModularPanel createFluidTicksPanel(PanelSyncManager syncManager, IPanelHandler panelHandler, ISuperFluidInterface superInterface) {
        syncManager.syncValue("tick", new StringSyncValue(() -> String.valueOf(superInterface.getTickTime()), superInterface::setTickTime));
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
                        .setMaxLength(11)
                        .autoUpdateOnChange(true)
                        .syncHandler("tick"))
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
}
