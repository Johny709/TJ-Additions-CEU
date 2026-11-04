package tja.machines.singleblocks;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.DoubleSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.scroll.VerticalScrollData;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.PhantomItemSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.MetaTileEntityGuiData;
import gregtech.api.util.DummyContainer;
import gregtech.client.renderer.texture.Textures;
import gregtech.core.sound.GTSoundEvents;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import tja.capability.handler.IRecipeMapProvider;
import tja.capability.workables.CrafterRecipeLogic;
import tja.mui.sync.SlotDisplayWidgetSH;
import tja.mui.widget.SlotDisplayWidget;
import tja.recipes.CountableIngredient;
import tja.recipes.RecipeUtils;
import tja.textures.TJATextures;
import tja.util.Color;
import tja.util.EnumFacingHelper;
import tja.util.TooltipHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class MetaTileEntityCrafter extends TJATieredWorkableMetaTileEntity implements IRecipeMapProvider {

    private final CrafterRecipeLogic recipeLogic = new CrafterRecipeLogic(this);
    private final InventoryCrafting inventoryCrafting = new InventoryCrafting(new DummyContainer(), 3, 3);
    private final ItemStackHandler craftingInventory = new NotifiableItemStackHandler(this, 9, this, false);
    private final ItemStackHandler encodingInventory;
    private final ItemStackHandler resultInventory = new NotifiableItemStackHandler(this, 1, this, false);
    private final Int2ObjectMap<Triple<IRecipe, NonNullList<CountableIngredient>, NonNullList<ItemStack>>> recipeMap = new Int2ObjectOpenHashMap<>();
    private final int encodingSlots;
    private final int parallel;
    private IRecipe currentRecipe;


    public MetaTileEntityCrafter(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        this.encodingSlots = 6 + (tier * 3);
        this.parallel = this.getTier();
        this.encodingInventory = new NotifiableItemStackHandler(this, this.encodingSlots, this, false);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityCrafter(this.metaTileEntityId, this.getTier());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, @Nonnull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("tja.multiblock.large_crafter.description"));
        tooltip.add(I18n.format("tja.multiblock.large_crafter.slots", this.encodingSlots));
        tooltip.add(I18n.format("tja.multiblock.parallel", this.parallel));
        tooltip.add(I18n.format("tja.machine.crafter.tooltip"));
        tooltip.add(TooltipHelper.blinkingText(Color.YELLOW, 20, "tja.multiblock.large_crafter.warning"));
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new NotifiableItemStackHandler(this, 18, this, false);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new NotifiableItemStackHandler(this, 4, this, true);
    }

    @Override
    public void update() {
        super.update();
        if (!this.getWorld().isRemote)
            this.recipeLogic.update();
    }

    @Override
    public @Nonnull ModularPanel buildUI(MetaTileEntityGuiData data, PanelSyncManager syncManager, UISettings settings) {
        syncManager.syncValue("progress", new DoubleSyncValue(() -> this.recipeLogic.getProgress() == 0 ? 0 :
                1.0 * this.recipeLogic.getProgress() / this.recipeLogic.getMaxProgress()));
        syncManager.registerSlotGroup(new SlotGroup("item_input", 9, 1, true));

        return ModularPanel.defaultPanel("crafter.gui", 176, 216)
                .child(IKey.lang(this.getMetaFullName()).asWidget()
                        .pos(7, 5))
                .child(new Grid()
                        .pos(7, 14)
                        .size(54, 54)
                        .gridOfSizeWidth(this.craftingInventory.getSlots(), 3, (x, y, i) -> new PhantomItemSlot()
                                .slot(new ModularSlot(this.craftingInventory, i)
                                        .changeListener((itemStack, amount, client, init) ->
                                                this.setCraftingResult(i, this.craftingInventory.getStackInSlot(i))))))
                .child(new Grid()
                        .pos(109, 14)
                        .size(64, 54)
                        .scrollable(new VerticalScrollData(false, 10) {{
                            this.setScrollSize(encodingInventory.getSlots() * 18 / 3);
                        }})
                        .gridOfSizeWidth(this.encodingInventory.getSlots(), 3, (x, y, i) -> new SlotDisplayWidget()
                                .syncHandler(new SlotDisplayWidgetSH(this.encodingInventory, i)
                                        .onPressedConsumer((button, slot, stack) -> {
                                            if (button == 0) {
                                                this.clearCraftingResult();
                                                final NonNullList<ItemStack> itemStacks = this.recipeMap.get(slot).getRight();
                                                for (int j = 0; j < itemStacks.size(); j++) {
                                                    this.setCraftingResult(j, itemStacks.get(j));
                                                }
                                            } else if (button == 1) {
                                                this.removeRecipe(slot);
                                            }
                                        }))))
                .child(new Grid()
                        .pos(7, 72)
                        .size(162, 36)
                        .gridOfSizeHeight(this.importItems.getSlots(), 2, (x, y, i) -> new ItemSlot()
                                .slot(new ModularSlot(this.importItems, i)
                                        .slotGroup("item_input"))))
                .child(Flow.row()
                        .pos(61, 112)
                        .size(72, 18)
                        .children(this.exportItems.getSlots(), i -> new ItemSlot()
                                .slot(new ModularSlot(this.exportItems, i)
                                        .canPut(false))))
                .child(new SlotDisplayWidget()
                        .pos(72, 28)
                        .syncHandler(new SlotDisplayWidgetSH(this.resultInventory, 0)
                                .onPressedConsumer((button, slot, stack) -> this.addRecipe(this.currentRecipe))))
                .child(new ProgressWidget()
                        .pos(37, 111)
                        .size(22)
                        .texture(GTGuiTextures.PROGRESS_BAR_ARROW, 22)
                        .syncHandler("progress"))
                .bindPlayerInventory();
    }

    private void addRecipe(IRecipe recipe) {
        if (recipe != null) {
            for (int i = 0; i < this.encodingSlots; i++) {
                if (!this.recipeMap.containsKey(i)) {
                    this.encodingInventory.setStackInSlot(i, recipe.getRecipeOutput());
                    final NonNullList<ItemStack> itemStacks = NonNullList.create();
                    for (int j = 0; j < this.craftingInventory.getSlots(); j++) {
                        itemStacks.add(this.craftingInventory.getStackInSlot(j));
                    }
                    this.recipeMap.put(i, new ImmutableTriple<>(recipe, RecipeUtils.mergeIngredients(recipe.getIngredients()), itemStacks));
                    this.markDirty();
                    return;
                }
            }
        }
    }

    private void removeRecipe(int slot) {
        this.recipeMap.remove(slot);
        final Int2ObjectMap<Triple<IRecipe, NonNullList<CountableIngredient>, NonNullList<ItemStack>>> recipeMap = new Int2ObjectArrayMap<>();
        int i = 0;
        for (int j = 0; j < this.encodingInventory.getSlots(); j++)
            this.encodingInventory.setStackInSlot(j, ItemStack.EMPTY);
        for (Map.Entry<Integer, Triple<IRecipe, NonNullList<CountableIngredient>, NonNullList<ItemStack>>> recipeEntry : this.recipeMap.entrySet()) {
            recipeMap.put(i, recipeEntry.getValue());
            this.encodingInventory.setStackInSlot(i++, recipeEntry.getValue().getLeft().getRecipeOutput());
        }
        this.recipeMap.clear();
        this.recipeMap.putAll(recipeMap);
        this.markDirty();
    }

    private void clearCraftingResult() {
        for (int i = 0; i < this.craftingInventory.getSlots(); i++) {
            this.craftingInventory.setStackInSlot(i, ItemStack.EMPTY);
            this.inventoryCrafting.setInventorySlotContents(i, ItemStack.EMPTY);
        }
    }

    private void setCraftingResult(int index, ItemStack stack) {
        this.craftingInventory.setStackInSlot(index, stack);
        this.inventoryCrafting.setInventorySlotContents(index, stack);
        this.currentRecipe = CraftingManager.findMatchingRecipe(this.inventoryCrafting, this.getWorld());
        this.resultInventory.setStackInSlot(0, this.currentRecipe != null ? this.currentRecipe.getRecipeOutput() : ItemStack.EMPTY);
        this.markDirty();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.MULTIBLOCK_WORKABLE_OVERLAY.renderOrientedState(renderState, translation, pipeline, this.frontFacing, this.recipeLogic.isActive(),
                this.recipeLogic.isWorkingEnabled());
        TJATextures.CRAFTER.renderSided(EnumFacingHelper.getTopFacingFrom(this.getFrontFacing()), renderState, translation, pipeline);
        Textures.PIPE_OUT_OVERLAY.renderSided(this.getOutputFacing(), renderState, translation, pipeline);
        if (this.isAutoOutputItems())
            Textures.ITEM_OUTPUT_OVERLAY.renderSided(this.getOutputFacing(), renderState, translation, pipeline);
        if (this.isAutoOutputFluids())
            Textures.FLUID_OUTPUT_OVERLAY.renderSided(this.getOutputFacing(), renderState, translation, pipeline);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        final NBTTagList recipeList = new NBTTagList();
        for (Map.Entry<Integer, Triple<IRecipe, NonNullList<CountableIngredient>, NonNullList<ItemStack>>> recipeEntry : this.recipeMap.entrySet()) {
            final NBTTagCompound recipeNBT = new NBTTagCompound();
            final NBTTagList patternNBT = new NBTTagList();
            final NonNullList<ItemStack> itemStacks = recipeEntry.getValue().getRight();
            for (ItemStack itemStack : itemStacks) {
                patternNBT.appendTag(itemStack.serializeNBT());
            }
            recipeNBT.setInteger("index", recipeEntry.getKey());
            recipeNBT.setString("id", recipeEntry.getValue().getLeft().getRegistryName().toString());
            recipeNBT.setTag("craftingPattern", patternNBT);
            recipeList.appendTag(recipeNBT);
        }
        data.setTag("recipeList", recipeList);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        final NBTTagList recipeList = data.getTagList("recipeList", 10);
        for (int i = 0; i < recipeList.tagCount(); i++) {
            final int index = recipeList.getCompoundTagAt(i).getInteger("index");
            final IRecipe recipe = CraftingManager.getRecipe(new ResourceLocation(recipeList.getCompoundTagAt(i).getString("id")));
            if (recipe != null) {
                final NonNullList<ItemStack> itemStacks = NonNullList.create();
                final NBTTagList patternNBT = recipeList.getCompoundTagAt(i).getTagList("craftingPattern", 10);
                for (int j = 0; j < patternNBT.tagCount(); j++) {
                    itemStacks.add(new ItemStack(patternNBT.getCompoundTagAt(j)));
                }
                this.recipeMap.put(index, new ImmutableTriple<>(recipe, RecipeUtils.mergeIngredients(recipe.getIngredients()), itemStacks));
                this.encodingInventory.setStackInSlot(index, recipe.getRecipeOutput());
            }
        }
    }

    @Override
    public Int2ObjectMap<Triple<IRecipe, NonNullList<CountableIngredient>, NonNullList<ItemStack>>> getCrafterRecipeMap() {
        return this.recipeMap;
    }

    @Override
    public int getParallel() {
        return this.parallel;
    }

    @Override
    public boolean isActive() {
        return this.recipeLogic.isActive();
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.recipeLogic.isWorkingEnabled();
    }

    @Override
    public void setWorkingEnabled(boolean b) {
        this.recipeLogic.setWorkingEnabled(b);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SoundEvent getSound() {
        return GTSoundEvents.ASSEMBLER;
    }
}
