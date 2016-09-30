package com.simon816.chatui.tabs.config;

import com.simon816.chatui.ChatUI;
import com.simon816.chatui.PlayerChatView;
import com.simon816.chatui.PlayerContext;
import com.simon816.chatui.tabs.Tab;
import com.simon816.chatui.ui.AnchorPaneUI;
import com.simon816.chatui.ui.LineFactory;
import com.simon816.chatui.ui.UIComponent;
import com.simon816.chatui.ui.table.TableScrollHelper;
import com.simon816.chatui.ui.table.TableUI;
import com.simon816.chatui.util.TextUtils;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.util.function.BooleanSupplier;

public class ConfigEditTab extends Tab {

    boolean isTabActive(CommandSource src) {
        return ChatUI.getView(src).getWindow().getActiveTab() == this;
    }

    ClickAction<?> clickAction(Runnable action) {
        return clickAction(() -> {
            action.run();
            return true;
        });
    }

    ClickAction<?> clickAction(BooleanSupplier action) {
        return TextActions.executeCallback(src -> {
            if (!isTabActive(src)) {
                return;
            }
            if (action.getAsBoolean()) {
                ChatUI.getView(src).update();
            }
        });
    }

    private class ButtonBar implements UIComponent {

        public ButtonBar() {
        }

        @Override
        public void draw(PlayerContext ctx, LineFactory lineFactory) {
            Text.Builder builder = Text.builder();
            if (ConfigEditTab.this.control.getActiveEntry() == null) {
                builder.append(Text.of(clickAction(ConfigEditTab.this.scroll::scrollUp),
                        ConfigEditTab.this.scroll.canScrollUp() ? TextColors.WHITE : TextColors.DARK_GRAY, "[Scroll Up] "));
                builder.append(Text.of(clickAction(ConfigEditTab.this.scroll::scrollDown),
                        ConfigEditTab.this.scroll.canScrollDown() ? TextColors.WHITE : TextColors.DARK_GRAY, "[Scroll Down] "));
                if (ConfigEditTab.this.control.options.canAdd && !ConfigEditTab.this.control.inDeleteMode()) {
                    builder.append(Text.of(clickAction(() -> {
                        ConfigEditTab.this.nodeBuilder = NodeBuilder.forNode(ConfigEditTab.this.control.getNode(), ConfigEditTab.this);
                    }), TextColors.GREEN, "[Add]"));
                }
            } else {
                builder.append(Text.of(clickAction(ConfigEditTab.this.control::closeActiveEntry), TextColors.RED, "[Close]"));
            }
            if (ConfigEditTab.this.control.options.canDelete) {
                builder.append(Text.of(clickAction(ConfigEditTab.this.control::setDeleteModeOrDeleteNode), TextColors.RED, " [Delete]"));
            }
            lineFactory.appendNewLine(builder.build());
        }

    }

    private class Breadcrumb implements UIComponent {

        public Breadcrumb() {
        }

        @Override
        public void draw(PlayerContext ctx, LineFactory lineFactory) {
            Text.Builder builder = Text.builder();
            Object[] path = ConfigEditTab.this.control.getPath();
            for (int i = 0; i < path.length; i++) {
                Text.Builder part = Text.builder(path[i].toString());
                final int distance = path.length - i - 1;
                part.color(TextColors.BLUE).onClick(clickAction(() -> {
                    int dist = distance;
                    ConfigurationNode newNode = ConfigEditTab.this.control.getNode();
                    while (dist-- > 0) {
                        newNode = newNode.getParent();
                    }
                    ConfigEditTab.this.control.setNode(newNode);
                }));
                builder.append(part.build());
                if (i < path.length - 1) {
                    builder.append(Text.of("->"));
                }
            }
            lineFactory.addAll(TextUtils.splitLines(builder.build(), ctx.width));
        }

    }

    public static class Options {

        public static final Options DEFAULTS = new Options(true, true, true, null);
        public final boolean canAdd;
        public final boolean canDelete;
        public final boolean canEdit;
        public final String rootNodeName;

        public Options(boolean add, boolean edit, boolean delete, String rootName) {
            this.canAdd = add;
            this.canEdit = edit;
            this.canDelete = delete;
            this.rootNodeName = rootName;
        }
    }

    public static abstract class ActionHandler {

        public static final ActionHandler NONE = new ActionHandler() {
        };

        public void onNodeChanged(ConfigurationNode node) {
        }

        public void onNodeRemoved(Object key) {
        }

        public void onNodeAdded(ConfigurationNode node) {
        }
    }

    private final AnchorPaneUI pane;
    final ConfigTabControl control;
    private final Text title;
    final TableScrollHelper scroll;

    NodeBuilder nodeBuilder;

    public ConfigEditTab(ConfigurationNode node, Text title) {
        this(node, title, Options.DEFAULTS, ActionHandler.NONE);
    }

    public ConfigEditTab(ConfigurationNode rootNode, Text title, Options options, ActionHandler handler) {
        this.title = title;
        this.control = new ConfigTabControl(this, rootNode, options, handler);
        ConfigTableModel model = this.control.createTableModel();
        this.scroll = new TableScrollHelper(model);
        this.pane = new AnchorPaneUI();
        this.pane.addWithConstraint(new Breadcrumb(), AnchorPaneUI.ANCHOR_TOP);
        this.pane.getChildren().add(new TableUI(model, this.control.createTableRenderer()));
        this.pane.addWithConstraint(new ButtonBar(), AnchorPaneUI.ANCHOR_BOTTOM);
    }

    @Override
    public Text getTitle() {
        return this.title;
    }

    @Override
    public Text draw(PlayerContext ctx) {
        if (this.nodeBuilder != null) {
            return this.nodeBuilder.draw(ctx);
        }
        return this.pane.draw(ctx);
    }

    @Override
    public void onTextEntered(PlayerChatView view, Text input) {
        if (this.nodeBuilder != null) {
            this.nodeBuilder.recieveInput(view, input.toPlain());
            return;
        }
        if (this.control.getActiveEntry() == null) {
            return;
        }
        ConfigurationNode node = this.control.getNode().getNode(this.control.getActiveEntry().key);
        Object value = this.control.getActiveEntry().value.onSetValue(input.toPlain());
        node.setValue(value);
        this.control.handler.onNodeChanged(node);
        this.control.closeActiveEntry();
        view.update();
    }

}
