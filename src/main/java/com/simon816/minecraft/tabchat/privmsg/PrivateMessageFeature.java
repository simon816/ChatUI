package com.simon816.minecraft.tabchat.privmsg;

import com.google.common.collect.Maps;
import com.simon816.minecraft.tabchat.AbstractFeature;
import com.simon816.minecraft.tabchat.PlayerChatView;
import com.simon816.minecraft.tabchat.PlayerContext;
import com.simon816.minecraft.tabchat.tabs.NewTab.LaunchTabButton;
import com.simon816.minecraft.tabchat.tabs.Tab;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PrivateMessageFeature extends AbstractFeature {

    static class MessageWithTab extends Tab {

        private static final Text TITLE = Text.of("Private Message");

        @Override
        public Text getTitle() {
            return TITLE;
        }

        @Override
        public Text draw(PlayerContext ctx) {
            Text.Builder builder = Text.builder();
            int remaining = ctx.height;
            for (int i = 0; i < ctx.height / 2; i++) {
                builder.append(Text.NEW_LINE);
            }
            remaining -= ctx.height / 2;
            builder.append(Text.of("Type the name of the player you want to message:"));
            for (int i = 0; i < remaining; i++) {
                builder.append(Text.NEW_LINE);
            }
            return builder.build();
        }

        @Override
        public void onTextEntered(PlayerChatView view, Text input) {
            Optional<Player> player = Sponge.getServer().getPlayer(input.toPlain());
            if (!player.isPresent()) {
                return;
            }
            privateView.get(view.getPlayer().getUniqueId()).createPrivateMessageTab(player.get(), true);
            view.getWindow().removeTab(this);
            view.update();
        }
    }

    static final Map<UUID, PlayerPrivateView> privateView = Maps.newHashMap();

    @Override
    protected void onNewPlayerView(PlayerChatView view) {
        privateView.put(view.getPlayer().getUniqueId(), new PlayerPrivateView(view));
        view.getNewTab().addButton(new LaunchTabButton("Private Message", () -> new MessageWithTab()));
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
/*
 Unused
@formatter:off
view.getOutgoingPipeline().addHandler((message, sender) -> {
    message = TextUtils.unwrap(message);
    if (message instanceof TranslatableText) {
        String id = ((TranslatableText) message).getTranslation().getId();
        if (id.equals("commands.message.display.outgoing") || id.equals("commands.message.display.incoming")) {
            ImmutableList<Object> args = ((TranslatableText) message).getArguments();
            String name = ((Text) args.get(0)).toPlain();
            Optional<Player> incommingPlayer = Sponge.getGame().getServer().getPlayer(name);
            if (incommingPlayer.isPresent()) {
                createPrivateMessageTab(incommingPlayer.get(), id.equals("commands.message.display.incoming"))
                        .appendMessage(new MessageEvent.MessageFormatter(Text.of(name), (Text) args.get(1)).toText());
                return true;
            }
        }
    }
    return false;
});
 */
}
