package com.simon816.chatui.util;

import com.google.common.collect.Lists;
import com.simon816.chatui.lib.PlayerContext;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;

public class TextBuffer {

    private final ArrayList<Text> buffer = Lists.newArrayList();
    private int width;
    private final PlayerContext ctx;


    public TextBuffer(PlayerContext ctx) {
        this.ctx = ctx;
    }

    public void append(Text text) {
        this.buffer.add(text);
        this.width += this.ctx.utils().getWidth(text);
    }

    public int getWidth() {
        return this.width;
    }

    public ArrayList<Text> getContents() {
        return this.buffer;
    }

    public void clear() {
        this.buffer.clear();
        this.width = 0;
    }
}
