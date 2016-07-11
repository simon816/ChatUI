package com.simon816.minecraft.tabchat.tabs.canvas;

import com.simon816.minecraft.tabchat.tabs.canvas.CanvasTab.Context;
import com.simon816.minecraft.tabchat.tabs.canvas.CanvasTab.Layer;
import com.simon816.minecraft.tabchat.tabs.canvas.CanvasTab.RenderingContext;
import com.simon816.minecraft.tabchat.tabs.canvas.LineDrawingContext.PixelMetadata;
import org.spongepowered.api.text.format.TextColors;

import java.awt.image.BufferedImage;

/**
 * Uses the braille unicode characters.
 *
 * Standard dimensions: 212 x 128.
 */
public class BrailleRenderContext extends RenderingContext {

    @Override
    public Context getType() {
        return Context.BRAILLE;
    }

    @Override
    protected LineDrawingContext createDrawContext(int width, int height) {
        return new LineDrawingContext(width, height, '\u2800', new PixelMetadata(TextColors.WHITE));
    }

    public void drawRect(int x1, int y1, int x2, int y2) {
        this.layers.add(new Rect(x1, y1, x2, y2));
    }

    public void drawImage(int x, int y, BufferedImage img) {
        this.layers.add(new ImageLayer(x, y, img));
    }

    private static char set(char c, int x, int y) {
        y = y & 3;
        if ((x & 1) == 0) {
            return (char) (c | 1 << (y == 3 ? 6 : y));
        } else {
            return (char) (c | 1 << ((y == 3 ? 4 : y) + 3));
        }
    }

    private static void setRelative(LineDrawingContext ctx, int x, int y) {
        int realX = x >>> 1;
        int realY = y >>> 2;
        char c = ctx.getChar(realX, realY);
        if (c == 0) {
            c = '\u2800';
        }
        ctx.write(realX, realY, set(c, x, y));
    }

    private static class Rect implements Layer {

        private int x1;
        private int y1;
        private int x2;
        private int y2;

        public Rect(int x1, int y1, int x2, int y2) {
            this.x1 = x1;
            this.x2 = x2;
            this.y1 = y1;
            this.y2 = y2;
        }

        @Override
        public void draw(LineDrawingContext ctx) {
            for (int y = this.y1; y < this.y2; y++) {
                for (int x = this.x1; x < this.x2; x++) {
                    setRelative(ctx, x, y);
                }
            }
        }

    }

    private static class ImageLayer implements Layer {

        private final int x;
        private final int y;
        private final BufferedImage img;

        public ImageLayer(int x, int y, BufferedImage img) {
            this.x = x;
            this.y = y;
            this.img = img;
        }

        @Override
        public void draw(LineDrawingContext ctx) {
            ctx.data(new PixelMetadata(TextColors.WHITE));
            // TODO configurable threshold level
            int min = 0x80; // min==80 && max==FF -> normal
            int max = 0xFF; // min==0 && max==80 -> negative
            for (int y = 0; y < this.img.getHeight(); y++) {
                for (int x = 0; x < this.img.getWidth(); x++) {
                    int rgb = this.img.getRGB(x, y);
                    int grayValue = (((rgb >> 16) & 0xFF) + ((rgb >> 8) & 0xFF) + (rgb & 0xFF)) / 3;
                    if (grayValue >= min && grayValue <= max) {
                        setRelative(ctx, (int) x + this.x, y + this.y);
                    }
                }
            }
        }

    }

}
