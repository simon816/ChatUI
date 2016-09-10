package com.simon816.minecraft.tabchat.ui;

import com.google.common.collect.Lists;
import com.simon816.minecraft.tabchat.PlayerContext;
import org.spongepowered.api.text.Text;

import java.util.List;

public class Frame implements UIComponent {

    private static class ComponentDrawer {

        final UIComponent component;
        private final int rule;

        public ComponentDrawer(UIComponent component, int rule) {
            this.component = component;
            this.rule = rule;
        }

        public int stickyType() {
            return this.rule;
        }

    }

    public static final int STICK_TOP = 1;
    public static final int STICK_BOTTOM = 2;

    private final List<ComponentDrawer> components = Lists.newArrayList();

    public void addComponent(UIComponent component) {
        addComponent(component, 0);
    }

    public void addComponent(UIComponent component, int rule) {
        this.components.add(new ComponentDrawer(component, rule));
    }

    @Override
    public int draw(Text.Builder builder, PlayerContext ctx) {
        int lines = ctx.height;
        List<Text> top = Lists.newArrayList();
        List<Text> bottom = Lists.newArrayList();
        for (ComponentDrawer component : this.components) {
            if (component.stickyType() != 0) {
                Text.Builder compText = Text.builder();
                lines -= component.component.draw(compText, ctx.withHeight(lines));
                if (component.stickyType() == STICK_TOP) {
                    top.add(compText.build());
                } else if (component.stickyType() == STICK_BOTTOM) {
                    bottom.add(compText.build());
                }
            }
        }
        builder.append(top);
        for (ComponentDrawer component : this.components) {
            if (component.stickyType() == 0) {
                lines -= component.component.draw(builder, ctx.withHeight(lines));
            }
        }
        builder.append(bottom);
        return ctx.height - lines;
    }

}
