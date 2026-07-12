package tja.mixin;

import com.google.common.collect.ImmutableList;
import tja.TJAValues;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.List;

public class LateMixinLoader implements ILateMixinLoader {

    @Override
    public List<String> getMixinConfigs() {
        final ImmutableList.Builder<String> builder = ImmutableList.builder();
        builder.add("mixins.tja.gregtech.json");
        if (TJAValues.isModLoaded("appliedenergistics2"))
            builder.add("mixins.tja.ae2.json");
        return builder.build();
    }
}
