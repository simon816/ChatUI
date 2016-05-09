package com.simon816.minecraft.tabchat.tabs.canvas;

import com.google.common.collect.Lists;
import com.simon816.minecraft.tabchat.PlayerContext;
import com.simon816.minecraft.tabchat.tabs.Tab;
import org.spongepowered.api.text.Text;

import java.util.List;

public class CanvasTab extends Tab {

    private final Text title;
    private RenderingContext context;

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
    public Text draw(PlayerContext ctx) {
        if (this.context == null) {
            return super.draw(ctx);
        }
        Text.Builder builder = Text.builder();
        LineDrawingContext drawContext = this.context.createDrawContext(ctx.width, ctx.height);
        for (Layer layer : this.context.layers) {
            layer.draw(drawContext);
        }
        Text.Builder[] lines = drawContext.render();
        for (int i = 0; i < lines.length; i++) {
            builder.append(lines[i].append(Text.NEW_LINE).build());
        }
        return builder.build();
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

    public enum Context {
        BLOCKS,
        BRAILLE
    }

    public static abstract class RenderingContext {

        protected final List<Layer> layers = Lists.newArrayList();

        public abstract Context getType();

        protected LineDrawingContext createDrawContext(int width, int height) {
            return new LineDrawingContext(width, height);
        }

        public void clear() {
            this.layers.clear();
        }

    }

    public interface Layer {

        void draw(LineDrawingContext ctx);
    }

}
