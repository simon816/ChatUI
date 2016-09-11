package com.simon816.chatui.ui;

import com.simon816.chatui.ITextDrawable;
import com.simon816.chatui.PlayerContext;
import org.spongepowered.api.text.Text;

public interface UIComponent extends ITextDrawable {

    @Override
    default Text draw(PlayerContext ctx) {
        Text.Builder builder = Text.builder();
        draw(builder, ctx);
        return builder.build();
    }

    int draw(Text.Builder builder, PlayerContext ctx);

}
