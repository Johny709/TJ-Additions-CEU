package tja.machines.singleblocks;

import codechicken.lib.colour.ColourRGBA;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
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
import gregtech.api.metatileentity.IFastRenderMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.IMetaTileEntityGuiHolder;
import gregtech.api.mui.MetaTileEntityGuiData;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTUtility;
import net.minecraft.block.SoundType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.tuple.Pair;
import tja.TJAValues;
import tja.capability.TJADataCodes;
import tja.items.handlers.LargeItemStackHandler;
import tja.textures.TJATextures;
import tja.util.TooltipHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static gregtech.api.util.GTUtility.convertOpaqueRGBA_CLtoRGB;


public class MetaTileEntityCompressedChest extends MetaTileEntity implements IFastRenderMetaTileEntity, IMetaTileEntityGuiHolder {

    private static final IndexedCuboid6 CHEST_COLLISION = new IndexedCuboid6(null, new Cuboid6(1 / 16.0, 1 / 16.0, 1 / 16.0, 15 / 16.0, 14 / 16.0, 15 / 16.0));
    private static final int ROW_SIZE = 27;
    private static final int AMOUNT_OF_ROWS = 27;

    private final boolean isInfinite;
    private final Material material;
    private float lidAngle;
    private float prevLidAngle;
    private int numPlayersUsing;

    public MetaTileEntityCompressedChest(ResourceLocation metaTileEntityId, boolean isInfinite) {
        super(metaTileEntityId);
        this.isInfinite = isInfinite;
        this.material = this.isInfinite ? Materials.Neutronium : Materials.Obsidian;
        this.initializeInventory();
        this.itemInventory = this.importItems;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityCompressedChest(this.metaTileEntityId, this.isInfinite);
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
    public void update() {
        super.update();
        final BlockPos blockPos = this.getPos();
        this.prevLidAngle = this.lidAngle;

        if (this.numPlayersUsing > 0 && this.lidAngle == 0.0F) {
            final double soundX = blockPos.getX() + 0.5;
            final double soundZ = blockPos.getZ() + 0.5;
            final double soundY = blockPos.getY() + 0.5;
            this.getWorld().playSound(null, soundX, soundY, soundZ, SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, 0.5F, getWorld().rand.nextFloat() * 0.1F + 0.9F);
        }

        if ((this.numPlayersUsing == 0 && this.lidAngle > 0.0F) || (this.numPlayersUsing > 0 && this.lidAngle < 1.0F)) {
            final float previousValue = this.lidAngle;

            if (this.numPlayersUsing > 0) {
                this.lidAngle = Math.min(this.lidAngle + 0.1F, 1.0F);
            } else {
                this.lidAngle = Math.max(this.lidAngle - 0.1F, 0.0F);
            }
            if (this.lidAngle < 0.5F && previousValue >= 0.5F) {
                final double soundX = blockPos.getX() + 0.5;
                final double soundZ = blockPos.getZ() + 0.5;
                final double soundY = blockPos.getY() + 0.5;
                this.getWorld().playSound(null, soundX, soundY, soundZ, SoundEvents.BLOCK_CHEST_CLOSE, SoundCategory.BLOCKS, 0.5F, getWorld().rand.nextFloat() * 0.1F + 0.9F);
            }
        }
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
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        if (ModHandler.isMaterialWood(this.material)) {
            return Pair.of(TJATextures.WOODEN_CHEST.getParticleTexture(), this.getPaintingColor());
        } else {
            int color = ColourRGBA.multiply(
                    GTUtility.convertRGBtoOpaqueRGBA_CL(this.material.getMaterialRGB()),
                    GTUtility.convertRGBtoOpaqueRGBA_CL(this.getPaintingColor())
            );
            color = convertOpaqueRGBA_CLtoRGB(color);
            return Pair.of(TJATextures.METAL_CHEST.getParticleTexture(), color);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {}

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntityFast(CCRenderState renderState, Matrix4 translation, float partialTicks) {
        float angle = this.prevLidAngle + (this.lidAngle - this.prevLidAngle) * partialTicks;
        angle = 1.0f - (1.0f - angle) * (1.0f - angle) * (1.0f - angle);
        final float resultLidAngle = angle * 90.0f;
        if (ModHandler.isMaterialWood(this.material)) {
            ColourMultiplier multiplier = new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()));
            TJATextures.WOODEN_CHEST.render(renderState, translation, new IVertexOperation[]{multiplier}, this.getFrontFacing(), resultLidAngle);
        } else {
            ColourMultiplier multiplier = new ColourMultiplier(ColourRGBA.multiply(
                    GTUtility.convertRGBtoOpaqueRGBA_CL(this.material.getMaterialRGB()),
                    GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering())));
            TJATextures.METAL_CHEST.render(renderState, translation, new IVertexOperation[]{multiplier}, this.getFrontFacing(), resultLidAngle);
        }
    }

    @Override
    public @Nonnull ModularPanel buildUI(MetaTileEntityGuiData metaTileEntityGuiData, PanelSyncManager panelSyncManager, UISettings uiSettings) {
        panelSyncManager.registerSlotGroup(new SlotGroup("compressed_chest_inventory", ROW_SIZE, 1, true)
                .setAllowSorting(true));

        panelSyncManager.addOpenListener(this::onContainerOpen);
        panelSyncManager.addCloseListener(this::onContainerClose);

        return ModularPanel.defaultPanel("compressed_chest.gui",  Math.max(176, 14 + Math.min(27, ROW_SIZE) * 18), 18 * Math.min(12, AMOUNT_OF_ROWS) + 112)
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
                                        .slotGroup("compressed_chest_inventory")
                                        .ignoreMaxStackSize(this.isInfinite))))
                .bindPlayerInventory();
    }

    private void onContainerOpen(EntityPlayer player) {
        if (!player.isSpectator()) {
            if (this.numPlayersUsing < 0) {
                this.numPlayersUsing = 0;
            }
            ++this.numPlayersUsing;
            this.writeCustomData(TJADataCodes.PLAYERS_USING_CHEST, buffer -> buffer.writeInt(this.numPlayersUsing));
        }
    }

    private void onContainerClose(EntityPlayer player) {
        if (!player.isSpectator()) {
            --this.numPlayersUsing;
            this.writeCustomData(TJADataCodes.PLAYERS_USING_CHEST, buffer -> buffer.writeInt(this.numPlayersUsing));
        }
    }

    @Override
    public void addCollisionBoundingBox(List<IndexedCuboid6> collisionList) {
        collisionList.add(CHEST_COLLISION);
    }

    @Override
    public void writeInitialSyncData(@Nonnull PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(this.numPlayersUsing);
    }

    @Override
    public void receiveInitialSyncData(@Nonnull PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.numPlayersUsing = buf.readInt();
    }

    @Override
    public void receiveCustomData(int dataId, @Nonnull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == TJADataCodes.PLAYERS_USING_CHEST) {
            this.numPlayersUsing = buf.readInt();
        }
    }

    @Override
    public int getLightOpacity() {
        return 0;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public String getHarvestTool() {
        return ModHandler.isMaterialWood(this.material) ? "axe" : "pickaxe";
    }

    @Override
    public double getCoverPlateThickness() {
        return 1.0 / 16.0; //1/16th of the block size
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(this.getPos().add(-1, 0, -1), this.getPos().add(2, 2, 2));
    }

    @Nonnull
    @Override
    public SoundType getSoundType() {
        return ModHandler.isMaterialWood(this.material) ? SoundType.WOOD : SoundType.METAL;
    }
}
