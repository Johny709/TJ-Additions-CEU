package tja.machines.singleblocks;

import codechicken.lib.colour.ColourRGBA;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.scroll.VerticalScrollData;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.IMetaTileEntityGuiHolder;
import gregtech.api.mui.MetaTileEntityGuiData;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.block.SoundType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.tuple.Pair;
import tja.TJAValues;
import tja.items.handlers.LargeItemStackHandler;
import tja.util.TooltipHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;


public class MetaTileEntityCompressedCrate extends MetaTileEntity implements IMetaTileEntityGuiHolder {

    private static final int ROW_SIZE = 27;
    private static final int AMOUNT_OF_ROWS = 27;
    private final boolean isInfinite;
    private final Material material;

    public MetaTileEntityCompressedCrate(ResourceLocation metaTileEntityId, boolean isInfinite) {
        super(metaTileEntityId);
        this.isInfinite = isInfinite;
        this.material = this.isInfinite ? Materials.Neutronium : Materials.Obsidian;
        this.initializeInventory();
        this.itemInventory = this.importItems;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityCompressedCrate(this.metaTileEntityId, this.isInfinite);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tja.machine.compressed_chest.description"));
        tooltip.add(I18n.format("tja.machine.universal.stack", TJAValues.thousandFormat.format(this.isInfinite ? Integer.MAX_VALUE : 64)));
        tooltip.add(I18n.format("tja.machine.universal.slots", TJAValues.thousandFormat.format(ROW_SIZE * AMOUNT_OF_ROWS)));
        final NBTTagCompound compound = stack.getTagCompound();
        if (compound == null || compound.isEmpty()) return;
        final NBTTagList itemList = compound.getCompoundTag("Inventory").getTagList("Items", 10);
        final int size = itemList.tagCount() / 10;
        if (itemList.tagCount() > 0)
            tooltip.add(I18n.format("tja.machine.compressed_chest.slot_filled", TJAValues.thousandFormat.format(itemList.tagCount()), TJAValues.thousandFormat.format(ROW_SIZE * AMOUNT_OF_ROWS)));
        TooltipHelper.shiftText(tooltip, tip -> {
            TooltipHelper.pageText(tip, size, (tip1, tooltipHandler) -> {
                final int start = tooltipHandler.getIndex() * 10;
                for (int i = start; i < Math.min(itemList.tagCount(), start + 10); i++) {
                    final NBTTagCompound itemCompound = itemList.getCompoundTagAt(i);
                    final ItemStack itemStack = new ItemStack(Item.getByNameOrId(itemCompound.getString("id")), itemCompound.getInteger("Count"), itemCompound.getShort("Damage"));
                    tip1.add(I18n.format("tja.machine.compressed_chest.slot", TJAValues.thousandFormat.format(itemCompound.getInteger("Slot")), itemStack.getDisplayName(), TJAValues.thousandFormat.format(itemStack.getCount())));
                }
            });
        });
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return this.isInfinite ? new LargeItemStackHandler(ROW_SIZE * AMOUNT_OF_ROWS, Integer.MAX_VALUE) : new ItemStackHandler(ROW_SIZE * AMOUNT_OF_ROWS);
    }

    @Override
    public void clearMachineInventory(@Nonnull List<ItemStack> itemBuffer) {
        // doesn't drop items
    }

    @Override
    public void writeItemStackData(NBTTagCompound itemStack) {
        super.writeItemStackData(itemStack);
        itemStack.setTag("Inventory", ((ItemStackHandler) this.importItems).serializeNBT());
    }

    @Override
    public void initFromItemStackData(NBTTagCompound itemStack) {
        super.initFromItemStackData(itemStack);
        ((ItemStackHandler) this.importItems).deserializeNBT(itemStack.getCompoundTag("Inventory"));
    }

    @Override
    public boolean hasFrontFacing() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        if (this.material.toString().contains("wood")) {
            Textures.WOODEN_CRATE.render(renderState, translation, GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()), pipeline);
        } else {
            int baseColor = ColourRGBA.multiply(GTUtility.convertRGBtoOpaqueRGBA_CL(this.material.getMaterialRGB()), GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()));
            Textures.METAL_CRATE.render(renderState, translation, baseColor, pipeline);
        }
    }

    @Override
    public @Nonnull ModularPanel buildUI(MetaTileEntityGuiData metaTileEntityGuiData, PanelSyncManager panelSyncManager, UISettings uiSettings) {
        panelSyncManager.registerSlotGroup(new SlotGroup("compressed_crate_inventory", ROW_SIZE, 1, true)
                .setAllowSorting(true));

        return ModularPanel.defaultPanel("compressed_crate.gui",  Math.max(176, 14 + Math.min(27, ROW_SIZE) * 18), 18 * Math.min(12, AMOUNT_OF_ROWS) + 112)
                .child(new TextWidget<>(IKey.lang(this.getMetaFullName()))
                        .pos(7, 5))
                .child(new Grid()
                        .pos(7, 18)
                        .size(490, 216)
                        .scrollable(new VerticalScrollData() {{
                            this.setScrollSize(AMOUNT_OF_ROWS * 18);
                        }})
                        .gridOfSizeWidth(this.importItems.getSlots(), ROW_SIZE, (x, y, i) -> new ItemSlot()
                                .slot(new ModularSlot(this.importItems, i)
                                        .slotGroup("compressed_crate_inventory")
                                        .ignoreMaxStackSize(this.isInfinite))))
                .bindPlayerInventory();
    }

    @Override
    public int getLightOpacity() {
        return 1;
    }

    @Override
    public String getHarvestTool() {
        return this.material.toString().contains("wood") ? "axe" : "pickaxe";
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        if (ModHandler.isMaterialWood(material)) {
            return Pair.of(Textures.WOODEN_CRATE.getParticleTexture(), getPaintingColor());
        } else {
            int color = ColourRGBA.multiply(
                    GTUtility.convertRGBtoOpaqueRGBA_CL(this.material.getMaterialRGB()),
                    GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColor()));
            color = GTUtility.convertOpaqueRGBA_CLtoRGB(color);
            return Pair.of(Textures.METAL_CRATE.getParticleTexture(), color);
        }
    }

    @Nonnull
    @Override
    public SoundType getSoundType() {
        return ModHandler.isMaterialWood(this.material) ? SoundType.WOOD : SoundType.METAL;
    }
}
