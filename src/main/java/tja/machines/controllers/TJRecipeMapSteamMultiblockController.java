package tja.machines.controllers;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.RecipeMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class TJRecipeMapSteamMultiblockController extends RecipeMapMultiblockController {

    protected IMultipleTankHandler steamTank;

    public TJRecipeMapSteamMultiblockController(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap) {
        super(metaTileEntityId, recipeMap);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, @Nonnull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.universal.tooltip.parallel", this.recipeMapWorkable.getParallelLimit()));
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.steamTank = new FluidTankList(true, this.getAbilities(MultiblockAbility.STEAM));
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.steamTank = new FluidTankList(true);
    }

    public IMultipleTankHandler getSteamTank() {
        return this.steamTank;
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
    }
}
