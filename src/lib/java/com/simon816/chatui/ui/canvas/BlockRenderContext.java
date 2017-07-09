package com.simon816.chatui.ui.canvas;

import com.simon816.chatui.lib.PlayerContext;
import com.simon816.chatui.ui.canvas.CanvasUI.Context;
import com.simon816.chatui.ui.canvas.CanvasUI.RenderingContext;
import com.simon816.chatui.ui.canvas.LineDrawingContext.PixelMetadata;
import com.simon816.chatui.ui.canvas.ShapeFunction.DrawHandler;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

public class BlockRenderContext extends RenderingContext {

    private static final PixelMetadata EMPTY_DATA = new PixelMetadata(TextColors.BLACK);

    @Override
    public Context getType() {
        return Context.BLOCKS;
    }

    @Override
    protected LineDrawingContext createDrawContext(PlayerContext ctx) {
        return new LineDrawingContext(ctx, '\u2063', EMPTY_DATA);
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

}
