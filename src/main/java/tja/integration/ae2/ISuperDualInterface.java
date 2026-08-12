package tja.integration.ae2;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.UITexture;

public interface ISuperDualInterface extends ISuperInterface, ISuperFluidInterface {

    @Override
    default int getTickTime() {
        return ISuperInterface.super.getTickTime();
    }

    @Override
    default void setTickTime(String tickTime) {
        ISuperInterface.super.setTickTime(tickTime);
    }

    default IDrawable getItemTabTexture() {
        return UITexture.EMPTY;
    }

    default IDrawable getFluidTabTexture() {
        return UITexture.EMPTY;
    }

    default IDrawable getExtraTabTexture() {
        return UITexture.EMPTY;
    }
}
