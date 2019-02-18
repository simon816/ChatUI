package com.simon816.chatui.ui.canvas;

import com.simon816.chatui.lib.PlayerContext;
import com.simon816.chatui.ui.canvas.CanvasUI.Context;
import com.simon816.chatui.ui.canvas.CanvasUI.Layer;
import com.simon816.chatui.ui.canvas.CanvasUI.RenderingContext;
import com.simon816.chatui.ui.canvas.LineDrawingContext.PixelMetadata;
import com.simon816.chatui.ui.canvas.ShapeFunction.DrawHandler;
import org.spongepowered.api.text.format.TextColors;

import java.awt.image.BufferedImage;

/**
 * Uses the braille unicode characters.
 *
 * Standard dimensions: 212 x 144.
 */
public class BrailleRenderContext extends RenderingContext {

    private static final PixelMetadata EMPTY_DATA = new PixelMetadata(TextColors.WHITE);

    private static final ShapeFunction.DrawHandler DRAW_HANDLER = new DrawHandler() {

        @Override
        public void write(LineDrawingContext ctx, int x, int y, Object... args) {
            boolean set = (Boolean) args[0];
            boolean corrections = (Boolean) args[1];
            if (corrections) {
                y = (int) (y * 0.7);
            }
            writeRelative(ctx, x, y, set);
        }
    };

    private boolean corrections = false;

    @Override
    public Context getType() {
        return Context.BRAILLE;
    }

    @Override
    protected LineDrawingContext createDrawContext(PlayerContext ctx) {
        return new LineDrawingContext(ctx, '\u2800', EMPTY_DATA);
    }

    /**
     * Adjusts the output to look more correct due to the gaps between lines on
     * the client.
     *
     * @param visualCorrections
     */
    public void setVisualCorrections(boolean visualCorrections) {
        this.corrections = visualCorrections;
    }

    public void drawRect(int x1, int y1, int x2, int y2, boolean set) {
        addLayer(new ShapeFunction.Rect(DRAW_HANDLER, x1, y1, x2, y2, set, this.corrections));
    }

    public void drawLine(int x1, int y1, int x2, int y2, boolean set) {
        addLayer(new ShapeFunction.Line(DRAW_HANDLER, x1, y1, x2, y2, set, this.corrections));
    }

    public void drawCircle(int x, int y, int r, boolean set) {
        addLayer(new ShapeFunction.Circle(DRAW_HANDLER, x, y, r, set, this.corrections));
    }

    public void drawImage(int x, int y, BufferedImage img) {
        this.drawImage(x, y, img, 0x80, 0xFF);
    }

    public void drawImage(int x, int y, BufferedImage img, int grayMin, int grayMax) {
        addLayer(new ImageLayer(x, y, img, grayMin, grayMax));
    }

    private static char set(char c, int x, int y) {
        y = y & 3;
        if ((x & 1) == 0) {
            return (char) (c | 1 << (y == 3 ? 6 : y));
        } else {
            return (char) (c | 1 << ((y == 3 ? 4 : y) + 3));
        }
    }

    private static char clear(char c, int x, int y) {
        y = y & 3;
        if ((x & 1) == 0) {
            return (char) (c & ~(1 << (y == 3 ? 6 : y)));
        } else {
            return (char) (c & ~(1 << ((y == 3 ? 4 : y) + 3)));
        }
    }

    static void writeRelative(LineDrawingContext ctx, int x, int y, boolean set) {
        int realX = x >>> 1;
        int realY = y >>> 2;
        char c = ctx.getChar(realX, realY);
        if (c == 0) {
            if (!set) {
                return;
            }
            c = '\u2800';
        }
        ctx.write(realX, realY, set ? set(c, x, y) : clear(c, x, y));
    }

    private static class ImageLayer implements Layer {

        private final int x;
        private final int y;
        private final BufferedImage img;
        private final int grayMin;
        private final int grayMax;

        public ImageLayer(int x, int y, BufferedImage img, int grayMin, int grayMax) {
            this.x = x;
            this.y = y;
            this.img = img;
            this.grayMin = grayMin;
            this.grayMax = grayMax;
        }

        @Override
        public void draw(LineDrawingContext ctx) {
            ctx.data(new PixelMetadata(TextColors.WHITE));
            for (int y = 0; y < this.img.getHeight(); y++) {
                for (int x = 0; x < this.img.getWidth(); x++) {
                    int rgb = this.img.getRGB(x, y);
                    int grayValue = (((rgb >> 16) & 0xFF) + ((rgb >> 8) & 0xFF) + (rgb & 0xFF)) / 3;
                    boolean isSet = grayValue >= this.grayMin && grayValue <= this.grayMax;
                    writeRelative(ctx, x + this.x, y + this.y, isSet);
                }
            }
        }

    }

}
