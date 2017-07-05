package com.simon816.chatui.group;

import com.simon816.chatui.lib.PlayerChatView;
import com.simon816.chatui.lib.PlayerContext;
import com.simon816.chatui.tabs.Tab;
import com.simon816.chatui.ui.AnchorPaneUI;
import com.simon816.chatui.ui.LineFactory;
import com.simon816.chatui.ui.UIComponent;
import com.simon816.chatui.ui.table.TableUI;
import com.simon816.chatui.util.Utils;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

class ChatGroupTab extends Tab {

    boolean createGroup;
    final ChatGroupFeature feature;

    public ChatGroupTab(ChatGroupFeature feature, PlayerChatView view, AnchorPaneUI root) {
        super(Text.of("Chat Groups"), root);
        this.feature = feature;
        root.addChildren(new TableUI(feature.getTable(), new GroupListRenderer(view, feature)));
        root.addWithConstraint(new Toolbar(), AnchorPaneUI.ANCHOR_BOTTOM);
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

        Toolbar() {
        }

        @Override
        public void draw(PlayerContext ctx, LineFactory lineFactory) {
            Text.Builder builder = Text.builder();
            LiteralText.Builder createButton = Text.builder("[Create Group]");
            if (ChatGroupTab.this.feature.canCreateGroup(ctx.getPlayer())) {
                createButton.onClick(Utils.execClick(view -> {
                    ChatGroupTab.this.createGroup = !ChatGroupTab.this.createGroup;
                    view.update();
                }));
                createButton.color(TextColors.GREEN);
                if (ChatGroupTab.this.createGroup) {
                    lineFactory.appendNewLine(Text.of("Type group name in chat:"), ctx.forceUnicode);
                    createButton.content("[Cancel]");
                    createButton.color(TextColors.RED);
                }
            } else {
                createButton.onHover(TextActions.showText(Text.of("You do not have permission to create groups")));
                createButton.color(TextColors.GRAY);
            }
            builder.append(createButton.build());
            lineFactory.appendNewLine(builder.build(), ctx.forceUnicode);
        }
    }

}
