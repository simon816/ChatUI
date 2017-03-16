package com.simon816.chatui;

import com.google.common.collect.Lists;
import com.simon816.chatui.ui.AnchorPaneUI;
import com.simon816.chatui.ui.LineFactory;
import com.simon816.chatui.ui.UIComponent;
import com.simon816.chatui.ui.UIPane;
import com.simon816.chatui.ui.table.DefaultTableRenderer;
import com.simon816.chatui.ui.table.TableModel;
import com.simon816.chatui.ui.table.TableRenderer;
import com.simon816.chatui.ui.table.TableScrollHelper;
import com.simon816.chatui.ui.table.TableUI;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextFormat;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.ban.Ban;

import java.util.Collection;
import java.util.List;
import java.util.function.BooleanSupplier;

import javax.swing.SwingConstants;

public class PlayerList {

    private final AnchorPaneUI root;
    final List<Addon> addons = Lists.newArrayList();

    public static interface Addon {

        Text getColumnValue(Player playerInList);

    }

    public PlayerList(Player player) {
        this.root = new AnchorPaneUI();
        TableModel model = createTableModel();
        TableScrollHelper scroll = new TableScrollHelper(model);
        UIComponent scrollButtons = new UIComponent() {

            @Override
            public void draw(PlayerContext ctx, LineFactory lineFactory) {
                Text.Builder builder = Text.builder();
                builder.append(Text.of(clickAction(scroll::scrollUp),
                        scroll.canScrollUp() ? TextColors.WHITE : TextColors.DARK_GRAY, "[Scroll Up] "));
                builder.append(Text.of(clickAction(scroll::scrollDown),
                        scroll.canScrollDown() ? TextColors.WHITE : TextColors.DARK_GRAY, "[Scroll Down] "));
                lineFactory.appendNewLine(builder.build(), ctx.forceUnicode);
            }
            @Override
            public int getPrefHeight(PlayerContext ctx) {
                return 1;
            }
        };
        TableUI table = new TableUI(model, createTableRenderer(scroll));
        this.root.getChildren().add(table);
        this.root.addWithConstraint(scrollButtons, AnchorPaneUI.ANCHOR_BOTTOM | AnchorPaneUI.ANCHOR_LEFT | AnchorPaneUI.ANCHOR_RIGHT);
        this.addDefaultAddons(player);
    }

    public UIPane getRoot() {
        return this.root;
    }

    private TableRenderer createTableRenderer(TableScrollHelper scroll) {
        return new DefaultTableRenderer() {

            private final TableViewport viewport = scroll.createViewport();

            @Override
            public TableViewport getViewport() {
                return this.viewport;
            }

            @Override
            protected int getCellAlignment(int row, int column) {
                if (column == 0) {
                    return SwingConstants.RIGHT;
                }
                return SwingConstants.CENTER;
            }

        };
    }

    private TableModel createTableModel() {
        return new TableModel() {

            private List<Player> pList() {
                Collection<Player> collection = Sponge.getServer().getOnlinePlayers();
                if (collection instanceof List) {
                    return (List<Player>) collection;
                }
                return Lists.newArrayList(collection);
            }

            @Override
            public int getRowCount() {
                return pList().size();
            }

            @Override
            public int getColumnCount() {
                return 1 + PlayerList.this.addons.size();
            }

            @Override
            public Object getCellValue(int row, int column) {
                if (column == 0) {
                    return pList().get(row).getDisplayNameData().displayName().get();
                }
                return PlayerList.this.addons.get(column - 1).getColumnValue(pList().get(row));
            }
        };
    }

    public ClickAction<?> clickAction(Runnable action) {
        return clickAction(() -> {
            action.run();
            return true;
        });
    }

    public ClickAction<?> clickAction(BooleanSupplier action) {
        return ChatUI.execClick(src -> {
//            if (ChatUI.getView(src).getWindow().getActiveTab() != this) {
//                return;
//            }
            if (action.getAsBoolean()) {
                ChatUI.getView(src).update();
            }
        });
    }

    public void addAddon(Addon addon) {
        this.addons.add(addon);
    }

    private void addDefaultAddons(Player player) {
        if (player.hasPermission(ChatUI.ADMIN_PERMISSON)) {
            TextFormat link = TextFormat.of(TextColors.BLUE, TextStyles.UNDERLINE);
            addAddon(listPlayer -> Text.builder("Kick").format(link).onClick(clickAction(() -> listPlayer.kick())).build());
            addAddon(listPlayer -> Text.builder("Ban").format(link)
                    .onClick(clickAction(() -> Sponge.getServiceManager().provideUnchecked(BanService.class).addBan(Ban.of(listPlayer.getProfile()))))
                    .build());
        }
    }

}
