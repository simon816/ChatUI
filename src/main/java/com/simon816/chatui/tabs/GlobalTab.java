package com.simon816.chatui.tabs;

import org.spongepowered.api.text.Text;

public final class GlobalTab extends TextBufferTab {

    @Override
    public Text getTitle() {
        return appendUnreadLabel(Text.builder("Global")).build();
    }

    @Override
    public boolean hasCloseButton() {
        return false;
    }

}
