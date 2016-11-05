package com.simon816.chatui.ui;

import com.simon816.chatui.util.TextUtils;
import org.spongepowered.api.text.Text;

public class MergingLineFactory extends LineFactory {

    private int currLineOverwrite = -1;
    private int baseOffset = 0;
    private int currentMaxWidth = 0;

    @Override
    public void appendNewLine(Text text, boolean forceUnicode) {
        this.currentMaxWidth = Math.max(this.currentMaxWidth, TextUtils.getWidth(text, forceUnicode));
        if (this.currLineOverwrite == -1) {
            super.appendNewLine(text, forceUnicode);
            return;
        } else if (this.currLineOverwrite < getLines().size()) {
            getLines().set(this.currLineOverwrite, appendOnto(getLines().get(this.currLineOverwrite), text, forceUnicode));
        } else {
            getLines().add(appendOnto(null, text, forceUnicode));
        }
        this.currLineOverwrite++;
    }

    private Text appendOnto(Text existing, Text newText, boolean forceUnicode) {
        if (newText.isEmpty()) {
            return existing == null ? newText : existing;
        }
        int exWidth = existing == null ? 0 : TextUtils.getWidth(existing, forceUnicode);
        Text.Builder builder = Text.builder();
        StringBuilder spaces = new StringBuilder();
        TextUtils.padSpaces(spaces, this.baseOffset - exWidth);
        Text space = Text.builder(spaces.toString()).build();
        if (existing != null) {
            builder.append(existing);
        }
        builder.append(space, newText);
        return builder.build();
    }

    @Override
    protected int currentLineCount() {
        if (this.currLineOverwrite == -1) {
            return super.currentLineCount();
        }
        return this.currLineOverwrite;
    }

    public void rewind() {
        this.currLineOverwrite = 0;
        this.baseOffset += this.currentMaxWidth;
        this.currentMaxWidth = 0;
    }

    public int getCurrentDrawWidth() {
        return this.currentMaxWidth;
    }
}
