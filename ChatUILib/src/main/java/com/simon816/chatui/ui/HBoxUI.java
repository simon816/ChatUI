package com.simon816.chatui.ui;

import com.simon816.chatui.lib.PlayerContext;

import java.util.List;

public class HBoxUI extends UIPane {

    @Override
    public void draw(PlayerContext ctx, LineFactory lineFactory) {
        // This is all very rough atm
        int preferedSize = 0;
        List<UIComponent> children = this.getChildren();
        if (children.isEmpty()) {
            return;
        }
        for (UIComponent child : children) {
            preferedSize += child.getPrefWidth(ctx);
        }
        int remaining = ctx.width;
        MergingLineFactory lf = new MergingLineFactory();
        if (preferedSize <= ctx.width) {
            for (UIComponent child : children) {
                child.draw(ctx.withWidth(child.getPrefWidth(ctx.withWidth(remaining))), lf);
                lf.rewind();
            }
        } else {
            int prefWidth = ctx.width / children.size();
            for (UIComponent child : children) {
                child.draw(ctx.withWidth(prefWidth), lf);
                remaining -= lf.getCurrentDrawWidth();
                lf.rewind();
                if (remaining <= 0) {
                    break;
                }
            }
        }
        lineFactory.merge(lf, ctx);
    }

    @Override
    public int getMinWidth(PlayerContext ctx) {
        int width = 0;
        for (UIComponent child : this.getChildren()) {
            width += child.getMinWidth(ctx);
        }
        return width;
    }

    @Override
    public int getPrefWidth(PlayerContext ctx) {
        int width = 0;
        for (UIComponent child : this.getChildren()) {
            width += child.getPrefWidth(ctx);
        }
        return width;
    }

    @Override
    public int getMaxWidth(PlayerContext ctx) {
        int width = 0;
        for (UIComponent child : this.getChildren()) {
            width += child.getMaxWidth(ctx);
        }
        return width;
    }
}
