package com.simon816.minecraft.tabchat.tabs;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.simon816.minecraft.tabchat.PlayerChatView;
import com.simon816.minecraft.tabchat.PlayerContext;
import com.simon816.minecraft.tabchat.TabbedChat;
import com.simon816.minecraft.tabchat.util.TextUtils;
import com.simon816.minecraft.tabchat.util.Utils;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextFormat;
import org.spongepowered.api.text.format.TextStyles;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

public class ConfigEditTab extends Tab {

    public static class Options {

        public static final Options DEFAULTS = new Options(true, true, true, null);
        public final boolean canAdd;
        public final boolean canDelete;
        public final boolean canEdit;
        public final String rootNodeName;

        public Options(boolean add, boolean edit, boolean delete, String rootName) {
            this.canAdd = add;
            this.canEdit = edit;
            this.canDelete = delete;
            this.rootNodeName = rootName;
        }
    }

    public static abstract class ActionHandler {

        public static final ActionHandler NONE = new ActionHandler() {
        };

        public void onNodeChanged(ConfigurationNode node) {
        }

        public void onNodeRemoved(Object key) {
        }

        public void onNodeAdded(ConfigurationNode node) {
        }
    }

    NodeBuilder nodeBuilder;
    ConfigurationNode node;
    private int offset = 0;
    int widestKey;
    int widestValue;
    private ConfigEntry activeEntry;
    boolean deleteMode;
    private final Object[] ignored;
    final Options options;
    final ActionHandler handler;
    private final Text title;

    public ConfigEditTab(ConfigurationNode node, Text title) {
        this(node, title, Options.DEFAULTS, ActionHandler.NONE);
    }

    public ConfigEditTab(ConfigurationNode node, Text title, Options options, ActionHandler handler) {
        this.ignored = node.getParent().getPath();
        this.node = node;
        this.title = title;
        this.options = options;
        this.handler = handler;
    }

    private static class NodeBuilder {

        protected final ConfigEditTab tab;
        protected String key;
        protected Object value;
        private String valueTypeName;
        private ValueType valueType;

        public NodeBuilder(ConfigEditTab tab) {
            this.tab = tab;
        }

        private boolean hasKey() {
            return !keyRequired() || this.key != null;
        }

        protected boolean keyRequired() {
            return true;
        }

        public void draw(Text.Builder builder, PlayerContext ctx) {
            int rem = ctx.height;
            builder.append(Text.of(TextStyles.BOLD, "Create new node"), Text.NEW_LINE);
            builder.append(Text.of("Path: ", TextColors.GREEN, Joiner.on('.').join(this.tab.node.getPath())), Text.NEW_LINE);
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
                    this.tab.handler.onNodeAdded(newNode);
                    this.tab.nodeBuilder = null;
                })).build());
            }
            builder.append(Text.NEW_LINE);
        }

        protected ConfigurationNode submitValue() {
            return this.tab.node.getNode(this.key).setValue(this.value);
        }

        private Text getKeyText() {
            return Text.builder(this.key == null ? "[Enter in chat]" : this.key)
                    .color(this.key == null ? TextColors.RED : TextColors.GREEN)
                    .style(this.key == null ? TextStyles.BOLD : TextStyles.RESET)
                    .build();
        }

        private int addValueTypes(Text.Builder builder) {
            builder.append(simpleType("String", ValueType.STRING, ""), Text.NEW_LINE,
                    simpleType("Boolean", ValueType.BOOLEAN, Boolean.FALSE), Text.NEW_LINE,
                    simpleType("Long", ValueType.NUMBER, Long.valueOf(0)), Text.NEW_LINE,
                    simpleType("Double", ValueType.NUMBER, Double.valueOf(0)), Text.NEW_LINE,
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

        private Text simpleType(String name, ValueType type, Object def) {
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
    }

    private static class ListNodeBuilder extends NodeBuilder {

        public ListNodeBuilder(ConfigEditTab tab) {
            super(tab);
        }

        @Override
        protected boolean keyRequired() {
            return false;
        }

        @Override
        protected ConfigurationNode submitValue() {
            return this.tab.node.getAppendedNode().setValue(this.value);
        }
    }

    @Override
    public Text getTitle() {
        return this.title;
    }

    public void setNode(ConfigurationNode node) {
        if (this.activeEntry != null) {
            return;
        }
        this.node = node;
        this.offset = 0;
    }

    @Override
    public Text draw(PlayerContext ctx) {
        Text.Builder builder = Text.builder();
        if (this.nodeBuilder != null) {
            this.nodeBuilder.draw(builder, ctx);
            return builder.build();
        }
        builder.append(createBreadcrumb(), Text.NEW_LINE);
        List<ConfigEntry> entries = getEntries();
        int remaining = ctx.height - 2;
        int lenOfBar = TextUtils.getWidth('│', false);
        int lenOfCross = TextUtils.getWidth('┼', false);
        this.widestKey = Utils.ensureMultiple(this.widestKey + lenOfBar, lenOfCross) - lenOfBar;
        this.widestValue = Utils.ensureMultiple(this.widestValue + lenOfBar, lenOfCross) - lenOfBar;
        int i;
        for (i = this.offset; i < entries.size() && remaining > 0; i++) {
            builder.append(entries.get(i).getEntryText(this));
            builder.append(Text.NEW_LINE);
            remaining -= 1;
            if (remaining > 0) {
                boolean focus = hasFocus(entries.get(i)) || (i + 1 < entries.size() && hasFocus(entries.get(i + 1)));
                Text.Builder lineBuilder = Text.builder();
                boolean last = i == entries.size() - 1;
                TextUtils.startRepeatTerminate(lineBuilder, last ? '└' : '├', '─', last ? '┴' : '┼',
                        this.widestKey + lenOfBar + lenOfCross + 3);
                TextUtils.repeatAndTerminate(lineBuilder, '─', last ? '┘' : '┤',
                        this.widestValue + lenOfBar - 1);
                if (!focus) {
                    lineBuilder.color(TextColors.DARK_GRAY);
                }
                builder.append(lineBuilder.build(), Text.NEW_LINE);
            }
            remaining -= 1;
        }
        for (int rem = 0; rem < remaining; rem++) {
            builder.append(Text.NEW_LINE);
        }
        builder.append(createToolBar(i, entries.size() - 1), Text.NEW_LINE);
        return builder.build();
    }

    boolean isTabActive(CommandSource src) {
        return TabbedChat.getView(src).getWindow().getActiveTab() == this;
    }

    private ClickAction<?> scrollUp() {
        return TextActions.executeCallback(src -> {
            if (!isTabActive(src) || this.offset == 0) {
                return;
            }
            if (this.offset == 1) {
                this.offset--;
            } else {
                this.offset -= 2;
            }
            TabbedChat.getView(src).update();
        });
    }

    private ClickAction<?> scrollDown(int lastIndex, int maxOffset) {
        return TextActions.executeCallback(src -> {
            if (!isTabActive(src) || lastIndex > maxOffset) {
                return;
            }
            if (this.offset + 2 <= (maxOffset + 1) + (this.offset - lastIndex)) {
                this.offset += 2;
            } else {
                this.offset++;
            }
            TabbedChat.getView(src).update();
        });
    }

    private ClickAction<?> addNew() {
        return TextActions.executeCallback(src -> {
            if (!isTabActive(src)) {
                return;
            }
            if (this.node.hasListChildren()) {
                this.nodeBuilder = new ListNodeBuilder(this);
            } else if (this.node.hasMapChildren()) {
                this.nodeBuilder = new NodeBuilder(this);
            }
            TabbedChat.getView(src).update();
        });
    }

    private ClickAction<?> closeEntry() {
        return TextActions.executeCallback(src -> {
            if (!isTabActive(src) || this.activeEntry == null) {
                return;
            }
            this.activeEntry = null;
            TabbedChat.getView(src).update();
        });
    }

    private ClickAction<?> deleteEntry() {
        return TextActions.executeCallback(src -> {
            if (!isTabActive(src)) {
                return;
            }
            if (this.activeEntry == null) {
                this.deleteMode = !this.deleteMode;
            } else {
                this.node.removeChild(this.activeEntry.key);
                this.handler.onNodeRemoved(this.activeEntry.key);
                this.activeEntry = null;
            }
            TabbedChat.getView(src).update();
        });
    }

    private Text createToolBar(int lastIndex, int maxOffset) {
        Text.Builder builder = Text.builder();
        if (this.activeEntry == null) {
            builder.append(Text.of(scrollUp(), this.offset == 0 ? TextColors.DARK_GRAY : TextColors.WHITE, "[Scroll Up] "));
            builder.append(Text.of(scrollDown(lastIndex, maxOffset),
                    lastIndex > maxOffset ? TextColors.DARK_GRAY : TextColors.WHITE, "[Scroll Down] "));
            if (this.options.canAdd && (this.node.hasListChildren() || this.node.hasMapChildren()) && !this.deleteMode) {
                builder.append(Text.of(addNew(), TextColors.GREEN, "[Add]"));
            }
        } else {
            builder.append(Text.of(closeEntry(), TextColors.RED, "[Close]"));
        }
        if (this.options.canDelete) {
            builder.append(Text.of(deleteEntry(), TextColors.RED, " [Delete]"));
        }
        return builder.build();
    }

    public boolean hasFocus(ConfigEntry entry) {
        return this.activeEntry == null || this.activeEntry.key.equals(entry.key);
    }

    public boolean hasExplicitFocus(ConfigEntry entry) {
        return this.activeEntry != null && this.activeEntry.key.equals(entry.key);
    }

    private List<ConfigEntry> getEntries() {
        this.widestKey = 0;
        this.widestValue = 0;
        List<ConfigEntry> entries = Lists.newArrayList();
        if (this.node.hasMapChildren()) {
            for (Entry<Object, ? extends ConfigurationNode> entry : this.node.getChildrenMap().entrySet()) {
                ConfigEntry confEntry = new ConfigEntry(entry.getKey(), nodeToValue(entry.getValue()));
                entries.add(confEntry);
                this.widestKey = Math.max(this.widestKey, confEntry.width);
                this.widestValue = Math.max(this.widestValue, TextUtils.getWidth(confEntry.value.toText(hasFocus(confEntry))));
            }
        } else if (this.node.hasListChildren()) {
            List<? extends ConfigurationNode> children = this.node.getChildrenList();
            for (int i = 0; i < children.size(); i++) {
                ConfigEntry confEntry = new ConfigEntry(i, nodeToValue(children.get(i)));
                entries.add(confEntry);
                this.widestKey = Math.max(this.widestKey, confEntry.width);
                this.widestValue = Math.max(this.widestValue, TextUtils.getWidth(confEntry.value.toText(hasFocus(confEntry))));
            }
        } else {
            ConfigEntry entry = new ConfigEntry(this.node.getKey(), nodeToValue(this.node));
            entries.add(entry);
            this.widestKey = entry.width;
            this.widestValue = TextUtils.getWidth(entry.value.toText(hasFocus(entry)));
        }
        return entries;
    }

    private ConfigValue nodeToValue(ConfigurationNode node) {
        if (node.hasMapChildren() || node.hasListChildren()) {
            return new ComplexValue(node);
        }
        Object value = node.getValue();
        if (value instanceof Number) {
            return new SimpleValue(value, ValueType.NUMBER);
        } else if (value instanceof Boolean) {
            return new SimpleValue(value, ValueType.BOOLEAN);
        } else if (value instanceof String) {
            return new SimpleValue(value, ValueType.STRING);
        } else if (value == null) {
            return new SimpleValue(value, ValueType.NULL);
        }
        return new UnknownValueType(value);
    }

    private Text createBreadcrumb() {
        Text.Builder builder = Text.builder();
        Object[] path = this.node.getPath();
        boolean isRoot = true;
        for (int i = 0; i < path.length; i++) {
            if (i < this.ignored.length && this.ignored[i] == path[i]) {
                continue;
            }
            if (isRoot && this.options.rootNodeName != null) {
                path[i] = this.options.rootNodeName;
            }
            isRoot = false;
            Text.Builder part = Text.builder(path[i].toString());
            final int distance = path.length - i - 1;
            part.color(TextColors.BLUE).onClick(TextActions.executeCallback(src -> {
                if (!isTabActive(src)) {
                    return;
                }
                int dist = distance;
                ConfigurationNode newNode = this.node;
                while (dist-- > 0) {
                    newNode = newNode.getParent();
                }
                setNode(newNode);
                TabbedChat.getView(src).update();
            }));
            builder.append(part.build());
            if (i < path.length - 1) {
                builder.append(Text.of("->"));
            }
        }
        return builder.build();
    }

    public void setFocused(ConfigEntry entry) {
        this.activeEntry = entry;
    }

    @Override
    public void onTextEntered(PlayerChatView view, Text input) {
        if (this.nodeBuilder != null) {
            this.nodeBuilder.recieveInput(view, input.toPlain());
            return;
        }
        if (this.activeEntry == null) {
            return;
        }
        ConfigurationNode node = this.node.getNode(this.activeEntry.key);
        Object value = this.activeEntry.value.onSetValue(input.toPlain());
        node.setValue(value);
        this.handler.onNodeChanged(node);
        this.activeEntry = null;
        view.update();
    }

    static abstract class ConfigValue {

        public abstract Text toText(boolean focus);

        protected ClickAction<?> createClickAction(ConfigEntry entry, ConfigEditTab boundTab) {
            return null;
        }

        public Object onSetValue(String input) {
            throw new UnsupportedOperationException("Cannot set value of this node");
        }

    }

    private static enum ValueType {
        BOOLEAN(Boolean.class) {

            @Override
            public Text.Builder toText(Object value) {
                return super.toText(value).color((Boolean) value ? TextColors.GREEN : TextColors.RED);
            }

            @Override
            public void onClick(ConfigEntry entry, ConfigEditTab tab) {
                ConfigurationNode node = tab.node.getNode(entry.key);
                node.setValue(!this.<Boolean>checkType(node.getValue()));
                tab.handler.onNodeChanged(node);
            }

            @Override
            public Object setValue(Object old, String input) {
                return Boolean.parseBoolean(input);
            }
        },
        NUMBER(Number.class) {

            @Override
            public Object setValue(Object old, String input) {
                if (old instanceof Double) {
                    return Double.parseDouble(input);
                } else if (old instanceof Long) {
                    return Long.parseLong(input);
                } else if (old instanceof Integer) {
                    return Integer.parseInt(input);
                }
                // Safest bet - use double
                return Double.parseDouble(input);
            }

            @Override
            public Text.Builder toText(Object value) {
                return super.toText(value).color(TextColors.GOLD);
            }
        },
        STRING(String.class) {

            @Override
            public Object setValue(Object old, String input) {
                return input;
            }
        },
        NULL(null) {

            @Override
            public void onClick(ConfigEntry entry, ConfigEditTab tab) {
            }

            @Override
            public ClickAction<?> createClickAction(Object value, ConfigEntry entry, ConfigEditTab boundTab) {
                return null;
            }

            @Override
            public Text.Builder toText(Object value) {
                return Text.builder("NULL");
            }

            @Override
            public Object setValue(Object old, String input) {
                throw new UnsupportedOperationException("Cannot set value of this node");
            }
        };

        private final Class<?> type;

        private ValueType(Class<?> type) {
            this.type = type;
        }

        @SuppressWarnings("unchecked")
        protected final <T> T checkType(Object value) {
            if (this == NULL) {
                checkArgument(value == null, "Value must be null");
                return null;
            }
            checkArgument(this.type.isInstance(value), "Value must be instance of %s, found %s", this.type, value.getClass());
            return (T) value;
        }

        public Text.Builder toText(Object value) {
            return Text.builder(checkType(value).toString());
        }

        public void onClick(ConfigEntry entry, ConfigEditTab tab) {
            tab.setFocused(entry);
        }

        public ClickAction<?> createClickAction(Object value, ConfigEntry entry, ConfigEditTab boundTab) {
            checkType(value);
            if (boundTab.hasExplicitFocus(entry)) {
                return TextActions.suggestCommand(value.toString());
            }
            return TextActions.executeCallback(src -> {
                if (!boundTab.isTabActive(src)) {
                    return;
                }
                this.onClick(entry, boundTab);
                TabbedChat.getView(src).update();
            });
        }

        public abstract Object setValue(Object old, String input);

    }

    private static class SimpleValue extends ConfigValue {

        private final ValueType type;
        final Object value;
        private final Text.Builder textBuilder;

        public SimpleValue(Object value, ValueType type) {
            this.type = type;
            this.value = value;
            this.textBuilder = this.type.toText(value);
        }

        @Override
        public Text toText(boolean focus) {
            if (!focus) {
                this.textBuilder.color(TextColors.DARK_GRAY);
            }
            return this.textBuilder.build();
        }

        @Override
        protected ClickAction<?> createClickAction(ConfigEntry entry, ConfigEditTab boundTab) {
            if (!boundTab.options.canEdit) {
                return null;
            }
            return this.type.createClickAction(this.value, entry, boundTab);
        }

        @Override
        public Object onSetValue(String input) {
            return this.type.setValue(this.value, input);
        }
    }

    private static class ComplexValue extends ConfigValue {

        private static final Text LINK = Text.of(TextFormat.of(TextColors.BLUE, TextStyles.UNDERLINE), "Click to open");
        private static final Text LINK_BLUR = Text.of(TextFormat.of(TextColors.DARK_GRAY, TextStyles.UNDERLINE), "Click to open");
        private final ConfigurationNode node;

        public ComplexValue(ConfigurationNode node) {
            this.node = node;
        }

        @Override
        public Text toText(boolean focus) {
            return focus ? LINK : LINK_BLUR;
        }

        @Override
        protected ClickAction<?> createClickAction(ConfigEntry entry, ConfigEditTab boundTab) {
            return TextActions.executeCallback(src -> {
                if (!boundTab.isTabActive(src)) {
                    return;
                }
                boundTab.setNode(this.node);
                TabbedChat.getView(src).update();
            });
        }
    }

    private static class UnknownValueType extends ConfigValue {

        private final Text text;

        public UnknownValueType(Object value) {
            this.text = Text.of("[Unknown] ", value);
        }

        @Override
        public Text toText(boolean focus) {
            return this.text;
        }
    }

    private static class ConfigEntry {

        public final Object key;
        private final Text keyText;
        final ConfigValue value;
        final int width;

        public ConfigEntry(Object key, ConfigValue value) {
            this.key = key;
            this.keyText = Text.of(key);
            this.value = value;
            this.width = TextUtils.getWidth(this.keyText);
        }

        public Text getEntryText(ConfigEditTab boundTab) {
            Text.Builder builder = Text.builder();
            boolean focus = boundTab.hasFocus(this);
            if (!focus) {
                builder.color(TextColors.DARK_GRAY);
            }
            Text bar = TextUtils.charCache('│');
            StringBuilder spaces = new StringBuilder();
            TextUtils.padSpaces(spaces, boundTab.widestKey - this.width);
            builder.append(bar);
            if (spaces.length() > 0) {
                builder.append(Text.of(spaces.toString()));
            }
            Text valueText = this.value.toText(focus && !boundTab.deleteMode);
            if (boundTab.deleteMode) {
                valueText = valueText.toBuilder().color(TextColors.GRAY).build();
            }
            builder.append(this.keyText, TextUtils.charCache('│'), valueText);
            if (boundTab.deleteMode) {
                builder.onClick(TextActions.executeCallback(src -> {
                    if (!boundTab.isTabActive(src)) {
                        return;
                    }
                    boundTab.node.removeChild(this.key);
                    boundTab.handler.onNodeRemoved(this.key);
                    boundTab.deleteMode = false;
                    TabbedChat.getView(src).update();
                }));
            } else if (focus) {
                builder.onClick(this.value.createClickAction(this, boundTab));
            }
            spaces = new StringBuilder();
            TextUtils.padSpaces(spaces, boundTab.widestValue - TextUtils.getWidth(valueText));
            if (spaces.length() > 0) {
                builder.append(Text.of(spaces.toString()));
            }
            builder.append(TextUtils.charCache('│'));
            return builder.build();
        }

    }

}
