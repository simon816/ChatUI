package com.simon816.chatui.lib;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Objects;
import com.simon816.chatui.util.FontData;
import com.simon816.chatui.util.TextUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

import java.util.UUID;

public class PlayerContext {

    public final int height;
    public final int width;
    public final boolean forceUnicode;

    private final UUID playerUUID;
    private final TextUtils utils;

    public PlayerContext(Player player, int width, int height, boolean forceUnicode, FontData fontData) {
        this(player.getUniqueId(), width, height, forceUnicode, new TextUtils(fontData, forceUnicode, player.getUniqueId()));
        checkArgument(height >= 1, "Height must be at least one");
        checkArgument(width >= 1, "Width must be at least one");
    }

    private PlayerContext(UUID playerUuid, int width, int height, boolean forceUnicode, TextUtils utils) {
        this.playerUUID = playerUuid;
        this.width = width;
        this.height = height;
        this.forceUnicode = forceUnicode;
        this.utils = utils;
    }

    public Player getPlayer() {
        return Sponge.getServer().getPlayer(this.playerUUID).get();
    }

    public TextUtils utils() {
        return this.utils;
    }

    public PlayerContext withHeight(int height) {
        checkArgument(height >= 1, "Height must be at least one");
        if (height == this.height) {
            return this;
        }
        return new PlayerContext(this.playerUUID, this.width, height, this.forceUnicode, this.utils);
    }

    public PlayerContext withWidth(int width) {
        checkArgument(width >= 1, "Width must be at least one");
        if (width == this.width) {
            return this;
        }
        return new PlayerContext(this.playerUUID, width, this.height, this.forceUnicode, this.utils);
    }

    public PlayerContext withUnicode(boolean forceUnicode) {
        if (forceUnicode == this.forceUnicode) {
            return this;
        }
        return new PlayerContext(this.playerUUID, this.width, this.height, forceUnicode,
                new TextUtils(this.utils.getFontData(), forceUnicode, this.playerUUID));
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("player", this.getPlayer())
                .add("width", this.width)
                .add("height", this.height)
                .add("forceUnicode", this.forceUnicode)
                .toString();
    }

}
