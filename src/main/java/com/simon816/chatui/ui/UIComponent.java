package com.simon816.chatui.ui;

import com.simon816.chatui.ITextDrawable;
import com.simon816.chatui.PlayerContext;
import org.spongepowered.api.text.Text;

public interface UIComponent extends ITextDrawable {

    @Override
    default Text draw(PlayerContext ctx) {
        LineFactory factory = new LineFactory();
        draw(ctx, factory);
        factory.fillBlank(ctx);
        return Text.builder().append(Text.joinWith(Text.NEW_LINE, factory.getLines())).build();
    }

    void draw(PlayerContext ctx, LineFactory lineFactory);

    default int getPrefHeight(PlayerContext ctx) {
        return ctx.height;
    }

    default int getMinHeight(PlayerContext ctx) {
        return getPrefHeight(ctx);
    }

    default int getMaxHeight(PlayerContext ctx) {
        return getPrefHeight(ctx);
    }

    default int getPrefWidth(PlayerContext ctx) {
        return ctx.width;
    }

    default int getMinWidth(PlayerContext ctx) {
        return getPrefWidth(ctx);
    }

    default int getMaxWidth(PlayerContext ctx) {
        return getPrefWidth(ctx);
    }

}
