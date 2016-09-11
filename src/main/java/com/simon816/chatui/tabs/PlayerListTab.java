package com.simon816.chatui.tabs;

import com.google.common.collect.Lists;
import com.simon816.chatui.ChatUI;
import com.simon816.chatui.PlayerContext;
import com.simon816.chatui.ui.Frame;
import com.simon816.chatui.ui.UIComponent;
import com.simon816.chatui.ui.table.DefaultTableRenderer;
import com.simon816.chatui.ui.table.TableModel;
import com.simon816.chatui.ui.table.TableRenderer;
import com.simon816.chatui.ui.table.TableScrollHelper;
import com.simon816.chatui.ui.table.TableUI;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Text.Builder;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextFormat;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.ban.Ban;

import java.util.Collection;
import java.util.List;
import java.util.function.BooleanSupplier;

import javax.swing.SwingConstants;

public class PlayerListTab extends Tab {

    private final Frame frame;
    final List<Addon> addons = Lists.newArrayList();

    public static interface Addon {

        Text getColumnValue(Player playerInList);

    }

    public PlayerListTab(Player player) {
        this.frame = new Frame();
        TableModel model = createTableModel();
        TableScrollHelper scroll = new TableScrollHelper(model);
        this.frame.addComponent(new UIComponent() {

            @Override
            public int draw(Builder builder, PlayerContext ctx) {
                builder.append(Text.of(clickAction(scroll::scrollUp),
                        scroll.canScrollUp() ? TextColors.WHITE : TextColors.DARK_GRAY, "[Scroll Up] "));
                builder.append(Text.of(clickAction(scroll::scrollDown),
                        scroll.canScrollDown() ? TextColors.WHITE : TextColors.DARK_GRAY, "[Scroll Down] "));
                builder.append(Text.NEW_LINE);
                return 1;
            }
        }, Frame.STICK_BOTTOM);
        this.frame.addComponent(new TableUI(model, createTableRenderer(scroll)));
        this.addDefaultAddons(player);
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
                return 1 + PlayerListTab.this.addons.size();
            }

            @Override
            public Object getCellValue(int row, int column) {
                if (column == 0) {
                    return pList().get(row).getDisplayNameData().displayName().get();
                }
                return PlayerListTab.this.addons.get(column - 1).getColumnValue(pList().get(row));
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
        return TextActions.executeCallback(src -> {
            if (ChatUI.getView(src).getWindow().getActiveTab() != this) {
                return;
            }
            if (action.getAsBoolean()) {
                ChatUI.getView(src).update();
            }
        });
    }

    public void addAddon(Addon addon) {
        this.addons.add(addon);
    }

    @Override
    public Text draw(PlayerContext ctx) {
        return this.frame.draw(ctx);
    }

    @Override
    public Text getTitle() {
        return Text.of("Player List");
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
