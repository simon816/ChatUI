package com.simon816.minecraft.tabchat;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Objects;
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
        checkArgument(height >= 1, "Height must be at least one");
        return new PlayerContext(this.player, this.width, height);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("player", this.player)
                .add("width", this.width)
                .add("height", this.height)
                .toString();
    }
}
