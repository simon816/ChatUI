package com.simon816.chatui.tabs.canvas;

import com.simon816.chatui.util.TextUtils;
import gnu.trove.map.hash.TIntCharHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import java.util.function.Consumer;

public class LineDrawingContext {

    public static final PixelMetadata DEFAULT_DATA = new PixelMetadata(TextColors.BLACK);
    public static final char DEFAULT_CHAR = '\u2063';

    private final Line[] lines;
    private final int width;
    private final char emptyChar;
    private final PixelMetadata emptyData;

    private PixelMetadata currentData;

    public LineDrawingContext(int width, int height) {
        this(width, height, DEFAULT_CHAR, DEFAULT_DATA);
    }

    public LineDrawingContext(int width, int height, char emptyChar, PixelMetadata emptyData) {
        this.lines = new Line[height];
        this.width = width;
        this.emptyChar = emptyChar;
        this.currentData = this.emptyData = emptyData;

    }

    Text.Builder[] render() {
        Text.Builder[] lines = new Text.Builder[this.lines.length];
        for (int i = 0; i < lines.length; i++) {
            lines[i] = Text.builder();
            Line line = this.lines[i];
            if (line == null) {
                continue;
            }
            lines[i].append(line.toText());
        }
        return lines;
    }

    public void data(PixelMetadata data) {
        this.currentData = data;
    }

    public void write(int x, int y, char c) {
        write(x, y, c, this.currentData);
    }

    public void write(int x, int y, char c, PixelMetadata data) {
        if (y > this.lines.length - 1 || y < 0) {
            return;
        }
        if (this.lines[y] == null) {
            this.lines[y] = new Line(this.width, this.emptyChar, this.emptyData);
        }
        this.lines[y].setData(x, c, data);
    }

    public char getChar(int x, int y) {
        if (y > this.lines.length - 1 || y < 0 || this.lines[y] == null) {
            return 0;
        }
        return this.lines[y].get(x);
    }

    public static class PixelMetadata {

        public final TextColor color;
        public final ClickAction<?> clickHandler;
        public final boolean locked;

        public PixelMetadata(TextColor color) {
            this(color, null, false);
        }

        public PixelMetadata(TextColor color, Consumer<CommandSource> callback, boolean locked) {
            this.color = color;
            this.clickHandler = callback == null ? null : TextActions.executeCallback(callback);
            this.locked = locked;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            PixelMetadata other = (PixelMetadata) obj;
            // Don't care about locked here
            return other.color == this.color && other.clickHandler == this.clickHandler;
        }

        public Text toText(String string) {
            Text.Builder b = Text.builder(string).color(this.color);
            if (this.clickHandler != null) {
                b.onClick(this.clickHandler);
            }
            return b.build();
        }

    }

    private static class Line {

        private final TIntCharHashMap characters = new TIntCharHashMap();
        private final TIntObjectHashMap<PixelMetadata> metadata = new TIntObjectHashMap<>();

        private final int cellWidth;
        private final char emptyChar;
        private final PixelMetadata emptyData;
        private final int maxIndex;
        private int currentMax;

        public Line(int maxWidth, char emptyChar, PixelMetadata emptyData) {
            this.cellWidth = (int) TextUtils.getWidth(emptyChar, false);
            this.maxIndex = (maxWidth / this.cellWidth) - 1;
            this.emptyChar = emptyChar;
            this.emptyData = emptyData;
        }

        public void setData(int index, char c, PixelMetadata data) {
            if (index < 0 || index > this.maxIndex) {
                return;
            }
            PixelMetadata existing = this.metadata.get(index);
            if (existing != null && existing.locked) {
                return;
            }
            this.currentMax = Math.max(this.currentMax, index);
            this.metadata.put(index, data == null ? this.emptyData : data);
            this.characters.put(index, c);
        }

        public char get(int index) {
            if (index < 0 || index > this.currentMax) {
                return 0;
            }
            return this.characters.get(index);
        }

        public Text toText() {
            StringBuilder string = new StringBuilder();
            Text.Builder rootBuilder = Text.builder();
            PixelMetadata prevData = null;
            for (int i = 0; i <= this.currentMax; i++) {
                PixelMetadata data = this.metadata.get(i);
                char c = this.characters.get(i);
                if (data == null) {
                    data = this.emptyData;
                    c = this.emptyChar;
                }
                if (prevData == null) {
                    prevData = data;
                }
                if (!prevData.equals(data)) {
                    rootBuilder.append(prevData.toText(string.toString()));
                    string = new StringBuilder();
                    prevData = data;
                }
                string.append(c);
                int w = (int) TextUtils.getWidth(c, false);
                if (w < this.cellWidth) {
                    rootBuilder.append(prevData.toText(string.toString()));
                    string = new StringBuilder();
                    for (int j = w; j < this.cellWidth; j++) {
                        string.append(TextUtils.PIXEL_CHAR);
                    }
                    rootBuilder.append(this.emptyData.toText(string.toString()));
                    string = new StringBuilder();
                }
            }
            if (string.length() > 0 && prevData != null) {
                rootBuilder.append(prevData.toText(string.toString()));
            }
            return rootBuilder.build();
        }
    }
}
