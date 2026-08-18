package tja.util;

import gregtech.api.capability.impl.ItemHandlerList;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.Collections;

public final class TJAGTUtils {

    public static final IItemHandlerModifiable DUMMY_ITEM_HANDLER = new ItemHandlerList(Collections.emptyList());
}
