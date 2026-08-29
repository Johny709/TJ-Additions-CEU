package tja.items;

import appeng.api.definitions.IItemDefinition;
import appeng.api.parts.IPartItem;
import appeng.core.Api;
import appeng.core.features.ItemDefinition;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import tja.TJA;
import tja.integration.ae2.items.*;

import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class TJAAE2Items {

    public static final Object2ObjectMap<ResourceLocation, IItemDefinition> TJ_ITEM_DEFINITION_REGISTRY = new Object2ObjectOpenHashMap<>();
    public static final Object2IntMap<Item> UPGRADES = new Object2IntOpenHashMap<>();

    public static IItemDefinition PART_DUAL_INTERFACE_V2;
    public static IItemDefinition PART_SUPER_INTERFACE;
    public static IItemDefinition PART_SUPER_FLUID_INTERFACE;
    public static IItemDefinition PART_SUPER_DUAL_INTERFACE;
    public static IItemDefinition PART_PATTERN_INTERFACE;
    public static IItemDefinition PART_STOCKING_INTERFACE;
    public static IItemDefinition PART_STOCKING_FLUID_INTERFACE;
    public static IItemDefinition PART_STOCKING_DUAL_INTERFACE;
    public static IItemDefinition PART_SUPER_ULTIMATE_INTERFACE;

    public static IItemDefinition PART_SUPER_INTERFACE_TERMINAL;
    public static IItemDefinition PART_CELL_TERMINAL;

    public static IItemDefinition MATERIAL_ITEM_CELL_65536K;
    public static IItemDefinition MATERIAL_ITEM_CELL_262144K;
    public static IItemDefinition MATERIAL_ITEM_CELL_1048M;
    public static IItemDefinition MATERIAL_ITEM_CELL_DIGITAL_SINGULARITY;
    public static IItemDefinition MATERIAL_FLUID_CELL_65536K;
    public static IItemDefinition MATERIAL_FLUID_CELL_262144K;
    public static IItemDefinition MATERIAL_FLUID_CELL_1048M;
    public static IItemDefinition MATERIAL_FLUID_CELL_DIGITAL_SINGULARITY;

    public static IItemDefinition ITEM_CELL_65536K;
    public static IItemDefinition ITEM_CELL_262144K;
    public static IItemDefinition ITEM_CELL_1048M;
    public static IItemDefinition ITEM_CELL_DIGITAL_SINGULARITY;
    public static IItemDefinition FLUID_CELL_65536K;
    public static IItemDefinition FLUID_CELL_262144K;
    public static IItemDefinition FLUID_CELL_1048M;
    public static IItemDefinition FLUID_CELL_DIGITAL_SINGULARITY;
    public static IItemDefinition ITEM_BLOCK_CONTAINER_64K;
    public static IItemDefinition ITEM_BLOCK_CONTAINER_65536K;
    public static IItemDefinition ITEM_BLOCK_CONTAINER_SINGULARITY;

    public static void init(IForgeRegistry<Item> registry) {
        PART_DUAL_INTERFACE_V2 = registerItem(registry, item -> new ItemDefinition("me.part.dual_interface_v2", new ItemPartDualInterfaceV2()));
        PART_SUPER_INTERFACE = registerItem(registry, item -> new ItemDefinition("me.part.super_interface", new ItemPartSuperInterface()));
        PART_SUPER_FLUID_INTERFACE = registerItem(registry, item -> new ItemDefinition("me.part.super_fluid_interface", new ItemPartSuperFluidInterface()));
        PART_SUPER_DUAL_INTERFACE = registerItem(registry, item -> new ItemDefinition("me.part.super_dual_interface", new ItemPartSuperDualInterface()));
        PART_PATTERN_INTERFACE = registerItem(registry, item -> new ItemDefinition("me.part.pattern_interface", new ItemPartPatternInterface()));
        PART_STOCKING_INTERFACE = registerItem(registry, item -> new ItemDefinition("me.part.stocking_interface", new ItemPartStockingInterface()));
        PART_STOCKING_FLUID_INTERFACE = registerItem(registry, item -> new ItemDefinition("me.part.stocking_fluid_interface", new ItemPartStockingFluidInterface()));
        PART_STOCKING_DUAL_INTERFACE = registerItem(registry, item -> new ItemDefinition("me.part.stocking_dual_interface", new ItemPartStockingDualInterface()));
        PART_SUPER_ULTIMATE_INTERFACE = registerItem(registry, item -> new ItemDefinition("me.part.super_ultimate_interface", new ItemPartSuperUltimateInterface()));

        PART_SUPER_INTERFACE_TERMINAL = registerItem(registry, item -> new ItemDefinition("me.part.super_interface_terminal", new ItemPartSuperInterfaceTerminal()));
        PART_CELL_TERMINAL = registerItem(registry, item -> new ItemDefinition("me.part.cell_terminal", new ItemPartCellTerminal()));

        MATERIAL_ITEM_CELL_65536K = registerItem(registry, item -> new ItemDefinition("me.material.item_cell.65536k", item));
        MATERIAL_ITEM_CELL_262144K = registerItem(registry, item -> new ItemDefinition("me.material.item_cell.262144k", item));
        MATERIAL_ITEM_CELL_1048M = registerItem(registry, item -> new ItemDefinition("me.material.item_cell.1048m", item));
        MATERIAL_ITEM_CELL_DIGITAL_SINGULARITY = registerItem(registry, item -> new ItemDefinition("me.material.item_cell.digital_singularity", item));
        MATERIAL_FLUID_CELL_65536K = registerItem(registry, item -> new ItemDefinition("me.material.fluid_cell.65536k", item));
        MATERIAL_FLUID_CELL_262144K = registerItem(registry, item -> new ItemDefinition("me.material.fluid_cell.262144k", item));
        MATERIAL_FLUID_CELL_1048M = registerItem(registry, item -> new ItemDefinition("me.material.fluid_cell.1048m", item));
        MATERIAL_FLUID_CELL_DIGITAL_SINGULARITY = registerItem(registry, item -> new ItemDefinition("me.material.fluid_cell.digital_singularity", item));

        ITEM_CELL_65536K = registerItem(registry, item -> new ItemDefinition("me.item_cell.65536k", new TJAItemStorageCell(MATERIAL_ITEM_CELL_65536K, 65536)));
        ITEM_CELL_262144K = registerItem(registry, item -> new ItemDefinition("me.item_cell.262144k", new TJAItemStorageCell(MATERIAL_ITEM_CELL_262144K, 262144)));
        ITEM_CELL_1048M = registerItem(registry, item -> new ItemDefinition("me.item_cell.1048m", new TJAItemStorageCell(MATERIAL_ITEM_CELL_1048M, 1048576)));
        ITEM_CELL_DIGITAL_SINGULARITY = registerItem(registry, item -> new ItemDefinition("me.item_cell.digital_singularity", new TJAItemStorageCell(MATERIAL_ITEM_CELL_DIGITAL_SINGULARITY, Integer.MAX_VALUE)));
        FLUID_CELL_65536K = registerItem(registry, item -> new ItemDefinition("me.fluid_cell.65536k", new TJAFluidStorageCell(MATERIAL_FLUID_CELL_65536K, 65536)));
        FLUID_CELL_262144K = registerItem(registry, item -> new ItemDefinition("me.fluid_cell.262144k", new TJAFluidStorageCell(MATERIAL_FLUID_CELL_262144K, 262144)));
        FLUID_CELL_1048M = registerItem(registry, item -> new ItemDefinition("me.fluid_cell.1048m", new TJAFluidStorageCell(MATERIAL_FLUID_CELL_1048M, 1048576)));
        FLUID_CELL_DIGITAL_SINGULARITY = registerItem(registry, item -> new ItemDefinition("me.fluid_cell.digital_singularity", new TJAFluidStorageCell(MATERIAL_FLUID_CELL_DIGITAL_SINGULARITY, Integer.MAX_VALUE)));

        ITEM_BLOCK_CONTAINER_64K = registerItem(registry, item -> new ItemDefinition("me.block_container.item_cell.64k", new TJABlockContainerItemStorageCell(Api.INSTANCE.definitions().materials().cell1kPart(), 64)));
        ITEM_BLOCK_CONTAINER_65536K = registerItem(registry, item -> new ItemDefinition("me.block_container.item_cell.65536k", new TJABlockContainerItemStorageCell(MATERIAL_ITEM_CELL_65536K, 65536)));
        ITEM_BLOCK_CONTAINER_SINGULARITY = registerItem(registry, item -> new ItemDefinition("me.block_container.item_cell.singularity", new TJABlockContainerItemStorageCell(MATERIAL_ITEM_CELL_DIGITAL_SINGULARITY, Integer.MAX_VALUE)));

        Api.INSTANCE.getPartModels().registerModels(TJ_ITEM_DEFINITION_REGISTRY.values().stream()
                .map(definition -> definition.maybeItem().orElse(null))
                .filter(item -> item instanceof IPartItem<?> && item.getRegistryName() != null)
                .map(item -> "part/" + item.getRegistryName().getPath())
                .flatMap(path -> Stream.of(path + "_base", path + "_off", path + "_on", path + "_has_channel"))
                .map(path -> new ResourceLocation(TJA.MOD_ID, path))
                .collect(Collectors.toList()));
    }

    @SideOnly(Side.CLIENT)
    public static void registerModels() {
        TJAAE2Items.TJ_ITEM_DEFINITION_REGISTRY.forEach((location, itemDefinition) -> ModelLoader.setCustomModelResourceLocation(itemDefinition.maybeItem().orElseThrow(() -> new NullPointerException("Item not found")), 0, new ModelResourceLocation(location, "inventory")));
    }

    private static IItemDefinition registerItem(IForgeRegistry<Item> registry, Function<Item, IItemDefinition> itemDefinition) {
        final IItemDefinition definition = itemDefinition.apply(new Item());
        final Item item = definition.maybeItem().orElse(null);
        final ResourceLocation resourceLocation = new ResourceLocation(TJA.MOD_ID, definition.identifier());
        assert item != null;
        item.setRegistryName(resourceLocation);
        item.setTranslationKey(definition.identifier());
        registry.register(item);
        TJ_ITEM_DEFINITION_REGISTRY.put(resourceLocation, definition);
        return definition;
    }
}
