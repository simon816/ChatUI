package com.simon816.chatui.group;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.simon816.chatui.ui.table.TableModel;
import org.spongepowered.api.entity.living.player.Player;

import java.util.List;
import java.util.Map;

class GroupList implements TableModel {

    private final Map<String, ChatGroup> groups = Maps.newHashMap();
    private final List<ChatGroup> groupList = Lists.newArrayList();

    GroupList() {
    }

    @Override
    public int getRowCount() {
        return this.groups.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public Object getCellValue(int row, int column) {
        ChatGroup group = this.groupList.get(row);
        if (column == 0) {
            return group;
        } else if (column == 1) {
            return Integer.toString(group.getPlayerCount());
        } else if (column == 2) {
            return group;
        }
        return null;
    }

    public ChatGroup addGroup(String name) {
        if (this.groups.containsKey(name)) {
            return null;
        }
        ChatGroup group = new ChatGroup(name);
        this.groups.put(name, group);
        this.groupList.add(group);
        return group;
    }

    public void removeGroup(ChatGroup group) {
        this.groups.remove(group.getName());
        this.groupList.remove(group);
        group.onRemoved();
    }

    public void removePlayer(Player player) {
        for (ChatGroup group : this.groupList) {
            group.removePlayer(player);
        }
    }
}