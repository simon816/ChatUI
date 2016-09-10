package com.simon816.minecraft.tabchat.tabs.config;

import com.google.common.base.Joiner;
import com.simon816.minecraft.tabchat.ITextDrawable;
import com.simon816.minecraft.tabchat.PlayerChatView;
import com.simon816.minecraft.tabchat.PlayerContext;
import com.simon816.minecraft.tabchat.TabbedChat;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.util.Collections;

class NodeBuilder implements ITextDrawable {

    protected final ConfigEditTab tab;
    protected String key;
    protected Object value;
    private String valueTypeName;
    private ConfigEntry.ValueType valueType;

    NodeBuilder(ConfigEditTab tab) {
        this.tab = tab;
    }

    private boolean hasKey() {
        return !keyRequired() || this.key != null;
    }

    protected boolean keyRequired() {
        return true;
    }

    @Override
    public Text draw(PlayerContext ctx) {
        Text.Builder builder = Text.builder();
        int rem = ctx.height;
        builder.append(Text.of(TextStyles.BOLD, "Create new node"), Text.NEW_LINE);
        builder.append(Text.of("Path: ", TextColors.GREEN, Joiner.on('.').join(this.tab.control.getNode().getPath())), Text.NEW_LINE);
        if (keyRequired()) {
            builder.append(Text.of("Key: ", getKeyText()), Text.NEW_LINE);
            rem -= 1;
        }
        builder.append(Text.of("Value Type: ",
                this.valueTypeName == null ? TextColors.RED : TextColors.GREEN,
                this.valueTypeName == null ? "" : this.valueTypeName), Text.NEW_LINE);
        rem -= 3;
        if (this.valueTypeName == null) {
            rem -= addValueTypes(builder);
        }
        if (!hasKey() || this.valueTypeName == null) {
            builder.append(Text.of("Value: ", TextColors.RED,
                    !hasKey() ? "[Waiting for key]" : this.valueTypeName == null ? "[Waiting for value type]" : this.value));
        } else {
            builder.append(Text.of("Value: ", TextColors.GREEN, this.value.toString(), TextColors.WHITE,
                    this.valueType != null ? " [Type in chat to change]" : ""));
        }
        for (int i = 0; i < rem - 1; i++) {
            builder.append(Text.NEW_LINE);
        }
        builder.append(Text.builder("[Cancel]").color(TextColors.RED).onClick(onClick(() -> {
            this.tab.nodeBuilder = null;
        })).build());

        if (hasKey() && this.value != null) {
            builder.append(Text.builder(" [Add Node]").color(TextColors.GREEN).onClick(onClick(() -> {
                ConfigurationNode newNode = submitValue();
                this.tab.control.handler.onNodeAdded(newNode);
                this.tab.control.refresh();
                this.tab.nodeBuilder = null;
            })).build());
        }
        builder.append(Text.NEW_LINE);
        return builder.build();
    }

    protected ConfigurationNode submitValue() {
        return this.tab.control.getNode().getNode(this.key).setValue(this.value);
    }

    private Text getKeyText() {
        return Text.builder(this.key == null ? "[Enter in chat]" : this.key)
                .color(this.key == null ? TextColors.RED : TextColors.GREEN)
                .style(this.key == null ? TextStyles.BOLD : TextStyles.RESET)
                .build();
    }

    private int addValueTypes(Text.Builder builder) {
        builder.append(simpleType("String", ConfigEntry.ValueType.STRING, ""), Text.NEW_LINE,
                simpleType("Boolean", ConfigEntry.ValueType.BOOLEAN, Boolean.FALSE), Text.NEW_LINE,
                simpleType("Long", ConfigEntry.ValueType.NUMBER, Long.valueOf(0)), Text.NEW_LINE,
                simpleType("Double", ConfigEntry.ValueType.NUMBER, Double.valueOf(0)), Text.NEW_LINE,
                complexType("Map", Collections.emptyMap()), Text.NEW_LINE,
                complexType("List", Collections.emptyList()), Text.NEW_LINE);
        return 6;
    }

    private Text complexType(String name, Object def) {
        return Text.builder("    " + name).color(TextColors.RED).onClick(onClick(() -> {
            this.valueTypeName = name;
            this.value = def;
        })).build();
    }

    private Text simpleType(String name, ConfigEntry.ValueType type, Object def) {
        return Text.builder("    " + name).color(TextColors.RED).onClick(onClick(() -> {
            this.valueTypeName = name;
            this.valueType = type;
            this.value = def;
        })).build();
    }

    private ClickAction<?> onClick(Runnable callback) {
        return TextActions.executeCallback(src -> {
            if (!this.tab.isTabActive(src) || this.tab.nodeBuilder != this) {
                return;
            }
            callback.run();
            TabbedChat.getView(src).update();
        });
    }

    public void recieveInput(PlayerChatView view, String input) {
        if (!hasKey()) {
            this.key = input;
        } else if (this.valueType != null && this.value != null) {
            try {
                this.value = this.valueType.setValue(this.value, input);
            } catch (IllegalArgumentException ex) {
                // Ignore
            }
        } else {
            return;
        }
        view.update();
    }

    private static class ListNodeBuilder extends NodeBuilder {

        ListNodeBuilder(ConfigEditTab tab) {
            super(tab);
        }

        @Override
        protected boolean keyRequired() {
            return false;
        }

        @Override
        protected ConfigurationNode submitValue() {
            return this.tab.control.getNode().getAppendedNode().setValue(this.value);
        }
    }

    public static NodeBuilder forNode(ConfigurationNode node, ConfigEditTab tab) {
        if (node.hasListChildren()) {
            return new ListNodeBuilder(tab);
        } else if (node.hasMapChildren()) {
            return new NodeBuilder(tab);
        }
        return null;
    }

}
