package com.simon816.chatui.ui;

import com.google.common.collect.Lists;
import com.simon816.chatui.lib.PlayerChatView;
import com.simon816.chatui.lib.PlayerContext;
import com.simon816.chatui.util.TextUtils;
import com.simon816.chatui.util.Utils;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.HoverAction;
import org.spongepowered.api.text.action.TextActions;

import java.util.List;
import java.util.function.Consumer;

public class Button implements UIComponent {

    private final String label;
    private boolean truncate;
    private Consumer<PlayerChatView> onClick;

    public Button(String label) {
        this.label = label;
    }

    public String getLabel() {
        return this.label;
    }

    public void setClickHandler(Consumer<PlayerChatView> handler) {
        this.onClick = handler;
    }

    public void truncateOverflow(boolean truncate) {
        this.truncate = truncate;
    }

    @Override
    public int getPrefHeight(PlayerContext ctx) {
        if (this.truncate) {
            return 3;
        }
        int desiredWidth = ctx.width;
        while (desiredWidth % 9 != 0) {
            desiredWidth -= 1;
        }
        int barWidth = TextUtils.getWidth('│', false, ctx.forceUnicode) * 2;
        return TextUtils.splitLines(Text.of(this.label), desiredWidth - barWidth - 3, ctx.forceUnicode).size() + 2;
    }

    @Override
    public int getPrefWidth(PlayerContext ctx) {
        return ctx.width;
    }

    @Override
    public int getMinWidth(PlayerContext ctx) {
        if (this.truncate) {
            return 6;
        }
        return TextUtils.getStringWidth(this.label, false, ctx.forceUnicode);
    }

    @Override
    public void draw(PlayerContext ctx, LineFactory lineFactory) {
        int desiredWidth = ctx.width;
        while (desiredWidth % 9 != 0) {
            desiredWidth -= 1;
        }
        int barWidth = TextUtils.getWidth('│', false, ctx.forceUnicode) * 2;
        HoverAction<?> hover = null;
        ClickAction<?> click = null;
        if (this.onClick != null) {
            click = Utils.execClick(this.onClick);
        }

        List<String> labelLines = null;
        String labelPart = this.label;
        int width = TextUtils.getStringWidth(this.label, false, ctx.forceUnicode);
        if (width > desiredWidth - barWidth - 3) {
            if (this.truncate) {
                int tWidth = width;
                // Trim down
                String t = labelPart;
                while (tWidth > desiredWidth - barWidth - 3 && t.length() > 0) {
                    t = t.substring(0, t.length() - 1);
                    // 6 is width of '...'
                    tWidth = TextUtils.getStringWidth(t, false, ctx.forceUnicode) + 6;
                }
                labelPart = t + "...";
                hover = TextActions.showText(Text.of(this.label));
            } else {
                labelLines = TextUtils.splitLines(labelPart, desiredWidth - barWidth - 3, ctx.forceUnicode);
            }
        }
        if (labelLines == null) {
            labelLines = Lists.newArrayList(labelPart);
        }

        lineFactory.appendNewLine(TextUtils.startRepeatTerminate('┌', '─', '┐', desiredWidth, ctx.forceUnicode), ctx.forceUnicode);

        for (String line : labelLines) {
            int tWidth = TextUtils.getStringWidth(line, false, ctx.forceUnicode);
            StringBuilder spaces = new StringBuilder();
            spaces.append('│');
            TextUtils.padSpaces(spaces, desiredWidth - tWidth - barWidth - 3);
            spaces.append('│');
            String left = spaces.substring(0, spaces.length() / 2);
            String right = spaces.substring(left.length());
            lineFactory.appendNewLine(Text.builder(left + line + right).onClick(click).onHover(hover).build(), ctx.forceUnicode);
        }
        lineFactory.appendNewLine(TextUtils.startRepeatTerminate('└', '─', '┘', desiredWidth, ctx.forceUnicode), ctx.forceUnicode);
    }
}
