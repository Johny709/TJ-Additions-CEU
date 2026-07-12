package tja.mixin.gregtech;

import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.WorkableTieredMetaTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.client.renderer.ICubeRenderer;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(value = WorkableTieredMetaTileEntity.class, remap = false)
public abstract class WorkableTieredMetaTileEntityMixin extends TieredMetaTileEntity {

    @Final
    @Mutable
    @Shadow
    private Function<Integer, Integer> tankScalingFunction;

    public WorkableTieredMetaTileEntityMixin(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
    }

    @Inject(method = "<init>(Lnet/minecraft/util/ResourceLocation;Lgregtech/api/recipes/RecipeMap;Lgregtech/client/renderer/ICubeRenderer;ILjava/util/function/Function;Z)V", at = @At("TAIL"))
    private void inject_Init(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, ICubeRenderer renderer, int tier, Function<Integer, Integer> tankScalingFunction, boolean handlesRecipeOutputs, CallbackInfo ci) {
        this.tankScalingFunction = t -> 64000;
        this.initializeInventory();
        this.reinitializeEnergyContainer();
    }
}
