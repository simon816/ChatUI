package com.simon816.chatui.ui;

import com.google.common.collect.Lists;
import com.simon816.chatui.lib.PlayerContext;
import org.spongepowered.api.text.Text;

import java.util.List;

public class LineFactory {

    private final List<Text> lines = Lists.newArrayList();

    public void appendNewLine(Text text, boolean forceUnicode) {
        this.lines.add(text);
    }

    public void insertNewLine(int index, Text text, boolean forceUnicode) {
        this.lines.add(index, text);
    }

    public void addAll(List<Text> lines, boolean forceUnicode) {
        for (Text line : lines) {
            appendNewLine(line, forceUnicode);
        }
    }

    public void addAll(Text[] lines, boolean forceUnicode) {
        for (Text line : lines) {
            appendNewLine(line, forceUnicode);
        }
    }

    public List<Text> getLines() {
        return this.lines;
    }

    public LineFactory merge(LineFactory other, boolean forceUnicode) {
        this.addAll(other.getLines(), forceUnicode);
        return this;
    }

    protected int currentLineCount() {
        return this.lines.size();
    }

    public void fillBlank(PlayerContext ctx) {
        int remaining = linesRemaining(ctx);
        while (remaining-- > 0) {
            appendNewLine(Text.EMPTY, ctx.forceUnicode);
        }
    }

    public int linesRemaining(PlayerContext ctx) {
        return ctx.height - this.currentLineCount();
    }

    public LineFactory fillThenMerge(PlayerContext ctx, LineFactory other) {
        List<Text> otherLines = other.getLines();
        int height = ctx.height - otherLines.size();
        if (height > 0) {
            fillBlank(ctx.withHeight(height));
        }
        addAll(otherLines, ctx.forceUnicode);
        return this;
    }

}
