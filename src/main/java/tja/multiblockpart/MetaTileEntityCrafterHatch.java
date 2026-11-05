package tja.multiblockpart;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.InteractionSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.scroll.VerticalScrollData;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.PhantomItemSlot;
import gregtech.api.capability.INotifiableHandler;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.AbilityInstances;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.IMetaTileEntityGuiHolder;
import gregtech.api.mui.MetaTileEntityGuiData;
import gregtech.api.util.DummyContainer;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockNotifiablePart;
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
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import tja.capability.handler.IRecipeMapProvider;
import tja.mui.sync.SlotDisplayWidgetSH;
import tja.mui.widget.SlotDisplayWidget;
import tja.recipes.CountableIngredient;
import tja.recipes.RecipeUtils;
import tja.textures.TJATextures;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class MetaTileEntityCrafterHatch extends MetaTileEntityMultiblockNotifiablePart implements IMetaTileEntityGuiHolder,
        IMultiblockAbilityPart<IRecipeMapProvider>,
        IRecipeMapProvider {

    private final InventoryCrafting inventoryCrafting = new InventoryCrafting(new DummyContainer(), 3, 3);
    private final ItemStackHandler craftingInventory = new NotifiableItemStackHandler(this, 9, this, false);
    private final ItemStackHandler encodingInventory;
    private final ItemStackHandler resultInventory = new NotifiableItemStackHandler(this, 1, this, false);
    private final Int2ObjectMap<Triple<IRecipe, NonNullList<CountableIngredient>, NonNullList<ItemStack>>> recipeMap = new Int2ObjectOpenHashMap<>();
    private final int encodingSlots;
    private Runnable clearRecipeCache;
    private IRecipe currentRecipe;

    public MetaTileEntityCrafterHatch(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier, false);
        this.encodingSlots = 6 + (tier * 3);
        this.encodingInventory = new NotifiableItemStackHandler(this, this.encodingSlots, this, false);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityCrafterHatch(this.metaTileEntityId, this.getTier());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, @Nonnull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("tja.machine.crafter_hatch.description"));
        tooltip.add(I18n.format("tja.multiblock.large_crafter.slots", this.encodingSlots));
        tooltip.add(I18n.format("tja.machine.crafter.tooltip"));
    }

    @Override
    public @Nonnull ModularPanel buildUI(MetaTileEntityGuiData data, PanelSyncManager syncManager, UISettings settings) {
        syncManager.syncValue("clear_recipe", new InteractionSyncHandler()
                .setOnMousePressed(mouseData -> {
                    this.clearCraftingResult();
                    this.setCraftingResult(0, ItemStack.EMPTY);
                }));

        return ModularPanel.defaultPanel("crafter_hatch.gui", 176, 156)
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
                                                if (this.clearRecipeCache != null)
                                                    this.clearRecipeCache.run();
                                                this.markDirty();
                                            }
                                        }))))
                .child(new SlotDisplayWidget()
                        .pos(72, 28)
                        .syncHandler(new SlotDisplayWidgetSH(this.resultInventory, 0)
                                .onPressedConsumer((button, slot, stack) -> this.addRecipe(this.currentRecipe))))
                .child(new ButtonWidget<>()
                        .pos(62, 14)
                        .size(8)
                        .background(GTGuiTextures.BUTTON_CLEAR_GRID)
                        .addTooltipLine(IKey.lang("tja.multiblock.large_crafter.clear_recipe"))
                        .syncHandler("clear_recipe"))
                .bindPlayerInventory();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        TJATextures.CRAFTER.renderSided(this.getFrontFacing(), renderState, translation, pipeline);
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
    public void addToMultiBlock(MultiblockControllerBase controllerBase) {
        super.addToMultiBlock(controllerBase);
        if (controllerBase instanceof IRecipeMapProvider)
            this.clearRecipeCache = ((IRecipeMapProvider) controllerBase)::clearRecipeCache;
        if (this.craftingInventory instanceof INotifiableHandler)
            ((INotifiableHandler) this.craftingInventory).addNotifiableMetaTileEntity(controllerBase);
        if (this.resultInventory instanceof INotifiableHandler)
            ((INotifiableHandler) this.resultInventory).addNotifiableMetaTileEntity(controllerBase);
        if (this.encodingInventory instanceof INotifiableHandler)
            ((INotifiableHandler) this.encodingInventory).addNotifiableMetaTileEntity(controllerBase);
    }

    @Override
    public void removeFromMultiBlock(MultiblockControllerBase controllerBase) {
        super.removeFromMultiBlock(controllerBase);
        this.clearRecipeCache = null;
        if (this.craftingInventory instanceof INotifiableHandler)
            ((INotifiableHandler) this.craftingInventory).removeNotifiableMetaTileEntity(controllerBase);
        if (this.resultInventory instanceof INotifiableHandler)
            ((INotifiableHandler) this.resultInventory).removeNotifiableMetaTileEntity(controllerBase);
        if (this.encodingInventory instanceof INotifiableHandler)
            ((INotifiableHandler) this.encodingInventory).removeNotifiableMetaTileEntity(controllerBase);
    }

    @Override
    public void registerAbilities(@Nonnull AbilityInstances abilityInstances) {
        abilityInstances.add(this);
    }

    @Override
    public @Nonnull MultiblockAbility<IRecipeMapProvider> getAbility() {
        return TJAMultiblockAbility.CRAFTER_HATCH;
    }

    @Override
    public Int2ObjectMap<Triple<IRecipe, NonNullList<CountableIngredient>, NonNullList<ItemStack>>> getCrafterRecipeMap() {
        return this.recipeMap;
    }
}
