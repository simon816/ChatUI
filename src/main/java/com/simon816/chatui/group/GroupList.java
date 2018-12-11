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
        return 2;
    }

    @Override
    public Object getCellValue(int row, int column) {
        return this.groupList.get(row);
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

    public int removeGroup(ChatGroup group) {
        this.groups.remove(group.getName());
        int index = this.groupList.indexOf(group);
        this.groupList.remove(index);
        group.onRemoved();
        return index;
    }

    public void removePlayer(Player player) {
        for (ChatGroup group : this.groupList) {
            group.removePlayer(player);
        }
    }
}
