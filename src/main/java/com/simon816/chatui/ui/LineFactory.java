package com.simon816.chatui.ui;

import com.google.common.collect.Lists;
import com.simon816.chatui.PlayerContext;
import org.spongepowered.api.text.Text;

import java.util.List;

public class LineFactory {

    private final List<Text> lines = Lists.newArrayList();

    public void appendNewLine(Text text) {
        this.lines.add(text);
    }

    public void addAll(List<Text> lines) {
        for (Text line : lines) {
            appendNewLine(line);
        }
    }

    public void addAll(Text[] lines) {
        for (Text line : lines) {
            appendNewLine(line);
        }
    }

    public List<Text> getLines() {
        return this.lines;
    }

    public LineFactory merge(LineFactory other) {
        this.addAll(other.getLines());
        return this;
    }

    protected int currentLineCount() {
        return this.lines.size();
    }

    public void fillBlank(PlayerContext ctx) {
        int remaining = linesRemaining(ctx);
        while (remaining-- > 0) {
            appendNewLine(Text.EMPTY);
        }
    }

    public int linesRemaining(PlayerContext ctx) {
        return ctx.height - this.currentLineCount();
    }

    public LineFactory fillThenMerge(PlayerContext ctx, LineFactory other) {
        List<Text> otherLines = other.getLines();
        fillBlank(ctx.withHeight(ctx.height - otherLines.size()));
        addAll(otherLines);
        return this;
    }

}
