package com.simon816.chatui.impl;

import com.simon816.chatui.tabs.config.ConfigEditTab;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.type.GlobalConfig;

import java.io.IOException;
import java.lang.reflect.Field;

public class ImplementationConfig {

    private enum SupportedType {
        SPONGE("sponge") {

            @Override
            protected ConfigEditTab.ActionHandler createHandler() {
                return new ConfigEditTab.ActionHandler() {

                    private void save() {
                        SpongeConfig<GlobalConfig> conf = SpongeImpl.getGlobalConfig();
                        try {
                            Field loaderField = SpongeConfig.class.getDeclaredField("loader");
                            loaderField.setAccessible(true);
                            ((ConfigurationLoader<?>) loaderField.get(conf)).save(conf.getRootNode());
                            conf.reload();
                        } catch (ReflectiveOperationException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onNodeAdded(ConfigurationNode node) {
                        save();
                    }

                    @Override
                    public void onNodeChanged(ConfigurationNode node) {
                        save();
                    }

                    @Override
                    public void onNodeRemoved(Object key) {
                        save();
                    }
                };
            }

            @Override
            public ConfigurationNode rootNode() {
                return SpongeImpl.getGlobalConfig().getRootNode();
            }
        };

        final String implId;
        final ConfigEditTab.ActionHandler handler;

        private SupportedType(String id) {
            this.implId = id;
            this.handler = createHandler();
        }

        protected abstract ConfigEditTab.ActionHandler createHandler();

        public abstract ConfigurationNode rootNode();
    }

    static {
        SupportedType type = null;
        for (SupportedType testType : SupportedType.values()) {
            if (Sponge.getPlatform().getImplementation().getId().equals(testType.implId)) {
                type = testType;
                break;
            }
        }
        ImplementationConfig.type = type;
    }

    private static SupportedType type;

    public static boolean isSupported() {
        return type != null;
    }

    public static ConfigurationNode getRootNode() {
        return type != null ? type.rootNode() : null;
    }

    public static ConfigEditTab.ActionHandler getHandler() {
        return type != null ? type.handler : null;
    }

}
