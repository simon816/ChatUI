package com.simon816.chatui.tabs.config;

import static com.google.common.base.Preconditions.checkArgument;

import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextFormat;
import org.spongepowered.api.text.format.TextStyles;

class ConfigEntry {

    static abstract class ConfigValue {

        public abstract Text toText(boolean focus);

        protected ClickAction<?> createClickAction(ConfigEntry entry, ConfigEditTab boundTab) {
            return null;
        }

        public Object onSetValue(String input) {
            throw new UnsupportedOperationException("Cannot set value of this node");
        }

    }

    static class UnknownValueType extends ConfigValue {

        private final Text text;

        public UnknownValueType(Object value) {
            this.text = Text.of("[Unknown] ", value);
        }

        @Override
        public Text toText(boolean focus) {
            return this.text;
        }
    }

    static class ComplexValue extends ConfigValue {

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
            return boundTab.clickAction(() -> {
                boundTab.control.setNode(this.node);
            });
        }
    }

    static class SimpleValue extends ConfigValue {

        private final ValueType type;
        private Object value;
        private Text text;

        public SimpleValue(Object value, ValueType type) {
            this.type = type;
            this.value = value;
            this.text = this.type.toText(this.value).build();
        }

        @Override
        public Text toText(boolean focus) {
            if (!focus) {
                return this.text.toBuilder().color(TextColors.DARK_GRAY).build();
            }
            return this.text;
        }

        @Override
        protected ClickAction<?> createClickAction(ConfigEntry entry, ConfigEditTab boundTab) {
            if (!boundTab.control.options.canEdit) {
                return null;
            }
            return this.type.createClickAction(this.value, entry, boundTab);
        }

        @Override
        public Object onSetValue(String input) {
            this.value = this.type.setValue(this.value, input);
            this.text = this.type.toText(this.value).build();
            return this.value;
        }
    }

    static enum ValueType {
        BOOLEAN(Boolean.class) {

            @Override
            public Text.Builder toText(Object value) {
                return super.toText(value).color((Boolean) value ? TextColors.GREEN : TextColors.RED);
            }

            @Override
            public void onClick(ConfigEntry entry, ConfigEditTab tab) {
                ConfigurationNode node = tab.control.getNode().getNode(entry.key);
                Object value = entry.value.onSetValue(Boolean.toString(!this.<Boolean>checkType(node.getValue())));
                node.setValue(value);
                tab.control.onNodeChanged(node);
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
            tab.control.setFocused(entry);
        }

        public ClickAction<?> createClickAction(Object value, ConfigEntry entry, ConfigEditTab boundTab) {
            checkType(value);
            if (boundTab.control.hasExplicitFocus(entry)) {
                return TextActions.suggestCommand(value.toString());
            }
            return boundTab.clickAction(() -> {
                this.onClick(entry, boundTab);
            });
        }

        public abstract Object setValue(Object old, String input);

    }

    public final Object key;
    final ConfigValue value;

    public ConfigEntry(Object key, ConfigValue value) {
        this.key = key;
        this.value = value;
    }

}
