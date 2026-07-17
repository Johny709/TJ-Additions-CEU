package tja.machines.multiblocks;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.mui.GTGuiTheme;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.recipes.RecipeMap;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.util.ResourceLocation;
import tja.capability.workables.TJSteamMultiblockRecipeLogic;
import tja.machines.controllers.TJRecipeMapSteamMultiblockController;

import javax.annotation.Nonnull;

public class MetaTileEntityMegaCokeOven extends TJRecipeMapSteamMultiblockController {

    public MetaTileEntityMegaCokeOven(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMap.getByName("coke_oven_2"));
        this.recipeMapWorkable = new TJSteamMultiblockRecipeLogic(this, true);
        this.recipeMapWorkable.setParallelLimit(512);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityMegaCokeOven(this.metaTileEntityId);
    }

    @Nonnull
    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX")
                .aisle("XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX")
                .aisle("XXXXXXXXX", "XCCCCCCCX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX")
                .aisle("XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX")
                .aisle("XXXXXXXXX", "XCCCCCCCX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX")
                .aisle("XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX")
                .aisle("XXXXXXXXX", "XCCCCCCCX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX")
                .aisle("XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX", "X#C#C#C#X", "XXXXXXXXX")
                .aisle("XXXXXXXXX", "XXXXSXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX")
                .where('S', this.selfPredicate())
                .where('C', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.COKE_BRICKS)))
                .where('X', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.COKE_BRICKS)).setMinGlobalLimited(100)
                        .or(autoAbilities(false, false, true, true, false, true, false)))
                .where('#', air())
                .build();
    }

    @Override
    public GTGuiTheme getUITheme() {
        return GTGuiTheme.PRIMITIVE;
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.COKE_BRICKS;
    }
}
