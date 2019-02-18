package com.simon816.chatui.lib.event;

import com.simon816.chatui.lib.config.PlayerSettings;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;

public class PlayerChangeConfigEvent implements Event {

    private final Cause cause;
    private final Player player;
    private final PlayerSettings oldSettings;
    private final PlayerSettings newSettings;

    public PlayerChangeConfigEvent(Player player, PlayerSettings oldSettings, PlayerSettings newSettings, Cause cause) {
        this.player = player;
        this.oldSettings = oldSettings;
        this.newSettings = newSettings;
        this.cause = cause;
    }

    @Override
    public Cause getCause() {
        return this.cause;
    }

    public Player getPlayer() {
        return this.player;
    }

    public PlayerSettings getNewSettings() {
        return this.newSettings;
    }

    public PlayerSettings getOldSettings() {
        return this.oldSettings;
    }
}
