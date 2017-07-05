package com.simon816.chatui;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

public class FeatureManager {

    private final List<AbstractFeature> features = Lists.newArrayList();
    private Map<String, Supplier<AbstractFeature>> featuresToLoad = Maps.newHashMap();

    public void registerFeature(Object plugin, String id, Supplier<AbstractFeature> featureLoader) {
        checkState(this.featuresToLoad != null, "Not accepting new features to be registered");
        String featureId = Sponge.getPluginManager().fromInstance(plugin).get().getId() + ":" + id;
        this.featuresToLoad.putIfAbsent(featureId, featureLoader);
    }

    public void load() {
        for (Entry<String, Supplier<AbstractFeature>> entry : this.featuresToLoad.entrySet()) {
            if (canLoad(entry.getKey())) {
                AbstractFeature feature = entry.getValue().get();
                feature.setConfigRoot(featureConfig(entry.getKey()));
                feature.onInit();
                this.features.add(feature);
            }
        }
        this.featuresToLoad = null;
    }

    private ConfigurationNode featureConfig(String featureId) {
        ConfigurationNode config = Config.getRootNode().getNode("features", featureId, "config");
        if (config.isVirtual()) {
            config.setValue(Collections.emptyMap());
        }
        return config;
    }

    private boolean canLoad(String featureId) {
        ConfigurationNode enabled = Config.getRootNode().getNode("features", featureId, "enabled");
        if (enabled.isVirtual()) {
            enabled.setValue(true);
        }
        return enabled.getBoolean(true);
    }

    public void initFeatures(ActivePlayerChatView view) {
        for (AbstractFeature feature : this.features) {
            feature.onNewPlayerView(view);
        }
    }

    public void removeFeatures(ActivePlayerChatView view) {
        for (AbstractFeature feature : this.features) {
            feature.onViewClose(view);
        }
    }

}
