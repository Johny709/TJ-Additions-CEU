package tja.mixin.gregtech;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tja.capability.IEnergyContainerStorage;

@Mixin(value = TieredMetaTileEntity.class, remap = false)
public abstract class MixinTieredMetaTileEntity implements IEnergyContainerStorage {

    @Shadow
    protected IEnergyContainer energyContainer;

    @Override
    public IEnergyContainer getEnergyContainer() {
        return this.energyContainer;
    }
}
