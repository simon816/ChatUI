package com.simon816.chatui.ui.canvas;

import com.google.common.collect.Lists;
import com.simon816.chatui.lib.PlayerContext;
import com.simon816.chatui.ui.LineFactory;
import com.simon816.chatui.ui.UIComponent;
import org.spongepowered.api.text.Text;

import java.util.List;

public class CanvasUI implements UIComponent {

    private RenderingContext context;

    @Override
    public int getMinHeight(PlayerContext ctx) {
        return 1;
    }

    @Override
    public int getMinWidth(PlayerContext ctx) {
        if (this.context == null) {
            return 0;
        }
        return this.context.createDrawContext(0, 0).getMinWidth();
    }

    @SuppressWarnings("unchecked")
    public <T extends RenderingContext> T getContext(Context type) {
        if (this.context == null) {
            switch (type) {
                case BLOCKS:
                    this.context = new BlockRenderContext();
                    break;
                case BRAILLE:
                    this.context = new BrailleRenderContext();
                    break;
            }
            return (T) this.context;
        }
        return type == this.context.getType() ? (T) this.context : null;
    }

    @Override
    public void draw(PlayerContext ctx, LineFactory lineFactory) {
        if (this.context == null) {
            return;
        }
        LineDrawingContext drawContext = this.context.createDrawContext(ctx.width, ctx.height);
        for (Layer layer : this.context.layers) {
            layer.draw(drawContext);
        }
        Text[] rendered = drawContext.render();
        lineFactory.addAll(rendered, ctx.forceUnicode);
    }

    public enum Context {
        BLOCKS,
        BRAILLE
    }

    public static abstract class RenderingContext {

        final List<Layer> layers = Lists.newArrayList();

        public abstract Context getType();

        protected LineDrawingContext createDrawContext(int width, int height) {
            return new LineDrawingContext(width, height);
        }

        public void addLayer(Layer layer) {
            this.layers.add(layer);
        }

        public void clear() {
            this.layers.clear();
        }

    }

    public interface Layer {

        void draw(LineDrawingContext ctx);
    }
}
