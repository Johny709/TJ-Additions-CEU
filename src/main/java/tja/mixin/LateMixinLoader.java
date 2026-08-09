package tja.mixin;

import com.google.common.collect.ImmutableList;
import tja.TJAValues;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.List;

public class LateMixinLoader implements ILateMixinLoader {

    @Override
    public List<String> getMixinConfigs() {
        final ImmutableList.Builder<String> builder = ImmutableList.builder();
        if (TJAValues.isModLoaded(TJAValues.GREGTECH_MOD_ID))
            builder.add("mixins.tja.gregtech.json");
        if (TJAValues.isModLoaded(TJAValues.AE2_MOD_ID))
            builder.add("mixins.tja.ae2.json");
        if (TJAValues.isModLoaded(TJAValues.THEONEPROBE_MOD_ID))
            builder.add("mixins.tja.theoneprobe.json");
        return builder.build();
    }
}
