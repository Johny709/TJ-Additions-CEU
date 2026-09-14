package tja.items;

import appeng.core.Api;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.factory.GuiFactories;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.scroll.VerticalScrollData;
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
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import tja.items.handlers.FilteredItemStackHandler;
import tja.mui.MUIUtils;
import tja.mui.TJAGuiTextures;
import tja.mui.slot.TJAModularSlot;
import tja.util.TJAItemUtils;

import javax.annotation.Nonnull;

public class ItemSuperPatternMultiplier extends Item implements IGuiHolder<GuiData> {

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

        return ModularPanel.defaultPanel("super_pattern_multiplier.gui", 176, 170)
                .child(new Grid()
                        .pos(7, 7)
                        .size(166, 72)
                        .scrollable(new VerticalScrollData() {{
                            this.setScrollSize(multiPatternSlots.getSlots() * 18 / 9);
                        }})
                        .gridOfSizeWidth(multiPatternSlots.getSlots(), 9, (x, y, i) -> new ItemSlot()
                                .setEnabledIf(slot -> i / 9 <= multiUpgradeSlots.getSlotsFilled())
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
                .bindPlayerInventory();
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World worldIn, @Nonnull EntityPlayer playerIn, @Nonnull EnumHand handIn) {
        if (!worldIn.isRemote && TJAAE2Items.SUPER_PATTERN_MULTIPLIER.isSameAs(playerIn.getHeldItemMainhand()))
            GuiFactories.playerInventory().openFromHand(playerIn, handIn);
        return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
    }
}
