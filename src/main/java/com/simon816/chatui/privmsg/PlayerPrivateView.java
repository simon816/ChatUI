package com.simon816.chatui.privmsg;

import com.google.common.collect.Maps;
import com.simon816.chatui.ActivePlayerChatView;

import java.util.Map;
import java.util.UUID;

class PlayerPrivateView {

    private final Map<UUID, PrivateMessageTab> privateChatTabs = Maps.newHashMap();
    private final ActivePlayerChatView view;

    PlayerPrivateView(ActivePlayerChatView view) {
        this.view = view;
    }

    public UUID getPlayerId() {
        return this.view.getPlayer().getUniqueId();
    }

    public String getPlayerName() {
        return this.view.getPlayer().getName();
    }

    void update() {
        this.view.update();
    }

    PrivateMessageTab createPrivateMessageTab(PlayerPrivateView other, boolean switchTab) {
        PrivateMessageTab tab = this.privateChatTabs.get(other.getPlayerId());
        if (tab == null) {
            this.privateChatTabs.put(other.getPlayerId(), tab = new PrivateMessageTab(this, other));
            this.view.getWindow().addTab(tab, switchTab);
        } else {
            if (switchTab) {
                this.view.getWindow().setTab(tab);
            }
        }
        return tab;
    }

    void onClose() {
        for (PrivateMessageTab tab : this.privateChatTabs.values()) {
            tab.getOther().removeConversation(getPlayerId(), true);
        }
        this.privateChatTabs.clear();
    }

    void removeConversation(UUID playerId, boolean updateWindow) {
        PrivateMessageTab tab = this.privateChatTabs.remove(playerId);
        if (updateWindow && tab != null) {
            this.view.getWindow().removeTab(tab);
            this.view.update();
        }
    }
}
