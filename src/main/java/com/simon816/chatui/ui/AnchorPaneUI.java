package com.simon816.chatui.ui;

import com.google.common.collect.Lists;
import com.simon816.chatui.PlayerContext;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.List;

public class AnchorPaneUI extends UIPane {

    public static final int ANCHOR_TOP = 1;
    public static final int ANCHOR_RIGHT = 2;
    public static final int ANCHOR_BOTTOM = 4;
    public static final int ANCHOR_LEFT = 8;

    private final Object2IntMap<UIComponent> constraints = new Object2IntOpenHashMap<>();

    public AnchorPaneUI() {
    }

    public AnchorPaneUI(UIComponent... children) {
        addChildren(children);
    }

    @Override
    public void draw(PlayerContext ctx, LineFactory lineFactory) {
        LineFactory top = new LineFactory();
        LineFactory unrestraint = new LineFactory();
        LineFactory bottom = new LineFactory();
        List<UIComponent> remainingUnrestraint = Lists.newArrayList();
        List<UIComponent> children = getChildren();
        for (UIComponent child : children) {
            int constraint = this.constraints.getInt(child);
            int prefHeight = child.getPrefHeight(ctx);
            if (hasFlag(constraint, ANCHOR_BOTTOM)) {
                child.draw(ctx.withHeight(prefHeight), bottom);
            }
            if (hasFlag(constraint, ANCHOR_TOP)) {
                child.draw(ctx.withHeight(prefHeight), top);
            }
            if (constraint == 0) {
                remainingUnrestraint.add(child);
            }
        }
        int remaining = lineFactory.linesRemaining(ctx) - top.currentLineCount() - bottom.currentLineCount();
        for (UIComponent child : remainingUnrestraint) {
            child.draw(ctx.withHeight(remaining), unrestraint);
            remaining = unrestraint.linesRemaining(ctx);
        }
        lineFactory.merge(top, ctx.forceUnicode).merge(unrestraint, ctx.forceUnicode).fillThenMerge(ctx, bottom);
    }

    private static boolean hasFlag(int value, int flag) {
        return (value & flag) == flag;
    }

    public void addWithConstraint(UIComponent component, int constraints) {
        if (hasFlag(constraints, ANCHOR_BOTTOM) && hasFlag(constraints, ANCHOR_TOP)) {
            throw new UnsupportedOperationException("Cannot anchor to TOP *and* BOTTOM yet");
        }
        getChildren().add(component);
        this.constraints.put(component, constraints);
    }

}
