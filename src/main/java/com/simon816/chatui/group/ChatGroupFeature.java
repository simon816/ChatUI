package com.simon816.chatui.group;

import com.simon816.chatui.AbstractFeature;
import com.simon816.chatui.ActivePlayerChatView;
import com.simon816.chatui.ChatUI;
import com.simon816.chatui.PlayerChatView;
import com.simon816.chatui.PlayerContext;
import com.simon816.chatui.tabs.NewTab;
import com.simon816.chatui.tabs.Tab;
import com.simon816.chatui.ui.AnchorPaneUI;
import com.simon816.chatui.ui.LineFactory;
import com.simon816.chatui.ui.UIComponent;
import com.simon816.chatui.ui.table.DefaultColumnRenderer;
import com.simon816.chatui.ui.table.DefaultTableRenderer;
import com.simon816.chatui.ui.table.TableColumnRenderer;
import com.simon816.chatui.ui.table.TableModel;
import com.simon816.chatui.ui.table.TableUI;
import com.simon816.chatui.util.TextUtils;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ChatGroupFeature extends AbstractFeature {

    private final GroupList groupList = new GroupList();

    @Override
    protected void onInit() {
        Map<Object, ? extends ConfigurationNode> groupMap = getConfigRoot().getNode("groups").getChildrenMap();
        for (Entry<Object, ? extends ConfigurationNode> entry : groupMap.entrySet()) {
            ChatGroup group = this.groupList.addGroup(entry.getKey().toString());
            group.setNode(entry.getValue());
        }
    }

    public TableModel table() {
        return this.groupList;
    }

    public void addGroup(String name) {
        ChatGroup group = this.groupList.addGroup(name);
        if (group != null) {
            group.setNode(getConfigRoot().getNode("groups", name).setValue(Collections.emptyList()));
        }
    }

    void removeGroup(ChatGroup group) {
        this.groupList.removeGroup(group);
    }

    @Override
    protected void onNewPlayerView(PlayerChatView view) {
        if (!(view instanceof ActivePlayerChatView)) {
            return;
        }
        ((ActivePlayerChatView) view).getNewTab().addButton("Chat Groups",
                new NewTab.LaunchTabAction(() -> new ChatGroupTab(this, view, new AnchorPaneUI())));
    }

    @Override
    protected void onViewClose(PlayerChatView view) {
        this.groupList.removePlayer(view.getPlayer());
    }

    private static class ChatGroupTab extends Tab {

        boolean createGroup;
        private final ChatGroupFeature feature;

        public ChatGroupTab(ChatGroupFeature feature, PlayerChatView view, AnchorPaneUI root) {
            super(Text.of("Chat Groups"), root);
            this.feature = feature;
            root.addChildren(new TableUI(feature.table(), new GroupListRenderer(view, feature)));
            root.addWithConstraint(new Toolbar(view), AnchorPaneUI.ANCHOR_BOTTOM);
        }

        @Override
        public void onTextInput(PlayerChatView view, Text input) {
            if (this.createGroup) {
                this.createGroup = false;
                this.feature.addGroup(input.toPlain());
                view.update();
            }
        }

        private class Toolbar implements UIComponent {

            private PlayerChatView view;

            Toolbar(PlayerChatView view) {
                this.view = view;
            }

            @Override
            public void draw(PlayerContext ctx, LineFactory lineFactory) {
                Text.Builder builder = Text.builder();
                LiteralText.Builder createButton = Text.builder("[Create Group]");
                if (this.view.getPlayer().hasPermission(ChatUI.ADMIN_PERMISSON)) {
                    createButton.onClick(ChatUI.execClick(src -> {
                        ChatGroupTab.this.createGroup = !ChatGroupTab.this.createGroup;
                        ChatUI.getView(src).update();
                    }));
                    if (ChatGroupTab.this.createGroup) {
                        createButton.content("[Cancel]");
                    }
                } else {
                    createButton.color(TextColors.GRAY);
                }
                builder.append(createButton.build());
                lineFactory.appendNewLine(builder.build(), ctx.forceUnicode);
            }
        }

    }

    private static class GroupListRenderer extends DefaultTableRenderer {

        ChatGroupFeature feature;
        PlayerChatView view;

        GroupListRenderer(PlayerChatView view, ChatGroupFeature feature) {
            this.view = view;
            this.feature = feature;
        }

        @Override
        public TableColumnRenderer createColumnRenderer(int columnIndex) {
            if (columnIndex == 0) {
                return new DefaultColumnRenderer() {

                    @Override
                    public List<Text> renderCell(Object value, int row, int tableWidth, boolean forceUnicode) {
                        ChatGroup group = (ChatGroup) value;
                        return TextUtils.splitLines(Text.of(ChatUI.execClick(src -> {
                            ChatUI.getActiveView(src).getWindow().addTab(group.getTab((Player) src), true);
                            ChatUI.getView(src).update();
                        }), group.getName()), getPrefWidth(), forceUnicode);
                    }
                };
            } else if (columnIndex == 2) {
                return new DefaultColumnRenderer() {

                    @Override
                    public List<Text> renderCell(Object value, int row, int tableWidth, boolean forceUnicode) {
                        ChatGroup group = (ChatGroup) value;
                        if (GroupListRenderer.this.view.getPlayer().hasPermission(ChatUI.ADMIN_PERMISSON)) {
                            return TextUtils.splitLines(Text.of(ChatUI.execClick(src -> {
                                GroupListRenderer.this.feature.removeGroup(group);
                                ChatUI.getView(src).update();
                            }), TextColors.RED, "X"), getPrefWidth(), forceUnicode);
                        } else {
                            return Collections.singletonList(Text.of(TextColors.GRAY, "X"));
                        }
                    }
                };
            }
            return super.createColumnRenderer(columnIndex);
        }
    }

}
