package com.simon816.chatui.ui.canvas;

import com.simon816.chatui.ui.canvas.CanvasUI.Layer;

public abstract class ShapeFunction implements Layer {

    public static interface DrawHandler {

        default void setup(LineDrawingContext ctx, Object... args) {
        }

        default void finish(LineDrawingContext ctx) {
        }

        void write(LineDrawingContext ctx, int x, int y, Object... args);
    }

    private final DrawHandler handler;
    private final Object[] args;

    protected ShapeFunction(DrawHandler handler, Object... args) {
        this.handler = handler;
        this.args = args;
    }

    @Override
    public void draw(LineDrawingContext ctx) {
        this.handler.setup(ctx, this.args);
        this.draw0(ctx);
        this.handler.finish(ctx);
    }

    protected abstract void draw0(LineDrawingContext ctx);

    protected void write(LineDrawingContext ctx, int x, int y) {
        this.handler.write(ctx, x, y, this.args);
    }

    public static class Rect extends ShapeFunction {

        private final int x1;
        private final int y1;
        private final int x2;
        private final int y2;

        public Rect(DrawHandler handler, int x1, int y1, int x2, int y2, Object... args) {
            super(handler, args);
            this.x1 = x1;
            this.x2 = x2;
            this.y1 = y1;
            this.y2 = y2;
        }

        @Override
        protected void draw0(LineDrawingContext ctx) {
            for (int y = this.y1; y < this.y2; y++) {
                for (int x = this.x1; x < this.x2; x++) {
                    write(ctx, x, y);
                }
            }
        }
    }

    public static class Circle extends ShapeFunction {

        private final int x;
        private final int y;
        private final int r;

        public Circle(DrawHandler handler, int x, int y, int r, Object... args) {
            super(handler, args);
            this.x = x;
            this.y = y;
            if (r < 0) {
                throw new IllegalArgumentException("Radius cannot be negative");
            }
            this.r = r;
        }

        @Override
        protected void draw0(LineDrawingContext ctx) {
            float rSquared = this.r * this.r;
            int r = this.r;
            int xOff = this.x;
            int yOff = this.y;
            // This function was arbitrarily made but makes decent circles
            for (int y = -r; y <= r; y++) {
                double ySquared = (y - 0.5) * (y + 0.5);
                for (int x = -r; x <= r; x++) {
                    if (Math.floor((x - 0.5) * (x + 0.5) + ySquared) <= rSquared) {
                        write(ctx, xOff + x, yOff + y);
                    }
                }
            }
        }
    }

    public static class Line extends ShapeFunction {

        private final int x1;
        private final int y1;
        private final int x2;
        private final int y2;

        public Line(DrawHandler handler, int x1, int y1, int x2, int y2, Object... args) {
            super(handler, args);
            this.x1 = x1;
            this.x2 = x2;
            this.y1 = y1;
            this.y2 = y2;
        }

        @Override
        protected void draw0(LineDrawingContext ctx) {
            int lowX = this.x1;
            int highX = this.x2;
            int lowY = this.y1;
            int highY = this.y2;
            if (lowX > highX) {
                highX = this.x1;
                lowX = this.x2;
            }
            if (lowY > highY) {
                highY = this.y1;
                lowY = this.y2;
            }
            int dx = highX - lowX;
            int dy = highY - lowY;
            double ratioY = (double) dy / dx;
            double ratioX = (double) dx / dy;
            for (int xShift = 0; xShift <= dx; xShift++) {
                int yShift = (int) Math.round(xShift * ratioY);
                write(ctx, lowX + xShift, lowY + yShift);
            }
            for (int yShift = 0; yShift <= dy; yShift++) {
                int xShift = (int) Math.round(yShift * ratioX);
                write(ctx, lowX + xShift, lowY + yShift);
            }
        }
    }
}
