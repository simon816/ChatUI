package com.simon816.chatui.lib.event;

import static com.google.common.base.Preconditions.checkNotNull;

import com.simon816.chatui.lib.PlayerChatView;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;

public class CreatePlayerViewEvent implements Event {

    private final Cause cause;
    private final Player player;

    private PlayerChatView view;

    public CreatePlayerViewEvent(PlayerChatView view, Player player, Cause cause) {
        this.view = view;
        this.player = player;
        this.cause = cause;
    }

    @Override
    public Cause getCause() {
        return this.cause;
    }

    public Player getPlayer() {
        return this.player;
    }

    public PlayerChatView getView() {
        return this.view;
    }

    public void setView(PlayerChatView view) {
        this.view = checkNotNull(view, "view");
    }
}
