package com.simon816.chatui.ui.canvas;

import com.simon816.chatui.lib.PlayerChatView;
import com.simon816.chatui.lib.PlayerContext;
import com.simon816.chatui.util.Utils;
import it.unimi.dsi.fastutil.ints.Int2CharMap;
import it.unimi.dsi.fastutil.ints.Int2CharOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import java.util.function.Consumer;

public class LineDrawingContext {

    private final Line[] lines;
    private final int width;
    private final char emptyChar;
    private final PixelMetadata emptyData;
    private final int emptyCharWidth;

    private PixelMetadata currentData;

    public LineDrawingContext(PlayerContext ctx, char emptyChar, PixelMetadata emptyData) {
        this.lines = new Line[ctx.height];
        this.width = ctx.width;
        this.emptyChar = emptyChar;
        this.currentData = this.emptyData = emptyData;
        this.emptyCharWidth = ctx.utils().getWidth(emptyChar, false);
    }

    int getMinWidth() {
        return this.emptyCharWidth;
    }

    Text[] render() {
        int lastNonNull = 0;
        Text[] lines = new Text[this.lines.length];
        for (int i = 0; i < lines.length; i++) {
            Line line = this.lines[i];
            if (line == null) {
                lines[i] = Text.EMPTY;
                continue;
            }
            lastNonNull = i;
            lines[i] = line.toText();
        }
        if (lastNonNull < lines.length - 1) {
            Text[] newLines = new Text[lastNonNull + 1];
            System.arraycopy(lines, 0, newLines, 0, lastNonNull + 1);
            lines = newLines;
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
            this.lines[y] = new Line(this.width, this.emptyChar, this.emptyCharWidth, this.emptyData);
        }
        this.lines[y].setData(x, c, data);
    }

    public char getChar(int x, int y) {
        if (y > this.lines.length - 1 || y < 0 || this.lines[y] == null) {
            return 0;
        }
        return this.lines[y].get(x);
    }

    public PixelMetadata getData(int x, int y) {
        if (y > this.lines.length - 1 || y < 0 || this.lines[y] == null) {
            return null;
        }
        return this.lines[y].metadata.get(x);
    }

    public static class PixelMetadata {

        public final TextColor color;
        public final ClickAction<?> clickHandler;
        public final boolean locked;

        public PixelMetadata(TextColor color) {
            this(color, null, false);
        }

        public PixelMetadata(TextColor color, Consumer<PlayerChatView> callback, boolean locked) {
            this.color = color;
            this.clickHandler = callback == null ? null : Utils.execClick(callback);
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

        private final Int2CharMap characters = new Int2CharOpenHashMap();
        final Int2ObjectMap<PixelMetadata> metadata = new Int2ObjectOpenHashMap<>();

        private final int cellWidth;
        private final char emptyChar;
        private final PixelMetadata emptyData;
        private final int maxIndex;
        private int currentMax;

        public Line(int maxWidth, char emptyChar, int emptyCharWidth, PixelMetadata emptyData) {
            this.cellWidth = emptyCharWidth;
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
            // Special case: RESET will remove the data
            if (data != null && data.color == TextColors.RESET) {
                this.metadata.remove(index);
                this.characters.remove(index);
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
            if (this.characters.isEmpty()) {
                return Text.EMPTY;
            }
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
            }
            if (string.length() > 0 && prevData != null) {
                rootBuilder.append(prevData.toText(string.toString()));
            }
            return rootBuilder.build();
        }
    }
}
