package com.simon816.chatui.lib.config;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.MoreObjects;
import com.simon816.chatui.lib.PlayerContext;
import com.simon816.chatui.util.FontData;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.entity.living.player.Player;

@ConfigSerializable
public class PlayerSettings {

    public static final int DEFAULT_BUFFER_WIDTH = 320;
    public static final int DEFAULT_BUFFER_HEIGHT = 180;
    public static final int LINE_HEIGHT = 9;
    public static final int DEFAULT_BUFFER_HEIGHT_LINES = DEFAULT_BUFFER_HEIGHT / LINE_HEIGHT;

    @Setting("display-width")
    private int width = DEFAULT_BUFFER_WIDTH;

    @Setting("display-height")
    private int height = DEFAULT_BUFFER_HEIGHT;

    @Setting("force-unicode")
    private boolean forceUnicode = false;

    @Setting("font-data")
    private String fontData = null;

    public PlayerSettings() {
    }

    public PlayerSettings(int width, int height, boolean forceUnicode, String fontData) {
        this.width = width;
        this.height = height;
        this.forceUnicode = forceUnicode;
        this.fontData = fontData;
    }

    public int getWidth() {
        return this.width;
    }

    public PlayerSettings withWidth(int width) {
        checkArgument(width >= 1, "Width must be at least one");
        return new PlayerSettings(width, this.height, this.forceUnicode, this.fontData);
    }

    public int getHeightLines() {
        return this.height / LINE_HEIGHT;
    }

    public int getHeight() {
        return this.height;
    }

    public PlayerSettings withHeight(int height) {
        checkArgument(height >= 1, "Height must be at least one");
        return new PlayerSettings(this.width, height, this.forceUnicode, this.fontData);
    }

    public boolean getForceUnicode() {
        return this.forceUnicode;
    }

    public PlayerSettings withUnicode(boolean forceUnicode) {
        return new PlayerSettings(this.width, this.height, forceUnicode, this.fontData);
    }

    public String getFontData() {
        return this.fontData;
    }

    public PlayerSettings withFontData(String fontData) {
        FontData.checkValid(fontData);
        if (fontData != null && fontData.isEmpty()) {
            fontData = null;
        }
        return new PlayerSettings(this.width, this.height, this.forceUnicode, fontData);
    }

    public PlayerContext createContext(Player player) {
        return new PlayerContext(player, getWidth(), getHeightLines(), getForceUnicode(), FontData.fromString(getFontData()));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        PlayerSettings settings = (PlayerSettings) obj;
        return settings.width == this.width && settings.height == this.height && settings.forceUnicode == this.forceUnicode
                && ((this.fontData == null && settings.fontData == null) || (this.fontData != null && this.fontData.equals(settings.fontData)));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("width", this.width)
                .add("height", this.height)
                .add("forceUnicode", this.forceUnicode)
                .add("fontData", this.fontData)
                .toString();
    }
}
