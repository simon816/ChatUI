package com.simon816.chatui;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Objects;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Locale;

public class PlayerContext {

    public final Player player;
    public final int height;
    public final int width;
    public final boolean forceUnicode;

    public PlayerContext(Player player, int width, int height, boolean forceUnicode) {
        this.player = player;
        this.width = width;
        this.height = height;
        this.forceUnicode = forceUnicode;
    }

    public PlayerContext withHeight(int height) {
        checkArgument(height >= 1, "Height must be at least one");
        return new PlayerContext(this.player, this.width, height, this.forceUnicode);
    }

    public PlayerContext withWidth(int width) {
        checkArgument(width >= 1, "Width must be at least one");
        return new PlayerContext(this.player, width, this.height, this.forceUnicode);
    }

    public PlayerContext withUnicode(boolean forceUnicode) {
        return new PlayerContext(this.player, this.width, this.height, forceUnicode);
    }

    public Locale getLocale() {
        return this.player.getLocale();
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
