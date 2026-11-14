package tja.integration.ae2;

import appeng.api.config.CondenserOutput;
import appeng.api.config.LockCraftingMode;
import appeng.helpers.ICustomNameObject;
import appeng.helpers.IInterfaceHost;

public interface ISuperInterface extends ICustomNameObject, IPrioritySetter, IInterfaceHost, ITIckSetter, IItemConfigSetter {

    void setBlockingMode(boolean blockingMode);

    void setLockCrafting(LockCraftingMode lockCraftingMode);

    void setInterfaceTerminal(boolean interfaceTerminal);

    void setFluidPacket(boolean fluidPacket);

    void setSplittingItemsFluids(boolean splittingItemsFluids);

    void setBlockModeEx(CondenserOutput blockModeEx);

    void setIntelligentBlocking(boolean intelligentBlocking);

    default void setItemAutoPull(boolean autoPull) {}

    default void setItemAutoPush(boolean autoPush) {}
}
