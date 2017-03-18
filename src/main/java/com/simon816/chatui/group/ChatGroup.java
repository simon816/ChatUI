package com.simon816.chatui.group;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.simon816.chatui.ChatUI;
import com.simon816.chatui.tabs.Tab;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

class ChatGroup {

    private final String name;
    private final Set<UUID> players = Sets.newHashSet();
    private final Map<UUID, ChatBufferTab> tabs = Maps.newHashMap();

    private ConfigurationNode configNode;
    private boolean ignoreClose;
    private List<Text> messages = Lists.newArrayList();

    public ChatGroup(String name) {
        this.name = name;
    }

    public void onMessage(Text formatted) {
        for (Entry<UUID, ChatBufferTab> entry : this.tabs.entrySet()) {
            entry.getValue().appendMessage(formatted);
            ChatUI.getView(entry.getKey()).update();
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

    public int getPlayerCount() {
        return this.players.size();
    }

    public void setNode(ConfigurationNode node) {
        this.configNode = node;
        this.players.addAll(node.getList(obj -> UUID.fromString(obj.toString())));
    }

    public void onRemoved() {
        this.ignoreClose = true;
        for (Entry<UUID, ChatBufferTab> entry : this.tabs.entrySet()) {
            ChatUI.getActiveView(entry.getKey()).getWindow().removeTab(entry.getValue());
            ChatUI.getView(entry.getKey()).update();
        }
        this.players.clear();
        this.tabs.clear();
        this.ignoreClose = false;
    }

    public Tab getTab(Player player) {
        this.players.add(player.getUniqueId());
        ChatBufferTab tab = this.tabs.get(player.getUniqueId());
        if (tab == null) {
            this.tabs.put(player.getUniqueId(), tab = new ChatBufferTab(this));
            updateConfig();
        }
        return tab;
    }

    private void updateConfig() {
        Collection<String> newList = Collections2.transform(this.players, UUID::toString);
        this.configNode.setValue(newList);
    }

    public void removePlayer(Player player) {
        this.tabs.remove(player.getUniqueId());
    }

    public void onTabClosed(ChatBufferTab tab) {
        if (this.ignoreClose) {
            return;
        }
        for (Iterator<ChatBufferTab> iterator = this.tabs.values().iterator(); iterator.hasNext();) {
            if (iterator.next() == tab) {
                iterator.remove();
                break;
            }
        }
    }
}
