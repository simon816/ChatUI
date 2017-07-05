package com.simon816.chatui.lib.config;

import com.google.common.base.Objects;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

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

    public PlayerSettings() {
    }

    public PlayerSettings(int width, int height, boolean forceUnicode) {
        this.width = width;
        this.height = height;
        this.forceUnicode = forceUnicode;
    }

    public int getWidth() {
        return this.width;
    }

    public PlayerSettings withWidth(int width) {
        return new PlayerSettings(width, this.height, this.forceUnicode);
    }

    public int getHeightLines() {
        return this.height / LINE_HEIGHT;
    }

    public int getHeight() {
        return this.height;
    }

    public PlayerSettings withHeight(int height) {
        return new PlayerSettings(this.width, height, this.forceUnicode);
    }

    public boolean getForceUnicode() {
        return this.forceUnicode;
    }

    public PlayerSettings withUnicode(boolean forceUnicode) {
        return new PlayerSettings(this.width, this.height, forceUnicode);
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
        return settings.width == this.width && settings.height == this.height && settings.forceUnicode == this.forceUnicode;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("width", this.width)
                .add("height", this.height)
                .add("forceUnicode", this.forceUnicode)
                .toString();
    }
}
