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
        int dashWidth = ctx.utils().getWidth('─', false);
        int desiredWidth = ctx.width;
        desiredWidth -= desiredWidth % dashWidth;

        int barWidth = ctx.utils().getWidth('│', false) * 2;
        int cornerWidth = ctx.utils().getWidth('┌', false) + ctx.utils().getWidth('┐', false);
        int trimWidth = (desiredWidth - cornerWidth) % dashWidth;
        int innerWidth = desiredWidth - barWidth - trimWidth;

        return ctx.utils().splitLines(Text.of(this.label), innerWidth).size() + 2;
    }

    @Override
    public int getPrefWidth(PlayerContext ctx) {
        return ctx.width;
    }

    @Override
    public int getMinWidth(PlayerContext ctx) {
        if (this.truncate) {
            return ctx.utils().getWidth('.', false) * 3;
        }
        return ctx.utils().getStringWidth(this.label, false);
    }

    @Override
    public void draw(PlayerContext ctx, LineFactory lineFactory) {
        int dashWidth = ctx.utils().getWidth('─', false);
        int desiredWidth = ctx.width;
        desiredWidth -= desiredWidth % dashWidth;
        int barWidth = ctx.utils().getWidth('│', false) * 2;
        HoverAction<?> hover = null;
        ClickAction<?> click = null;
        if (this.onClick != null) {
            click = Utils.execClick(this.onClick);
        }

        Text.Builder topBar = Text.builder();
        int trimWidth = ctx.utils().startRepeatTerminate(topBar, '┌', '─', '┐', desiredWidth);
        lineFactory.appendNewLine(topBar.build(), ctx);
        int innerWidth = desiredWidth - barWidth - trimWidth;

        List<String> labelLines = null;
        String labelPart = this.label;
        int width = ctx.utils().getStringWidth(this.label, false);
        if (width > innerWidth) {
            if (this.truncate) {
                int dotWidth = ctx.utils().getWidth('.', false) * 3;
                int tWidth = width;
                // Trim down
                String t = labelPart;
                while (tWidth > innerWidth && t.length() > 0) {
                    t = t.substring(0, t.length() - 1);
                    tWidth = ctx.utils().getStringWidth(t, false) + dotWidth;
                }
                labelPart = t + "...";
                hover = TextActions.showText(Text.of(this.label));
            } else {
                labelLines = ctx.utils().splitLines(labelPart, innerWidth);
            }
        }
        if (labelLines == null) {
            labelLines = Lists.newArrayList(labelPart);
        }

        for (String lineContent : labelLines) {
            int lineWidth = ctx.utils().getStringWidth(lineContent, false);
            StringBuilder line = new StringBuilder();
            line.append('│');
            TextUtils.padSpaces(line, innerWidth - lineWidth);
            line.append('│');
            line.insert(line.length() / 2, lineContent);
            lineFactory.appendNewLine(Text.builder(line.toString()).onClick(click).onHover(hover).build(), ctx);
        }
        lineFactory.appendNewLine(ctx.utils().startRepeatTerminate('└', '─', '┘', desiredWidth), ctx);
    }
}
