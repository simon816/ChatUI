package com.simon816.minecraft.tabchat.ui;

import com.simon816.minecraft.tabchat.ITextDrawable;
import com.simon816.minecraft.tabchat.PlayerContext;
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
