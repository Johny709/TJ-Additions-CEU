package tja.integration.theoneprobe.impl;

import io.netty.buffer.ByteBuf;
import mcjty.theoneprobe.api.IElement;
import mcjty.theoneprobe.network.NetworkTools;
import tja.integration.theoneprobe.TheOneProbeModule;
import tja.rendering.TJAGuiUtils;

import java.awt.*;

public class ElementProgressBar implements IElement {

    private final String[] args;
    private Dimension size;

    public ElementProgressBar(String... args) {
        this.args = args;
    }

    public ElementProgressBar(ByteBuf byteBuf) {
        final int size = byteBuf.readInt();
        this.args = new String[size];
        for (int i = 0; i < size; i++) {
            if (i == 0) {
                this.args[i] = ElementTJAText.formatLocaleText(NetworkTools.readString(byteBuf));
            } else this.args[i] = NetworkTools.readString(byteBuf);
        }
        this.size = TJAGuiUtils.getBarSize(this.args[0], this.args[1], this.args[2], this.args[3], this.args[4], this.args[6]);
    }

    @Override
    public void render(int x, int y) {
        TJAGuiUtils.drawBar(x, y, this.args[0], this.args[1], this.args[2], this.args[3], this.args[4], this.args[5], this.args[6], 0xFFFFFF);
    }

    @Override
    public int getWidth() {
        return this.size.width;
    }

    @Override
    public int getHeight() {
        return this.size.height + 4;
    }

    @Override
    public void toBytes(ByteBuf byteBuf) {
        byteBuf.writeInt(this.args.length);
        for (String arg : this.args) {
            NetworkTools.writeString(byteBuf, arg);
        }
    }

    @Override
    public int getID() {
        return TheOneProbeModule.ELEMENT_PROGRESS_BAR;
    }
}
