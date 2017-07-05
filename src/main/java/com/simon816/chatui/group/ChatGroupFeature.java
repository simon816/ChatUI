package com.simon816.chatui.group;

import com.simon816.chatui.AbstractFeature;
import com.simon816.chatui.ActivePlayerChatView;
import com.simon816.chatui.ChatUI;
import com.simon816.chatui.tabs.NewTab;
import com.simon816.chatui.ui.AnchorPaneUI;
import com.simon816.chatui.ui.table.TableModel;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.entity.living.player.Player;

import java.util.List;

public class ChatGroupFeature extends AbstractFeature {

    private static final String PERM_CREATE = ChatUI.ADMIN_PERMISSON + ".group.create";
    private static final String PERM_DELETE = ChatUI.ADMIN_PERMISSON + ".group.delete";

    private final GroupList groupList = new GroupList();

    @Override
    protected void onInit() {
        List<? extends ConfigurationNode> groupList = getConfigRoot().getNode("groups").getChildrenList();
        for (ConfigurationNode node : groupList) {
            this.groupList.addGroup(node.getString());
        }
    }

    @Override
    protected void onNewPlayerView(ActivePlayerChatView view) {
        view.getNewTab().addButton("Chat Groups",
                new NewTab.LaunchTabAction(() -> new ChatGroupTab(this, view, new AnchorPaneUI())));
    }

    @Override
    protected void onViewClose(ActivePlayerChatView view) {
        this.groupList.removePlayer(view.getPlayer());
    }

    TableModel getTable() {
        return this.groupList;
    }

    void addGroup(String name) {
        ChatGroup group = this.groupList.addGroup(name);
        if (group != null) {
            getConfigRoot().getNode("groups").getAppendedNode().setValue(name);
        }
    }

    void removeGroup(ChatGroup group) {
        int index = this.groupList.removeGroup(group);
        getConfigRoot().getNode("groups").removeChild(index);
    }

    boolean canCreateGroup(Player player) {
        return player.hasPermission(PERM_CREATE);
    }

    boolean canDeleteGroup(ChatGroup group, Player player) {
        return player.hasPermission(PERM_DELETE);
    }

}
