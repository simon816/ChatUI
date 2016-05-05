package com.simon816.minecraft.tabchat.tabs;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

public final class GlobalTab extends TextBufferTab {

    public GlobalTab(Player player) {
        super(player);
    }

    @Override
    public Text getTitle() {
        return appendUnreadLabel(Text.builder("Global")).build();
    }

    @Override
    public boolean hasCloseButton() {
        return false;
    }

}
