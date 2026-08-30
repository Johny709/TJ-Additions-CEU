package tja.mixin.gregtech;

import com.cleanroommc.modularui.api.drawable.IKey;
import gregtech.api.block.IHeatingCoilBlockStats;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.metatileentity.multiblock.ui.MultiblockUIBuilder;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.logic.OCResult;
import gregtech.api.recipes.properties.RecipePropertyStorage;
import gregtech.api.util.KeyUtil;
import gregtech.common.blocks.BlockWireCoil;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityLargeChemicalReactor;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tja.TJAConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Mixin(value = MetaTileEntityLargeChemicalReactor.class, remap = false)
public abstract class MixinMetaTileEntityLargeChemicalReactor extends RecipeMapMultiblockController {

    @Unique
    private int coilTier;

    public MixinMetaTileEntityLargeChemicalReactor(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap) {
        super(metaTileEntityId, recipeMap);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void inject_Init(ResourceLocation metaTileEntityId, CallbackInfo ci) {
        if (!TJAConfig.enableLCRBonus) return;
        this.recipeMapWorkable = new MultiblockRecipeLogic(this, true) {
            @Override
            protected void modifyOverclockPost(@Nonnull OCResult ocResult, @Nonnull RecipePropertyStorage storage) {
                super.modifyOverclockPost(ocResult, storage);

                if (coilTier <= 0) return;

                // each coil above cupronickel (coilTier = 0) uses 5% less energy
                ocResult.setEut(Math.max(1, (long) (ocResult.eut() * (1.0 - coilTier * 0.05))));
            }
        };
    }

    @SideOnly(Side.CLIENT)
    @Inject(method = "addInformation", at = @At("TAIL"))
    private void injectAddInformation(ItemStack stack, @Nullable World player, @Nonnull List<String> tooltip, boolean advanced, CallbackInfo ci) {
        if (TJAConfig.enableLCRBonus)
            tooltip.add(I18n.format("gregtech.multiblock.large_chemical_reactor.coil_bonus"));
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        if (!TJAConfig.enableLCRBonus) return;
        final IHeatingCoilBlockStats coilBlockStats = context.getOrDefault("CoilType", BlockWireCoil.CoilType.CUPRONICKEL);
        this.coilTier = coilBlockStats.getTier();
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.coilTier = 0;
    }

    @Override
    protected void configureDisplayText(MultiblockUIBuilder builder) {
        super.configureDisplayText(builder);
        if (!TJAConfig.enableLCRBonus) return;
        builder.addEmptyLine()
                .addCustom((keyManager, syncer) -> {
                    // Coil energy discount line
                    if (!this.isStructureFormed()) return;
                    final IKey energyDiscount = KeyUtil.number(TextFormatting.AQUA, syncer.syncLong(100 - 5L * this.coilTier), "%");
                    final IKey base = KeyUtil.lang(TextFormatting.GRAY, "gregtech.multiblock.cracking_unit.energy", energyDiscount);
                    final IKey hover = KeyUtil.lang(TextFormatting.GRAY, "gregtech.multiblock.cracking_unit.energy_hover");
                    keyManager.add(KeyUtil.setHover(base, hover));
                });
    }
}
