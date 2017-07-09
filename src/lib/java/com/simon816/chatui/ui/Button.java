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
        int barWidth = ctx.utils().getWidth('│', false) * 2;
        return ctx.utils().splitLines(Text.of(this.label), desiredWidth - barWidth - 3).size() + 2;
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
        return ctx.utils().getStringWidth(this.label, false);
    }

    @Override
    public void draw(PlayerContext ctx, LineFactory lineFactory) {
        int desiredWidth = ctx.width;
        while (desiredWidth % 9 != 0) {
            desiredWidth -= 1;
        }
        int barWidth = ctx.utils().getWidth('│', false) * 2;
        HoverAction<?> hover = null;
        ClickAction<?> click = null;
        if (this.onClick != null) {
            click = Utils.execClick(this.onClick);
        }

        List<String> labelLines = null;
        String labelPart = this.label;
        int width = ctx.utils().getStringWidth(this.label, false);
        if (width > desiredWidth - barWidth - 3) {
            if (this.truncate) {
                int tWidth = width;
                // Trim down
                String t = labelPart;
                while (tWidth > desiredWidth - barWidth - 3 && t.length() > 0) {
                    t = t.substring(0, t.length() - 1);
                    // 6 is width of '...'
                    tWidth = ctx.utils().getStringWidth(t, false) + 6;
                }
                labelPart = t + "...";
                hover = TextActions.showText(Text.of(this.label));
            } else {
                labelLines = ctx.utils().splitLines(labelPart, desiredWidth - barWidth - 3);
            }
        }
        if (labelLines == null) {
            labelLines = Lists.newArrayList(labelPart);
        }

        lineFactory.appendNewLine(ctx.utils().startRepeatTerminate('┌', '─', '┐', desiredWidth), ctx);

        for (String line : labelLines) {
            int tWidth = ctx.utils().getStringWidth(line, false);
            StringBuilder spaces = new StringBuilder();
            spaces.append('│');
            TextUtils.padSpaces(spaces, desiredWidth - tWidth - barWidth - 3);
            spaces.append('│');
            String left = spaces.substring(0, spaces.length() / 2);
            String right = spaces.substring(left.length());
            lineFactory.appendNewLine(Text.builder(left + line + right).onClick(click).onHover(hover).build(), ctx);
        }
        lineFactory.appendNewLine(ctx.utils().startRepeatTerminate('└', '─', '┘', desiredWidth), ctx);
    }
}
