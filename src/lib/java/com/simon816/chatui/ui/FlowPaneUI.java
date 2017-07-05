package com.simon816.chatui.ui;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.Lists;
import com.simon816.chatui.lib.PlayerContext;

import java.util.Iterator;
import java.util.List;

public class FlowPaneUI extends UIPane {

    public static final int WRAP_HORIZONALLY = 1;
    public static final int WRAP_VERTICALLY = 2;

    private int mode = WRAP_HORIZONALLY;

    public FlowPaneUI() {
    }

    public FlowPaneUI(int mode) {
        setWrapMode(mode);
    }

    public void setWrapMode(int mode) {
        checkArgument(mode == WRAP_HORIZONALLY || mode == WRAP_VERTICALLY, "Invalid mode");
        this.mode = mode;
    }

    @Override
    public void draw(PlayerContext ctx, LineFactory lineFactory) {
        List<UIComponent> childrenToAdd = Lists.newArrayList(getChildren());
        UIPane masterPane;
        if (this.mode == WRAP_HORIZONALLY) {
            masterPane = new VBoxUI();
        } else if (this.mode == WRAP_VERTICALLY) {
            masterPane = new HBoxUI();
        } else {
            return;
        }
        while (!childrenToAdd.isEmpty()) {
            UIPane prefPane = this.mode == WRAP_HORIZONALLY ? new HBoxUI() : new VBoxUI();

            for (Iterator<UIComponent> iterator = childrenToAdd.iterator(); iterator.hasNext(); iterator.remove()) {
                prefPane.getChildren().add(iterator.next());
                if ((this.mode == WRAP_HORIZONALLY && prefPane.getPrefWidth(ctx) > ctx.width)
                        || (this.mode == WRAP_VERTICALLY && prefPane.getPrefHeight(ctx) > ctx.height)) {
                    prefPane.getChildren().remove(prefPane.getChildren().size() - 1);
                    break;
                }
            }
            if (prefPane.getChildren().isEmpty()) {
                break; // Could not fit anymore on
            }
            masterPane.getChildren().add(prefPane);
        }
        masterPane.draw(ctx, lineFactory);
    }

}
