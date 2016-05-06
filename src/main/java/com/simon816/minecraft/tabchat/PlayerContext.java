package com.simon816.minecraft.tabchat;

import org.spongepowered.api.entity.living.player.Player;

public class PlayerContext {

    public final Player player;
    public final int height;
    public final int width;

    public PlayerContext(Player player, int width, int height) {
        this.player = player;
        this.width = width;
        this.height = height;
    }

    public PlayerContext withHeight(int height) {
        return new PlayerContext(this.player, this.width, height);
    }

}
