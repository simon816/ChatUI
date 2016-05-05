package com.simon816.minecraft.tabchat.privmsg;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.simon816.minecraft.tabchat.PlayerChatView;
import com.simon816.minecraft.tabchat.util.TextUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TranslatableText;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PrivateMessageFeature {

    private final Map<UUID, PrivateMessageTab> privateChatTabs = Maps.newHashMap();
    private final PlayerChatView view;

    public PrivateMessageFeature(PlayerChatView view) {
        this.view = view;
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
    }

    private PrivateMessageTab createPrivateMessageTab(Player other, boolean switchTab) {
        PrivateMessageTab tab = this.privateChatTabs.get(other.getUniqueId());
        if (tab == null) {
            this.privateChatTabs.put(other.getUniqueId(), tab = new PrivateMessageTab(this.view, other));
            this.view.getWindow().addTab(tab, switchTab);
        }
        return tab;
    }

}
