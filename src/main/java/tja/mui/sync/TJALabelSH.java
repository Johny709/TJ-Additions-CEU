package tja.mui.sync;

import com.cleanroommc.modularui.value.sync.SyncHandler;
import net.minecraft.network.PacketBuffer;

import java.util.function.Supplier;

public class TJALabelSH extends SyncHandler {

    private final Supplier<String> recipeUid;
    private Supplier<String> localeSupplier;
    private String locale;
    private String uid;

    public TJALabelSH() {
        this(null);
    }

    public TJALabelSH(Supplier<String> recipeUid) {
        this.recipeUid = recipeUid;
    }

    /**
     * Set translatable text to be displayed in the label.
     * This is redundant if {@link #setDynamicLocale(Supplier)} is set.
     */
    public TJALabelSH setLocale(String locale) {
        this.locale = locale;
        return this;
    }

    /**
     * Setting this will update the locale automatically and replace locale defined in {@link #setLocale(String)}
     */
    public TJALabelSH setDynamicLocale(Supplier<String> localeSupplier) {
        this.localeSupplier = localeSupplier;
        return this;
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) {
        if (id == 1) {
            this.uid = buf.readString(Short.MAX_VALUE);
        } else if (id == 2) {
            this.locale = buf.readString(Short.MAX_VALUE);
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) {

    }

    @Override
    public void detectAndSendChanges(boolean init) {
        if (this.recipeUid != null) {
            final String uid = this.recipeUid.get();
            if (uid != null && !uid.equals(this.uid)) {
                this.uid = uid;
                this.syncToClient(1, buffer -> buffer.writeString(this.uid));
            }
        }
        if (this.localeSupplier != null) {
            final String locale = this.localeSupplier.get();
            if (this.locale == null || !this.locale.equals(locale)) {
                this.locale = locale;
                this.syncToClient(2, buffer -> buffer.writeString(this.locale));
            }
        }
    }

    public String getRecipeUid() {
        return this.uid;
    }

    public String getLocale() {
        return this.locale;
    }
}
