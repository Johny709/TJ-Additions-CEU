package tja.mui.sync;

import com.cleanroommc.modularui.value.sync.SyncHandler;
import net.minecraft.network.PacketBuffer;

import java.util.function.IntConsumer;


public class SelectionWidgetGroupSH extends SyncHandler {

    private IntConsumer indexListener;
    private int selectedIndex = -1;

    /**
     * Fires every time a new selection index has been selected both on client and server.
     * The value can be -1 when nothing is selected so make sure to check for that.
     */
    public void setIndexListener(IntConsumer indexListener) {
        this.indexListener = indexListener;
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) {
        if (id == 1) {
            this.selectedIndex = buf.readInt();
            if (this.indexListener != null)
                this.indexListener.accept(this.selectedIndex);
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) {
        if (id == 1) {
            this.selectedIndex = buf.readInt();
            if (this.indexListener != null)
                this.indexListener.accept(this.selectedIndex);
            this.syncToClient(1, buffer -> buffer.writeInt(this.selectedIndex));
        }
    }

    public int getSelectedIndex() {
        return this.selectedIndex;
    }
}
