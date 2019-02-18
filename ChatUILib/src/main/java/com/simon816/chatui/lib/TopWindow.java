package com.simon816.chatui.lib;

import org.spongepowered.api.text.Text;

public interface TopWindow extends ITextDrawable {

    void onClose();

    void onTextInput(PlayerChatView view, Text input);

    boolean onCommand(PlayerChatView view, String[] args);

}
