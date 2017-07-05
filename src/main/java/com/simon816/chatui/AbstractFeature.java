package com.simon816.chatui;

import ninja.leaping.configurate.ConfigurationNode;

public abstract class AbstractFeature {

    private ConfigurationNode node;

    final void setConfigRoot(ConfigurationNode node) {
        this.node = node;
    }

    final protected ConfigurationNode getConfigRoot() {
        return this.node;
    }

    protected void onInit() {
    }

    protected void onNewPlayerView(ActivePlayerChatView view) {
    }

    protected void onViewClose(ActivePlayerChatView view) {
    }

}
