package com.simon816.minecraft.tabchat.tabs;

import com.simon816.minecraft.tabchat.ITextDrawable;
import com.simon816.minecraft.tabchat.PlayerChatView;
import com.simon816.minecraft.tabchat.PlayerContext;
import org.spongepowered.api.text.Text;

public abstract class Tab implements ITextDrawable {

    @Override
    public Text draw(PlayerContext ctx) {
        Text.Builder builder = Text.builder();
        for (int i = 0; i < ctx.height; i++) {
            builder.append(Text.NEW_LINE);
        }
        return builder.build();
    }

    public abstract Text getTitle();

    public void onBlur() {
    }

    public void onFocus() {
    }

    public boolean hasCloseButton() {
        return true;
    }

    public void onClose() {
    }

    public void onTextEntered(PlayerChatView view, Text input) {
    }
}
