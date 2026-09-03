package tja.blocks;

import gregtech.api.block.VariantItemBlock;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.function.Function;

import static gregtech.common.blocks.MetaBlocks.statePropertiesToString;

public final class TJAMetaBlocks {

    public static final BlockBatteryCell BATTERY_CELL = new BlockBatteryCell();
    public static final BlockTieredGlass TIERED_GLASS = new BlockTieredGlass();
    public static final BlockSolidCasings SOLID_CASINGS = new BlockSolidCasings();

    public static void init(IForgeRegistry<Block> blocks) {
        blocks.register(BATTERY_CELL);
        blocks.register(TIERED_GLASS);
        blocks.register(SOLID_CASINGS);
    }

    public static void registerItemBlocks(IForgeRegistry<Item> items) {
        items.register(createItemBlock(BATTERY_CELL, VariantItemBlock::new));
        items.register(createItemBlock(TIERED_GLASS, VariantItemBlock::new));
        items.register(createItemBlock(SOLID_CASINGS, VariantItemBlock::new));
    }

    @SideOnly(Side.CLIENT)
    public static void registerItemModels() {
        registerItemModel(BATTERY_CELL);
        registerItemModel(TIERED_GLASS);
        registerItemModel(SOLID_CASINGS);
    }

    @SideOnly(Side.CLIENT)
    private static void registerItemModel(Block block) {
        for (IBlockState state : block.getBlockState().getValidStates()) {
            // noinspection ConstantConditions
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block),
                    block.getMetaFromState(state),
                    new ModelResourceLocation(block.getRegistryName(),
                            statePropertiesToString(state.getProperties())));
        }
    }

    private static <T extends Block> ItemBlock createItemBlock(T block, Function<T, ItemBlock> producer) {
        ItemBlock itemBlock = producer.apply(block);
        ResourceLocation registryName = block.getRegistryName();
        if (registryName == null) {
            throw new IllegalArgumentException("Block " + block.getTranslationKey() + " has no registry name.");
        }
        itemBlock.setRegistryName(registryName);
        return itemBlock;
    }
}
