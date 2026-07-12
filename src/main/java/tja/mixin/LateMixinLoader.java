package tja.mixin;

import com.google.common.collect.ImmutableList;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.List;

public class LateMixinLoader implements ILateMixinLoader {

    @Override
    public List<String> getMixinConfigs() {
        return ImmutableList.of("mixins.tt.json");
    }
}
