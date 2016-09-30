package com.simon816.chatui.privmsg;

import com.google.common.collect.Maps;
import com.simon816.chatui.AbstractFeature;
import com.simon816.chatui.PlayerChatView;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.util.Map;
import java.util.UUID;

public class PrivateMessageFeature extends AbstractFeature {

    static final Map<UUID, PlayerPrivateView> privateView = Maps.newHashMap();

    @Override
    protected void onNewPlayerView(PlayerChatView view) {
        privateView.put(view.getPlayer().getUniqueId(), new PlayerPrivateView(view));
        view.getPlayerList().addAddon(player -> {
            Text.Builder builder = Text.builder("Message");
            if (player != view.getPlayer()) {
                builder.onClick(view.getPlayerList()
                        .clickAction(() -> newPrivateMessage(view.getPlayer(), player)))
                        .color(TextColors.BLUE).style(TextStyles.UNDERLINE);
            } else {
                builder.color(TextColors.GRAY);
            }
            return builder.build();
        });
    }

    @Override
    protected void onViewClose(PlayerChatView view) {
        PlayerPrivateView privView = privateView.remove(view.getPlayer().getUniqueId());
        for (PrivateMessageTab chat : privView.privateChatTabs.values()) {
            PrivateMessageTab otherTab = chat.otherPlayerView.privateChatTabs.remove(view.getPlayer().getUniqueId());
            if (otherTab != null) {
                chat.otherPlayerView.view.getWindow().removeTab(otherTab);
                chat.otherPlayerView.view.update();
            }
        }
    }

    private void newPrivateMessage(Player sourcePlayer, Player destPlayer) {
        privateView.get(sourcePlayer.getUniqueId()).createPrivateMessageTab(destPlayer, true);
    }

    static class PlayerPrivateView {

        final Map<UUID, PrivateMessageTab> privateChatTabs = Maps.newHashMap();
        final PlayerChatView view;

        PlayerPrivateView(PlayerChatView view) {
            this.view = view;
        }

        PrivateMessageTab createPrivateMessageTab(Player other, boolean switchTab) {
            PrivateMessageTab tab = this.privateChatTabs.get(other.getUniqueId());
            if (tab == null) {
                this.privateChatTabs.put(other.getUniqueId(), tab = new PrivateMessageTab(this, privateView.get(other.getUniqueId())));
                this.view.getWindow().addTab(tab, switchTab);
            } else {
                if (switchTab) {
                    this.view.getWindow().setTab(tab);
                }
            }
            return tab;
        }

        void removeTab(UUID otherPlayerUuid) {
            this.privateChatTabs.remove(otherPlayerUuid);
        }
    }
}
