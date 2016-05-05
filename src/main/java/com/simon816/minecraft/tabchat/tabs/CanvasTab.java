package com.simon816.minecraft.tabchat.tabs;

import com.google.common.collect.Lists;
import com.simon816.minecraft.tabchat.PlayerChatView;
import com.simon816.minecraft.tabchat.TabbedChat;
import com.simon816.minecraft.tabchat.util.TextUtils;
import gnu.trove.map.hash.TIntCharHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;
import java.util.function.Consumer;

//https://en.wikipedia.org/wiki/Block_Elements

// Standard width = 9
public class CanvasTab extends Tab {

    private final List<Layer> layers = Lists.newArrayList();
    private final Text title;

    public CanvasTab() {
        this(Text.of("Canvas"));
    }

    public CanvasTab(Text title) {
        this.title = title;
    }

    @Override
    public Text getTitle() {
        return this.title;
    }

    @Override
    public Text draw(int height) {
        Text.Builder builder = Text.builder();
        // Tests
        if (this.layers.isEmpty()) {
            drawRect(5, 5, 10, 10, TextColors.WHITE);
            drawRect(6, 6, 9, 9, TextColors.RED);
            drawRect(7, 8, 15, 10, TextColors.BLUE);
            drawCircle(30, 7, 6, TextColors.GREEN);
            drawString("Test", 0, 5, TextColors.GOLD);
        }
        DrawContext context = new DrawContext(height);
        for (Layer layer : this.layers) {
            layer.draw(context);
        }
        Text.Builder[] lines = context.render();
        for (int i = 0; i < lines.length; i++) {
            builder.append(lines[i].append(Text.NEW_LINE).build());
        }
        return builder.build();
    }

    public void drawRect(int x1, int y1, int x2, int y2, TextColor color) {
        this.layers.add(new Rect(x1, y1, x2, y2, color));
    }

    public void drawCircle(int x, int y, int r, TextColor color) {
        this.layers.add(new Circle(x, y, r, color));
    }

    public void drawString(String string, int x, int y, TextColor color) {
        this.layers.add(new StringLayer(string, x, y, color));
    }

    interface Layer {

        void draw(DrawContext ctx);
    }

    private static class StringLayer implements Layer {

        private final char[] chars;
        private final int x;
        private final int y;
        private final TextColor color;

        public StringLayer(String string, int x, int y, TextColor color) {
            this.chars = string.toCharArray();
            this.x = x;
            this.y = y;
            this.color = color;
        }

        @Override
        public void draw(DrawContext ctx) {
            ctx.data(new DrawContext.PixelMetadata(this.color));
            for (int i = 0; i < this.chars.length; i++) {
                ctx.write(this.x + i, this.y, this.chars[i]);
            }
        }

    }

    private static class Circle implements Layer {

        private final int x;
        private final int y;
        private final int r;
        private final TextColor color;

        public Circle(int x, int y, int r, TextColor color) {
            this.x = x;
            this.y = y;
            this.r = r;
            this.color = color;
        }

        @Override
        public void draw(DrawContext ctx) {
            ctx.data(new DrawContext.PixelMetadata(this.color));
            for (int y = -this.r; y <= this.r; y++) {
                for (int x = -this.r; x <= this.r; x++) {
                    if (x * x + y * y <= this.r * this.r) {
                        ctx.write(this.x + x, this.y + y, '\u2588');
                    }
                }
            }
        }
    }

    private static class Rect implements Layer {

        private int x1;
        private int y1;
        private int x2;
        private int y2;
        private TextColor color;
        private Consumer<CommandSource> clickAction;
        private boolean showArrows;

        public Rect(int x1, int y1, int x2, int y2, TextColor color) {
            this.x1 = x1;
            this.x2 = x2;
            this.y1 = y1;
            this.y2 = y2;
            this.color = color;
            this.clickAction = src -> {
                PlayerChatView view = TabbedChat.getView((Player) src);
                this.showArrows = !this.showArrows;
                view.update();
            };
        }

        @Override
        public void draw(DrawContext ctx) {
            ctx.data(new DrawContext.PixelMetadata(this.color, this.clickAction, false));
            for (int y = this.y1; y < this.y2; y++) {
                for (int x = this.x1; x < this.x2; x++) {
                    ctx.write(x, y, '\u2588');
                }
            }
            if (this.showArrows) {
                ctx.write((this.x1 + this.x2) / 2, this.y1 - 1, '^', new DrawContext.PixelMetadata(TextColors.WHITE, src -> {
                    PlayerChatView view = TabbedChat.getView((Player) src);
                    this.y1--;
                    this.y2--;
                    view.update();
                } , true));
                ctx.write((this.x1 + this.x2) / 2, this.y2, 'v', new DrawContext.PixelMetadata(TextColors.WHITE, src -> {
                    PlayerChatView view = TabbedChat.getView((Player) src);
                    this.y1++;
                    this.y2++;
                    view.update();
                } , true));
                ctx.write(this.x1 - 1, (this.y1 + this.y2) / 2, '<', new DrawContext.PixelMetadata(TextColors.WHITE, src -> {
                    PlayerChatView view = TabbedChat.getView((Player) src);
                    this.x1--;
                    this.x2--;
                    view.update();
                } , true));
                ctx.write(this.x2, (this.y1 + this.y2) / 2, '>', new DrawContext.PixelMetadata(TextColors.WHITE, src -> {
                    PlayerChatView view = TabbedChat.getView((Player) src);
                    this.x1++;
                    this.x2++;
                    view.update();
                } , true));
            }
        }
    }

    private static class DrawContext {

        private Line[] lines;
        private PixelMetadata currentData = Line.DEFAULT_DATA;

        public DrawContext(int height) {
            this.lines = new Line[height];
        }

        public Text.Builder[] render() {
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
                this.lines[y] = new Line();
            }
            this.lines[y].setData(x, c, data);
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

            private static final PixelMetadata DEFAULT_DATA = new PixelMetadata(TextColors.BLACK);
            private static final char EMPTY_CHAR = '\u2063';
            private final TIntCharHashMap characters = new TIntCharHashMap();
            private final TIntObjectHashMap<PixelMetadata> metadata = new TIntObjectHashMap<>();
            private int max;

            public Line() {
            }

            public void setData(int index, char c, PixelMetadata data) {
                if (index < 0 || index > 34) {
                    return;
                }
                PixelMetadata existing = this.metadata.get(index);
                if (existing != null && existing.locked) {
                    return;
                }
                this.max = Math.max(this.max, index);
                this.metadata.put(index, data == null ? DEFAULT_DATA : data);
                this.characters.put(index, c);
            }

            public Text toText() {
                StringBuilder string = new StringBuilder();
                Text.Builder rootBuilder = Text.builder();
                PixelMetadata prevData = null;
                for (int i = 0; i <= this.max; i++) {
                    PixelMetadata data = this.metadata.get(i);
                    char c = this.characters.get(i);
                    if (data == null) {
                        data = DEFAULT_DATA;
                        c = EMPTY_CHAR;
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
                    int w = c == EMPTY_CHAR ? 9 : (int) TextUtils.getWidth(c, false);
                    for (int j = w; j < 9; j++) {
                        string.append('\u205a');
                    }
                }
                if (string.length() > 0 && prevData != null) {
                    rootBuilder.append(prevData.toText(string.toString()));
                }
                return rootBuilder.build();
            }
        }
    }
}
