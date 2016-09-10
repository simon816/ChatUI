package com.simon816.minecraft.tabchat.tabs.config;

import com.google.common.collect.Lists;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.List;
import java.util.Map.Entry;

class ConfigTabControl {

    private final List<ConfigEntry> entryList = Lists.newArrayList();
    final ConfigEditTab.Options options;
    final ConfigEditTab.ActionHandler handler;
    private final Object[] ignoredPath;
    final ConfigEditTab tab;

    private ConfigurationNode currentNode;
    private ConfigEntry activeEntry;
    private boolean deleteMode;

    ConfigTabControl(ConfigEditTab tab, ConfigurationNode rootNode, ConfigEditTab.Options options, ConfigEditTab.ActionHandler handler) {
        this.tab = tab;
        this.currentNode = rootNode;
        updateEntryList(rootNode);
        Object[] ignore = rootNode.getParent().getPath();
        if (ignore.length == 1 && ignore[0] == null) {
            ignore = new Object[0];
        }
        this.ignoredPath = ignore;
        this.options = options;
        this.handler = handler;
    }

    private void updateEntryList(ConfigurationNode node) {
        this.entryList.clear();
        if (node.hasMapChildren()) {
            for (Entry<Object, ? extends ConfigurationNode> entry : node.getChildrenMap().entrySet()) {
                ConfigEntry confEntry = new ConfigEntry(entry.getKey(), nodeToValue(entry.getValue()));
                this.entryList.add(confEntry);
            }
        } else if (node.hasListChildren()) {
            List<? extends ConfigurationNode> children = node.getChildrenList();
            for (int i = 0; i < children.size(); i++) {
                ConfigEntry confEntry = new ConfigEntry(i, nodeToValue(children.get(i)));
                this.entryList.add(confEntry);
            }
        } else {
            throw new IllegalArgumentException("Root node must be a Map or List type");
        }
    }

    private ConfigEntry.ConfigValue nodeToValue(ConfigurationNode node) {
        if (node.hasMapChildren() || node.hasListChildren()) {
            return new ConfigEntry.ComplexValue(node);
        }
        Object value = node.getValue();
        if (value instanceof Number) {
            return new ConfigEntry.SimpleValue(value, ConfigEntry.ValueType.NUMBER);
        } else if (value instanceof Boolean) {
            return new ConfigEntry.SimpleValue(value, ConfigEntry.ValueType.BOOLEAN);
        } else if (value instanceof String) {
            return new ConfigEntry.SimpleValue(value, ConfigEntry.ValueType.STRING);
        } else if (value == null) {
            return new ConfigEntry.SimpleValue(value, ConfigEntry.ValueType.NULL);
        }
        return new ConfigEntry.UnknownValueType(value);
    }

    public ConfigTableRenderer createTableRenderer() {
        return new ConfigTableRenderer(this, this.tab.scroll);
    }

    public ConfigTableModel createTableModel() {
        return new ConfigTableModel(this);
    }

    public boolean closeActiveEntry() {
        if (this.activeEntry == null) {
            return false;
        }
        this.activeEntry = null;
        return true;
    }

    public void setDeleteModeOrDeleteNode() {
        if (this.activeEntry == null) {
            this.deleteMode = !this.deleteMode;
        } else {
            deleteNode(this.activeEntry.key);
        }
    }

    public void deleteNode(Object key) {
        this.currentNode.removeChild(key);
        this.handler.onNodeRemoved(key);
        this.deleteMode = false;
        this.activeEntry = null;
        refresh();
    }

    public boolean inDeleteMode() {
        return this.deleteMode;
    }

    public ConfigurationNode getNode() {
        return this.currentNode;
    }

    public Object[] getPath() {
        Object[] absolute = this.currentNode.getPath();
        Object[] relative = new Object[absolute.length - this.ignoredPath.length];
        for (int i = 0; i < absolute.length; i++) {
            if (i < this.ignoredPath.length) {
                continue;
            }
            if (i == this.ignoredPath.length && this.options.rootNodeName != null) {
                absolute[i] = this.options.rootNodeName;
            }
            relative[i - this.ignoredPath.length] = absolute[i];
        }
        return relative;
    }

    public void setNode(ConfigurationNode node) {
        if (this.activeEntry != null) {
            return;
        }
        this.currentNode = node;
        updateEntryList(node);
        this.tab.scroll.reset();
    }

    public List<ConfigEntry> getEntries() {
        return this.entryList;
    }

    public ConfigEntry getActiveEntry() {
        return this.activeEntry;
    }

    public boolean hasFocus(ConfigEntry entry) {
        return this.activeEntry == null || this.activeEntry.key.equals(entry.key);
    }

    public boolean hasExplicitFocus(ConfigEntry entry) {
        return this.activeEntry != null && this.activeEntry.key.equals(entry.key);
    }

    public void setFocused(ConfigEntry entry) {
        this.activeEntry = entry;
    }

    public void refresh() {
        updateEntryList(this.currentNode);
    }

}
