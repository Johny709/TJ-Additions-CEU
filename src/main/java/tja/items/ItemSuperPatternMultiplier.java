package tja.items;

import appeng.core.Api;
import baubles.api.BaubleType;
import baubles.api.IBauble;
import baubles.api.cap.BaublesCapabilities;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.factory.GuiFactories;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.InteractionSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.scroll.VerticalScrollData;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.common.Optional;
import tja.TJAValues;
import tja.items.handlers.FilteredItemStackHandler;
import tja.mui.MUIUtils;
import tja.mui.TJAGuiTextures;
import tja.mui.slot.TJAModularSlot;
import tja.util.TJAItemUtils;
import tja.util.TJAUtility;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Optional.Interface(iface = "baubles.api.IBauble", modid = TJAValues.BAUBLES_MOD_ID)
public class ItemSuperPatternMultiplier extends Item implements IGuiHolder<GuiData>, IBauble {

    @Override
    public ModularPanel buildUI(GuiData data, PanelSyncManager syncManager, UISettings settings) {
        final ItemStack patternMultiTool = data.getMainHandItem();
        final NBTTagCompound compound = TJAItemUtils.getCompoundFromStack(patternMultiTool);
        final NBTTagCompound invTag = compound.getCompoundTag("inv");
        final NBTTagCompound upgradeTag = compound.getCompoundTag("upgrades");
        final FilteredItemStackHandler multiPatternSlots = new FilteredItemStackHandler(72, 64)
                .setItemStackPredicate((slot, itemStack) -> itemStack.isItemEqual(Api.INSTANCE.definitions().materials().blankPattern().maybeStack(1).orElse(ItemStack.EMPTY)) ||
                        itemStack.isItemEqual(Api.INSTANCE.definitions().items().encodedPattern().maybeStack(1).orElse(ItemStack.EMPTY)) || itemStack.isItemEqual(TJAItemUtils.getItemStackFromName("ae2fc:dense_encoded_pattern")));
        multiPatternSlots.setOnContentsChangedPost((slot, itemStack) -> MUIUtils.writePatternMultiToolToNBT(multiPatternSlots, invTag));
        final FilteredItemStackHandler multiUpgradeSlots = new FilteredItemStackHandler(7, 1)
                .setItemStackPredicate((slot, itemStack) -> itemStack.isItemEqual(Api.INSTANCE.definitions().materials().cardCapacity().maybeStack(1).orElse(ItemStack.EMPTY)));
        multiUpgradeSlots.setOnContentsChangedPost((slot, itemStack) -> MUIUtils.writePatternMultiToolToNBT(multiUpgradeSlots, upgradeTag));

        syncManager.syncValue("pattern_multiply_2", new InteractionSyncHandler()
                .setOnMousePressed(mouseData -> TJAUtility.changeInterfacePatternAmount(multiPatternSlots, m -> m * 2,
                        () -> MUIUtils.writePatternMultiToolToNBT(multiPatternSlots, invTag))));
        syncManager.syncValue("pattern_multiply_3", new InteractionSyncHandler()
                .setOnMousePressed(mouseData -> TJAUtility.changeInterfacePatternAmount(multiPatternSlots, m -> m * 3,
                        () -> MUIUtils.writePatternMultiToolToNBT(multiPatternSlots, invTag))));
        syncManager.syncValue("pattern_add_1", new InteractionSyncHandler()
                .setOnMousePressed(mouseData -> TJAUtility.changeInterfacePatternAmount(multiPatternSlots, m -> m + 1,
                        () -> MUIUtils.writePatternMultiToolToNBT(multiPatternSlots, invTag))));
        syncManager.syncValue("pattern_divide_2", new InteractionSyncHandler()
                .setOnMousePressed(mouseData -> TJAUtility.changeInterfacePatternAmount(multiPatternSlots, m -> m / 2,
                        () -> MUIUtils.writePatternMultiToolToNBT(multiPatternSlots, invTag))));
        syncManager.syncValue("pattern_divide_3", new InteractionSyncHandler()
                .setOnMousePressed(mouseData -> TJAUtility.changeInterfacePatternAmount(multiPatternSlots, m -> m / 3,
                        () -> MUIUtils.writePatternMultiToolToNBT(multiPatternSlots, invTag))));
        syncManager.syncValue("pattern_sub_1", new InteractionSyncHandler()
                .setOnMousePressed(mouseData -> TJAUtility.changeInterfacePatternAmount(multiPatternSlots, m -> m - 1,
                        () -> MUIUtils.writePatternMultiToolToNBT(multiPatternSlots, invTag))));
        syncManager.syncValue("pattern_clear", new InteractionSyncHandler()
                .setOnMousePressed(mouseData -> TJAUtility.clearPatterns(multiPatternSlots,
                        () -> MUIUtils.writePatternMultiToolToNBT(multiPatternSlots, invTag))));
        final IntSyncValue multiPatternUpgrades = new IntSyncValue(multiUpgradeSlots::getSlotsFilled);
        syncManager.syncValue("multi_pattern_upgrades", multiPatternUpgrades);

        syncManager.registerSlotGroup(new SlotGroup("upgrade_slots", 1, 1, true));
        syncManager.registerSlotGroup(new SlotGroup("pattern_slots", 9, 2, true));

        final Flow upgradeArea = Flow.col();
        settings.getRecipeViewerSettings().addExclusionArea(upgradeArea);

        syncManager.addOpenListener(player -> {
            MUIUtils.readPatternMultiToolNBT(multiPatternSlots, invTag.getTagList("Items", 10));
            MUIUtils.readPatternMultiToolNBT(multiUpgradeSlots, upgradeTag.getTagList("Items", 10));
            if (patternMultiTool.getTagCompound() == null || patternMultiTool.getTagCompound().isEmpty()) {
                compound.setTag("inv", invTag);
                compound.setTag("upgrades", upgradeTag);
                patternMultiTool.setTagCompound(compound);
            }
        });

        return ModularPanel.defaultPanel("super_pattern_multiplier.gui", 176, 200)
                .child(new Grid()
                        .pos(7, 7)
                        .size(166, 72)
                        .scrollable(new VerticalScrollData() {{
                            this.setScrollSize(multiPatternSlots.getSlots() * 18 / 9);
                        }})
                        .gridOfSizeWidth(multiPatternSlots.getSlots(), 9, (x, y, i) -> new ItemSlot()
                                .setEnabledIf(slot -> i / 9 <= multiPatternUpgrades.getIntValue())
                                .background(GuiTextures.SLOT_ITEM, TJAGuiTextures.PATTERN_OVERLAY)
                                .slot(new TJAModularSlot(multiPatternSlots, i)
                                        .slotGroup("pattern_slots"))))
                .child(upgradeArea
                        .left(179)
                        .size(32, 140)
                        .background(GuiTextures.MC_BACKGROUND)
                        .children(multiUpgradeSlots.getSlots(), i -> new ItemSlot()
                                .pos(7, 7 + (i * 18))
                                .background(GuiTextures.SLOT_ITEM, TJAGuiTextures.UPGRADE_OVERLAY)
                                .slot(new TJAModularSlot(multiUpgradeSlots, i)
                                        .slotGroup("upgrade_slots"))))
                .child(new ButtonWidget<>()
                        .pos(7, 80)
                        .size(18)
                        .overlay(IKey.str("*2"))
                        .hoverBackground(GuiTextures.MC_BUTTON_HOVERED)
                        .addTooltipLine(IKey.lang("gui.action.MULTIPLY_2.name"))
                        .addTooltipLine(IKey.lang("gui.pattern_term.auto_fill_pattern.MULTIPLY_2.text")
                                .style(TextFormatting.GRAY))
                        .syncHandler("pattern_multiply_2"))
                .child(new ButtonWidget<>()
                        .pos(25, 80)
                        .size(18)
                        .overlay(IKey.str("*3"))
                        .hoverBackground(GuiTextures.MC_BUTTON_HOVERED)
                        .addTooltipLine(IKey.lang("gui.action.MULTIPLY_3.name"))
                        .addTooltipLine(IKey.lang("gui.pattern_term.auto_fill_pattern.MULTIPLY_3.text")
                                .style(TextFormatting.GRAY))
                        .syncHandler("pattern_multiply_3"))
                .child(new ButtonWidget<>()
                        .pos(43, 80)
                        .size(18)
                        .overlay(IKey.str("+1"))
                        .hoverBackground(GuiTextures.MC_BUTTON_HOVERED)
                        .addTooltipLine(IKey.lang("gui.tooltips.appliedenergistics2.IncreaseByOne"))
                        .addTooltipLine(IKey.lang("gui.tooltips.appliedenergistics2.IncreaseByOneDesc")
                                .style(TextFormatting.GRAY))
                        .syncHandler("pattern_add_1"))
                .child(new ButtonWidget<>()
                        .pos(7, 98)
                        .size(18)
                        .overlay(IKey.str("/2"))
                        .hoverBackground(GuiTextures.MC_BUTTON_HOVERED)
                        .addTooltipLine(IKey.lang("gui.action.DIVIDE_2.name"))
                        .addTooltipLine(IKey.lang("gui.pattern_term.auto_fill_pattern.DIVIDE_2.text")
                                .style(TextFormatting.GRAY))
                        .syncHandler("pattern_divide_2"))
                .child(new ButtonWidget<>()
                        .pos(25, 98)
                        .size(18)
                        .overlay(IKey.str("/3"))
                        .hoverBackground(GuiTextures.MC_BUTTON_HOVERED)
                        .addTooltipLine(IKey.lang("gui.action.DIVIDE_3.name"))
                        .addTooltipLine(IKey.lang("gui.pattern_term.auto_fill_pattern.DIVIDE_3.text")
                                .style(TextFormatting.GRAY))
                        .syncHandler("pattern_divide_3"))
                .child(new ButtonWidget<>()
                        .pos(43, 98)
                        .size(18)
                        .overlay(IKey.str("-1"))
                        .hoverBackground(GuiTextures.MC_BUTTON_HOVERED)
                        .addTooltipLine(IKey.lang("gui.tooltips.appliedenergistics2.DecreaseByOne"))
                        .addTooltipLine(IKey.lang("gui.tooltips.appliedenergistics2.DecreaseByOneDesc")
                                .style(TextFormatting.GRAY))
                        .syncHandler("pattern_sub_1"))
                .child(new ButtonWidget<>()
                        .pos(61, 80)
                        .size(36)
                        .overlay(IKey.str("X"))
                        .hoverBackground(GuiTextures.MC_BUTTON_HOVERED)
                        .addTooltipLine(IKey.lang("nae2.pattern_multiplier.unencode"))
                        .addTooltipLine(IKey.lang("nae2.pattern_multiplier.unencode.desc")
                                .style(TextFormatting.GRAY))
                        .syncHandler("pattern_clear"))
                .child(SlotGroupWidget.playerInventory(7, true, (i, slot) -> {
                    if (data.getPlayer().inventory.getStackInSlot(i) == patternMultiTool)
                        slot.setEnabled(false);
                    return slot;
                }));
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World worldIn, @Nonnull EntityPlayer playerIn, @Nonnull EnumHand handIn) {
        if (!worldIn.isRemote && TJAAE2Items.SUPER_PATTERN_MULTIPLIER.isSameAs(playerIn.getHeldItemMainhand()))
            GuiFactories.playerInventory().openFromHand(playerIn, handIn);
        return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
    }

    @Nullable
    @Override
    @Optional.Method(modid = TJAValues.BAUBLES_MOD_ID)
    public ICapabilityProvider initCapabilities(@Nonnull ItemStack stack, @Nullable NBTTagCompound nbt) {
        return new ICapabilityProvider() {
            @Override
            public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
                return capability == BaublesCapabilities.CAPABILITY_ITEM_BAUBLE;
            }

            @Nullable
            @Override
            public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
                if (capability == BaublesCapabilities.CAPABILITY_ITEM_BAUBLE)
                    return BaublesCapabilities.CAPABILITY_ITEM_BAUBLE.cast(ItemSuperPatternMultiplier.this);
                return null;
            }
        };
    }

    @Override
    @Optional.Method(modid = TJAValues.BAUBLES_MOD_ID)
    public BaubleType getBaubleType(ItemStack itemStack) {
        return BaubleType.TRINKET;
    }
}
