package com.simon816.chatui.privmsg;

import com.google.common.collect.Maps;
import com.simon816.chatui.AbstractFeature;
import com.simon816.chatui.ActivePlayerChatView;
import com.simon816.chatui.PlayerChatView;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.util.Map;
import java.util.UUID;

public class PrivateMessageFeature extends AbstractFeature {

    private final Map<UUID, PlayerPrivateView> privateView = Maps.newHashMap();

    @Override
    protected void onNewPlayerView(PlayerChatView view) {
        if (!(view instanceof ActivePlayerChatView)) {
            return;
        }
        ActivePlayerChatView activeView = (ActivePlayerChatView) view;
        this.privateView.put(view.getPlayer().getUniqueId(), new PlayerPrivateView(activeView));
        installMessageButton(activeView);
    }

    private void installMessageButton(ActivePlayerChatView view) {
        UUID playerId = view.getPlayer().getUniqueId();
        view.getPlayerList().addAddon(player -> {
            UUID otherId = player.getUniqueId();
            Text.Builder builder = Text.builder("Message");
            if (!otherId.equals(playerId)) {
                if (this.privateView.containsKey(otherId)) {
                    builder.onClick(view.getPlayerList()
                            .clickAction(() -> newPrivateMessage(playerId, otherId)))
                            .onHover(TextActions.showText(Text.of("Send a private message")))
                            .color(TextColors.BLUE).style(TextStyles.UNDERLINE);
                } else {
                    builder.color(TextColors.GRAY)
                            .onHover(TextActions.showText(Text.of("This player doesn't have Chat UI enabled")));
                }

            } else {
                builder.color(TextColors.GRAY)
                        .onHover(TextActions.showText(Text.of("Cannot send message to yourself")));
            }
            return builder.build();
        });
    }

    @Override
    protected void onViewClose(PlayerChatView view) {
        PlayerPrivateView privView = this.privateView.remove(view.getPlayer().getUniqueId());
        if (privView == null) {
            return;
        }
        privView.onClose();
    }

    private PlayerPrivateView getView(UUID uuid) {
        return this.privateView.get(uuid);
    }

    private void newPrivateMessage(UUID sourcePlayer, UUID destPlayer) {
        PlayerPrivateView other = getView(destPlayer);
        if (other != null) {
            getView(sourcePlayer).createPrivateMessageTab(other, true);
        }
    }
}
