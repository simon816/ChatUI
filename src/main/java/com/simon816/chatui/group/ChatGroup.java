package com.simon816.chatui.group;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.simon816.chatui.ChatUI;
import com.simon816.chatui.lib.ChatUILib;
import com.simon816.chatui.tabs.Tab;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

class ChatGroup {

    private final String name;
    private final BiMap<UUID, ChatBufferTab> tabs = HashBiMap.create();

    private boolean ignoreClose;
    private List<Text> messages = Lists.newArrayList();

    public ChatGroup(String name) {
        this.name = name;
    }

    public void onMessage(Text formatted) {
        for (Entry<UUID, ChatBufferTab> entry : this.tabs.entrySet()) {
            entry.getValue().appendMessage(formatted);
            ChatUILib.getView(entry.getKey()).update();
        }
        this.messages.add(formatted);
        if (this.messages.size() > 100) {
            this.messages.remove(0);
        }
    }

    public List<Text> getBacklog() {
        return this.messages;
    }

    public String getName() {
        return this.name;
    }

    public void onRemoved() {
        this.ignoreClose = true;
        for (Entry<UUID, ChatBufferTab> entry : this.tabs.entrySet()) {
            ChatUI.getActiveView(entry.getKey()).getWindow().removeTab(entry.getValue());
            ChatUILib.getView(entry.getKey()).update();
        }
        this.tabs.clear();
        this.ignoreClose = false;
    }

    public Tab getTab(Player player) {
        ChatBufferTab tab = this.tabs.get(player.getUniqueId());
        if (tab == null) {
            this.tabs.put(player.getUniqueId(), tab = new ChatBufferTab(this));
        }
        return tab;
    }

    public void removePlayer(Player player) {
        this.tabs.remove(player.getUniqueId());
    }

    public void onTabClosed(ChatBufferTab tab) {
        if (this.ignoreClose) {
            return;
        }
        this.tabs.inverse().remove(tab);
    }
}
