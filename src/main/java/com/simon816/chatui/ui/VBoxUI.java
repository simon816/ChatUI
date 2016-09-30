package com.simon816.chatui.ui;

import com.simon816.chatui.PlayerContext;

import java.util.List;

public class VBoxUI extends UIPane {

    @Override
    public void draw(PlayerContext ctx, LineFactory lineFactory) {
        int preferedSize = 0;
        List<UIComponent> children = this.getChildren();
        for (UIComponent child : children) {
            preferedSize += child.getPrefHeight(ctx);
        }
        int remaining = ctx.height;
        if (preferedSize <= ctx.height) {
            for (UIComponent child : children) {
                child.draw(ctx.withHeight(child.getPrefHeight(ctx.withHeight(remaining))), lineFactory);
            }
        } else {
            for (UIComponent child : children) {
                child.draw(ctx.withHeight(child.getMinHeight(ctx.withHeight(remaining))), lineFactory);
                remaining -= child.getMinHeight(ctx);
                if (remaining <= 0) {
                    break;
                }
            }
        }
    }

    @Override
    public int getMinHeight(PlayerContext ctx) {
        int height = 0;
        for (UIComponent child : this.getChildren()) {
            height += child.getMinHeight(ctx);
        }
        return height;
    }

    @Override
    public int getPrefHeight(PlayerContext ctx) {
        int height = 0;
        for (UIComponent child : this.getChildren()) {
            height += child.getPrefHeight(ctx);
        }
        return height;
    }

    @Override
    public int getMaxHeight(PlayerContext ctx) {
        int height = 0;
        for (UIComponent child : this.getChildren()) {
            height += child.getMaxHeight(ctx);
        }
        return height;
    }
}
