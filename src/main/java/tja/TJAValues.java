package tja;

import net.minecraftforge.fml.common.Loader;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TJAValues {

    private static final ConcurrentMap<String, Boolean> IS_MOD_LOADED_CACHE = new ConcurrentHashMap<>();

    public static boolean isModLoaded(String modid) {
        if (IS_MOD_LOADED_CACHE.containsKey(modid)) {
            return IS_MOD_LOADED_CACHE.get(modid);
        }
        boolean isLoaded = Loader.instance().getIndexedModList().containsKey(modid);
        IS_MOD_LOADED_CACHE.put(modid, isLoaded);
        return isLoaded;
    }
}
