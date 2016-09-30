package com.simon816.chatui.ui.canvas;

import com.simon816.chatui.ui.canvas.CanvasUI.Context;
import com.simon816.chatui.ui.canvas.CanvasUI.Layer;
import com.simon816.chatui.ui.canvas.CanvasUI.RenderingContext;
import com.simon816.chatui.ui.canvas.LineDrawingContext.PixelMetadata;
import com.simon816.chatui.ui.canvas.ShapeFunction.DrawHandler;
import org.spongepowered.api.text.format.TextColor;

public class BlockRenderContext extends RenderingContext {

    @Override
    public Context getType() {
        return Context.BLOCKS;
    }

    private static final ShapeFunction.DrawHandler DRAW_HANDLER = new DrawHandler() {

        @Override
        public void setup(LineDrawingContext ctx, Object... args) {
            ctx.data(new PixelMetadata((TextColor) args[0]));
        }

        @Override
        public void write(LineDrawingContext ctx, int x, int y, Object... args) {
            ctx.write(x, y, '█');
        }
    };

    public void drawRect(int x1, int y1, int x2, int y2, TextColor color) {
        addLayer(new ShapeFunction.Rect(DRAW_HANDLER, x1, y1, x2, y2, color));
    }

    public void drawLine(int x1, int y1, int x2, int y2, TextColor color) {
        addLayer(new ShapeFunction.Line(DRAW_HANDLER, x1, y1, x2, y2, color));
    }

    public void drawCircle(int x, int y, int r, TextColor color) {
        addLayer(new ShapeFunction.Circle(DRAW_HANDLER, x, y, r, color));
    }

    public void drawString(String string, int x, int y, TextColor color) {
        addLayer(new StringLayer(string, x, y, color));
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

}
