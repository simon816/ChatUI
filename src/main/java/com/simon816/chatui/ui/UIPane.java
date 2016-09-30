package com.simon816.chatui.ui;

import com.google.common.collect.Lists;
import com.simon816.chatui.PlayerContext;

import java.util.List;

public abstract class UIPane implements UIComponent {

    private final List<UIComponent> children = Lists.newArrayList();

    public List<UIComponent> getChildren() {
        return this.children;
    }

    public void addChildren(UIComponent... components) {
        for (UIComponent component : components) {
            this.children.add(component);
        }
    }

    @Override
    public int getMinWidth(PlayerContext ctx) {
        int width = 0;
        for (UIComponent child : this.children) {
            width = Math.max(width, child.getMinWidth(ctx));
        }
        return width;
    }

    @Override
    public int getPrefWidth(PlayerContext ctx) {
        int width = 0;
        for (UIComponent child : this.children) {
            width = Math.max(width, child.getPrefWidth(ctx));
        }
        return width;
    }

    @Override
    public int getMaxWidth(PlayerContext ctx) {
        int width = 0;
        for (UIComponent child : this.children) {
            width = Math.max(width, child.getPrefWidth(ctx));
        }
        return width;
    }

    @Override
    public int getMinHeight(PlayerContext ctx) {
        int height = 0;
        for (UIComponent child : this.children) {
            height = Math.max(height, child.getMinHeight(ctx));
        }
        return height;
    }

    @Override
    public int getPrefHeight(PlayerContext ctx) {
        int height = 0;
        for (UIComponent child : this.children) {
            height = Math.max(height, child.getPrefHeight(ctx));
        }
        return height;
    }

    @Override
    public int getMaxHeight(PlayerContext ctx) {
        int height = 0;
        for (UIComponent child : this.children) {
            height = Math.max(height, child.getMaxHeight(ctx));
        }
        return height;
    }
}
