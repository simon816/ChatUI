package com.simon816.minecraft.tabchat.tabs.canvas;

import com.simon816.minecraft.tabchat.PlayerChatView;
import com.simon816.minecraft.tabchat.TabbedChat;
import com.simon816.minecraft.tabchat.tabs.canvas.CanvasTab.Context;
import com.simon816.minecraft.tabchat.tabs.canvas.CanvasTab.Layer;
import com.simon816.minecraft.tabchat.tabs.canvas.CanvasTab.RenderingContext;
import com.simon816.minecraft.tabchat.tabs.canvas.LineDrawingContext.PixelMetadata;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import java.util.function.Consumer;

public class BlockRenderContext extends RenderingContext {

    @Override
    public Context getType() {
        return Context.BLOCKS;
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
        public void draw(LineDrawingContext ctx) {
            ctx.data(new PixelMetadata(this.color));
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
        public void draw(LineDrawingContext ctx) {
            ctx.data(new PixelMetadata(this.color));
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
        public void draw(LineDrawingContext ctx) {
            ctx.data(new PixelMetadata(this.color, this.clickAction, false));
            for (int y = this.y1; y < this.y2; y++) {
                for (int x = this.x1; x < this.x2; x++) {
                    ctx.write(x, y, '\u2588');
                }
            }
            if (this.showArrows) {
                ctx.write((this.x1 + this.x2) / 2, this.y1 - 1, '^', new PixelMetadata(TextColors.WHITE, src -> {
                    PlayerChatView view = TabbedChat.getView(src);
                    this.y1--;
                    this.y2--;
                    view.update();
                } , true));
                ctx.write((this.x1 + this.x2) / 2, this.y2, 'v', new PixelMetadata(TextColors.WHITE, src -> {
                    PlayerChatView view = TabbedChat.getView(src);
                    this.y1++;
                    this.y2++;
                    view.update();
                } , true));
                ctx.write(this.x1 - 1, (this.y1 + this.y2) / 2, '<', new PixelMetadata(TextColors.WHITE, src -> {
                    PlayerChatView view = TabbedChat.getView((Player) src);
                    this.x1--;
                    this.x2--;
                    view.update();
                } , true));
                ctx.write(this.x2, (this.y1 + this.y2) / 2, '>', new PixelMetadata(TextColors.WHITE, src -> {
                    PlayerChatView view = TabbedChat.getView((Player) src);
                    this.x1++;
                    this.x2++;
                    view.update();
                } , true));
            }
        }
    }

}
